package com.cheetahnet.ping.service;


import com.africastalking.AfricasTalking;
import com.africastalking.SmsService;
import com.africastalking.sms.Recipient;
import com.cheetahnet.ping.model.DeviceEntity;
import com.cheetahnet.ping.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class PingService {
    private final DeviceRepository deviceRepository;

    private volatile boolean isRunning; // flag to control the loop
    @Autowired
    public PingService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public List<DeviceEntity> fetchAllDevices() {
        return (List<DeviceEntity>) deviceRepository.findAll();
    }

    public void updateDeviceStatus(DeviceEntity deviceEntity) {
        deviceRepository.save(deviceEntity);
    }

    public void checkDeviceConnectionStatus(String phoneNumber) throws InterruptedException {


        isRunning = true;
        int batchSize = 7;
        while (isRunning) {
            List<DeviceEntity> deviceEntities = (List<DeviceEntity>) deviceRepository.findAll();
//            AtomicInteger count = new AtomicInteger(0);

            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (List<DeviceEntity> batch : partition(deviceEntities, batchSize)) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    for (DeviceEntity device : batch) {
                        String status = ping(device, phoneNumber);
                        device.setConnection_status(status);
                        updateDeviceStatus(device);
                    }
                }, executor);
                futures.add(future);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            executor.shutdown();

            Thread.sleep(3000); // add a delay between iterations
        }
    }


    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }


    private String ping(DeviceEntity deviceEntity, String phoneNumber) {
        String ipAddress = deviceEntity.getIp_address();
        String deviceName = deviceEntity.getDevice_name();
        String connectionStatus = deviceEntity.getConnection_status();

        // Use Java ProcessBuilder to run the ping command
        ProcessBuilder processBuilder = new ProcessBuilder("ping", "-n", "1", "-w", "3000", ipAddress);
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                // Update the deviceEntity status to "Online"
                return "Online";
            } else {
                if (exitCode != 0 && connectionStatus.equals("Offline")) {
                    // The deviceEntity was already offline, no need to send a message
                    return "Offline";
                } else {
                    // Send a message to the user's phone number
                    String message = "DeviceEntity: " + deviceName + " is Currently Offline. Pinged IP: " + ipAddress;
                    boolean messageSent = sendMessage(phoneNumber, message);
                    if (messageSent) {
                        // Update the deviceEntity status to "Offline"
                        return "Offline";
                    } else {
                        return "Offline";
                    }

                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    private boolean sendMessage(String phoneNumber, String message) {
        // Initialize
        String username = "cheetah";    // use 'sandbox' for development in the test environment
        String apiKey = "9d231c83ae862a529fef4b00aed8cf47ca2626daf898e5cd317703efae35313e";       // use your sandbox app API key for development in the test environment
        AfricasTalking.initialize(username, apiKey);

        // Initialize a service e.g. SMS
        SmsService sms = AfricasTalking.getService(AfricasTalking.SERVICE_SMS);
        String[] recipients = {phoneNumber};
        String from = "Ping Service";
        try {

            // Send the SMS
            List<Recipient> response = sms.send(message, from, recipients, true);

            // Check the response status
            for (Recipient recipient : response) {
                // status is either "Success" or "error message" return true if success and false if error
                if (recipient.status.equals("Success")) {
                    return true;
                }else{
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public void stopCheckingDeviceConnectionStatus() {
        isRunning = false;
    }


    public boolean getIsRunning() {
        return isRunning;
    }
}