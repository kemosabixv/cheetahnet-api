package com.cheetahnet.ping.controller;

import com.cheetahnet.ping.model.DeviceEntity;
import com.cheetahnet.ping.service.NetworkInterfacesService;
import com.cheetahnet.ping.service.PingService;
import com.cheetahnet.ping.service.SNMPService;
import com.cheetahnet.ping.service.UBNTDiscoveryService;
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
    private final SNMPService snmpService;
    private final UBNTDiscoveryService ubntDiscoveryService;



    @Autowired
    public RPCController(PingService pingService, NetworkInterfacesService networkInterfacesService, SNMPService snmpService, UBNTDiscoveryService ubntDiscoveryService) {
        this.pingService = pingService;
        this.networkInterfacesService = networkInterfacesService;
        this.snmpService = snmpService;
        this.ubntDiscoveryService = ubntDiscoveryService;

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



    @GetMapping("/update_device/{ipaddress}")
    public Object[] update_device(@PathVariable("ipaddress") String ipaddress) throws Exception {
        try{
            System.out.println(ipaddress);
            Object[] responses = new Object[3];
            responses[0] = "Success";
            responses[1] = snmpService.update_radiomode_connectedfrom_single(ipaddress);
            responses[2] = ubntDiscoveryService.update_device(ipaddress);
            return responses;
        } catch (Exception e) {
            Object[] responses = new Object[1];
            responses[0] = "Error; " + e.getMessage();
            return responses;

        }

    }






}
