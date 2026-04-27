package com.track.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.track.common.PermissionChecker;
import com.track.common.Result;
import com.track.entity.ApiInterface;
import com.track.entity.DictIdSequence;
import com.track.entity.TrackAttribute;
import com.track.entity.TrackConfig;
import com.track.entity.User;
import com.track.repository.ApiInterfaceRepository;
import com.track.repository.DictIdSequenceRepository;
import com.track.repository.TrackAttributeRepository;
import com.track.repository.TrackConfigRepository;
import com.track.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class TrackAttributeService {

    private static final String TYPE_USER = "user";
    private static final String TYPE_SYSTEM = "system";
    private static final String TYPE_CUSTOM = "custom";
    private static final String SOURCE_NODE_CONTENT = "node_content";
    private static final String SOURCE_API_DATA = "api_data";
    private static final Set<String> VALID_TYPES = new HashSet<String>(Arrays.asList(TYPE_USER, TYPE_SYSTEM, TYPE_CUSTOM));
    private static final Set<String> VALID_SOURCE_TYPES = new HashSet<String>(
            Arrays.asList(SOURCE_NODE_CONTENT, SOURCE_API_DATA, "global_object", "local_cache", "static_value")
    );
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private TrackAttributeRepository trackAttributeRepository;

    @Autowired
    private ApiInterfaceRepository apiInterfaceRepository;

    @Autowired
    private TrackConfigRepository trackConfigRepository;

    @Autowired
    private DictIdSequenceRepository dictIdSequenceRepository;

    @Autowired
    private PermissionChecker permissionChecker;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Result<Page<TrackAttribute>> list(String keyword, String attributeType, Integer pageNum, Integer pageSize) {
        final String finalKeyword = trim(keyword);
        final String finalAttributeType = trim(attributeType);
        Pageable pageable = PageRequest.of(safePageNum(pageNum) - 1, safePageSize(pageSize), Sort.by(Sort.Direction.DESC, "createTime"));

        Page<TrackAttribute> page = trackAttributeRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<Predicate>();
            predicates.add(cb.equal(root.get("status"), 0));
            if (finalKeyword != null) {
                predicates.add(cb.or(
                        cb.like(root.get("attributeName"), "%" + finalKeyword + "%"),
                        cb.like(root.get("attributeField"), "%" + finalKeyword + "%")
                ));
            }
            if (finalAttributeType != null) {
                predicates.add(cb.equal(root.get("attributeType"), finalAttributeType));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        return Result.success(page);
    }

    public Result<TrackAttribute> detail(Long id) {
        TrackAttribute entity = trackAttributeRepository.findById(id).orElse(null);
        if (entity == null || entity.getStatus() == null || entity.getStatus() != 0) {
            return Result.error("属性不存在");
        }
        return Result.success(entity);
    }

    public Result<List<TrackAttribute>> all() {
        return Result.success(trackAttributeRepository.findByStatusOrderByCreateTimeDesc(0));
    }

    @Transactional
    public Result<TrackAttribute> add(TrackAttribute request) {
        if (request == null) {
            return Result.error("请求体不能为空");
        }
        String validationError = validateRequest(request, null);
        if (validationError != null) {
            return Result.error(validationError);
        }

        LocalDateTime now = LocalDateTime.now();
        String username = currentUsername();

        TrackAttribute entity = new TrackAttribute();
        entity.setAttributeId(generateAttributeId(now));
        fillBaseFields(entity, request);
        entity.setStatus(0);
        entity.setCreateBy(username);
        entity.setUpdateBy(username);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);

        TrackAttribute saved = trackAttributeRepository.save(entity);
        return Result.success(saved);
    }

    @Transactional
    public Result<TrackAttribute> update(TrackAttribute request) {
        if (request == null || request.getId() == null) {
            return Result.error("属性ID不能为空");
        }
        TrackAttribute existing = trackAttributeRepository.findById(request.getId()).orElse(null);
        if (existing == null || existing.getStatus() == null || existing.getStatus() != 0) {
            return Result.error("属性不存在");
        }

        String validationError = validateRequest(request, existing.getId());
        if (validationError != null) {
            return Result.error(validationError);
        }

        fillBaseFields(existing, request);
        existing.setUpdateBy(currentUsername());
        existing.setUpdateTime(LocalDateTime.now());

        TrackAttribute saved = trackAttributeRepository.save(existing);
        return Result.success(saved);
    }

    @Transactional
    public Result<Void> delete(Long id) {
        TrackAttribute existing = trackAttributeRepository.findById(id).orElse(null);
        if (existing == null || existing.getStatus() == null || existing.getStatus() != 0) {
            return Result.error("属性不存在");
        }
        if (isAttributeReferenced(existing.getAttributeId())) {
            return Result.error("该属性已被事件引用，无法删除");
        }

        existing.setStatus(1);
        existing.setUpdateBy(currentUsername());
        existing.setUpdateTime(LocalDateTime.now());
        trackAttributeRepository.save(existing);
        return Result.success();
    }

    public boolean existsActiveByInterfaceId(Long interfaceId) {
        if (interfaceId == null) {
            return false;
        }
        return trackAttributeRepository.existsByInterfaceIdAndStatus(interfaceId, 0);
    }

    private void fillBaseFields(TrackAttribute target, TrackAttribute request) {
        String attributeType = trim(request.getAttributeType());
        target.setAttributeName(trim(request.getAttributeName()));
        target.setAttributeField(trim(request.getAttributeField()));
        target.setAttributeType(attributeType);

        if (!TYPE_CUSTOM.equals(attributeType)) {
            target.setSourceType(null);
            target.setSourceValue(null);
            target.setInterfaceId(null);
            target.setInterfacePath(null);
            target.setDefaultValue(null);
            return;
        }

        String sourceType = trim(request.getSourceType());
        String sourceValue = trimNullable(request.getSourceValue());
        Long interfaceId = request.getInterfaceId();
        String defaultValue = trimNullable(request.getDefaultValue());

        String interfacePath = null;
        if (SOURCE_NODE_CONTENT.equals(sourceType)) {
            sourceValue = null;
            interfaceId = null;
        } else if (SOURCE_API_DATA.equals(sourceType)) {
            ApiInterface api = apiInterfaceRepository.findById(interfaceId).orElse(null);
            interfacePath = api == null ? null : api.getPath();
        } else {
            interfaceId = null;
        }

        target.setSourceType(sourceType);
        target.setSourceValue(sourceValue);
        target.setInterfaceId(interfaceId);
        target.setInterfacePath(interfacePath);
        target.setDefaultValue(defaultValue);
    }

    private String validateRequest(TrackAttribute request, Long excludeId) {
        String attributeName = trim(request.getAttributeName());
        if (attributeName == null) {
            return "属性名称不能为空";
        }
        if (attributeName.length() > 100) {
            return "属性名称长度不能超过100";
        }

        String attributeField = trim(request.getAttributeField());
        if (attributeField == null) {
            return "属性字段不能为空";
        }
        if (attributeField.length() > 100) {
            return "属性字段长度不能超过100";
        }

        String attributeType = trim(request.getAttributeType());
        if (attributeType == null || !VALID_TYPES.contains(attributeType)) {
            return "属性类型不正确";
        }

        boolean nameExists = excludeId == null
                ? trackAttributeRepository.existsByAttributeTypeAndAttributeNameAndStatus(attributeType, attributeName, 0)
                : trackAttributeRepository.existsByAttributeTypeAndAttributeNameAndStatusAndIdNot(attributeType, attributeName, 0, excludeId);
        if (nameExists) {
            return "同类型下属性名称已存在";
        }

        if (!TYPE_CUSTOM.equals(attributeType)) {
            return null;
        }

        String sourceType = trim(request.getSourceType());
        if (sourceType == null || !VALID_SOURCE_TYPES.contains(sourceType)) {
            return "来源类型不正确";
        }

        if (!SOURCE_NODE_CONTENT.equals(sourceType) && trim(request.getSourceValue()) == null) {
            return "变量路径/值不能为空";
        }

        if (SOURCE_API_DATA.equals(sourceType)) {
            if (request.getInterfaceId() == null) {
                return "接口数据来源必须选择接口";
            }
            ApiInterface api = apiInterfaceRepository.findById(request.getInterfaceId()).orElse(null);
            if (api == null) {
                return "接口不存在";
            }
        }
        return null;
    }

    private boolean isAttributeReferenced(String attributeId) {
        if (attributeId == null) {
            return false;
        }
        List<TrackConfig> configs = trackConfigRepository.findAll();
        for (TrackConfig config : configs) {
            String params = trim(config.getParams());
            if (params == null) {
                continue;
            }
            try {
                List<Map<String, Object>> rows = objectMapper.readValue(params, new TypeReference<List<Map<String, Object>>>() {
                });
                for (Map<String, Object> row : rows) {
                    String current = trim(asString(row.get("attributeId")));
                    if (attributeId.equals(current)) {
                        return true;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    private String generateAttributeId(LocalDateTime now) {
        String bizDate = now.format(DATE_FORMATTER);
        String sequenceKey = "ATTR" + bizDate;
        DictIdSequence sequence = dictIdSequenceRepository.findForUpdate(sequenceKey);
        if (sequence == null) {
            sequence = new DictIdSequence();
            sequence.setBizDate(sequenceKey);
            sequence.setSeq(1);
        } else {
            sequence.setSeq(sequence.getSeq() + 1);
        }
        sequence.setUpdateTime(now);
        dictIdSequenceRepository.save(sequence);
        return "ATTR" + bizDate + String.format("%08d", sequence.getSeq());
    }

    private String currentUsername() {
        Long userId = permissionChecker.getCurrentUserId();
        if (userId == null) {
            return "system";
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || trim(user.getUsername()) == null) {
            return "system";
        }
        return user.getUsername();
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private int safePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int safePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? 10 : pageSize;
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String trimNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
