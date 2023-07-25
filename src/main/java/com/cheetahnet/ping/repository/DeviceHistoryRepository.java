package com.cheetahnet.ping.repository;


import com.cheetahnet.ping.model.DeviceHistoryEntitiy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceHistoryRepository extends JpaRepository<DeviceHistoryEntitiy, Long> {
    DeviceHistoryEntitiy findByDeviceId(Long deviceId);
}
