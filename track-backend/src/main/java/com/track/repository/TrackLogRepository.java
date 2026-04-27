package com.track.repository;

import com.track.entity.TrackLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrackLogRepository extends JpaRepository<TrackLog, Long> {
    List<TrackLog> findByLogTypeAndRequirementIdOrderByOperateTimeDesc(String logType, String requirementId);

    List<TrackLog> findByLogTypeAndToStatusAndOperateTimeGreaterThanEqualAndOperateTimeLessThan(
            String logType,
            String toStatus,
            LocalDateTime start,
            LocalDateTime end
    );
}
