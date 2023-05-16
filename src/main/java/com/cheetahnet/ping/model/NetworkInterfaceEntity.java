package com.cheetahnet.ping.model;

import jakarta.persistence.*;
@Entity
@Table(name = "tbl_interfaces")
public class NetworkInterfaceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "interfacename")
    private String interfaceName;

    @Column(name = "interfacedisplayname")
    private String interfaceDisplayName;

    @Column(name = "ipaddress")
    private String ipAddress;

    @Column(name = "subnet")
    private String subnet;

    // Getter for 'id' field
    public int getId() {
        return id;
    }

    // Setter for 'id' field
    public void setId(int id) {
        this.id = id;
    }

    // Getter for 'interfaceName' field
    public String getInterfaceName() {
        return interfaceName;
    }

    // Setter for 'interfaceName' field
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getInterfaceDisplayName() {
        return interfaceDisplayName;
    }

    public void setInterfaceDisplayName(String interfaceDisplayName) {
        this.interfaceDisplayName = interfaceDisplayName;
    }

    // Getter for 'ipAddress' field
    public String getIpAddress() {
        return ipAddress;
    }

    // Setter for 'ipAddress' field
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    // Getter for 'subnet' field
    public String getSubnet() {
        return subnet;
    }

    // Setter for 'subnet' field
    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }
}
