package com.track.repository;

import com.track.entity.TrackRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface TrackRequirementRepository extends JpaRepository<TrackRequirement, Long>, JpaSpecificationExecutor<TrackRequirement> {
    TrackRequirement findByRequirementId(String requirementId);

    List<TrackRequirement> findByRequirementIdIn(Collection<String> requirementIds);

    long countByCreateTimeGreaterThanEqualAndCreateTimeLessThan(LocalDateTime start, LocalDateTime end);

    long countByStatusIn(Collection<String> statuses);

    @Query("select count(r) from TrackRequirement r " +
            "where r.status not in :excludedStatuses " +
            "and r.status <> :onlineStatus " +
            "and r.expectedOnlineDate <= :deadline")
    long countAlertRequirements(@Param("excludedStatuses") Collection<String> excludedStatuses,
                                @Param("onlineStatus") String onlineStatus,
                                @Param("deadline") LocalDate deadline);

    List<TrackRequirement> findByCreateTimeGreaterThanEqualAndCreateTimeLessThan(LocalDateTime start, LocalDateTime end);
}
