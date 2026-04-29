package com.track.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.track.common.PermissionChecker;
import com.track.common.Result;
import com.track.entity.ApiInterface;
import com.track.entity.TrackConfig;
import com.track.entity.User;
import com.track.repository.ApiInterfaceRepository;
import com.track.repository.TrackAttributeRepository;
import com.track.repository.TrackConfigRepository;
import com.track.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.Predicate;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApiInterfaceService {

    @Autowired
    private ApiInterfaceRepository apiInterfaceRepository;

    @Autowired
    private TrackConfigRepository trackConfigRepository;

    @Autowired
    private TrackAttributeRepository trackAttributeRepository;

    @Autowired
    private PermissionChecker permissionChecker;

    @Autowired
    private DataPermissionService dataPermissionService;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Result<Page<ApiInterface>> list(String keyword, Integer pageNum, Integer pageSize) {
        DataPermissionService.DataScope scope = currentScope();
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));

        Page<ApiInterface> page = apiInterfaceRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isEmpty()) {
                predicates.add(cb.or(
                        cb.like(root.get("name"), "%" + keyword + "%"),
                        cb.like(root.get("path"), "%" + keyword + "%")
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

        Set<Long> referencedIds = getReferencedInterfaceIds();
        page.getContent().forEach(item -> item.setReferenced(referencedIds.contains(item.getId())));

        return Result.success(page);
    }

    public Result<List<ApiInterface>> all() {
        DataPermissionService.DataScope scope = currentScope();
        if (scope.isAllData()) {
            return Result.success(apiInterfaceRepository.findAll());
        }
        if (scope.getDeptIds().isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        return Result.success(apiInterfaceRepository.findAll((root, query, cb) -> root.get("deptId").in(scope.getDeptIds())));
    }

    public Result<ApiInterface> detail(Long id) {
        ApiInterface apiInterface = apiInterfaceRepository.findById(id).orElse(null);
        if (apiInterface == null) {
            return Result.error("Interface does not exist");
        }
        if (!canAccess(apiInterface.getDeptId(), currentScope())) {
            return Result.error("No permission to access this record");
        }
        return Result.success(apiInterface);
    }

    public Result<ApiInterface> add(ApiInterface apiInterface) {
        Long deptId = resolveWriteDeptId(apiInterface.getDeptId());
        if (deptId == null) {
            return Result.error("No writable department found");
        }
        apiInterface.setId(null);
        apiInterface.setDeptId(deptId);
        apiInterface.setCreateTime(LocalDateTime.now());
        apiInterface.setUpdateTime(LocalDateTime.now());
        return Result.success(apiInterfaceRepository.save(apiInterface));
    }

    public Result<ApiInterface> update(ApiInterface apiInterface) {
        ApiInterface existing = apiInterfaceRepository.findById(apiInterface.getId()).orElse(null);
        if (existing == null) {
            return Result.error("Interface does not exist");
        }
        if (!canAccess(existing.getDeptId(), currentScope())) {
            return Result.error("No permission to modify this record");
        }

        Long deptId = resolveWriteDeptId(apiInterface.getDeptId());
        if (deptId == null) {
            deptId = existing.getDeptId();
        }
        if (deptId == null) {
            return Result.error("No writable department found");
        }

        apiInterface.setDeptId(deptId);
        apiInterface.setUpdateTime(LocalDateTime.now());
        apiInterface.setCreateTime(existing.getCreateTime());
        return Result.success(apiInterfaceRepository.save(apiInterface));
    }

    public Result<Void> delete(Long id) {
        ApiInterface existing = apiInterfaceRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.error("Interface does not exist");
        }
        if (!canAccess(existing.getDeptId(), currentScope())) {
            return Result.error("No permission to delete this record");
        }
        if (trackAttributeRepository.existsByInterfaceIdAndStatus(id, 0)) {
            return Result.error("接口已被属性引用，无法删除");
        }
        if (getReferencedInterfaceIds().contains(id)) {
            return Result.error("接口已被事件引用，无法删除");
        }
        apiInterfaceRepository.deleteById(id);
        return Result.success();
    }

    @Transactional
    public Result<Map<String, Object>> importTxt(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("请选择导入文件");
        }

        String fileName = trim(file.getOriginalFilename());
        if (fileName == null || !fileName.toLowerCase(Locale.ROOT).endsWith(".txt")) {
            return Result.error("仅支持导入txt文件");
        }

        Long deptId = resolveWriteDeptId(null);
        if (deptId == null) {
            return Result.error("No writable department found");
        }

        int totalCount = 0;
        int successCount = 0;
        List<Map<String, Object>> failList = new ArrayList<>();
        Set<String> importPathSet = new HashSet<>();

        try {
            String text = new String(file.getBytes(), StandardCharsets.UTF_8);
            text = stripBom(text);

            JsonNode root = objectMapper.readTree(text);
            JsonNode apisNode = root == null ? null : root.get("apis");
            if (apisNode == null || !apisNode.isArray()) {
                return Result.error("导入内容缺少apis数组");
            }

            for (int i = 0; i < apisNode.size(); i++) {
                JsonNode item = apisNode.get(i);
                if (item == null || item.isNull()) {
                    continue;
                }
                totalCount++;
                int rowNum = i + 1;

                String name = trim(readTextNode(item, "apiName"));
                String path = trim(readTextNode(item, "operationType"));
                String description = trimNullable(readTextNode(item, "description"));

                if (name == null) {
                    failList.add(buildImportFail(rowNum, "apiName不能为空"));
                    continue;
                }
                if (path == null) {
                    failList.add(buildImportFail(rowNum, "operationType不能为空"));
                    continue;
                }
                if (importPathSet.contains(path)) {
                    failList.add(buildImportFail(rowNum, "operationType在导入文件中重复"));
                    continue;
                }
                if (apiInterfaceRepository.existsByPath(path)) {
                    failList.add(buildImportFail(rowNum, "接口路径已存在"));
                    continue;
                }

                ApiInterface apiInterface = new ApiInterface();
                apiInterface.setName(name);
                apiInterface.setPath(path);
                apiInterface.setDescription(description);
                apiInterface.setDeptId(deptId);
                apiInterface.setCreateTime(LocalDateTime.now());
                apiInterface.setUpdateTime(LocalDateTime.now());

                try {
                    apiInterfaceRepository.save(apiInterface);
                    importPathSet.add(path);
                    successCount++;
                } catch (DataIntegrityViolationException e) {
                    failList.add(buildImportFail(rowNum, "接口路径已存在"));
                }
            }
        } catch (Exception e) {
            return Result.error("解析导入文件失败: " + e.getMessage());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalCount", totalCount);
        result.put("successCount", successCount);
        result.put("failCount", failList.size());
        result.put("failList", failList);
        return Result.success(result);
    }

    public Result<List<String>> getReferencedInterfacePaths() {
        Set<Long> referencedIds = getReferencedInterfaceIds();
        if (referencedIds.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        List<ApiInterface> interfaces = apiInterfaceRepository.findAllById(referencedIds);
        if (permissionChecker.getCurrentUserId() == null) {
            List<String> allPaths = interfaces.stream()
                    .map(ApiInterface::getPath)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            return Result.success(allPaths);
        }
        DataPermissionService.DataScope scope = currentScope();
        List<String> paths = interfaces.stream()
                .filter(i -> canAccess(i.getDeptId(), scope))
                .map(ApiInterface::getPath)
                .collect(Collectors.toList());
        return Result.success(paths);
    }

    private Set<Long> getReferencedInterfaceIds() {
        Set<Long> interfaceIds = new HashSet<>();
        List<TrackConfig> enabledConfigs = trackConfigRepository.findAll().stream()
                .filter(c -> c.getStatus() != null && c.getStatus() == 1)
                .collect(Collectors.toList());

        for (TrackConfig config : enabledConfigs) {
            if (config.getParams() == null || config.getParams().isEmpty()) {
                continue;
            }
            try {
                List<Map<String, Object>> params = objectMapper.readValue(config.getParams(),
                        new TypeReference<List<Map<String, Object>>>() {
                        });
                for (Map<String, Object> param : params) {
                    if ("api_data".equals(param.get("sourceType")) && param.get("interfaceId") != null) {
                        Object interfaceIdObj = param.get("interfaceId");
                        if (interfaceIdObj instanceof Number) {
                            interfaceIds.add(((Number) interfaceIdObj).longValue());
                        } else {
                            try {
                                interfaceIds.add(Long.parseLong(String.valueOf(interfaceIdObj)));
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return interfaceIds;
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

    private String readTextNode(JsonNode node, String fieldName) {
        if (node == null || fieldName == null) {
            return null;
        }
        JsonNode valueNode = node.get(fieldName);
        if (valueNode == null || valueNode.isNull()) {
            return null;
        }
        return valueNode.asText();
    }

    private Map<String, Object> buildImportFail(Integer rowNum, String message) {
        Map<String, Object> fail = new LinkedHashMap<>();
        fail.put("rowNum", rowNum);
        fail.put("message", message);
        return fail;
    }

    private String stripBom(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        return content.charAt(0) == '\uFEFF' ? content.substring(1) : content;
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
