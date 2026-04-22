package com.track.repository;

import com.track.entity.DictIdSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;

@Repository
public interface DictIdSequenceRepository extends JpaRepository<DictIdSequence, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from DictIdSequence s where s.bizDate = :bizDate")
    DictIdSequence findForUpdate(@Param("bizDate") String bizDate);
}
