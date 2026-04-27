package com.track.repository;

import com.track.entity.TrackConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackConfigRepository extends JpaRepository<TrackConfig, Long>, JpaSpecificationExecutor<TrackConfig> {
    List<TrackConfig> findByStatus(Integer status);
}
