package com.track.repository;

import com.track.entity.DictParamItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DictParamItemRepository extends JpaRepository<DictParamItem, Long> {
    List<DictParamItem> findByParamIdAndStatusOrderByIdAsc(String paramId, Integer status);

    List<DictParamItem> findByParamIdOrderByIdAsc(String paramId);

    List<DictParamItem> findByParamIdInOrderByIdAsc(List<String> paramIds);

    List<DictParamItem> findByParamIdAndIdIn(String paramId, List<Long> ids);

    List<DictParamItem> findByIdInAndStatus(List<Long> ids, Integer status);

    List<DictParamItem> findByIdInAndParamIdAndStatus(List<Long> ids, String paramId, Integer status);

    List<DictParamItem> findByParamIdAndStatusAndExtraAttrOrderByIdAsc(String paramId, Integer status, String extraAttr);

    Optional<DictParamItem> findFirstByParamIdAndStatusOrderByIdAsc(String paramId, Integer status);

    Optional<DictParamItem> findFirstByParamIdAndItemCodeAndStatusOrderByIdAsc(String paramId, String itemCode, Integer status);
}
