package com.cheetahnet.ping.repository;


import com.cheetahnet.ping.model.MastEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MastRepository extends JpaRepository<MastEntity, Long> {

    MastEntity findByMastId(String MastId);
}
