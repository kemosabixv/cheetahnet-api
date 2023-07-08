package com.cheetahnet.ping.controller;

import com.cheetahnet.ping.model.DeviceEntity;
import com.cheetahnet.ping.repository.NetworkInterfaceRepository;
import com.cheetahnet.ping.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/rpc")
public class RPCController {
    private final PingService pingService;
    private final NetworkInterfacesService networkInterfacesService;
    private final NetworkInterfacesService2 networkInterfacesService2;
    private final SNMPService snmpService;
    private final UBNTDiscoveryService ubntDiscoveryService;
    private final SnmpTestData snmpTestData;
    private final  DiscoveryTestData discoveryTestData;


    @Autowired
    public RPCController(PingService pingService, NetworkInterfacesService networkInterfacesService, NetworkInterfacesService2 networkInterfacesService2, SNMPService snmpService, UBNTDiscoveryService ubntDiscoveryService, SnmpTestData snmpTestData, DiscoveryTestData discoveryTestData) {
        this.pingService = pingService;
        this.networkInterfacesService = networkInterfacesService;
        this.networkInterfacesService2 = networkInterfacesService2;
        this.snmpService = snmpService;
        this.ubntDiscoveryService = ubntDiscoveryService;
        this.snmpTestData = snmpTestData;
        this.discoveryTestData = discoveryTestData;

    }

    @GetMapping("/devices")
    public ResponseEntity<List<DeviceEntity>> getDevices() {
        List<DeviceEntity> deviceEntities = pingService.fetchAllDevices();
        return new ResponseEntity<>(deviceEntities, HttpStatus.OK);
    }

    @GetMapping("/start-ping/{phoneNumber}")
    public ResponseEntity<String> checkDeviceConnectionStatus(@PathVariable("phoneNumber") String phoneNumber) throws InterruptedException {
        String message = "Ping module started";

        CompletableFuture.runAsync(() -> {
            try {
                pingService.checkDeviceConnectionStatus(phoneNumber);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        return ResponseEntity.ok(message);
    }

    @GetMapping("/stop-ping")
    public ResponseEntity<String> stopCheckingDeviceConnectionStatus() {
        pingService.stopCheckingDeviceConnectionStatus();
        String message = "Ping module stopped";
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @GetMapping("/ping-status")
    public ResponseEntity<Map<String, Object>> getPingMonitoringStatus() {
        boolean isRunning = pingService.getIsRunning();
        Map<String, Object> response = new HashMap<>();
        response.put("isRunning", isRunning);
        return ResponseEntity.ok(response);
    }

    @Autowired
    private NetworkInterfaceRepository networkInterfaceRepository;

    @GetMapping("/update_radiomode_connectedfrom")
    public ResponseEntity<String> update_radiomode_connectedfrom() throws Exception {
        String result = snmpService.update_radiomode_connectedfrom();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/snmpgetruntimedevicedata/{ipaddress}")
    public ResponseEntity<String> snmpgetruntimedevicedata(@PathVariable("ipaddress") String ipaddress) throws InterruptedException {
        String result = null;
        try {
            System.out.println(ipaddress);
            result = snmpService.snmpGetRuntimeDeviceData(ipaddress);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/snmpgetrecurringdevicedata/{ipaddress}")
    public ResponseEntity<String> snmpgetrecurringdevicedata(@PathVariable("ipaddress") String ipaddress) throws InterruptedException {
        String result = null;
        try {
            System.out.println(ipaddress);
            result = snmpService.snmpGetRecurringDeviceData(ipaddress);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/return-interfaces")
    public ResponseEntity<String> returnInterfaces() {
        String result = networkInterfacesService.getIpAddressAndSubnetForInterface();
        if (result != null) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Error retrieving interface information.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/return-all-interfaces")
    public ResponseEntity<String> returnAllInterfaces() {
        String result = networkInterfacesService2.getAllNetworkInterfacesInfo();
        if (result != null) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Error retrieving interface information.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/discover-radios")
    public ResponseEntity<String> discoverRadios() {
        String response = ubntDiscoveryService.discoverUBNTDevices();

        // Check if the response is an error (stack trace)
        if (response.startsWith("java.io.IOException")) {
            String errorResponse = "Error discovering radios.";

            // Create a HashMap to hold the error response data
            HashMap<String, Object> errorResponseMap = new HashMap<>();
            errorResponseMap.put("error", errorResponse);

            // Return the error response entity with HTTP status INTERNAL_SERVER_ERROR
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponseMap.toString());
        }

        // Return the response entity with the response string and HTTP status OK
        return ResponseEntity.ok(response);
    }





    @GetMapping("/test-data")
    public String returnTestData() {
        String jsonData = snmpTestData.getJsonData();
        System.out.println(jsonData);
        return jsonData;
    }

    @GetMapping("/discovery-test-data")
    public String returnDiscoveryTestData() {
        String jsonData = discoveryTestData.getJsonData();
        System.out.println(jsonData);
        return jsonData;
    }

    @GetMapping("/discovery-test-data-2")
    public String returnDiscoveryTestData2() {
        String jsonData = discoveryTestData.getJsonData2();
        System.out.println(jsonData);
        return jsonData;
    }


}
