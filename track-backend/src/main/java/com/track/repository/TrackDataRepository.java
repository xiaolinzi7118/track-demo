package com.track.repository;

import com.track.entity.TrackData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackDataRepository extends JpaRepository<TrackData, Long>, JpaSpecificationExecutor<TrackData> {
}
