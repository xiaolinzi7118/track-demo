package com.track.repository;

import com.track.entity.ApiInterface;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiInterfaceRepository extends JpaRepository<ApiInterface, Long>, JpaSpecificationExecutor<ApiInterface> {
    boolean existsByPath(String path);
}
