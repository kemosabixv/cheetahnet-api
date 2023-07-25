package com.cheetahnet.ping.repository;


import com.cheetahnet.ping.model.DeviceConnectionHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceConnectionHistoryRepository extends JpaRepository<DeviceConnectionHistoryEntity, Long> {
}
