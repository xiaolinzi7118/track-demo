package com.track.service;

import com.track.common.Result;
import com.track.common.SnowflakeIdGenerator;
import com.track.entity.User;
import com.track.repository.UserDataDeptRepository;
import com.track.repository.UserRepository;
import com.track.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    public static final int BUILTIN_SUPER_ADMIN_YES = 1;


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserDataDeptRepository userDataDeptRepository;

    @Autowired
    private DataPermissionService dataPermissionService;

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Page<User> pageList(String keyword, Integer pageNum, Integer pageSize) {
        int safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        String key = keyword == null ? null : keyword.trim();
        if (key != null && key.isEmpty()) {
            key = null;
        }

        final String finalKey = key;
        Pageable pageable = PageRequest.of(safePageNum - 1, safePageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        return userRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.or(
                    cb.isNull(root.get("isBuiltinSuperAdmin")),
                    cb.notEqual(root.get("isBuiltinSuperAdmin"), BUILTIN_SUPER_ADMIN_YES)
            ));
            if (finalKey != null) {
                predicates.add(cb.or(
                        cb.like(root.get("username"), "%" + finalKey + "%"),
                        cb.like(root.get("nickname"), "%" + finalKey + "%")
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);
    }

    public Result<User> addUser(User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Result.error("Username is required");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return Result.error("Password is required");
        }
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return Result.error("Username already exists");
        }

        if (user.getIsBuiltinSuperAdmin() == null) {
            user.setIsBuiltinSuperAdmin(0);
        }

        Long primaryDeptId = dataPermissionService.resolvePrimaryDeptId(user.getPrimaryDeptId());
        if (primaryDeptId == null) {
            return Result.error("No valid primary department found");
        }
        user.setPrimaryDeptId(primaryDeptId);

        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        if (user.getId() == null) {
            user.setId(snowflakeIdGenerator.nextId());
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        userRepository.save(user);

        return Result.success(user);
    }

    public Result<User> updateUser(User user) {
        User existing = userRepository.findById(user.getId()).orElse(null);
        if (existing == null) {
            return Result.error("User does not exist");
        }

        existing.setNickname(user.getNickname());
        existing.setAvatar(user.getAvatar());
        existing.setStatus(user.getStatus());

        if (user.getPrimaryDeptId() != null) {
            Long primaryDeptId = dataPermissionService.resolvePrimaryDeptId(user.getPrimaryDeptId());
            if (primaryDeptId == null) {
                return Result.error("No valid primary department found");
            }
            existing.setPrimaryDeptId(primaryDeptId);
        } else if (existing.getPrimaryDeptId() == null) {
            Long defaultDeptId = dataPermissionService.getDefaultDeptId();
            if (defaultDeptId == null) {
                return Result.error("No valid primary department found");
            }
            existing.setPrimaryDeptId(defaultDeptId);
        }

        existing.setUpdateTime(LocalDateTime.now());
        userRepository.save(existing);
        existing.setPassword(null);
        return Result.success(existing);
    }

    public Result<Void> deleteUser(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return Result.error("User does not exist");
        }
        if (isBuiltInSuperAdmin(user)) {
            return Result.error("Built-in super admin cannot be deleted");
        }
        userRoleRepository.deleteByUserId(id);
        userDataDeptRepository.deleteByUserId(id);
        userRepository.deleteById(id);
        return Result.success();
    }

    public Result<Void> updatePassword(Long id, String password) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return Result.error("User does not exist");
        }
        if (isBuiltInSuperAdmin(user)) {
            return Result.error("Built-in super admin cannot be updated");
        }
        user.setPassword(password);
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);
        return Result.success();
    }

    public Result<Void> updateStatus(Long id, Integer status) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return Result.error("User does not exist");
        }
        if (isBuiltInSuperAdmin(user)) {
            return Result.error("Built-in super admin status cannot be changed");
        }
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);
        return Result.success();
    }

    public boolean isBuiltInSuperAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        User user = findById(userId);
        return isBuiltInSuperAdmin(user);
    }

    public boolean isBuiltInSuperAdmin(User user) {
        return user != null && Integer.valueOf(BUILTIN_SUPER_ADMIN_YES).equals(user.getIsBuiltinSuperAdmin());
    }
}
