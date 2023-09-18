package com.cheetahnet.ping.service;

import java.net.*;
import java.util.*;
import com.cheetahnet.ping.model.DeviceEntity;
import com.cheetahnet.ping.model.DeviceHistoryEntitiy;
import com.cheetahnet.ping.model.NetworkInterfaceEntity;
import com.cheetahnet.ping.repository.DeviceHistoryRepository;
import com.cheetahnet.ping.repository.DeviceRepository;
import com.cheetahnet.ping.repository.NetworkInterfaceRepository;
import com.google.gson.Gson;
import java.time.LocalDateTime;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SNMPService {

    private final NetworkInterfaceRepository networkInterfaceRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceHistoryRepository deviceHistoryRepository;

    @Autowired
    public SNMPService(NetworkInterfaceRepository networkInterfaceRepository, DeviceRepository deviceRepository, DeviceHistoryRepository deviceHistoryRepository) {
        this.networkInterfaceRepository = networkInterfaceRepository;
        this.deviceRepository = deviceRepository;
        this.deviceHistoryRepository = deviceHistoryRepository;
    }
    private NetworkInterface getNetworkInterfaceByName(String interfaceName) throws SocketException {
        return NetworkInterface.getByName(interfaceName);
    }
    private static String getKey(String snmpResponse) {
        String[] parts = snmpResponse.split(",", 2);
        return parts[0].trim();

    }
    private static String getValue(String snmpResponse) {
        String[] parts = snmpResponse.split(",", 2);
        return parts[1].trim();
    }

    public String update_radiomode_connectedfrom() throws Exception {

        NetworkInterfaceEntity networkInterfaceEntity = networkInterfaceRepository.findById(1);
        String interfaceName = networkInterfaceEntity.getInterfaceName();
        NetworkInterface networkInterface = getNetworkInterfaceByName(interfaceName);
        if (networkInterface == null) {
            throw new Exception("Network interface not found");
        }
        //get all devices
        List<DeviceEntity> deviceEntities = deviceRepository.findAll();
        //get array of ip addresses
        List<String> ipAddresses = new ArrayList<>();
        for (DeviceEntity deviceEntity : deviceEntities) {
            ipAddresses.add(deviceEntity.getIpAddress());
        }
        //initialize response and error arraylists
        List<Map<String, Object>> responsesArray = new ArrayList<>();
        List<Map<String, Object>> errorArray = new ArrayList<>();


        //get the ip address of the network interface
        InetAddress inetAddress = networkInterface.getInetAddresses().nextElement();
        //log the ip address of the network interface
        System.out.println("method: update_radiomode_connectedfrom");
        System.out.println("IP Address: " + inetAddress.getHostAddress());
        // Create the UDP address for the network interface
        UdpAddress udpAddress = new UdpAddress(inetAddress.getHostAddress() + "/5000");
        /* Create an SNMP session */
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping(udpAddress);
        Snmp snmp = new Snmp(transport);
        transport.listen();
        System.out.println("SNMP Session Created");
        // Define the community target
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setRetries(1);
        target.setTimeout(500);
        target.setVersion(SnmpConstants.version1);
        System.out.println("Community Target Created");
        // Loop through the IP address arraylist and send SNMP requests
        for (String ipAddress : ipAddresses) {

            // Create the target address object using the IP address
            Address targetAddressObject = new UdpAddress(ipAddress + "/161");
            target.setAddress(targetAddressObject);
            // Create a PDU for the SNMP request
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.41112.1.4.1.1.2.1"))); // Radio mode OID
            pdu.setType(PDU.GET);

            ResponseEvent event = snmp.send(pdu, target);
            System.out.println("SNMP request sent" + ipAddress);
            if (event != null && event.getResponse() != null) {
                System.out.println("SNMP response received");
                VariableBinding[] variableBindings = event.getResponse().toArray();
                int radioMode = variableBindings[0].getVariable().toInt();
                String radioModeStr;
                String currentIp = "ip, " + ipAddress;
                Map<String, Object> responses = new HashMap<>();
                if (radioMode == 4 || radioMode == 3 || radioMode == 2) {
                    radioModeStr = "radio_mode,AP";
                    String RadioModeSet = "AP";
                    String connectedFrom = "0";
                    DeviceEntity deviceEntity = deviceRepository.findByIpAddress(ipAddress);
                    //save the previous radio mode and connected from to device history table
                    long deviceid = deviceEntity.getDeviceId();
                    DeviceHistoryEntitiy deviceHistoryEntitiy = deviceHistoryRepository.findByDeviceId(deviceid);
                    String previousRadioMode = deviceEntity.getWirelessMode();
                    String previousConnectedFrom = deviceEntity.getConnectedFrom();
                    deviceHistoryEntitiy.setWirelessMode(previousRadioMode);
                    deviceHistoryEntitiy.setConnectedFrom(previousConnectedFrom);
                    deviceHistoryEntitiy.setOperation("update");
                    LocalDateTime dateCreated = LocalDateTime.now();
                    deviceHistoryEntitiy.setTimeStamp(dateCreated);
                    deviceHistoryRepository.save(deviceHistoryEntitiy);
                    //update the radio mode and connected from in device table
                    deviceEntity.setWirelessMode(RadioModeSet);
                    deviceEntity.setConnectedFrom(connectedFrom);
                    deviceRepository.save(deviceEntity);
                    String deviceName = deviceEntity.getDeviceName();
                    String deviceNameStr = "device_name," + deviceName;
                    System.out.println("deviceName: " + deviceName);
                    responses.put(getKey(deviceNameStr), getValue(deviceNameStr));
                    responses.put(getKey(currentIp), getValue(currentIp));
                    responses.put(getKey(radioModeStr), getValue(radioModeStr));
                    responses.put("connected_from", "0");
                    responsesArray.add(responses);
                } else {
                    radioModeStr = "radio_mode,Station";
                    PDU pdu2 = new PDU();
                    pdu2.add(new VariableBinding(new OID(".1.3.6.1.4.1.41112.1.4.7.1.2"))); // ubntStaMac OID
                    pdu2.setType(PDU.GETNEXT);
                    ResponseEvent event2 = snmp.send(pdu2, target);
                    System.out.println("SNMP2 request sent");
                    if (event2 != null && event2.getResponse() != null) {
                        System.out.println("SNMP2 response received");
                        VariableBinding[] variableBindings2 = event2.getResponse().toArray();
                        String connected_fromStr = variableBindings2[0].getVariable().toString();
                        System.out.println("connected_fromStr: " + connected_fromStr);
                        if(Objects.equals(connected_fromStr, "Null")){
                            connected_fromStr = "disconnected";
                            String RadioModeSet = "Station";
                            String connectedFromSet = "0";
                            String connected_from = "connected_from," + connected_fromStr;
                            DeviceEntity deviceEntity = deviceRepository.findByIpAddress(ipAddress);
                            //save the previous radio mode and connected from to device history table
                            long deviceid = deviceEntity.getDeviceId();
                            DeviceHistoryEntitiy deviceHistoryEntitiy = deviceHistoryRepository.findByDeviceId(deviceid);
                            String previousRadioMode = deviceEntity.getWirelessMode();
                            String previousConnectedFrom = deviceEntity.getConnectedFrom();
                            deviceHistoryEntitiy.setWirelessMode(previousRadioMode);
                            deviceHistoryEntitiy.setConnectedFrom(previousConnectedFrom);
                            deviceHistoryEntitiy.setOperation("update");
                            LocalDateTime dateCreated = LocalDateTime.now();
                            deviceHistoryEntitiy.setTimeStamp(dateCreated);
                            deviceHistoryRepository.save(deviceHistoryEntitiy);
                            //update the radio mode and connected from in device table
                            deviceEntity.setWirelessMode(RadioModeSet);
                            deviceEntity.setConnectedFrom(connectedFromSet);
                            deviceRepository.save(deviceEntity);
                            String deviceName = deviceEntity.getDeviceName();
                            String deviceNameStr = "device_name," + deviceName;
                            System.out.println("deviceName: " + deviceName);
                            responses.put(getKey(deviceNameStr), getValue(deviceNameStr));
                            responses.put(getKey(currentIp), getValue(currentIp));
                            responses.put(getKey(radioModeStr), getValue(radioModeStr));
                            responses.put(getKey(connected_from), getValue(connected_from));
                            responsesArray.add(responses);

                        }
                        else {
                            String connected_from = "connected_from," + connected_fromStr;
                            // Get the device name from the connected_fromStr
                            DeviceEntity DeviceConnectedFrom = deviceRepository.findBydeviceName(connected_fromStr);
                            // Get the device id from the device name
                            Long connectedFromDeviceId = DeviceConnectedFrom.getDeviceId();
                            String RadioModeSet = "Station";
                            //Get the device from the current IP address
                            DeviceEntity deviceEntity = deviceRepository.findByIpAddress(ipAddress);
                            //save the previous radio mode and connected from to device history table
                            long deviceid = deviceEntity.getDeviceId();
                            DeviceHistoryEntitiy deviceHistoryEntitiy = deviceHistoryRepository.findByDeviceId(deviceid);
                            String previousRadioMode = deviceEntity.getWirelessMode();
                            String previousConnectedFrom = deviceEntity.getConnectedFrom();
                            deviceHistoryEntitiy.setWirelessMode(previousRadioMode);
                            deviceHistoryEntitiy.setConnectedFrom(previousConnectedFrom);
                            deviceHistoryEntitiy.setOperation("update");
                            LocalDateTime dateCreated = LocalDateTime.now();
                            deviceHistoryEntitiy.setTimeStamp(dateCreated);
                            deviceHistoryRepository.save(deviceHistoryEntitiy);
                            //update the radio mode and connected from in device table
                            deviceEntity.setWirelessMode(RadioModeSet);
                            deviceEntity.setConnectedFrom(connectedFromDeviceId.toString());
                            deviceRepository.save(deviceEntity);
                            String deviceName = deviceEntity.getDeviceName();
                            String deviceNameStr = "device_name," + deviceName;
                            System.out.println("deviceName: " + deviceName);
                            responses.put(getKey(deviceNameStr), getValue(deviceNameStr));
                            responses.put(getKey(currentIp), getValue(currentIp));
                            responses.put(getKey(radioModeStr), getValue(radioModeStr));
                            responses.put(getKey(connected_from), getValue(connected_from));
                            responsesArray.add(responses);
                        }
                    } else {
                        System.out.println("SNMP2 response not received");
                        String RadioModeSet = "Station";
                        String connectedFromSet = "0";
                        DeviceEntity deviceEntity = deviceRepository.findByIpAddress(ipAddress);
                        //save the previous radio mode and connected from to device history table
                        long deviceid = deviceEntity.getDeviceId();
                        DeviceHistoryEntitiy deviceHistoryEntitiy = deviceHistoryRepository.findByDeviceId(deviceid);
                        String previousRadioMode = deviceEntity.getWirelessMode();
                        String previousConnectedFrom = deviceEntity.getConnectedFrom();
                        deviceHistoryEntitiy.setWirelessMode(previousRadioMode);
                        deviceHistoryEntitiy.setConnectedFrom(previousConnectedFrom);
                        deviceHistoryEntitiy.setOperation("update");
                        LocalDateTime dateCreated = LocalDateTime.now();
                        deviceHistoryEntitiy.setTimeStamp(dateCreated);
                        deviceHistoryRepository.save(deviceHistoryEntitiy);
                        //update the radio mode and connected from in device table
                        deviceEntity.setWirelessMode(RadioModeSet);
                        deviceEntity.setConnectedFrom(connectedFromSet);
                        deviceRepository.save(deviceEntity);
                        String deviceName = deviceEntity.getDeviceName();
                        String deviceNameStr = "device_name," + deviceName;
                        System.out.println("deviceName: " + deviceName);
                        responses.put(getKey(deviceNameStr), getValue(deviceNameStr));
                        responses.put(getKey(currentIp), getValue(currentIp));
                        responses.put(getKey(radioModeStr), getValue(radioModeStr));
                        responses.put("connected_from","no response");
                    }
                }
            } else {
                System.out.println("SNMP response not received");
                Map<String, Object> error = new HashMap<>();
                DeviceEntity deviceEntity = deviceRepository.findByIpAddress(ipAddress);
                String deviceName = deviceEntity.getDeviceName();
                String deviceNameStr = "device_name," + deviceName;
                System.out.println("deviceName: " + deviceName);
                error.put(getKey(deviceNameStr), getValue(deviceNameStr));
                error.put("ip", ipAddress);
                error.put("error", "No response");
                errorArray.add(error);
            }
        }
        // Close the SNMP session
        snmp.close();
        System.out.println("SNMP Session Closed");
        ArrayList<Object> resultArray = new ArrayList<>();


        resultArray.add("success_data");
        resultArray.add(responsesArray);
        resultArray.add("error_data");
        resultArray.add(errorArray);

        Gson gson = new Gson();
        return gson.toJson(resultArray);
    }

    public String snmpGetRuntimeDeviceData(String ipaddress) throws Exception {
        System.out.println(ipaddress);
        NetworkInterfaceEntity networkInterfaceEntity = networkInterfaceRepository.findById(1);
        String interfaceName = networkInterfaceEntity.getInterfaceName();
        NetworkInterface networkInterface = getNetworkInterfaceByName(interfaceName);
        if (networkInterface == null) {
            throw new Exception("Network interface not found");
        }
        List<Map<String, Object>> responsesArray = new ArrayList<>();
        //get the ip address of the network interface
        InetAddress inetAddress = networkInterface.getInetAddresses().nextElement();
        //log the ip address of the network interface
        System.out.println("method: snmpGetRuntimeDeviceData");
        System.out.println("IP Address: " + inetAddress.getHostAddress());
        // Create the UDP address for the network interface
        UdpAddress udpAddress = new UdpAddress(inetAddress.getHostAddress() + "/5001");
        /* Create an SNMP session */
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping(udpAddress);
        Snmp snmp = new Snmp(transport);
        transport.listen();
        System.out.println("SNMP Session Created");
        // Define the community target
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setRetries(1);
        target.setTimeout(500);
        target.setVersion(SnmpConstants.version1);
        System.out.println("Community Target Created");
        //log ip argument
        System.out.println("target ip: " + ipaddress);
        //Check for any other instances where the address or port might be used// Create the target address object using the IP address
        Address targetAddressObject = new UdpAddress(ipaddress + "/161");
        target.setAddress(targetAddressObject);
        // Create a PDU for the SNMP request
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.41112.1.4.5.1.2.1")));//SSID OID
        pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.41112.1.4.5.1.14.1")));//ChanWidth OID
        pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.41112.1.4.1.1.3.1")));//Channel code OID
        pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.41112.1.4.1.1.4.1")));//Frequency OID
        pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.41112.1.4.1.1.6.1")));//RadioTxPower OID
        pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.41112.1.4.1.1.9.1")));//RadioAntenna OID
        pdu.add(new VariableBinding(new OID(".1.3.6.1.2.1.2.2.1.5.2")));//Ifspeed2 OID
        pdu.setType(PDU.GET);

        ResponseEvent event = snmp.send(pdu, target);
        System.out.println("SNMP request sent" + ipaddress);
        if (event != null && event.getResponse() != null) {
            System.out.println("SNMP response received");
            VariableBinding[] variableBindings = event.getResponse().toArray();
            System.out.println("responses: " + Arrays.toString(variableBindings));
            String SSID = variableBindings[0].getVariable().toString();
            String ChanWidth = variableBindings[1].getVariable().toString();
            String ChannelCode = variableBindings[2].getVariable().toString();
            String Frequency = variableBindings[3].getVariable().toString();
            String RadioTxPower = variableBindings[4].getVariable().toString();
            String RadioAntenna = variableBindings[5].getVariable().toString();
            String lan;
            if(variableBindings[6].getVariable().toInt() > 0){
                lan = "Connected";
            }else {
                lan = "Disconnected";
            }
            Map<String, Object> response = new HashMap<>();
            response.put("SSID", SSID);
            response.put("ChanWidth", ChanWidth);
            response.put("ChannelCode", ChannelCode);
            response.put("Frequency", Frequency);
            response.put("RadioTxPower", RadioTxPower);
            response.put("RadioAntenna", RadioAntenna);
            response.put("lan", lan);
            responsesArray.add(response);
        } else {
            System.out.println("SNMP request timed out");
            snmp.close();
        }
        snmp.close();
        Gson gson = new Gson();
        return gson.toJson(responsesArray);
    }


    public String snmpGetRecurringDeviceData(String ipaddress) throws Exception {
//        System.out.println(ipaddress);
        NetworkInterfaceEntity networkInterfaceEntity = networkInterfaceRepository.findById(1);
        String interfaceName = networkInterfaceEntity.getInterfaceName();
        NetworkInterface networkInterface = getNetworkInterfaceByName(interfaceName);
        if (networkInterface == null) {
            throw new Exception("Network interface not found");
        }
        List<Map<String, Object>> responsesArray = new ArrayList<>();
        //get the ip address of the network interface
        InetAddress inetAddress = networkInterface.getInetAddresses().nextElement();
        //log the ip address of the network interface
//        System.out.println("method: snmpGetRecurringDeviceData");
        System.out.println("IP Address: " + inetAddress.getHostAddress());
        // Create the UDP address for the network interface
        UdpAddress udpAddress = new UdpAddress(inetAddress.getHostAddress() + "/5002");
        /* Create an SNMP session */
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping(udpAddress);
        Snmp snmp = new Snmp(transport);
        transport.listen();
//        System.out.println("SNMP Session Created");
        // Define the community target
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setRetries(1);
        target.setTimeout(500);
        target.setVersion(SnmpConstants.version1);
//        System.out.println("Community Target Created");
        //log ip argument
//        System.out.println("target ip: " + ipaddress);
        //Check for any other instances where the address or port might be used// Create the target address object using the IP address
        Address targetAddressObject = new UdpAddress(ipaddress + "/161");
        target.setAddress(targetAddressObject);
        // Create a PDU for the SNMP request
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(".1.3.6.1.2.1.1.3.0"))); // SysUptime OID
        pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.41112.1.4.5.1.5.1"))); // Signal OID
        pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.41112.1.4.6.1.3.1"))); //AirMaxQuality OID
        pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.41112.1.4.6.1.4.1"))); //AirMaxCapacity OID
        pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.41112.1.4.5.1.7.1")));//CCQ OID
        pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.41112.1.4.5.1.15.1")));//Station Count OID
        pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.41112.1.4.5.1.8.1"))); //NoiseFloor OID
        pdu.setType(PDU.GET);

        ResponseEvent event = snmp.send(pdu, target);
//        System.out.println("SNMP request sent" + ipaddress);
        if (event != null && event.getResponse() != null) {
//            System.out.println("SNMP response received");
            VariableBinding[] variableBindings = event.getResponse().toArray();
//            System.out.println("responses: " + Arrays.toString(variableBindings));
            String SysUptime = variableBindings[0].getVariable().toString();
            String signal = variableBindings[1].getVariable().toString();
            String AirMaxQuality = variableBindings[2].getVariable().toString();
            String AirMaxCapacity = variableBindings[3].getVariable().toString();
            String CCQ = variableBindings[4].getVariable().toString();
            String StationCount = variableBindings[5].getVariable().toString();
            String NoiseFloor = variableBindings[6].getVariable().toString();
            Map<String, Object> response = new HashMap<>();
            response.put("SysUptime", SysUptime);
            response.put("signal", signal);
            response.put("AirMaxQuality", AirMaxQuality);
            response.put("AirMaxCapacity", AirMaxCapacity);
            response.put("CCQ", CCQ);
            response.put("StationCount", StationCount);
            response.put("NoiseFloor", NoiseFloor);
            responsesArray.add(response);
        } else {
//            System.out.println("SNMP request timed out");
        }
        snmp.close();
        Gson gson = new Gson();
        return gson.toJson(responsesArray);
    }


    public Map<String, Object> update_radiomode_connectedfrom_single(String  ipaddress) throws Exception {
        NetworkInterfaceEntity networkInterfaceEntity = networkInterfaceRepository.findById(1);
        String interfaceName = networkInterfaceEntity.getInterfaceName();
        NetworkInterface networkInterface = getNetworkInterfaceByName(interfaceName);
        if (networkInterface == null) {
            throw new Exception("Network interface not found");
        }
        //get device entity
        DeviceEntity deviceEntity = deviceRepository.findByIpAddress(ipaddress);
        System.out.println("deviceEntity: " + deviceEntity);


        //get the ip address of the network interface
        InetAddress inetAddress = networkInterface.getInetAddresses().nextElement();
        //log the ip address of the network interface
        System.out.println("method: update_radiomode_connectedfrom_single");
        System.out.println("IP Address: " + inetAddress.getHostAddress());
        // Create the UDP address for the network interface
        UdpAddress udpAddress = new UdpAddress(inetAddress.getHostAddress() + "/5003");
        System.out.println("socketaddress" + udpAddress);

        /* Create an SNMP session */
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping(udpAddress);
        Snmp snmp = new Snmp(transport);
        transport.listen();
        System.out.println("SNMP Session Created");
        // Define the community target
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setRetries(1);
        target.setTimeout(500);
        target.setVersion(SnmpConstants.version1);
        System.out.println("Community Target Created");
        //log ip argument
        System.out.println("target ip: " + ipaddress);
        //Check for any other instances where the address or port might be used// Create the target address object using the IP address
        Address targetAddressObject = new UdpAddress(ipaddress + "/161");
        target.setAddress(targetAddressObject);
        // Create a PDU for the SNMP request
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.41112.1.4.1.1.2.1"))); // Radio mode OID
        pdu.setType(PDU.GET);
        Map<String, Object> responses = new HashMap<>();
        ResponseEvent event = snmp.send(pdu, target);
        System.out.println("update_radiomode_connectedfrom_single method socket");
        System.out.println("SNMP request sent" + ipaddress);
        if (event != null && event.getResponse() != null) {
            System.out.println("SNMP response received");
            VariableBinding[] variableBindings = event.getResponse().toArray();
            int radioMode = variableBindings[0].getVariable().toInt();
            if (radioMode == 4 || radioMode == 3 || radioMode == 2) {
                String RadioModeSet = "AP";
                String connectedFrom = "0";
                //save the previous radio mode and connected from to device history table
                long deviceid = deviceEntity.getDeviceId();
                DeviceHistoryEntitiy deviceHistoryEntitiy = deviceHistoryRepository.findByDeviceId(deviceid);
                String previousRadioMode = deviceEntity.getWirelessMode();
                String previousConnectedFrom = deviceEntity.getConnectedFrom();
                deviceHistoryEntitiy.setWirelessMode(previousRadioMode);
                deviceHistoryEntitiy.setConnectedFrom(previousConnectedFrom);
                deviceHistoryEntitiy.setOperation("update");
                LocalDateTime dateCreated = LocalDateTime.now();
                deviceHistoryEntitiy.setTimeStamp(dateCreated);
                deviceHistoryRepository.save(deviceHistoryEntitiy);
                //update the radio mode and connected from in device table
                deviceEntity.setWirelessMode(RadioModeSet);
                deviceEntity.setConnectedFrom(connectedFrom);
                deviceRepository.save(deviceEntity);
                System.out.println("deviceEntity: " + deviceEntity);
                responses.put("RadioMode", RadioModeSet);
                responses.put("connectedFrom", connectedFrom);
            } else {
                PDU pdu2 = new PDU();
                pdu2.add(new VariableBinding(new OID(".1.3.6.1.4.1.41112.1.4.7.1.2"))); // ubntMac
                pdu2.setType(PDU.GETNEXT);
                ResponseEvent event2 = snmp.send(pdu2, target);
                System.out.println("SNMP2 request sent");
                if (event2 != null && event2.getResponse() != null) {
                    System.out.println("SNMP2 response received");
                    VariableBinding[] variableBindings2 = event2.getResponse().toArray();
                    String connected_fromStr = variableBindings2[0].getVariable().toString();
                    System.out.println("connected_fromStr: " + connected_fromStr);
                    if (Objects.equals(connected_fromStr, "Null")) {
                        String RadioModeSet = "Station";
                        String connectedFromSet = "0";
                        //save the previous radio mode and connected from to device history table
                        long deviceid = deviceEntity.getDeviceId();
                        DeviceHistoryEntitiy deviceHistoryEntitiy = deviceHistoryRepository.findByDeviceId(deviceid);
                        String previousRadioMode = deviceEntity.getWirelessMode();
                        String previousConnectedFrom = deviceEntity.getConnectedFrom();
                        deviceHistoryEntitiy.setWirelessMode(previousRadioMode);
                        deviceHistoryEntitiy.setConnectedFrom(previousConnectedFrom);
                        deviceHistoryEntitiy.setOperation("update");
                        LocalDateTime dateCreated = LocalDateTime.now();
                        deviceHistoryEntitiy.setTimeStamp(dateCreated);
                        deviceHistoryRepository.save(deviceHistoryEntitiy);
                        //update the radio mode and connected from in device table
                        deviceEntity.setWirelessMode(RadioModeSet);
                        deviceEntity.setConnectedFrom(connectedFromSet);
                        deviceRepository.save(deviceEntity);
                        System.out.println("deviceEntity: " + deviceEntity);
                        responses.put("RadioMode", RadioModeSet);
                        responses.put("connectedFrom", connectedFromSet);
                    } else {
                        DeviceEntity DeviceConnectedFrom = deviceRepository.findBydeviceName(connected_fromStr);
                        // Get the device id from the device name
                        Long connectedFromDeviceId = DeviceConnectedFrom.getDeviceId();
                        String RadioModeSet = "Station";
                        //save the previous radio mode and connected from to device history table
                        long deviceid = deviceEntity.getDeviceId();
                        DeviceHistoryEntitiy deviceHistoryEntitiy = deviceHistoryRepository.findByDeviceId(deviceid);
                        String previousRadioMode = deviceEntity.getWirelessMode();
                        String previousConnectedFrom = deviceEntity.getConnectedFrom();
                        deviceHistoryEntitiy.setWirelessMode(previousRadioMode);
                        deviceHistoryEntitiy.setConnectedFrom(previousConnectedFrom);
                        deviceHistoryEntitiy.setOperation("update");
                        LocalDateTime dateCreated = LocalDateTime.now();
                        deviceHistoryEntitiy.setTimeStamp(dateCreated);
                        deviceHistoryRepository.save(deviceHistoryEntitiy);
                        //update the radio mode and connected from in device table
                        //Get the device from the current IP address
                        deviceEntity.setWirelessMode(RadioModeSet);
                        deviceEntity.setConnectedFrom(connectedFromDeviceId.toString());
                        deviceRepository.save(deviceEntity);
                        System.out.println("deviceEntity: " + deviceEntity);
                        responses.put("RadioMode", RadioModeSet);
                        responses.put("connectedFrom", connectedFromDeviceId.toString());
                    }

                } else {
                    System.out.println("SNMP2 response not received");
                    String RadioModeSet = "Station";
                    String connectedFromSet = "0";
                    //save the previous radio mode and connected from to device history table
                    long deviceid = deviceEntity.getDeviceId();
                    DeviceHistoryEntitiy deviceHistoryEntitiy = deviceHistoryRepository.findByDeviceId(deviceid);
                    String previousRadioMode = deviceEntity.getWirelessMode();
                    String previousConnectedFrom = deviceEntity.getConnectedFrom();
                    deviceHistoryEntitiy.setWirelessMode(previousRadioMode);
                    deviceHistoryEntitiy.setConnectedFrom(previousConnectedFrom);
                    deviceHistoryEntitiy.setOperation("update");
                    LocalDateTime dateCreated = LocalDateTime.now();
                    deviceHistoryEntitiy.setTimeStamp(dateCreated);
                    deviceHistoryRepository.save(deviceHistoryEntitiy);
                    //update the radio mode and connected from in device table
                    deviceEntity.setWirelessMode(RadioModeSet);
                    deviceEntity.setConnectedFrom(connectedFromSet);
                    deviceRepository.save(deviceEntity);
                    System.out.println("deviceEntity: " + deviceEntity);
                    responses.put("RadioMode", RadioModeSet);
                    responses.put("connectedFrom", connectedFromSet);
                }
            }
        } else {
            System.out.println("SNMP response not received");
            String RadioModeSet = "Null";
            String connectedFromSet = "Null";
            responses.put("RadioMode", RadioModeSet);
            responses.put("connectedFrom", connectedFromSet);
            System.out.println("SNMP Session Closed");
            snmp.close();
        }
        // Close the SNMP session
        snmp.close();
        System.out.println("SNMP Session Closed");
        return responses;
    }
}

