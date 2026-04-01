package com.bank.repository;

import com.bank.entity.AccountInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountInfoRepository extends JpaRepository<AccountInfo, Long> {
    List<AccountInfo> findByUserId(Long userId);
}
