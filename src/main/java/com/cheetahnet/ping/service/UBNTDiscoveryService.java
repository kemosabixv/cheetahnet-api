package com.cheetahnet.ping.service;

import com.cheetahnet.ping.model.DeviceEntity;
import com.cheetahnet.ping.model.NetworkInterfaceEntity;
import com.cheetahnet.ping.repository.DeviceRepository;
import com.cheetahnet.ping.repository.NetworkInterfaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class UBNTDiscoveryService {

    private final NetworkInterfaceRepository networkInterfaceRepository;
    private final DeviceRepository deviceRepository;
    // Constants for UBNT field types
    private static final byte UBNT_FIELD_MAC = 0x01;
    private static final byte UBNT_FIELD_MAC_AND_IP = 0x02;
    private static final byte UBNT_FIELD_TYPE_FIRMWARE_VERSION = 0x03;
    private static final byte UBNT_FIELD_UNKOWN_1 = 0x0a;
    private static final byte UBNT_FIELD_UNKOWN_2 = 0x0e;
    private static final byte UBNT_FIELD_UNKOWN_3 = 0x18;
    private static final byte UBNT_FIELD_UNKOWN_4 = 0x10;
    private static final byte UBNT_FIELD_TYPE_RADIO_NAME = 0x0b;
    private static final byte UBNT_FIELD_TYPE_MODEL_SHORT = 0x0c;
    private static final byte UBNT_FIELD_TYPE_ESSID = 0x0d;
    private static final byte UBNT_FIELD_TYPE_MODEL_FULL = 0x14;

    @Autowired
    public UBNTDiscoveryService(NetworkInterfaceRepository networkInterfaceRepository, DeviceRepository deviceRepository) {
        this.networkInterfaceRepository = networkInterfaceRepository;
        this.deviceRepository = deviceRepository;
    }

    private static final Object lock = new Object();

    private Map<String, Object> processResponse(byte[] responseData, int responseLength) {
        Map<String, Object> responses = new HashMap<>();
        int offset = 4;

        while (offset < responseLength) {
            byte fieldType = responseData[offset];

            if (fieldType == UBNT_FIELD_MAC || fieldType == UBNT_FIELD_MAC_AND_IP || fieldType == UBNT_FIELD_TYPE_RADIO_NAME || fieldType == UBNT_FIELD_TYPE_FIRMWARE_VERSION || fieldType == UBNT_FIELD_TYPE_MODEL_FULL || fieldType == UBNT_FIELD_TYPE_MODEL_SHORT || fieldType == UBNT_FIELD_TYPE_ESSID) {
                int fieldDataLength = (responseData[offset + 1] & 0xFF) << 8 | (responseData[offset + 2] & 0xFF);
                offset += 3;

                byte[] fieldData = new byte[fieldDataLength];
                System.arraycopy(responseData, offset, fieldData, 0, fieldDataLength);
                offset += fieldDataLength;

                String response = processField(fieldType, fieldData);
                assert response != null;
                String[] parts = response.split(",", 2);
                String key = parts[0].trim();
                String value = parts[1].trim();

                if (key.equals("ip")) {
                    if (!responses.containsKey(key)) {
                        responses.put(key, value);
                    }
                } else if (key.equals("firmware")) {
                    String[] valueParts = value.split("\\.");
                    if (valueParts.length >= 4) {
                        value = valueParts[0] + "." + valueParts[2] + "." + valueParts[3] + "." + valueParts[4];
                        responses.put(key, value);
                    }
                } else {
                    responses.put(key, value);
                }
            } else if (fieldType == UBNT_FIELD_UNKOWN_1 || fieldType == UBNT_FIELD_UNKOWN_2 || fieldType == UBNT_FIELD_UNKOWN_3 || fieldType == UBNT_FIELD_UNKOWN_4) {
                int fieldDataLength = (responseData[offset + 1] & 0xFF) << 8 | (responseData[offset + 2] & 0xFF);
                offset += (fieldDataLength + 3);
            } else {
                offset++;
            }
        }

        return responses;
    }

    private static String processField(byte fieldType, byte[] fieldData) {
        // Process the field based on its type
        if (fieldType == UBNT_FIELD_TYPE_RADIO_NAME) {
            String radioName = parseFieldDataASCII(fieldData);
            System.out.println("Radio Name found " + radioName);
            return ("name, " + radioName);
        } else if (fieldType == UBNT_FIELD_TYPE_FIRMWARE_VERSION) {
            String firmwareVersion = parseFieldDataASCII(fieldData);
            System.out.println("Firmware Version found " + firmwareVersion);
            return ("firmware, " + firmwareVersion);
        } else if (fieldType == UBNT_FIELD_TYPE_MODEL_SHORT) {
            String modelShort = parseFieldDataASCII(fieldData);
            System.out.println("Model Short found " + modelShort);
            return ("model_short, " + modelShort);
        } else if (fieldType == UBNT_FIELD_TYPE_ESSID) {
            String essid = parseFieldDataASCII(fieldData);
            System.out.println("ESSID found " + essid);
            return ("ssid, " + essid);
        } else if (fieldType == UBNT_FIELD_TYPE_MODEL_FULL) {
            String modelFull = parseFieldDataASCII(fieldData);
            System.out.println("Model Full found " + modelFull);
            return ("model, " + modelFull);
        } else if (fieldType == UBNT_FIELD_MAC) {
            String macAddress = parseFieldDataHex(fieldData); // Implement the logic to parse the MAC address
            System.out.println("MAC Address found " + macAddress);
            return ("mac, " + macAddress);
        } else if (fieldType == UBNT_FIELD_MAC_AND_IP) {
            System.out.println("MAC and IP found");
            //sout fieldData.length
            //loop through byte array and print each byte in hex
            for (byte b : fieldData) {
                String binary = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
                int decimal = Integer.parseInt(binary, 2);
                String hex = Integer.toHexString(decimal);
                System.out.println("MAC and IP Bytes");
                System.out.println(hex.toUpperCase());
            }
            System.out.println("fieldData.length MAC and IP (10 or 9): " + fieldData.length);
            byte[] ipAddressBytes = Arrays.copyOfRange(fieldData, 6, fieldData.length);
            //loop through byte array and print each byte in hex
            for (byte b : ipAddressBytes) {
                String binary = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
                int decimal = Integer.parseInt(binary, 2);
                String hex = Integer.toHexString(decimal);
                System.out.println("ipAddressBytes");
                System.out.println(hex.toUpperCase());
            }
            String ipAddress = parseFieldDataIp(ipAddressBytes);
            System.out.println("IP Address found " + ipAddress);
            return ("ip, " + ipAddress);
        }

        return null; // Skip processing for unknown field type
    }

    public static String parseFieldDataASCII(byte[] fieldData) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : fieldData) {
            char c = (char) b;
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    public static String parseFieldDataHex(byte[] fieldData) {
        StringBuilder hexBuilder = new StringBuilder();
        for (byte b : fieldData) {
            String binary = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            int decimal = Integer.parseInt(binary, 2);
            String hex = Integer.toHexString(decimal).toUpperCase();
            hexBuilder.append(hex).append(":");
        }
        // Remove the last colon if it exists
        if (hexBuilder.length() > 0) {
            hexBuilder.setLength(hexBuilder.length() - 1);
        }
        return hexBuilder.toString();
    }

    public static String parseFieldDataIp(byte[] fieldData) {
        StringBuilder ipBuilder = new StringBuilder();
        for (int i = 0; i < fieldData.length; i++) {
            int decimal = fieldData[i] & 0xFF;
            ipBuilder.append(decimal);
            if (i < fieldData.length - 1) {
                ipBuilder.append(".");
            }
        }
        return ipBuilder.toString();
    }


    public synchronized Map<String, Object> update_device(String ipaddress) {
        synchronized (lock) {
            try {
                NetworkInterfaceEntity networkInterfaceEntity = networkInterfaceRepository.findById(1);
                String interfaceName = networkInterfaceEntity.getInterfaceName();
                NetworkInterface networkInterface = NetworkInterface.getByName(interfaceName);
                InetAddress networkInterfaceIpAddress = InetAddress.getByName(networkInterfaceEntity.getIpAddress());

                DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET);
                channel.setOption(StandardSocketOptions.IP_MULTICAST_IF, networkInterface);
                channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                channel.setOption(StandardSocketOptions.SO_BROADCAST, true);
                channel.bind(new InetSocketAddress(networkInterfaceIpAddress, 8883));

                InetAddress broadcastAddress = InetAddress.getByName(ipaddress);
                int port = 10001;
                byte[] payload = {0x01, 0x00, 0x00, 0x00};

                DatagramPacket packet = new DatagramPacket(payload, payload.length, broadcastAddress, port);
                channel.send(ByteBuffer.wrap(packet.getData()), new InetSocketAddress(broadcastAddress, port));

                // Wait for a response within the timeout
                ByteBuffer responseBuffer = ByteBuffer.wrap(new byte[10000]);
                channel.configureBlocking(false);
                channel.socket().setSoTimeout(5000);
                Selector selector = Selector.open();
                channel.register(selector, SelectionKey.OP_READ);

                long startTime = System.currentTimeMillis();
                long elapsedTime = 0;

                Map<String, Object> responses = null;
                while (elapsedTime < 5000) {
                    if (selector.select(5000 - elapsedTime) == 0) {
                        break;
                    }

                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    for (SelectionKey key : selectedKeys) {
                        if (key.isReadable()) {
                            channel.receive(responseBuffer);

                            // Process the response if valid
                            if (responseBuffer.position() >= 4 && responseBuffer.get(0) == 0x01 && responseBuffer.get(1) == 0x00 && responseBuffer.get(2) == 0x00) {
                                responses = processResponse(responseBuffer.array(), responseBuffer.position());
                            }

                            responseBuffer.clear();
                        }
                    }

                    elapsedTime = System.currentTimeMillis() - startTime;
                }

                channel.close();
                if (responses != null) {
                    // Convert the response dataArray to JSON using Gson library
                    System.out.println(responses);
                    DeviceEntity deviceEntity = deviceRepository.findByIpAddress(ipaddress);
                    String device_name = (String) responses.get("name");
                    System.out.println("device_name: " + device_name);
                    String model = (String) responses.get("model");
                    System.out.println("model: " + model);
                    String model_short = (String) responses.get("model_short");
                    System.out.println("model: " + model_short);
                    String ssid = (String) responses.get("ssid");
                    System.out.println("ssid: " + ssid);
                    String firmware = (String) responses.get("firmware");
                    System.out.println("firmware: " + firmware);
                    String mac = (String) responses.get("mac");
                    System.out.println("mac: " + mac);

                    deviceEntity.setDeviceName(device_name);
                    deviceEntity.setDeviceModel(model);
                    deviceEntity.setDeviceModelShort(model_short);
                    deviceEntity.setSSID(ssid);
                    deviceEntity.setFirmwareVersion(firmware);
                    deviceEntity.setMacAddress(mac);

                    deviceRepository.save(deviceEntity);
                    responses.put("error", "0");

                } else {
                    responses = new HashMap<>();
                    System.out.println("No response from device");
                    responses.put("error", "1");
                    responses.put("message", "No response from device");
                }
                return responses;


            } catch (IOException e) {
                System.out.println("exception");
                Map<String, Object> result;
                result = new HashMap<>();
                result.put("error", "1");
                result.put("message", "Error updating device: " + e.getMessage());
                return result;
            }
        }
    }

}
