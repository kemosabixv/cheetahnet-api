package com.cheetahnet.ping.repository;

import com.cheetahnet.ping.model.DeviceEntity;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DeviceRepository extends CrudRepository<DeviceEntity, Long> {

}
