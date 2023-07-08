package com.cheetahnet.ping.service;

import com.cheetahnet.ping.model.NetworkInterfaceEntity;
import com.cheetahnet.ping.repository.NetworkInterfaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.*;
import java.util.Enumeration;
import java.util.List;

@Service
public class NetworkInterfacesService {

    private final NetworkInterfaceRepository networkInterfaceRepository;

    @Autowired
    public NetworkInterfacesService(NetworkInterfaceRepository networkInterfaceRepository) {
        this.networkInterfaceRepository = networkInterfaceRepository;
    }

    public String getIpAddressAndSubnetForInterface() {
        NetworkInterfaceEntity networkInterface = networkInterfaceRepository.findById(1);

        if (networkInterface != null) {
            String interfaceDisplayName = networkInterface.getInterfaceDisplayName();
            String interfaceName = getInterfaceNameForInterface(interfaceDisplayName);
            String ipAddress = getIpAddressForInterface(interfaceName);
            String subnet = getSubnetForInterface(interfaceName);
            if (ipAddress != null && subnet != null) {
                // Update the existing entity or create a new one
                if (networkInterface.getId() == 1) {
                    networkInterface.setInterfaceName(interfaceName);
                    networkInterface.setIpAddress(ipAddress);
                    networkInterface.setSubnet(subnet);
                } else {
                    // Create a new entity
                    networkInterface = new NetworkInterfaceEntity();
                    networkInterface.setId(1);
                    networkInterface.setInterfaceName(interfaceName);
                    networkInterface.setIpAddress(ipAddress);
                    networkInterface.setSubnet(subnet);
                }

                networkInterfaceRepository.save(networkInterface);

                return "Interface Name: " + interfaceName + "\nIP Address: " + ipAddress + "\nSubnet: " + subnet;
            } else {
                networkInterface.setIpAddress("");
                networkInterface.setSubnet("");
                networkInterfaceRepository.save(networkInterface);
                return "IP address or subnet not found for the interface.";

            }

        } else {
            networkInterface.setInterfaceName("Not Found");
            networkInterface.setIpAddress("");
            networkInterface.setSubnet("");
            networkInterfaceRepository.save(networkInterface);
            return "Interface not found for the given ID.";
        }
    }

    private String getInterfaceNameForInterface(String interfaceDisplayName) {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.getDisplayName().equals(interfaceDisplayName)) {
                    return networkInterface.getName();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return null; // Handle the case when the interface name is not found
    }


    private String getIpAddressForInterface(String interfaceName) {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.getName().equals(interfaceName)) {
                    Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress address = addresses.nextElement();
                        if (!address.isLoopbackAddress() && !address.isLinkLocalAddress() && !address.isMulticastAddress()) {
                            return address.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return null; // Handle the case when the interface or IP address is not found
    }


    private String getSubnetForInterface(String interfaceName) {
        try {
            NetworkInterface networkInterface = NetworkInterface.getByName(interfaceName);
            List<InterfaceAddress> addresses = networkInterface.getInterfaceAddresses();
            for (InterfaceAddress address : addresses) {
                if (address.getAddress() instanceof Inet4Address) {
                    String subnetMask = convertSubnetMask(address.getNetworkPrefixLength());
                    return subnetMask;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return null; // Handle the case when the interface or subnet mask is not found
    }

    private String convertSubnetMask(short prefixLength) {
        int subnetMask = 0xffffffff << (32 - prefixLength);
        int octet1 = (subnetMask >> 24) & 0xff;
        int octet2 = (subnetMask >> 16) & 0xff;
        int octet3 = (subnetMask >> 8) & 0xff;
        int octet4 = subnetMask & 0xff;
        return octet1 + "." + octet2 + "." + octet3 + "." + octet4;
    }




}
