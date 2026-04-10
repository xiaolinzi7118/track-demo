package com.track.repository;

import com.track.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    UserRole findByUserId(Long userId);

    List<UserRole> findByRoleId(Long roleId);

    void deleteByUserId(Long userId);
}
