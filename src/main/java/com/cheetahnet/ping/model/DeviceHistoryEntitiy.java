package com.cheetahnet.ping.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "tbl_devices_history")
public class DeviceHistoryEntitiy {

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

    @Column(name = "operation")
    private String operation;

    @Column(name = "timestamp")
    private LocalDateTime timeStamp;

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

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }
}

