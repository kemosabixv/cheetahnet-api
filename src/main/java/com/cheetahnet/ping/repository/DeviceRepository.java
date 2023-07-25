package com.cheetahnet.ping.repository;

import com.cheetahnet.ping.model.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<DeviceEntity, Long> {
    //get list of devices
    List<DeviceEntity> findAll();

    DeviceEntity findByIpAddress(String ipAddress);

    DeviceEntity findByWirelessMode(String wirelessMode);

    DeviceEntity findBydeviceName(String device_name);

}
