package com.track.repository;

import com.track.entity.TrackRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackRequirementRepository extends JpaRepository<TrackRequirement, Long>, JpaSpecificationExecutor<TrackRequirement> {
    TrackRequirement findByRequirementId(String requirementId);
}

