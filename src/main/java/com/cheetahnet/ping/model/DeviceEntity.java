package com.cheetahnet.ping.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_devices")
public class DeviceEntity {

    public DeviceEntity() {
    }

    public DeviceEntity(Long deviceid, String device_name, String mastId, String wireless_mode, String ip_address, String connected_from, String connection_status, LocalDateTime dateCreated) {
        this.deviceid = deviceid;
        this.device_name = device_name;
        this.mastid = mastid;
        this.wireless_mode = wireless_mode;
        this.ip_address = ip_address;
        this.connected_from = connected_from;
        this.connection_status = connection_status;
        this.dateCreated = dateCreated;
    }

    @Id
    @Column(name = "deviceid")
    private Long deviceid;

    @Column(nullable = false, name = "device_name")
    private String device_name;

    @Column(name = "mastid")
    private String mastid;

    @Column(name = "wireless_mode")
    private String wireless_mode;

    @Column(name = "ip_address")
    private String ip_address;

    @Column(name = "connected_from")
    private String connected_from;

    @Column(name = "connection_status")
    private String connection_status;

    @Column(nullable = false, name = "dateCreated")
    private LocalDateTime dateCreated;

    // Getters and Setters

    public Long getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(Long deviceid) {
        this.deviceid = deviceid;
    }

    public String getDevice_name() {
        return device_name;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }

    public String getMastid() {
        return mastid;
    }

    public void setMastid(String mastid) {
        this.mastid = mastid;
    }

    public String getWireless_mode() {
        return wireless_mode;
    }

    public void setWireless_mode(String wireless_mode) {
        this.wireless_mode = wireless_mode;
    }

    public String getIp_address() {
        return ip_address;
    }

    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }

    public String getConnected_from() {
        return connected_from;
    }

    public void setConnected_from(String connected_from) {
        this.connected_from = connected_from;
    }

    public String getConnection_status() {
        return connection_status;
    }

    public void setConnection_status(String connection_status) {
        this.connection_status = connection_status;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }
}
