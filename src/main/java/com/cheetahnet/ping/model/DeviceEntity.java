package com.cheetahnet.ping.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_devices")
public class DeviceEntity {

//    public DeviceEntity(Long deviceId, String device_name, String mastId, String wirelessMode, String ipAddress, String connectedFrom, String connectionStatus, String macAddress, String SSID, String deviceModel, String deviceModelShort, String firmwareVersion, LocalDateTime dateCreated) {
//        this.deviceId = deviceId;
//        this.device_name = device_name;
//        this.mastId = mastId;
//        this.wirelessMode = wirelessMode;
//        this.ipAddress = ipAddress;
//        this.connectedFrom = connectedFrom;
//        this.connectionStatus = connectionStatus;
//        this.macAddress = macAddress;
//        this.SSID = SSID;
//        this.deviceModel = deviceModel;
//        this.deviceModelShort = deviceModelShort;
//        this.firmwareVersion = firmwareVersion;
//        this.dateCreated = dateCreated;
//    }

    @Id
    @Column(name = "deviceid")
    private Long deviceId;

    @Column(nullable = false, name = "device_name")
    private String deviceName;

    @Column(name = "mastid")
    private String mastId;

    @Column(name = "wireless_mode")
    private String wirelessMode;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "connected_from")
    private String connectedFrom;

    @Column(name = "connection_status")
    private String connectionStatus;

    @Column(name = "mac")
    private String macAddress;

    @Column(name = "ssid")
    private String SSID;

    @Column(name = "device_model")
    private String deviceModel;

    @Column(name = "model_short")
    private String deviceModelShort;

    @Column(name = "firmware_version")
    private String firmwareVersion;

    @Column(nullable = false, name = "dateCreated")
    private LocalDateTime dateCreated;

    // Getters and Setters

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getMastId() {
        return mastId;
    }

    public void setMastId(String mastId) {
        this.mastId = mastId;
    }

    public String getWirelessMode() {
        return wirelessMode;
    }

    public void setWirelessMode(String wirelessMode) {
        this.wirelessMode = wirelessMode;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getConnectedFrom() {
        return connectedFrom;
    }

    public void setConnectedFrom(String connectedFrom) {
        this.connectedFrom = connectedFrom;
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDeviceModelShort() {
        return deviceModelShort;
    }

    public void setDeviceModelShort(String deviceModelShort) {
        this.deviceModelShort = deviceModelShort;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }
}
