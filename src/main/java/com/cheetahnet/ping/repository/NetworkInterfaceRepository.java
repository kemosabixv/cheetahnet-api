package com.cheetahnet.ping.repository;
import com.cheetahnet.ping.model.NetworkInterfaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface NetworkInterfaceRepository extends JpaRepository<NetworkInterfaceEntity, Long> {
    NetworkInterfaceEntity findById(int id);
    NetworkInterfaceEntity findByInterfaceName(String interfaceName);
}
