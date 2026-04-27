package com.track.repository;

import com.track.entity.TrackFileAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackFileAssetRepository extends JpaRepository<TrackFileAsset, Long> {
    TrackFileAsset findByFileId(String fileId);

    boolean existsByFileId(String fileId);
}
