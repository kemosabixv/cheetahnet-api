package com.cheetahnet.ping.controller;

import com.africastalking.AfricasTalking;
import com.africastalking.SmsService;
import com.africastalking.sms.Recipient;
import com.cheetahnet.ping.model.DeviceEntity;
import com.cheetahnet.ping.model.MastEntity;
import com.cheetahnet.ping.service.NetworkInterfacesService;
import com.cheetahnet.ping.service.PingService;
import com.cheetahnet.ping.service.SNMPService;
import com.cheetahnet.ping.service.UBNTDiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
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

    @GetMapping("/interfaces")
    public ResponseEntity<List<Map<String, Object>>> getInterfaces() {
        List<Map<String, Object>> interfaceList = new ArrayList<>();
        try {
            // Get all available network interfaces
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            int counter = 1;
            // Iterate through the interfaces and add their names to the list
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Map<String, Object> interfaceDetails = new HashMap<>();
                interfaceDetails.put("#", String.valueOf(counter));
                interfaceDetails.put("name", networkInterface.getName());
                interfaceDetails.put("displayname", networkInterface.getDisplayName());
                interfaceDetails.put("ip", String.valueOf(networkInterface.getInterfaceAddresses()));
                interfaceList.add(interfaceDetails);
                counter++;

            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return ResponseEntity.ok().headers(headers).body(interfaceList);
        } catch (SocketException e) {
            // Handle the exception if there's an issue with retrieving network interfaces
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/masts")
    public ResponseEntity<List<MastEntity>> getMasts() {
        List<MastEntity> mastEntities = pingService.fetchAllMasts();
        return new ResponseEntity<>(mastEntities, HttpStatus.OK);
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

    @GetMapping("/sms-test")
    public List<Recipient> testSms() throws IOException {
// Initialize
        String username = "cheetah";    // use 'sandbox' for development in the test environment
        String apiKey = "0f6a829fdcc0af768450fca110fc1e4ead9f1373c3606ef8874048ef6eb80835";       // use your sandbox app API key for development in the test environment
        AfricasTalking.initialize(username, apiKey);
        String message = "test";
        String phoneNumber = "+254746839553";
        String[] recipients = {phoneNumber};

        // Initialize a service e.g. SMS
        SmsService sms = AfricasTalking.getService(AfricasTalking.SERVICE_SMS);

        // Use the service
        List<Recipient> response = sms.send(message, recipients, true);
        return response;
    }







    @GetMapping("/update_device/{ipaddress}")
    public Object[] update_device(@PathVariable("ipaddress") String ipaddress) throws Exception {
        try{
            System.out.println(ipaddress);
            Object[] responses = new Object[3];
            responses[0] = "Success";
            responses[1] = snmpService.update_radiomode_connectedfrom_single(ipaddress);
            responses[2] = ubntDiscoveryService.update_device(ipaddress);
            System.out.println(responses);
            return responses;
        } catch (Exception e) {
            Object[] responses = new Object[1];
            responses[0] = "Error; " + e.getMessage();
            return responses;

        }

    }






}
