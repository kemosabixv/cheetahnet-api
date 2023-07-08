package com.cheetahnet.ping.repository;
import com.cheetahnet.ping.model.DeviceEntity;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.function.Function;

@Repository
public interface DeviceRepository extends JpaRepository<DeviceEntity, Long> {
    //get list of devices
    List<DeviceEntity> findAll();

    DeviceEntity findByIpAddress(String ipAddress);



    DeviceEntity findBydeviceName(String device_name);

}
