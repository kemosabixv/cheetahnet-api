package com.cheetahnet.ping.repository;


import com.cheetahnet.ping.model.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface MastRepository extends JpaRepository<DeviceEntity, Long> {
}
