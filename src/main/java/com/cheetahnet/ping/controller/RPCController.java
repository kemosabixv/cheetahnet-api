package com.cheetahnet.ping.controller;

import com.cheetahnet.ping.model.DeviceEntity;
import com.cheetahnet.ping.repository.NetworkInterfaceRepository;
import com.cheetahnet.ping.service.NetworkInterfacesService2;
import com.cheetahnet.ping.service.PingService;
import com.cheetahnet.ping.service.NetworkInterfacesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    public RPCController(PingService pingService, NetworkInterfacesService networkInterfacesService, NetworkInterfacesService2 networkInterfacesService2) {
        this.pingService = pingService;
        this.networkInterfacesService = networkInterfacesService;
        this.networkInterfacesService2 = networkInterfacesService2;

    }

    @GetMapping("/")
    public String helloWorld() {
        return "Hello World";
    }

    @GetMapping("/test")
    public String ping() {
        return "This is the ping controller";
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

        return new ResponseEntity<>(message, HttpStatus.OK);
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

    @GetMapping("/discovery")
    public void discovery() throws Exception {
        // Perform the necessary discovery logic
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
}
