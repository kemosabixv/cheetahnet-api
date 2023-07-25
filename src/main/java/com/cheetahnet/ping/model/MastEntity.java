package com.cheetahnet.ping.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;


@Entity
@Table(name = "tbl_masts")
public class MastEntity {

    @Id
    @Column(name = "mastid")
    private Long mastId;

    @Column(nullable = false, name = "mast_name")
    private String MastName;

    @Column(name = "location")
    private String Location;

    @Column(name = "height")
    private String Height;

    @Column(name = "connection_via")
    private String connectionVia;

    @Column(name = "connected_from")
    private String connectedFrom;


    @Column(nullable = false, name = "dateCreated")
    private LocalDateTime dateCreated;

    // Getters and Setters

    public Long getMastId() {
        return mastId;
    }

    public void setMastId(Long mastId) {
        this.mastId = mastId;
    }

    public String getMastName() {
        return MastName;
    }

    public void setMastName(String mastName) {
        MastName = mastName;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getHeight() {
        return Height;
    }

    public void setHeight(String height) {
        Height = height;
    }

    public String getConnectionVia() {
        return connectionVia;
    }

    public void setConnectionVia(String connectionVia) {
        this.connectionVia = connectionVia;
    }

    public String getConnectedFrom() {
        return connectedFrom;
    }

    public void setConnectedFrom(String connectedFrom) {
        this.connectedFrom = connectedFrom;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }
}

