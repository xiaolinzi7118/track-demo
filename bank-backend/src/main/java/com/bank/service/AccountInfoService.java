package com.bank.service;

import com.bank.entity.AccountInfo;
import com.bank.repository.AccountInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccountInfoService {

    @Autowired
    private AccountInfoRepository accountInfoRepository;

    @PostConstruct
    public void init() {
        if (accountInfoRepository.count() == 0) {
            AccountInfo account = new AccountInfo();
            account.setUserId(1L);
            account.setAccountNo("6225****8001");
            account.setBalance(128650.00);
            account.setAccountType("储蓄卡");
            account.setStatus(1);
            accountInfoRepository.save(account);
        }
    }

    public Map<String, Object> getSummary(Long userId) {
        List<AccountInfo> accounts = accountInfoRepository.findByUserId(userId);
        double totalBalance = accounts.stream().mapToDouble(a -> a.getBalance() != null ? a.getBalance() : 0).sum();
        Map<String, Object> result = new HashMap<>();
        result.put("totalBalance", totalBalance);
        result.put("accounts", accounts);
        return result;
    }
}
