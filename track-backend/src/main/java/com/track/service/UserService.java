package com.track.service;

import com.track.common.Result;
import com.track.entity.User;
import com.track.repository.UserRepository;
import com.track.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

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
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return Result.error("用户名已存在");
        }
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        userRepository.save(user);

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
