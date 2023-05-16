package com.cheetahnet.ping.service;

import org.springframework.stereotype.Service;

import java.net.*;
import java.util.Enumeration;

@Service
public class NetworkInterfacesService2 {
    public String getAllNetworkInterfacesInfo() {
        StringBuilder sb = new StringBuilder();

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                sb.append("Interface Name: ").append(networkInterface.getName()).append("\n");
                sb.append("Display Name: ").append(networkInterface.getDisplayName()).append("\n");
                sb.append("Index: ").append(networkInterface.getIndex()).append("\n");

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    sb.append("IP Address: ").append(address.getHostAddress()).append("\n");
                }

                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    StringBuilder macAddress = new StringBuilder();
                    for (byte b : mac) {
                        macAddress.append(String.format("%02X", b)).append(":");
                    }
                    if (macAddress.length() > 0) {
                        macAddress.setLength(macAddress.length() - 1);
                    }
                    sb.append("MAC Address: ").append(macAddress.toString()).append("\n");
                }

                sb.append("\n");
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}
