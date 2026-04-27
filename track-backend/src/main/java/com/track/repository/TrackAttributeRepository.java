package com.track.repository;

import com.track.entity.TrackAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TrackAttributeRepository extends JpaRepository<TrackAttribute, Long>, JpaSpecificationExecutor<TrackAttribute> {
    List<TrackAttribute> findByStatusOrderByCreateTimeDesc(Integer status);

    TrackAttribute findByAttributeIdAndStatus(String attributeId, Integer status);

    boolean existsByAttributeTypeAndAttributeNameAndStatus(String attributeType, String attributeName, Integer status);

    boolean existsByAttributeTypeAndAttributeNameAndStatusAndIdNot(String attributeType, String attributeName, Integer status, Long id);

    List<TrackAttribute> findByAttributeIdInAndStatus(Collection<String> attributeIds, Integer status);

    boolean existsByInterfaceIdAndStatus(Long interfaceId, Integer status);
}
