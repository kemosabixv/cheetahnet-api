package com.cheetahnet.ping.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "tbl_connection_status_history")
public class DeviceConnectionHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long Id;

    @Column(name = "offline_count")
    private int OfflineCount;

    @Column(name = "online_count")
    private int OnlineCount;

    @Column(name = "date_created")
    private LocalDateTime DateCreated;

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public int getOfflineCount() {
        return OfflineCount;
    }

    public void setOfflineCount(int offlineCount) {
        OfflineCount = offlineCount;
    }

    public int getOnlineCount() {
        return OnlineCount;
    }

    public void setOnlineCount(int onlineCount) {
        OnlineCount = onlineCount;
    }

    public LocalDateTime getDateCreated() {
        return DateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        DateCreated = dateCreated;
    }
}
