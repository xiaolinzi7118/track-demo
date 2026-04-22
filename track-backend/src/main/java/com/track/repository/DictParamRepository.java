package com.track.repository;

import com.track.entity.DictParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DictParamRepository extends JpaRepository<DictParam, Long>, JpaSpecificationExecutor<DictParam> {
    boolean existsByParamNameAndStatus(String paramName, Integer status);

    boolean existsByParamNameAndStatusAndIdNot(String paramName, Integer status, Long id);

    List<DictParam> findByParamIdIn(List<String> paramIds);

    Optional<DictParam> findByParamId(String paramId);
}
