package com.track.service;

import com.track.common.PermissionChecker;
import com.track.common.Result;
import com.track.entity.DictIdSequence;
import com.track.entity.DictParam;
import com.track.entity.DictParamItem;
import com.track.entity.User;
import com.track.repository.DictIdSequenceRepository;
import com.track.repository.DictParamItemRepository;
import com.track.repository.DictParamRepository;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DictParamService {

    @Autowired
    private DictParamRepository dictParamRepository;

    @Autowired
    private DictParamItemRepository dictParamItemRepository;

    @Autowired
    private DictIdSequenceRepository dictIdSequenceRepository;

    @Autowired
    private PermissionChecker permissionChecker;

    @Autowired
    private UserRepository userRepository;

    public Result<Page<DictParam>> list(String keyword, Integer pageNum, Integer pageSize) {
        int safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        String key = keyword == null ? null : keyword.trim();
        if (key != null && key.isEmpty()) {
            key = null;
        }

        final String finalKey = key;
        Pageable pageable = PageRequest.of(safePageNum - 1, safePageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<DictParam> params = dictParamRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), 0));
            if (finalKey != null) {
                predicates.add(cb.like(root.get("paramName"), "%" + finalKey + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);
        return Result.success(params);
    }

    public Result<DictParam> detail(Long id) {
        DictParam param = dictParamRepository.findById(id).orElse(null);
        if (param == null) {
            return Result.error("Parameter does not exist");
        }
        List<DictParamItem> items = dictParamItemRepository.findByParamIdAndStatusOrderByIdAsc(param.getParamId(), 0);
        param.setItems(items);
        return Result.success(param);
    }

    @Transactional
    public Result<DictParam> add(DictParam request) {
        String paramName = normalizeName(request.getParamName());
        if (paramName == null) {
            return Result.error("Parameter name is required");
        }
        if (dictParamRepository.existsByParamNameAndStatus(paramName, 0)) {
            return Result.error("Parameter name already exists");
        }

        LocalDateTime now = LocalDateTime.now();
        String username = getCurrentUsername();

        DictParam entity = new DictParam();
        entity.setParamId(generateParamId(now));
        entity.setParamName(paramName);
        entity.setIsSystem(0);
        entity.setStatus(0);
        entity.setCreateBy(username);
        entity.setUpdateBy(username);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity = dictParamRepository.save(entity);

        Result<Void> itemCheck = validateItemRequest(entity.getParamId(), request.getItems(), null);
        if (itemCheck.getCode() != 200) {
            throw new RuntimeException(itemCheck.getMessage());
        }
        saveItems(entity.getParamId(), request.getItems(), username, now);

        entity.setItems(dictParamItemRepository.findByParamIdAndStatusOrderByIdAsc(entity.getParamId(), 0));
        return Result.success(entity);
    }

    @Transactional
    public Result<DictParam> update(DictParam request) {
        if (request.getId() == null) {
            return Result.error("Parameter id is required");
        }
        DictParam existing = dictParamRepository.findById(request.getId()).orElse(null);
        if (existing == null || existing.getStatus() == null || existing.getStatus() != 0) {
            return Result.error("Parameter does not exist or is deleted");
        }

        String paramName = normalizeName(request.getParamName());
        if (paramName == null) {
            return Result.error("Parameter name is required");
        }
        if (dictParamRepository.existsByParamNameAndStatusAndIdNot(paramName, 0, existing.getId())) {
            return Result.error("Parameter name already exists");
        }

        Result<Void> itemCheck = validateItemRequest(existing.getParamId(), request.getItems(), request.getDeletedItemIds());
        if (itemCheck.getCode() != 200) {
            return Result.error(itemCheck.getMessage());
        }

        LocalDateTime now = LocalDateTime.now();
        String username = getCurrentUsername();

        if (existing.getIsSystem() != null && existing.getIsSystem() == 1
                && !paramName.equals(existing.getParamName())) {
            return Result.error("System parameter cannot be renamed");
        }

        existing.setParamName(paramName);
        existing.setUpdateBy(username);
        existing.setUpdateTime(now);
        dictParamRepository.save(existing);

        softDeleteItems(existing.getParamId(), request.getDeletedItemIds(), username, now);
        saveItems(existing.getParamId(), request.getItems(), username, now);

        existing.setItems(dictParamItemRepository.findByParamIdAndStatusOrderByIdAsc(existing.getParamId(), 0));
        return Result.success(existing);
    }

    @Transactional
    public Result<Void> delete(Long id) {
        DictParam existing = dictParamRepository.findById(id).orElse(null);
        if (existing == null || existing.getStatus() == null || existing.getStatus() != 0) {
            return Result.error("Parameter does not exist or is deleted");
        }

        LocalDateTime now = LocalDateTime.now();
        String username = getCurrentUsername();

        if (existing.getIsSystem() != null && existing.getIsSystem() == 1) {
            return Result.error("System parameter cannot be deleted");
        }

        existing.setStatus(1);
        existing.setUpdateBy(username);
        existing.setUpdateTime(now);
        dictParamRepository.save(existing);

        List<DictParamItem> activeItems = dictParamItemRepository.findByParamIdAndStatusOrderByIdAsc(existing.getParamId(), 0);
        for (DictParamItem item : activeItems) {
            item.setStatus(1);
            item.setUpdateBy(username);
            item.setUpdateTime(now);
        }
        if (!activeItems.isEmpty()) {
            dictParamItemRepository.saveAll(activeItems);
        }

        return Result.success();
    }

    public Result<List<DictParam>> idsList(List<String> paramIds) {
        if (paramIds == null || paramIds.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        List<String> distinctIds = paramIds.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        if (distinctIds.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        List<DictParam> params = dictParamRepository.findByParamIdIn(distinctIds);
        Map<String, DictParam> paramMap = params.stream()
                .collect(Collectors.toMap(DictParam::getParamId, Function.identity(), (a, b) -> a));

        List<DictParamItem> allItems = dictParamItemRepository.findByParamIdInOrderByIdAsc(distinctIds);
        Map<String, List<DictParamItem>> itemMap = allItems.stream()
                .collect(Collectors.groupingBy(DictParamItem::getParamId, LinkedHashMap::new, Collectors.toList()));

        List<DictParam> result = new ArrayList<>();
        for (String paramId : distinctIds) {
            DictParam param = paramMap.get(paramId);
            if (param == null) {
                continue;
            }
            param.setItems(itemMap.getOrDefault(paramId, new ArrayList<>()));
            result.add(param);
        }
        return Result.success(result);
    }

    public Result<List<DictParamItem>> deptOptions() {
        List<DictParamItem> depts = dictParamItemRepository.findByParamIdAndStatusOrderByIdAsc(DataPermissionService.DEPT_PARAM_ID, 0);
        return Result.success(depts);
    }

    private void saveItems(String paramId, List<DictParamItem> items, String username, LocalDateTime now) {
        if (items == null || items.isEmpty()) {
            return;
        }

        for (DictParamItem item : items) {
            String itemCode = trim(item.getItemCode());
            String itemName = trim(item.getItemName());
            if (item.getId() == null) {
                DictParamItem entity = new DictParamItem();
                entity.setParamId(paramId);
                entity.setItemCode(itemCode);
                entity.setItemName(itemName);
                entity.setStatus(0);
                entity.setCreateBy(username);
                entity.setUpdateBy(username);
                entity.setCreateTime(now);
                entity.setUpdateTime(now);
                dictParamItemRepository.save(entity);
                continue;
            }

            DictParamItem existing = dictParamItemRepository.findById(item.getId()).orElse(null);
            if (existing == null || !paramId.equals(existing.getParamId()) || existing.getStatus() == null || existing.getStatus() != 0) {
                throw new RuntimeException("Parameter item does not exist or is deleted: " + item.getId());
            }
            existing.setItemCode(itemCode);
            existing.setItemName(itemName);
            existing.setUpdateBy(username);
            existing.setUpdateTime(now);
            dictParamItemRepository.save(existing);
        }
    }

    private void softDeleteItems(String paramId, List<Long> deletedItemIds, String username, LocalDateTime now) {
        if (deletedItemIds == null || deletedItemIds.isEmpty()) {
            return;
        }
        List<Long> ids = deletedItemIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (ids.isEmpty()) {
            return;
        }

        List<DictParamItem> deleteItems = dictParamItemRepository.findByParamIdAndIdIn(paramId, ids);
        for (DictParamItem item : deleteItems) {
            if (item.getStatus() != null && item.getStatus() == 0) {
                item.setStatus(1);
                item.setUpdateBy(username);
                item.setUpdateTime(now);
            }
        }
        if (!deleteItems.isEmpty()) {
            dictParamItemRepository.saveAll(deleteItems);
        }
    }

    private Result<Void> validateItemRequest(String paramId, List<DictParamItem> items, List<Long> deletedItemIds) {
        if (items == null || items.isEmpty()) {
            return Result.success();
        }

        Set<Long> deleteSet = deletedItemIds == null ? new HashSet<>() :
                deletedItemIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());

        Set<String> codeSet = new HashSet<>();
        for (DictParamItem item : items) {
            if (item.getId() != null && deleteSet.contains(item.getId())) {
                continue;
            }
            String itemCode = trim(item.getItemCode());
            String itemName = trim(item.getItemName());
            if (itemCode == null) {
                return Result.error("Item code is required");
            }
            if (itemName == null) {
                return Result.error("Item name is required");
            }
            if (!codeSet.add(itemCode)) {
                return Result.error("Duplicate item code: " + itemCode);
            }
            if (item.getId() != null && paramId != null) {
                DictParamItem existing = dictParamItemRepository.findById(item.getId()).orElse(null);
                if (existing == null || !paramId.equals(existing.getParamId())) {
                    return Result.error("Parameter item does not exist: " + item.getId());
                }
            }
        }
        return Result.success();
    }

    private String generateParamId(LocalDateTime now) {
        String bizDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        DictIdSequence sequence = dictIdSequenceRepository.findForUpdate(bizDate);
        if (sequence == null) {
            sequence = new DictIdSequence();
            sequence.setBizDate(bizDate);
            sequence.setSeq(1);
        } else {
            sequence.setSeq(sequence.getSeq() + 1);
        }
        sequence.setUpdateTime(LocalDateTime.now());
        dictIdSequenceRepository.save(sequence);
        return "DICT" + bizDate + String.format("%08d", sequence.getSeq());
    }

    private String getCurrentUsername() {
        Long userId = permissionChecker.getCurrentUserId();
        if (userId == null) {
            return "system";
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return "system";
        }
        return user.getUsername();
    }

    private String normalizeName(String value) {
        return trim(value);
    }

    private String trim(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
