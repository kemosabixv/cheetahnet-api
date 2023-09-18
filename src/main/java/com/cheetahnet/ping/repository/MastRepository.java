package com.cheetahnet.ping.repository;
import com.cheetahnet.ping.model.MastEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MastRepository extends JpaRepository<MastEntity, Long> {
    MastEntity findByMastName(String mastName);
}