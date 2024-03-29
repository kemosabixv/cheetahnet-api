package com.cheetahnet.ping.service;


import com.africastalking.AfricasTalking;
import com.africastalking.SmsService;
import com.africastalking.sms.Recipient;
import com.cheetahnet.ping.model.DeviceEntity;
import com.cheetahnet.ping.model.MastEntity;
import com.cheetahnet.ping.repository.DeviceRepository;
import com.cheetahnet.ping.repository.MastRepository;
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

    private final MastRepository mastRepository;

    private volatile boolean isRunning; // flag to control the loop
    private NetworkInterface getNetworkInterfaceByName(String interfaceName) throws SocketException {
        return NetworkInterface.getByName(interfaceName);
    }
    @Autowired
    public PingService( DeviceRepository deviceRepository, MastRepository mastRepository) {
        this.deviceRepository = deviceRepository;
        this.mastRepository = mastRepository;
    }


    public void checkDeviceConnectionStatus(String phoneNumber) throws InterruptedException {
        isRunning = true;
        int batchSize = Runtime.getRuntime().availableProcessors();

        int clientMastId = 0;
        MastEntity mastEntity = mastRepository.findByMastName("Client");
        System.out.println(mastEntity.getMastName());
        System.out.println(mastEntity.getMastId());
        if (mastEntity != null) {
            try {
                clientMastId = Integer.parseInt(String.valueOf(mastEntity.getMastId()));
                System.out.println(clientMastId);
            } catch (NumberFormatException e) {
                System.out.println("Error parsing mastId as integer.");
            }
        } else {
            System.out.println("No 'Client' mast found.");
        }


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
                int finalClientMastId = clientMastId;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    for (DeviceEntity device : batch) {
//                        System.out.println("Checking device: " + device.getDeviceName() + " with IP: " + device.getIpAddress());
                        ping(device, phoneNumber, finalClientMastId);

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
            Thread.sleep(1000);
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

    private void ping(DeviceEntity deviceEntity, String phoneNumber, int clientMastId) {
        // Retrieve device information from the DeviceEntity object
        String ipAddress = deviceEntity.getIpAddress();
        String deviceName = deviceEntity.getDeviceName();
        String connectionStatus = deviceEntity.getConnectionStatus();
        int mastId = Integer.parseInt(deviceEntity.getMastId());

        // Use Java ProcessBuilder to run the ping command
//        ProcessBuilder processBuilder = new ProcessBuilder("ping", "-n", "3", "-w", "4000", ipAddress);
        try {
            // Start the ping process and wait for it to complete
            String cmd = "cmd /c ping -n 1 " + ipAddress + " | find \"TTL\"";
            Process proc = Runtime.getRuntime().exec(cmd);
            int exitCode = proc.waitFor();
            System.out.println("pinging " + ipAddress);
//            int exitCode = process.waitFor();
            System.out.println(exitCode);

            // Process the exit code to determine the ping result
            if (exitCode == 0){
                if (connectionStatus.equals("Offline")) {
                    // If the device was previously offline, send a local notification and an SMS
                    sendLocalNotification(exitCode, ipAddress);
                    if(mastId != clientMastId){
                            String message = "DeviceEntity: " + deviceName + " is back Online. Pinged IP: " + ipAddress;
                            sendSMSMessage(phoneNumber, message);
                        }
                    String status = "Online";
                    deviceEntity.setConnectionStatus(status);
                    updateDeviceStatus(deviceEntity);
                } else if ((connectionStatus.equals(""))) {
                    String status = "Online";
                    deviceEntity.setConnectionStatus(status);
                    updateDeviceStatus(deviceEntity);
                }
            }
            // The ping was unsuccessful (exitCode != 0)
            else if(exitCode !=0){
                if (connectionStatus.equals("Online")) {
                    // If the device was previously online, send a local notification and an SMS
                    sendLocalNotification(exitCode, ipAddress);
                    if(mastId != clientMastId){
                            String message = "DeviceEntity: " + deviceName + " is Currently Offline. Pinged IP: " + ipAddress;
                            sendSMSMessage(phoneNumber, message);
                        }
                    String status = "Offline";
                    deviceEntity.setConnectionStatus(status);
                    updateDeviceStatus(deviceEntity);
                } else if ((connectionStatus.equals(""))) {
                    String status = "Offline";
                    deviceEntity.setConnectionStatus(status);
                    updateDeviceStatus(deviceEntity);
                }
            }
        } catch (IOException | InterruptedException e) {
            // Exception occurred during the ping process
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void sendSMSMessage(String phoneNumber, String message) {
        // Initialize
        String username = "cheetah";    // use 'sandbox' for development in the test environment
        String apiKey = "0f6a829fdcc0af768450fca110fc1e4ead9f1373c3606ef8874048ef6eb80835";       // use your sandbox app API key for development in the test environment
        AfricasTalking.initialize(username, apiKey);

        // Initialize sms service
        SmsService sms = AfricasTalking.getService(AfricasTalking.SERVICE_SMS);
        String[] recipients = {phoneNumber};
//        String from = "Ping Service";
        try {

            // Send the SMS
            List<Recipient> response = sms.send(message, recipients, true);

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
            // URL object with the endpoint to send the request
            URI uri = new URI("http://localhost/cheetahnet/storenotification/" + encodedIpAddress + "/" + encodedExitCode);
            URL url = uri.toURL();
            System.out.println(url);

            // Open connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method
            connection.setRequestMethod("GET");

            // Send the request
            int responseCode = connection.getResponseCode();
            System.out.println("response code:" + responseCode);

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

    public void updateDeviceStatus(DeviceEntity deviceEntity) {
        deviceRepository.save(deviceEntity);
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

    public List<MastEntity> fetchAllMasts() {
        return mastRepository.findAll();
    }


}