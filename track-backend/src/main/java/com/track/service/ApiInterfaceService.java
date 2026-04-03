package com.track.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.track.common.Result;
import com.track.entity.ApiInterface;
import com.track.entity.TrackConfig;
import com.track.repository.ApiInterfaceRepository;
import com.track.repository.TrackConfigRepository;
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
public class ApiInterfaceService {

    @Autowired
    private ApiInterfaceRepository apiInterfaceRepository;

    @Autowired
    private TrackConfigRepository trackConfigRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Result<Page<ApiInterface>> list(String keyword, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));

        Page<ApiInterface> page = apiInterfaceRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isEmpty()) {
                predicates.add(cb.or(
                    cb.like(root.get("name"), "%" + keyword + "%"),
                    cb.like(root.get("path"), "%" + keyword + "%")
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        // 计算引用状态
        Set<Long> referencedIds = getReferencedInterfaceIds();
        page.getContent().forEach(item -> {
            item.setReferenced(referencedIds.contains(item.getId()));
        });

        return Result.success(page);
    }

    public Result<List<ApiInterface>> all() {
        return Result.success(apiInterfaceRepository.findAll());
    }

    public Result<ApiInterface> detail(Long id) {
        ApiInterface apiInterface = apiInterfaceRepository.findById(id).orElse(null);
        if (apiInterface == null) {
            return Result.error("接口不存在");
        }
        return Result.success(apiInterface);
    }

    public Result<ApiInterface> add(ApiInterface apiInterface) {
        apiInterface.setId(null);
        apiInterface.setCreateTime(LocalDateTime.now());
        apiInterface.setUpdateTime(LocalDateTime.now());
        return Result.success(apiInterfaceRepository.save(apiInterface));
    }

    public Result<ApiInterface> update(ApiInterface apiInterface) {
        ApiInterface existing = apiInterfaceRepository.findById(apiInterface.getId()).orElse(null);
        if (existing == null) {
            return Result.error("接口不存在");
        }
        apiInterface.setUpdateTime(LocalDateTime.now());
        apiInterface.setCreateTime(existing.getCreateTime());
        return Result.success(apiInterfaceRepository.save(apiInterface));
    }

    public Result<Void> delete(Long id) {
        apiInterfaceRepository.deleteById(id);
        return Result.success();
    }

    /**
     * 获取被已启用的埋点配置引用的接口路径列表
     */
    public Result<List<String>> getReferencedInterfacePaths() {
        Set<Long> referencedIds = getReferencedInterfaceIds();
        List<String> paths = new ArrayList<>();
        if (!referencedIds.isEmpty()) {
            List<ApiInterface> interfaces = apiInterfaceRepository.findAllById(referencedIds);
            paths = interfaces.stream().map(ApiInterface::getPath).collect(Collectors.toList());
        }
        return Result.success(paths);
    }

    /**
     * 从所有已启用的 TrackConfig 的 params JSON 中提取被引用的接口 ID 集合
     */
    private Set<Long> getReferencedInterfaceIds() {
        Set<Long> interfaceIds = new HashSet<>();
        List<TrackConfig> enabledConfigs = trackConfigRepository.findAll().stream()
                .filter(c -> c.getStatus() != null && c.getStatus() == 1)
                .collect(Collectors.toList());

        for (TrackConfig config : enabledConfigs) {
            if (config.getParams() != null && !config.getParams().isEmpty()) {
                try {
                    List<Map<String, Object>> params = objectMapper.readValue(config.getParams(),
                            new TypeReference<List<Map<String, Object>>>() {});
                    for (Map<String, Object> param : params) {
                        if ("api_data".equals(param.get("sourceType")) && param.get("interfaceId") != null) {
                            interfaceIds.add(((Number) param.get("interfaceId")).longValue());
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return interfaceIds;
    }
}
