package com.track.service;

import com.track.common.Result;
import com.track.entity.User;
import com.track.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void init() {
        if (userRepository.findByUsername("admin") == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("123456");
            admin.setNickname("管理员");
            admin.setStatus(1);
            admin.setCreateTime(LocalDateTime.now());
            userRepository.save(admin);
        }
    }

    public Result<User> login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (!password.equals(user.getPassword())) {
            return Result.error("密码错误");
        }
        if (user.getStatus() != 1) {
            return Result.error("用户已禁用");
        }
        user.setPassword(null);
        return Result.success(user);
    }
}
