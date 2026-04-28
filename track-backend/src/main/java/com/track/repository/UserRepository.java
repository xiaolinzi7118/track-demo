package com.track.repository;

import com.track.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    User findByUsername(String username);

    @Query("select u.id from User u where u.primaryDeptId in :deptIds")
    List<Long> findIdsByPrimaryDeptIdIn(@Param("deptIds") Collection<Long> deptIds);
}
