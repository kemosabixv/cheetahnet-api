package com.cheetahnet.ping.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "tbl_device_count_history")
public class DeviceCountHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long Id;

    @Column(name = "station_count")
    private int StationCount;

    @Column(name = "ap_count")
    private int APCount;

    @Column(name = "total_count")
    private int TotalCount;

    @Column(name = "date_created")
    private LocalDateTime DateCreated;

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public int getStationCount() {
        return StationCount;
    }

    public void setStationCount(int stationCount) {
        StationCount = stationCount;
    }

    public int getAPCount() {
        return APCount;
    }

    public void setAPCount(int APCount) {
        this.APCount = APCount;
    }

    public int getTotalCount() {
        return TotalCount;
    }

    public void setTotalCount(int totalCount) {
        TotalCount = totalCount;
    }

    public LocalDateTime getDateCreated() {
        return DateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        DateCreated = dateCreated;
    }
}
