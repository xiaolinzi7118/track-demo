package com.track.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.track.common.PermissionChecker;
import com.track.common.Result;
import com.track.entity.ApiInterface;
import com.track.entity.TrackAttribute;
import com.track.entity.TrackConfig;
import com.track.entity.TrackRequirement;
import com.track.entity.User;
import com.track.repository.ApiInterfaceRepository;
import com.track.repository.TrackAttributeRepository;
import com.track.repository.TrackConfigRepository;
import com.track.repository.TrackRequirementRepository;
import com.track.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrackConfigService {

    private static final String EVENT_TYPE_PAGE_VIEW = "page_view";
    private static final String EVENT_TYPE_CLICK = "click";
    private static final String ATTRIBUTE_TYPE_CUSTOM = "custom";
    private static final String SOURCE_NODE_CONTENT = "node_content";
    private static final String SOURCE_API_DATA = "api_data";
    private static final Set<String> VALID_EVENT_TYPES = new HashSet<>(Arrays.asList(EVENT_TYPE_PAGE_VIEW, EVENT_TYPE_CLICK));
    private static final Set<String> VALID_SOURCE_TYPES = new HashSet<>(Arrays.asList(
            SOURCE_NODE_CONTENT, SOURCE_API_DATA, "global_object", "local_cache", "static_value"
    ));
    private static final Set<String> SELECTABLE_REQUIREMENT_STATUS = new HashSet<>(Arrays.asList("SCHEDULING", "DEVELOPING"));
    private static final Map<String, String> REQUIREMENT_STATUS_LABELS = new HashMap<String, String>();

    static {
        REQUIREMENT_STATUS_LABELS.put("PENDING_REVIEW", "待审核");
        REQUIREMENT_STATUS_LABELS.put("SCHEDULING", "排期中");
        REQUIREMENT_STATUS_LABELS.put("DEVELOPING", "开发中");
        REQUIREMENT_STATUS_LABELS.put("TESTING", "测试中");
        REQUIREMENT_STATUS_LABELS.put("ONLINE", "已上线");
        REQUIREMENT_STATUS_LABELS.put("OFFLINE", "已下线");
        REQUIREMENT_STATUS_LABELS.put("REJECTED", "审核不通过");
    }

    @Autowired
    private TrackConfigRepository trackConfigRepository;

    @Autowired
    private TrackRequirementRepository trackRequirementRepository;

    @Autowired
    private TrackAttributeRepository trackAttributeRepository;

    @Autowired
    private ApiInterfaceRepository apiInterfaceRepository;

    @Autowired
    private PermissionChecker permissionChecker;

    @Autowired
    private DataPermissionService dataPermissionService;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Result<Page<TrackConfig>> list(String eventType, String keyword, Integer pageNum, Integer pageSize) {
        DataPermissionService.DataScope scope = currentScope();
        Pageable pageable = PageRequest.of(
                safePageNumber(pageNum) - 1,
                safePageSize(pageSize),
                Sort.by(Sort.Direction.DESC, "createTime")
        );

        final String finalEventType = trim(eventType);
        final String finalKeyword = trim(keyword);

        Page<TrackConfig> page = trackConfigRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<Predicate>();

            if (finalEventType != null) {
                predicates.add(cb.equal(root.get("eventType"), finalEventType));
            }

            if (finalKeyword != null) {
                predicates.add(cb.or(
                        cb.like(root.get("eventName"), "%" + finalKeyword + "%"),
                        cb.like(root.get("eventCode"), "%" + finalKeyword + "%")
                ));
            }

            if (!scope.isAllData()) {
                if (scope.getDeptIds().isEmpty()) {
                    return cb.disjunction();
                }
                predicates.add(root.get("deptId").in(scope.getDeptIds()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        hydrateRequirementInfo(page.getContent());
        return Result.success(page);
    }

    public Result<List<TrackConfig>> all() {
        Long userId = permissionChecker.getCurrentUserId();
        List<TrackConfig> enabledConfigs;
        if (userId == null) {
            enabledConfigs = trackConfigRepository.findAll((root, query, cb) -> cb.equal(root.get("status"), 1));
        } else {
            DataPermissionService.DataScope scope = currentScope();
            if (scope.isAllData()) {
                enabledConfigs = trackConfigRepository.findAll((root, query, cb) -> cb.equal(root.get("status"), 1));
            } else if (scope.getDeptIds().isEmpty()) {
                enabledConfigs = new ArrayList<TrackConfig>();
            } else {
                enabledConfigs = trackConfigRepository.findAll((root, query, cb) -> cb.and(
                        cb.equal(root.get("status"), 1),
                        root.get("deptId").in(scope.getDeptIds())
                ));
            }
        }
        enabledConfigs.sort(new Comparator<TrackConfig>() {
            @Override
            public int compare(TrackConfig o1, TrackConfig o2) {
                if (o1.getCreateTime() == null && o2.getCreateTime() == null) {
                    return 0;
                }
                if (o1.getCreateTime() == null) {
                    return 1;
                }
                if (o2.getCreateTime() == null) {
                    return -1;
                }
                return o2.getCreateTime().compareTo(o1.getCreateTime());
            }
        });
        hydrateRequirementInfo(enabledConfigs);
        return Result.success(enabledConfigs);
    }

    public Result<TrackConfig> detail(Long id) {
        TrackConfig config = trackConfigRepository.findById(id).orElse(null);
        if (config == null) {
            return Result.error("事件不存在");
        }
        if (!canAccess(config.getDeptId(), currentScope())) {
            return Result.error("无权访问该记录");
        }
        hydrateRequirementInfo(Collections.singletonList(config));
        return Result.success(config);
    }

    public Result<TrackConfig> add(TrackConfig request) {
        if (request == null) {
            return Result.error("请求体不能为空");
        }

        String baseValidation = validateEventBaseFields(request);
        if (baseValidation != null) {
            return Result.error(baseValidation);
        }

        Long deptId = resolveWriteDeptId(request.getDeptId());
        if (deptId == null) {
            return Result.error("未找到可写入的数据归属部门");
        }

        String urlPattern = normalizeUrlPattern(request.getUrlPattern());
        if (request.getStatus() != null && request.getStatus() == 1
                && !isEventCodeUrlUnique(request.getEventCode(), urlPattern, null)) {
            return Result.error("事件编码与生效页面URL组合在启用数据中必须唯一");
        }

        String normalizedParams = normalizeAndValidateParams(request.getParams(), request.getEventType());
        if (normalizedParams == null) {
            return Result.error("关联属性参数格式不正确");
        }

        LocalDateTime now = LocalDateTime.now();
        TrackConfig entity = new TrackConfig();
        entity.setEventName(trim(request.getEventName()));
        entity.setEventCode(trim(request.getEventCode()));
        entity.setEventType(trim(request.getEventType()));
        entity.setDescription(trimNullable(request.getDescription()));
        entity.setRequirementId(trim(request.getRequirementId()));
        entity.setParams(normalizedParams);
        entity.setUrlPattern(urlPattern.isEmpty() ? null : urlPattern);
        entity.setDeptId(deptId);
        entity.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        entity.setCreateTime(now);
        entity.setUpdateTime(now);

        TrackConfig saved = trackConfigRepository.save(entity);
        hydrateRequirementInfo(Collections.singletonList(saved));
        return Result.success(saved);
    }

    public Result<TrackConfig> update(TrackConfig request) {
        if (request == null || request.getId() == null) {
            return Result.error("事件ID不能为空");
        }

        TrackConfig existing = trackConfigRepository.findById(request.getId()).orElse(null);
        if (existing == null) {
            return Result.error("事件不存在");
        }
        if (!canAccess(existing.getDeptId(), currentScope())) {
            return Result.error("无权修改该记录");
        }

        String baseValidation = validateEventBaseFields(request);
        if (baseValidation != null) {
            return Result.error(baseValidation);
        }

        Long deptId = resolveWriteDeptId(request.getDeptId());
        if (deptId == null) {
            deptId = existing.getDeptId();
        }
        if (deptId == null) {
            return Result.error("未找到可写入的数据归属部门");
        }

        String urlPattern = normalizeUrlPattern(request.getUrlPattern());
        if (request.getStatus() != null && request.getStatus() == 1
                && !isEventCodeUrlUnique(request.getEventCode(), urlPattern, request.getId())) {
            return Result.error("事件编码与生效页面URL组合在启用数据中必须唯一");
        }

        String normalizedParams = normalizeAndValidateParams(request.getParams(), request.getEventType());
        if (normalizedParams == null) {
            return Result.error("关联属性参数格式不正确");
        }

        existing.setEventName(trim(request.getEventName()));
        existing.setEventCode(trim(request.getEventCode()));
        existing.setEventType(trim(request.getEventType()));
        existing.setDescription(trimNullable(request.getDescription()));
        existing.setRequirementId(trim(request.getRequirementId()));
        existing.setParams(normalizedParams);
        existing.setUrlPattern(urlPattern.isEmpty() ? null : urlPattern);
        existing.setDeptId(deptId);
        existing.setStatus(request.getStatus());
        existing.setUpdateTime(LocalDateTime.now());

        TrackConfig saved = trackConfigRepository.save(existing);
        hydrateRequirementInfo(Collections.singletonList(saved));
        return Result.success(saved);
    }

    public Result<Void> delete(Long id) {
        TrackConfig existing = trackConfigRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.error("事件不存在");
        }
        if (!canAccess(existing.getDeptId(), currentScope())) {
            return Result.error("无权删除该记录");
        }
        trackConfigRepository.deleteById(id);
        return Result.success();
    }

    public Result<Map<String, Object>> statistics() {
        DataPermissionService.DataScope scope = currentScope();
        List<TrackConfig> all;
        if (scope.isAllData()) {
            all = trackConfigRepository.findAll();
        } else if (scope.getDeptIds().isEmpty()) {
            all = new ArrayList<TrackConfig>();
        } else {
            all = trackConfigRepository.findAll((root, query, cb) -> root.get("deptId").in(scope.getDeptIds()));
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("total", (long) all.size());
        long pageViewCount = all.stream().filter(c -> EVENT_TYPE_PAGE_VIEW.equals(c.getEventType())).count();
        long clickCount = all.stream().filter(c -> EVENT_TYPE_CLICK.equals(c.getEventType())).count();
        result.put("pageViewCount", pageViewCount);
        result.put("clickCount", clickCount);
        return Result.success(result);
    }

    public Result<List<Map<String, Object>>> requirementOptions(String keyword) {
        final String finalKeyword = trim(keyword);
        List<TrackRequirement> requirements = trackRequirementRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<Predicate>();
            predicates.add(root.get("status").in(SELECTABLE_REQUIREMENT_STATUS));
            if (finalKeyword != null) {
                predicates.add(cb.like(root.get("title"), "%" + finalKeyword + "%"));
            }
            query.orderBy(cb.desc(root.get("updateTime")));
            return cb.and(predicates.toArray(new Predicate[0]));
        });

        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        for (TrackRequirement item : requirements) {
            Map<String, Object> option = new LinkedHashMap<String, Object>();
            option.put("requirementId", item.getRequirementId());
            option.put("title", item.getTitle());
            option.put("status", item.getStatus());
            option.put("statusLabel", REQUIREMENT_STATUS_LABELS.getOrDefault(item.getStatus(), item.getStatus()));
            data.add(option);
        }
        return Result.success(data);
    }

    private void hydrateRequirementInfo(List<TrackConfig> configs) {
        if (configs == null || configs.isEmpty()) {
            return;
        }

        Set<String> requirementIds = configs.stream()
                .map(TrackConfig::getRequirementId)
                .filter(Objects::nonNull)
                .filter(s -> !s.trim().isEmpty())
                .collect(Collectors.toSet());
        if (requirementIds.isEmpty()) {
            return;
        }

        List<TrackRequirement> requirements = trackRequirementRepository.findByRequirementIdIn(requirementIds);
        Map<String, TrackRequirement> requirementMap = requirements.stream()
                .collect(Collectors.toMap(TrackRequirement::getRequirementId, r -> r, (a, b) -> a));

        for (TrackConfig config : configs) {
            TrackRequirement requirement = requirementMap.get(config.getRequirementId());
            if (requirement == null) {
                continue;
            }
            config.setRequirementTitle(requirement.getTitle());
            config.setRequirementStatus(requirement.getStatus());
            config.setRequirementStatusLabel(REQUIREMENT_STATUS_LABELS.getOrDefault(requirement.getStatus(), requirement.getStatus()));
        }
    }

    private String validateEventBaseFields(TrackConfig request) {
        String eventName = trim(request.getEventName());
        if (eventName == null) {
            return "事件名称不能为空";
        }
        String eventCode = trim(request.getEventCode());
        if (eventCode == null) {
            return "事件编码不能为空";
        }
        String eventType = trim(request.getEventType());
        if (eventType == null || !VALID_EVENT_TYPES.contains(eventType)) {
            return "事件类型不正确";
        }
        if (request.getStatus() == null || (request.getStatus() != 0 && request.getStatus() != 1)) {
            return "状态值不正确";
        }

        String requirementId = trim(request.getRequirementId());
        if (requirementId == null) {
            return "关联需求不能为空";
        }
        TrackRequirement requirement = trackRequirementRepository.findByRequirementId(requirementId);
        if (requirement == null) {
            return "关联需求不存在";
        }
        if (!SELECTABLE_REQUIREMENT_STATUS.contains(requirement.getStatus())) {
            return "仅可关联排期中或开发中的需求";
        }
        return null;
    }

    private String normalizeAndValidateParams(String paramsText, String eventType) {
        String text = trimNullable(paramsText);
        if (text == null) {
            return "[]";
        }

        try {
            List<Map<String, Object>> rawList = objectMapper.readValue(text, new TypeReference<List<Map<String, Object>>>() {
            });
            if (rawList == null || rawList.isEmpty()) {
                return "[]";
            }

            Set<String> seenAttributeIds = new HashSet<String>();
            List<String> allAttributeIds = new ArrayList<String>();
            for (Map<String, Object> row : rawList) {
                String attributeId = trim(asString(row.get("attributeId")));
                if (attributeId == null) {
                    throw new RuntimeException("关联属性缺少attributeId");
                }
                if (!seenAttributeIds.add(attributeId)) {
                    throw new RuntimeException("关联属性不可重复: " + attributeId);
                }
                allAttributeIds.add(attributeId);
            }

            List<TrackAttribute> attributes = trackAttributeRepository.findByAttributeIdInAndStatus(allAttributeIds, 0);
            Map<String, TrackAttribute> attributeMap = attributes.stream()
                    .collect(Collectors.toMap(TrackAttribute::getAttributeId, a -> a, (a, b) -> a));

            for (String attributeId : allAttributeIds) {
                if (!attributeMap.containsKey(attributeId)) {
                    throw new RuntimeException("属性不存在或已删除: " + attributeId);
                }
            }

            List<Map<String, Object>> normalized = new ArrayList<Map<String, Object>>();
            for (Map<String, Object> row : rawList) {
                String attributeId = trim(asString(row.get("attributeId")));
                TrackAttribute attribute = attributeMap.get(attributeId);

                Map<String, Object> normalizedRow = new LinkedHashMap<String, Object>();
                normalizedRow.put("attributeId", attribute.getAttributeId());
                normalizedRow.put("attributeType", attribute.getAttributeType());
                normalizedRow.put("attributeName", attribute.getAttributeName());
                normalizedRow.put("attributeField", attribute.getAttributeField());
                normalizedRow.put("description", trimNullable(asString(row.get("description"))));

                if (ATTRIBUTE_TYPE_CUSTOM.equals(attribute.getAttributeType())) {
                    String sourceType = trim(asString(row.get("sourceType")));
                    if (sourceType == null) {
                        sourceType = trim(attribute.getSourceType());
                    }
                    if (sourceType == null || !VALID_SOURCE_TYPES.contains(sourceType)) {
                        throw new RuntimeException("个性化属性来源类型不正确: " + attribute.getAttributeName());
                    }

                    if (EVENT_TYPE_PAGE_VIEW.equals(eventType)
                            && (SOURCE_NODE_CONTENT.equals(sourceType) || SOURCE_API_DATA.equals(sourceType))) {
                        throw new RuntimeException("页面曝光事件不支持节点内容或接口数据来源");
                    }

                    String sourceValue = trimNullable(asString(row.get("sourceValue")));
                    if (sourceValue == null) {
                        sourceValue = trimNullable(attribute.getSourceValue());
                    }
                    Long interfaceId = asLong(row.get("interfaceId"));
                    if (interfaceId == null) {
                        interfaceId = attribute.getInterfaceId();
                    }
                    String defaultValue = trimNullable(asString(row.get("defaultValue")));
                    if (defaultValue == null) {
                        defaultValue = trimNullable(attribute.getDefaultValue());
                    }

                    String interfacePath = null;
                    if (SOURCE_NODE_CONTENT.equals(sourceType)) {
                        sourceValue = null;
                        interfaceId = null;
                    } else if (SOURCE_API_DATA.equals(sourceType)) {
                        if (interfaceId == null) {
                            throw new RuntimeException("接口数据来源必须选择接口");
                        }
                        if (sourceValue == null) {
                            throw new RuntimeException("接口数据来源必须填写变量路径");
                        }
                        ApiInterface apiInterface = apiInterfaceRepository.findById(interfaceId).orElse(null);
                        if (apiInterface == null) {
                            throw new RuntimeException("接口不存在: " + interfaceId);
                        }
                        interfacePath = apiInterface.getPath();
                    } else {
                        interfaceId = null;
                        if (sourceValue == null) {
                            throw new RuntimeException("来源类型对应的变量路径/值不能为空");
                        }
                    }

                    normalizedRow.put("sourceType", sourceType);
                    normalizedRow.put("sourceValue", sourceValue);
                    normalizedRow.put("interfaceId", interfaceId);
                    normalizedRow.put("interfacePath", interfacePath);
                    normalizedRow.put("defaultValue", defaultValue);
                } else {
                    normalizedRow.put("sourceType", null);
                    normalizedRow.put("sourceValue", null);
                    normalizedRow.put("interfaceId", null);
                    normalizedRow.put("interfacePath", null);
                    normalizedRow.put("defaultValue", null);
                }
                normalized.add(normalizedRow);
            }

            return objectMapper.writeValueAsString(normalized);
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isEventCodeUrlUnique(String eventCode, String normalizedUrlPattern, Long excludeId) {
        final String finalEventCode = trim(eventCode);
        final String finalUrlPattern = normalizedUrlPattern == null ? "" : normalizedUrlPattern;
        final Long finalExcludeId = excludeId;

        List<TrackConfig> conflict = trackConfigRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<Predicate>();
            predicates.add(cb.equal(root.get("status"), 1));
            predicates.add(cb.equal(root.get("eventCode"), finalEventCode));
            predicates.add(cb.equal(cb.coalesce(root.get("urlPattern"), ""), finalUrlPattern));
            if (finalExcludeId != null) {
                predicates.add(cb.notEqual(root.get("id"), finalExcludeId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        });
        return conflict.isEmpty();
    }

    private int safePageNumber(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int safePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? 10 : pageSize;
    }

    private DataPermissionService.DataScope currentScope() {
        Long userId = permissionChecker.getCurrentUserId();
        return dataPermissionService.getDataScope(userId);
    }

    private boolean canAccess(Long deptId, DataPermissionService.DataScope scope) {
        if (scope.isAllData()) {
            return true;
        }
        return deptId != null && scope.getDeptIds().contains(deptId);
    }

    private Long resolveWriteDeptId(Long requestedDeptId) {
        Long currentUserId = permissionChecker.getCurrentUserId();
        DataPermissionService.DataScope scope = currentScope();
        if (scope.isAllData()) {
            return dataPermissionService.resolvePrimaryDeptId(requestedDeptId);
        }

        if (requestedDeptId != null && scope.getDeptIds().contains(requestedDeptId)) {
            return requestedDeptId;
        }

        User user = currentUserId == null ? null : userRepository.findById(currentUserId).orElse(null);
        if (user != null && user.getPrimaryDeptId() != null && scope.getDeptIds().contains(user.getPrimaryDeptId())) {
            return user.getPrimaryDeptId();
        }

        return scope.getDeptIds().stream().findFirst().orElse(null);
    }

    private String normalizeUrlPattern(String urlPattern) {
        String value = trim(urlPattern);
        return value == null ? "" : value;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            String text = String.valueOf(value).trim();
            if (text.isEmpty()) {
                return null;
            }
            return Long.parseLong(text);
        } catch (Exception e) {
            return null;
        }
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
        return value.trim().isEmpty() ? null : value.trim();
    }
}
