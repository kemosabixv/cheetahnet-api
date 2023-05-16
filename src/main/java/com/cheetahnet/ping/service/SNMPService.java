package com.cheetahnet.ping.service;
import java.net.*;

import java.util.Arrays;
import java.util.List;

import com.cheetahnet.ping.model.NetworkInterfaceEntity;
import com.cheetahnet.ping.repository.NetworkInterfaceRepository;
import com.google.gson.Gson;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.beans.factory.annotation.Autowired;

public class SNMPService {

    private final NetworkInterfaceRepository networkInterfaceRepository;

    @Autowired
    public SNMPService(NetworkInterfaceRepository networkInterfaceRepository) {
        this.networkInterfaceRepository = networkInterfaceRepository;
    }

    public String scanDevices() throws Exception {

        NetworkInterfaceEntity networkInterfaceEntity = networkInterfaceRepository.findById(1);
        String interfaceName = networkInterfaceEntity.getInterfaceName();
        String targetAddress = networkInterfaceEntity.getIpAddress();
        String subnetMask = networkInterfaceEntity.getSubnet();
        NetworkInterface networkInterface = getNetworkInterfaceByName(interfaceName);
        if (networkInterface == null) {
            throw new Exception("Network interface not found");
        }

        // Calculate the network address and broadcast address of the LAN
        String[] ipAddressParts = targetAddress.split("\\.");
        String[] subnetMaskParts = subnetMask.split("\\.");
        int[] networkAddress = new int[4];
        int[] broadcastAddress = new int[4];

        for (int i = 0; i < 4; i++) {
            networkAddress[i] = Integer.parseInt(ipAddressParts[i]) & Integer.parseInt(subnetMaskParts[i]);
            broadcastAddress[i] = networkAddress[i] | (~Integer.parseInt(subnetMaskParts[i]) & 0xff);
        }

        String subnet = networkAddress[0] + "." + networkAddress[1] + "." + networkAddress[2] + ".";

        // Create a SNMP session
        TransportMapping transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();

        // Define the community target
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setRetries(1);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);

        // Loop through the IP address range and send SNMP requests
        List<Object> resultList = null;
        for (int i = networkAddress[3] + 1; i < broadcastAddress[3]; i++) {
            String currentAddress = subnet + i;

            // Add the current address to the  result array
            Object[] resultArray = {currentAddress};

            //set target address to current address with port 161
            target.setAddress(new UdpAddress(currentAddress + "/161"));


            // Create a PDU for the SNMP request
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.0"))); // SysName OID
            pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.41112.1.4.1.1.2"))); // Radio mode OID
//            pdu.add(new VariableBinding(new OID(".1.3.6.1.2.1.1.3.0"))); // SysUptime OID
            pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.41112.1.4.7.1.2"))); // Connected from OID
            pdu.add(new VariableBinding(new OID("..1.3.6.1.4.1.41112.1.4.5.1.5.1"))); // Signal OID

            pdu.setType(PDU.GET);
            ResponseEvent event = snmp.send(pdu, target);
            if (event != null && event.getResponse() != null) {
                VariableBinding[] variableBindings = event.getResponse().toArray();

                int radioMode = -1;
                String connectedFrom = null;
                for (VariableBinding vb : variableBindings) {
                    OID oid = vb.getOid();
                    Variable variable = vb.getVariable();
                    if (oid.toString().equals(".1.3.6.1.4.1.41112.1.4.1.1.2")) {
                        Integer32 variableInt = (Integer32) variable;
                        radioMode = variableInt.toInt();
                    } else if (oid.toString().equals(".1.3.6.1.4.1.41112.1.4.7.1.2") && radioMode == 1) {
                        OctetString variableString = (OctetString) variable;
                        connectedFrom = variableString.toString();
                        //add connected from to result array
                        resultArray[4] = connectedFrom;
                    }
                }

                if (radioMode != -1) {
                    String sysName = variableBindings[1].getVariable().toString();
                    String signal = variableBindings[4].getVariable().toString();
                    String ipAddress = currentAddress;

                    // Check the radio mode OID and set the radio mode string accordingly
                    String radioModeStr = "";
                    if (radioMode == 1) {
                        radioModeStr = "Station";
                    } else if (radioMode == 4) {
                        radioModeStr = "AP";
                    }

                    // add ipAddress, sysName, radioModeStr, signal to result array
                    resultArray[1] = ipAddress;
                    resultArray[2] = sysName;
                    resultArray[3] = radioModeStr;
                    resultArray[5] = signal;


                }
            }
            // Add the result array to the result list
            resultList = Arrays.asList(resultArray);

        }

        // Close the SNMP session
        snmp.close();
        //convert result list to json and return it
        return new Gson().toJson(resultList);
    }

    private static String convertMaskToCIDR(int mask) {
        int[] bits = new int[32];

        for (int i = 0; i < mask; i++) {
            bits[i] = 1;
        }

        String binaryMask = "";

        for (int i = 0; i < 32; i += 8) {
            int octet = 0;

            for (int j = i; j < i + 8; j++) {
                octet += bits[j] * Math.pow(2, 7 - (j % 8));
            }

            binaryMask += octet + ".";
        }
        return binaryMask.substring(0, binaryMask.length() - 1);
    }

    private NetworkInterface getNetworkInterfaceByName(String interfaceName) throws SocketException {
        return NetworkInterface.getByName(interfaceName);
    }
}

