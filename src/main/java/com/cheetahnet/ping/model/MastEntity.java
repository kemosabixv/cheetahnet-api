package com.cheetahnet.ping.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tbl_interfaces")
public class MastEntity {

    @Id
    @Column(name = "mastid")
    private int mastid;

    @Column(name = "mast_name")
    private String mast_name;

    @Column(name = "location")
    private String location;

    @Column(name = "connection_via")
    private String connection_via;

    @Column(name = "connected_from")
    private String connected_from;

    @Column(name = "dateCreated")
    private String dateCreated;

    public int getMastid() {
        return mastid;
    }

    public void setMastid(int mastid) {
        this.mastid = mastid;
    }

    public String getMast_name() {
        return mast_name;
    }

    public void setMast_name(String mast_name) {
        this.mast_name = mast_name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getConnection_via() {
        return connection_via;
    }

    public void setConnection_via(String connection_via) {
        this.connection_via = connection_via;
    }

    public String getConnected_from() {
        return connected_from;
    }

    public void setConnected_from(String connected_from) {
        this.connected_from = connected_from;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }
}
