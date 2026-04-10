package com.track.repository;

import com.track.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByParentIdOrderBySortOrder(Long parentId);

    List<Menu> findByMenuTypeAndStatusOrderBySortOrder(Integer menuType, Integer status);

    List<Menu> findByStatusOrderBySortOrder(Integer status);

    Menu findByMenuCode(String menuCode);
}
