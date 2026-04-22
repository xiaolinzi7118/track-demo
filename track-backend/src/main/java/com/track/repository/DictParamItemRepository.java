package com.track.repository;

import com.track.entity.DictParamItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DictParamItemRepository extends JpaRepository<DictParamItem, Long> {
    List<DictParamItem> findByParamIdAndStatusOrderByIdAsc(String paramId, Integer status);

    List<DictParamItem> findByParamIdOrderByIdAsc(String paramId);

    List<DictParamItem> findByParamIdInOrderByIdAsc(List<String> paramIds);

    List<DictParamItem> findByParamIdAndIdIn(String paramId, List<Long> ids);
}
