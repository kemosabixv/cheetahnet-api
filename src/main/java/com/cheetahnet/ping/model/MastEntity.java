package com.cheetahnet.ping.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_masts")
public class MastEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mastid")
    private Long mastId;

    @Column(nullable = false, name = "mast_name")
    private String mastName;

    @Column(name = "location")
    private String location;

    @Column(name = "height")
    private String height;

    @Column(name = "connection_via")
    private String connectionVia;

    @Column(name = "connected_from")
    private String connectedFrom;


    @Column(nullable = true, name = "dateCreated")
    private LocalDateTime dateCreated;

    // Getters and Setters...

    public Long getMastId() {
        return mastId;
    }

    public void setMastId(Long mastId) {
        this.mastId = mastId;
    }

    public String getMastName() {
        return mastName;
    }

    public void setMastName(String mastName) {
        this.mastName = mastName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
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

