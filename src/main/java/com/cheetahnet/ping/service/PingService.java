package com.cheetahnet.ping.service;


import com.africastalking.AfricasTalking;
import com.africastalking.SmsService;
import com.africastalking.sms.Recipient;
import com.cheetahnet.ping.model.DeviceEntity;
import com.cheetahnet.ping.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
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

    public void stopCheckingDeviceConnectionStatus() {
        isRunning = false;
    }

    public boolean getIsRunning() {
        return isRunning;
    }
    public List<DeviceEntity> fetchAllDevices() {
        return deviceRepository.findAll();
    }

    public void updateDeviceStatus(DeviceEntity deviceEntity) {
        deviceRepository.save(deviceEntity);
    }

    public void checkDeviceConnectionStatus(String phoneNumber) throws InterruptedException {
        isRunning = true;
        int batchSize = 7;

        // Continuously check device connection status while the flag is set to true
        while (isRunning) {
            // Fetch all device entities from the database
            List<DeviceEntity> deviceEntities = fetchAllDevices();

            // Create an executor service with a fixed thread pool size
            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            // Create a list to hold CompletableFuture objects
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            // Split the list of devices into batches
            for (List<DeviceEntity> batch : partition(deviceEntities, batchSize)) {
                // Create a CompletableFuture for each batch of devices
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    for (DeviceEntity device : batch) {
                        System.out.println("Checking device: " + device.getDeviceName() + " with IP: " + device.getIpAddress());
//                        String status = ping(device, phoneNumber);
                        ping(device, phoneNumber);

                    }
                }, executor);

                // Add the CompletableFuture to the list
                futures.add(future);
            }

            // Wait for all CompletableFuture objects to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // Shutdown the executor service
            executor.shutdown();

            // Add a delay between iterations
            Thread.sleep(10000);
        }
    }



    private <T> List<List<T>> partition(List<T> list, int size) {
        // Create a list to hold the partitions
        List<List<T>> partitions = new ArrayList<>();

        // Iterate over the input list in increments of the specified size
        for (int i = 0; i < list.size(); i += size) {
            // Create a sublist by extracting elements from the input list
            // starting at index 'i' up to 'i + size', or the end of the list
            List<T> partition = list.subList(i, Math.min(i + size, list.size()));

            // Add the sublist to the list of partitions
            partitions.add(partition);
        }

        // Return the list of partitions
        return partitions;
    }


    private void ping(DeviceEntity deviceEntity, String phoneNumber) {
        // Retrieve device information from the DeviceEntity object
        String ipAddress = deviceEntity.getIpAddress();
        String deviceName = deviceEntity.getDeviceName();
        String connectionStatus = deviceEntity.getConnectionStatus();

        // Use Java ProcessBuilder to run the ping command
        ProcessBuilder processBuilder = new ProcessBuilder("ping", "-n", "1", "-w", "3000", ipAddress);
        try {
            // Start the ping process and wait for it to complete
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            // Process the exit code to determine the ping result
            switch (exitCode) {
                case 0 -> {
                    // The ping was successful (exitCode = 0)
                    if (connectionStatus.equals("Offline")) {
                        // If the device was previously offline, send a local notification and an SMS
                        sendLocalNotification(exitCode, ipAddress);
                        String message = "DeviceEntity: " + deviceName + " is back Online. Pinged IP: " + ipAddress;
//                        boolean messageSent =
                        sendSMSMessage(phoneNumber, message);
                        String status = "Online";
                        deviceEntity.setConnectionStatus(status);
                        updateDeviceStatus(deviceEntity);}
//                        return "Online";
//                    } else {
//                        // If the device was already online, update the deviceEntity status to "Online"
//                        return "Online";
//                    }
                }
                default -> {
                    // The ping was unsuccessful (exitCode != 0)
                    if (connectionStatus.equals("Online")) {
                        // If the device was previously online, send a local notification and an SMS
                        sendLocalNotification(exitCode, ipAddress);
                        String message = "DeviceEntity: " + deviceName + " is Currently Offline. Pinged IP: " + ipAddress;
//                        boolean messageSent =
                        sendSMSMessage(phoneNumber, message);
                        String status = "Offline";
                        deviceEntity.setConnectionStatus(status);
                        updateDeviceStatus(deviceEntity);}
//
//                    } else {
//                        // If the device was already offline, no need to send a message
//                        return "Offline";
//                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            // Exception occurred during the ping process
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
        }
    }




    private void sendSMSMessage(String phoneNumber, String message) {
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
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendLocalNotification(int exitCode, String ip) {
        try {
            String encodedExitCode = URLEncoder.encode(String.valueOf(exitCode), StandardCharsets.UTF_8);
            String encodedIpAddress = URLEncoder.encode(ip, StandardCharsets.UTF_8);
            // Create a URL object with the endpoint you want to send the request to
            URI uri = new URI("http://localhost/cheetahnet/storenotification/" + encodedIpAddress + "/" + encodedExitCode);
            URL url = uri.toURL();
            System.out.println(url);

            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method (GET, POST, etc.)
            connection.setRequestMethod("GET");

            // Send the request
            int responseCode = connection.getResponseCode();

            // Log "sent" on the console if the request is successful
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Sent");
            }

            // Close the connection
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}