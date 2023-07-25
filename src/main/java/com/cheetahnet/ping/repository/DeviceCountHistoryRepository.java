package com.cheetahnet.ping.repository;

import com.cheetahnet.ping.model.DeviceCountHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceCountHistoryRepository extends JpaRepository<DeviceCountHistoryEntity, Long> {
}
