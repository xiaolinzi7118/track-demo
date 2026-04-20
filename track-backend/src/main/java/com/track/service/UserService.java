package com.track.service;

import com.track.common.Result;
import com.track.entity.User;
import com.track.entity.UserRole;
import com.track.repository.UserRepository;
import com.track.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

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

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Result<User> addUser(User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return Result.error("用户名已存在");
        }
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        userRepository.save(user);

        if (user.getAvatar() != null && user.getAvatar().equals("roleId")) {
            // avatar field is temporarily used to pass roleId from frontend
        }

        user.setPassword(null);
        return Result.success(user);
    }

    public Result<User> updateUser(User user) {
        User existing = userRepository.findById(user.getId()).orElse(null);
        if (existing == null) {
            return Result.error("用户不存在");
        }
        existing.setNickname(user.getNickname());
        existing.setAvatar(user.getAvatar());
        existing.setStatus(user.getStatus());
        existing.setUpdateTime(LocalDateTime.now());
        userRepository.save(existing);
        existing.setPassword(null);
        return Result.success(existing);
    }

    public Result<Void> deleteUser(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if ("admin".equals(user.getUsername())) {
            return Result.error("不能删除管理员账号");
        }
        userRoleRepository.deleteByUserId(id);
        userRepository.deleteById(id);
        return Result.success();
    }

    public Result<Void> updatePassword(Long id, String password) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(password);
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);
        return Result.success();
    }

    public Result<Void> updateStatus(Long id, Integer status) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if ("admin".equals(user.getUsername()) && status != 1) {
            return Result.error("不能禁用管理员账号");
        }
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);
        return Result.success();
    }
}
