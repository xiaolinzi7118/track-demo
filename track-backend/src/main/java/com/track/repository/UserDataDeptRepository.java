package com.track.repository;

import com.track.entity.UserDataDept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDataDeptRepository extends JpaRepository<UserDataDept, Long> {
    List<UserDataDept> findByUserId(Long userId);

    List<UserDataDept> findByUserIdIn(List<Long> userIds);

    void deleteByUserId(Long userId);
}
