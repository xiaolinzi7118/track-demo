package com.track.service;

import com.track.common.PermissionChecker;
import com.track.common.Result;
import com.track.dto.RequirementCreateRequest;
import com.track.dto.RequirementResubmitRequest;
import com.track.dto.RequirementStatusChangeRequest;
import com.track.entity.DictParamItem;
import com.track.entity.TrackLog;
import com.track.entity.TrackRequirement;
import com.track.entity.TrackRequirementAction;
import com.track.entity.User;
import com.track.repository.DictParamItemRepository;
import com.track.repository.TrackLogRepository;
import com.track.repository.TrackRequirementRepository;
import com.track.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RequirementService {
    private static final String STATUS_PENDING_REVIEW = "PENDING_REVIEW";
    private static final String STATUS_SCHEDULING = "SCHEDULING";
    private static final String STATUS_DEVELOPING = "DEVELOPING";
    private static final String STATUS_TESTING = "TESTING";
    private static final String STATUS_ONLINE = "ONLINE";
    private static final String STATUS_OFFLINE = "OFFLINE";
    private static final String STATUS_REJECTED = "REJECTED";

    private static final String LOG_TYPE_REQUIREMENT_MANAGE = "requirement_manage";
    private static final String LOG_ACTION_CREATE = "CREATE";
    private static final String LOG_ACTION_STATUS_CHANGE = "STATUS_CHANGE";
    private static final String LOG_ACTION_EDIT_RESUBMIT = "EDIT_RESUBMIT";

    private static final String ACTION_TYPE_CHANGE_STATUS = "CHANGE_STATUS";
    private static final String ACTION_TYPE_EDIT = "EDIT";

    private static final String BUSINESS_LINE_PARAM_ID = "DICT2026042200000001";
    private static final String DEV_TEAM_PARAM_ID = "DICT2026042200000002";

    private static final Set<String> VALID_PRIORITIES = new HashSet<>(Arrays.asList("P0", "P1", "P2"));
    private static final Set<String> VALID_SORT_FIELDS = new HashSet<>(Arrays.asList("priority", "createTime", "updateTime"));

    private static final Map<String, String> STATUS_NAME_MAP = new LinkedHashMap<>();

    static {
        STATUS_NAME_MAP.put(STATUS_PENDING_REVIEW, "待审核");
        STATUS_NAME_MAP.put(STATUS_SCHEDULING, "排期中");
        STATUS_NAME_MAP.put(STATUS_DEVELOPING, "开发中");
        STATUS_NAME_MAP.put(STATUS_TESTING, "测试中");
        STATUS_NAME_MAP.put(STATUS_ONLINE, "已上线");
        STATUS_NAME_MAP.put(STATUS_OFFLINE, "已下线");
        STATUS_NAME_MAP.put(STATUS_REJECTED, "审核不通过");
    }

    @Autowired
    private TrackRequirementRepository trackRequirementRepository;

    @Autowired
    private TrackLogRepository trackLogRepository;

    @Autowired
    private DictParamItemRepository dictParamItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionChecker permissionChecker;

    public Result<Page<TrackRequirement>> list(String title,
                                               String statusListText,
                                               String priority,
                                               String proposerName,
                                               String businessLineCode,
                                               String department,
                                               String sortField,
                                               String sortOrder,
                                               Integer pageNum,
                                               Integer pageSize) {
        int safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;

        String normalizedPriority = normalize(priority);
        if (normalizedPriority != null) {
            normalizedPriority = normalizedPriority.toUpperCase();
            if (!VALID_PRIORITIES.contains(normalizedPriority)) {
                return Result.error("Priority is invalid");
            }
        }

        final String finalPriority = normalizedPriority;
        final String finalTitle = normalize(title);
        final String finalProposerName = normalize(proposerName);
        final String finalBusinessLineCode = normalize(businessLineCode);
        final String finalDepartment = normalize(department);
        final String finalSortField = normalizeSortField(sortField);
        final boolean asc = "asc".equalsIgnoreCase(normalize(sortOrder));
        final List<String> statusList = parseStatusList(statusListText);
        Pageable pageable = PageRequest.of(safePageNum - 1, safePageSize);

        Page<TrackRequirement> page = trackRequirementRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (finalTitle != null) {
                predicates.add(cb.like(root.get("title"), "%" + finalTitle + "%"));
            }
            if (!statusList.isEmpty()) {
                predicates.add(root.get("status").in(statusList));
            }
            if (finalPriority != null) {
                predicates.add(cb.equal(root.get("priority"), finalPriority));
            }
            if (finalProposerName != null) {
                predicates.add(cb.like(root.get("proposerName"), "%" + finalProposerName + "%"));
            }
            if (finalBusinessLineCode != null) {
                predicates.add(cb.equal(root.get("businessLineCode"), finalBusinessLineCode));
            }
            if (finalDepartment != null) {
                predicates.add(cb.like(root.get("department"), "%" + finalDepartment + "%"));
            }

            if (!Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
                applySort(root, query, cb, finalSortField, asc);
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        Long currentUserId = permissionChecker.getCurrentUserId();
        boolean admin = isAdmin();
        boolean developer = !admin && isDeveloper();
        for (TrackRequirement requirement : page.getContent()) {
            requirement.setAvailableActions(buildAvailableActions(requirement, currentUserId, admin, developer));
        }
        return Result.success(page);
    }

    public Result<TrackRequirement> detail(String requirementId) {
        String rid = normalize(requirementId);
        if (rid == null) {
            return Result.error("Requirement id is required");
        }

        TrackRequirement requirement = trackRequirementRepository.findByRequirementId(rid);
        if (requirement == null) {
            return Result.error("Requirement does not exist");
        }

        Long currentUserId = permissionChecker.getCurrentUserId();
        boolean admin = isAdmin();
        boolean developer = !admin && isDeveloper();
        requirement.setAvailableActions(buildAvailableActions(requirement, currentUserId, admin, developer));
        requirement.setLogs(trackLogRepository.findByLogTypeAndRequirementIdOrderByOperateTimeDesc(
                LOG_TYPE_REQUIREMENT_MANAGE, requirement.getRequirementId()));
        return Result.success(requirement);
    }

    public Result<TrackRequirement> add(RequirementCreateRequest request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error(401, "User not found");
        }

        ValidationContext validation = validateAndResolveRequest(
                request.getTitle(),
                request.getBusinessLineCode(),
                request.getPriority(),
                request.getExpectedOnlineDate() == null ? null : request.getExpectedOnlineDate().toString(),
                request.getDevTeamCode(),
                request.getDescription()
        );
        if (validation.getErrorMessage() != null) {
            return Result.error(validation.getErrorMessage());
        }

        LocalDateTime now = LocalDateTime.now();
        TrackRequirement requirement = new TrackRequirement();
        requirement.setRequirementId(UUID.randomUUID().toString());
        requirement.setTitle(validation.getTitle());
        requirement.setStatus(STATUS_PENDING_REVIEW);
        requirement.setPriority(validation.getPriority());
        requirement.setBusinessLineCode(validation.getBusinessLineCode());
        requirement.setBusinessLineName(validation.getBusinessLineName());
        requirement.setDevTeamCode(validation.getDevTeamCode());
        requirement.setDevTeamName(validation.getDevTeamName());
        requirement.setExpectedOnlineDate(validation.getExpectedOnlineDate());
        requirement.setDescription(validation.getDescription());
        requirement.setProposerId(currentUser.getId());
        requirement.setProposerName(resolveUserDisplayName(currentUser));
        requirement.setDepartment(resolveDepartmentName(currentUser));
        requirement.setCreateTime(now);
        requirement.setUpdateTime(now);

        trackRequirementRepository.save(requirement);
        writeLog(requirement.getRequirementId(), LOG_ACTION_CREATE, null, STATUS_PENDING_REVIEW, "创建需求", currentUser, now);
        return Result.success(requirement);
    }

    public Result<TrackRequirement> changeStatus(RequirementStatusChangeRequest request) {
        String requirementId = normalize(request.getRequirementId());
        String targetStatus = normalize(request.getTargetStatus());
        String opinion = normalize(request.getOpinion());

        if (requirementId == null || targetStatus == null) {
            return Result.error("Requirement id and targetStatus are required");
        }
        if (!STATUS_NAME_MAP.containsKey(targetStatus)) {
            return Result.error("Target status is invalid");
        }

        TrackRequirement requirement = trackRequirementRepository.findByRequirementId(requirementId);
        if (requirement == null) {
            return Result.error("Requirement does not exist");
        }

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error(401, "User not found");
        }

        Long currentUserId = currentUser.getId();
        boolean admin = isAdmin();
        boolean developer = !admin && isDeveloper();

        List<TrackRequirementAction> availableActions = buildAvailableActions(requirement, currentUserId, admin, developer);
        boolean allowed = availableActions.stream()
                .filter(action -> ACTION_TYPE_CHANGE_STATUS.equals(action.getActionType()))
                .anyMatch(action -> targetStatus.equals(action.getTargetStatus()));
        if (!allowed) {
            return Result.error("No permission for this status transition");
        }

        if (STATUS_REJECTED.equals(targetStatus) && opinion == null) {
            return Result.error("Reject reason is required");
        }

        String fromStatus = requirement.getStatus();
        LocalDateTime now = LocalDateTime.now();
        requirement.setStatus(targetStatus);
        requirement.setUpdateTime(now);
        trackRequirementRepository.save(requirement);

        writeLog(requirement.getRequirementId(), LOG_ACTION_STATUS_CHANGE, fromStatus, targetStatus, opinion, currentUser, now);

        requirement.setAvailableActions(buildAvailableActions(requirement, currentUserId, admin, developer));
        return Result.success(requirement);
    }

    public Result<TrackRequirement> resubmit(RequirementResubmitRequest request) {
        String requirementId = normalize(request.getRequirementId());
        if (requirementId == null) {
            return Result.error("Requirement id is required");
        }

        TrackRequirement requirement = trackRequirementRepository.findByRequirementId(requirementId);
        if (requirement == null) {
            return Result.error("Requirement does not exist");
        }
        if (!STATUS_REJECTED.equals(requirement.getStatus())) {
            return Result.error("Only rejected requirement can be resubmitted");
        }

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error(401, "User not found");
        }
        boolean admin = isAdmin();
        if (!admin && !Objects.equals(requirement.getProposerId(), currentUser.getId())) {
            return Result.error("Only admin or proposer can resubmit");
        }

        ValidationContext validation = validateAndResolveRequest(
                request.getTitle(),
                request.getBusinessLineCode(),
                request.getPriority(),
                request.getExpectedOnlineDate() == null ? null : request.getExpectedOnlineDate().toString(),
                request.getDevTeamCode(),
                request.getDescription()
        );
        if (validation.getErrorMessage() != null) {
            return Result.error(validation.getErrorMessage());
        }

        boolean changed = isResubmitChanged(requirement, validation);
        if (!changed) {
            return Result.error("At least one field must be changed");
        }

        LocalDateTime now = LocalDateTime.now();
        requirement.setTitle(validation.getTitle());
        requirement.setPriority(validation.getPriority());
        requirement.setBusinessLineCode(validation.getBusinessLineCode());
        requirement.setBusinessLineName(validation.getBusinessLineName());
        requirement.setDevTeamCode(validation.getDevTeamCode());
        requirement.setDevTeamName(validation.getDevTeamName());
        requirement.setExpectedOnlineDate(validation.getExpectedOnlineDate());
        requirement.setDescription(validation.getDescription());
        requirement.setStatus(STATUS_PENDING_REVIEW);
        requirement.setUpdateTime(now);
        trackRequirementRepository.save(requirement);

        writeLog(requirement.getRequirementId(), LOG_ACTION_EDIT_RESUBMIT, STATUS_REJECTED, STATUS_PENDING_REVIEW, "编辑重新提交", currentUser, now);

        boolean developer = !admin && isDeveloper();
        requirement.setAvailableActions(buildAvailableActions(requirement, currentUser.getId(), admin, developer));
        return Result.success(requirement);
    }

    private void applySort(javax.persistence.criteria.Root<TrackRequirement> root,
                           javax.persistence.criteria.CriteriaQuery<?> query,
                           javax.persistence.criteria.CriteriaBuilder cb,
                           String sortField,
                           boolean asc) {
        if ("priority".equals(sortField)) {
            Expression<Integer> priorityOrder = cb.<Integer>selectCase()
                    .when(cb.equal(root.get("priority"), "P0"), 0)
                    .when(cb.equal(root.get("priority"), "P1"), 1)
                    .when(cb.equal(root.get("priority"), "P2"), 2)
                    .otherwise(9);
            if (asc) {
                query.orderBy(cb.asc(priorityOrder), cb.desc(root.get("createTime")));
            } else {
                query.orderBy(cb.desc(priorityOrder), cb.desc(root.get("createTime")));
            }
            return;
        }

        String dbField = "createTime";
        if ("updateTime".equals(sortField)) {
            dbField = "updateTime";
        }

        if (asc) {
            query.orderBy(cb.asc(root.get(dbField)));
        } else {
            query.orderBy(cb.desc(root.get(dbField)));
        }
    }

    private List<String> parseStatusList(String statusListText) {
        String value = normalize(statusListText);
        if (value == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(value.split(","))
                .map(this::normalize)
                .filter(Objects::nonNull)
                .filter(STATUS_NAME_MAP::containsKey)
                .distinct()
                .collect(Collectors.toList());
    }

    private String normalizeSortField(String sortField) {
        String value = normalize(sortField);
        if (value == null || !VALID_SORT_FIELDS.contains(value)) {
            return "createTime";
        }
        return value;
    }

    private List<TrackRequirementAction> buildAvailableActions(TrackRequirement requirement,
                                                               Long currentUserId,
                                                               boolean admin,
                                                               boolean developer) {
        List<TrackRequirementAction> actions = new ArrayList<>();
        String status = requirement.getStatus();

        if (STATUS_REJECTED.equals(status) && (admin || Objects.equals(requirement.getProposerId(), currentUserId))) {
            TrackRequirementAction action = new TrackRequirementAction();
            action.setActionType(ACTION_TYPE_EDIT);
            action.setLabel("编辑");
            action.setNeedOpinion(false);
            actions.add(action);
            return actions;
        }

        if (admin) {
            appendAdminStatusActions(status, actions);
            return actions;
        }
        if (developer) {
            appendDeveloperStatusActions(status, actions);
        }
        return actions;
    }

    private void appendAdminStatusActions(String status, List<TrackRequirementAction> actions) {
        if (STATUS_PENDING_REVIEW.equals(status)) {
            addStatusAction(actions, STATUS_SCHEDULING, false);
            addStatusAction(actions, STATUS_REJECTED, true);
            return;
        }
        if (STATUS_SCHEDULING.equals(status)) {
            addStatusAction(actions, STATUS_PENDING_REVIEW, false);
            addStatusAction(actions, STATUS_DEVELOPING, false);
            return;
        }
        if (STATUS_DEVELOPING.equals(status)) {
            addStatusAction(actions, STATUS_TESTING, false);
            addStatusAction(actions, STATUS_SCHEDULING, false);
            return;
        }
        if (STATUS_TESTING.equals(status)) {
            addStatusAction(actions, STATUS_ONLINE, false);
            addStatusAction(actions, STATUS_DEVELOPING, false);
            return;
        }
        if (STATUS_ONLINE.equals(status)) {
            addStatusAction(actions, STATUS_OFFLINE, false);
            addStatusAction(actions, STATUS_TESTING, false);
            return;
        }
        if (STATUS_OFFLINE.equals(status)) {
            addStatusAction(actions, STATUS_ONLINE, false);
        }
    }

    private void appendDeveloperStatusActions(String status, List<TrackRequirementAction> actions) {
        if (STATUS_SCHEDULING.equals(status)) {
            addStatusAction(actions, STATUS_DEVELOPING, false);
            return;
        }
        if (STATUS_DEVELOPING.equals(status)) {
            addStatusAction(actions, STATUS_TESTING, false);
            addStatusAction(actions, STATUS_SCHEDULING, false);
            return;
        }
        if (STATUS_TESTING.equals(status)) {
            addStatusAction(actions, STATUS_ONLINE, false);
            addStatusAction(actions, STATUS_DEVELOPING, false);
            return;
        }
        if (STATUS_ONLINE.equals(status)) {
            addStatusAction(actions, STATUS_TESTING, false);
        }
    }

    private void addStatusAction(List<TrackRequirementAction> actions, String targetStatus, boolean needOpinion) {
        TrackRequirementAction action = new TrackRequirementAction();
        action.setActionType(ACTION_TYPE_CHANGE_STATUS);
        action.setTargetStatus(targetStatus);
        action.setTargetStatusName(STATUS_NAME_MAP.get(targetStatus));
        action.setLabel("变更为" + STATUS_NAME_MAP.get(targetStatus));
        action.setNeedOpinion(needOpinion);
        actions.add(action);
    }

    private ValidationContext validateAndResolveRequest(String title,
                                                        String businessLineCode,
                                                        String priority,
                                                        String expectedOnlineDateText,
                                                        String devTeamCode,
                                                        String description) {
        ValidationContext context = new ValidationContext();
        context.setTitle(normalize(title));
        context.setBusinessLineCode(normalize(businessLineCode));
        context.setPriority(normalize(priority));
        context.setDevTeamCode(normalize(devTeamCode));
        context.setDescription(normalizeNullable(description));

        if (context.getTitle() == null) {
            context.setErrorMessage("Title is required");
            return context;
        }
        if (context.getTitle().length() > 200) {
            context.setErrorMessage("Title length cannot exceed 200");
            return context;
        }
        if (context.getBusinessLineCode() == null) {
            context.setErrorMessage("Business line is required");
            return context;
        }
        if (context.getPriority() == null) {
            context.setErrorMessage("Priority is required");
            return context;
        }
        context.setPriority(context.getPriority().toUpperCase());
        if (!VALID_PRIORITIES.contains(context.getPriority())) {
            context.setErrorMessage("Priority is invalid");
            return context;
        }
        if (context.getDevTeamCode() == null) {
            context.setErrorMessage("Dev team is required");
            return context;
        }
        if (expectedOnlineDateText == null) {
            context.setErrorMessage("Expected online date is required");
            return context;
        }
        try {
            context.setExpectedOnlineDate(java.time.LocalDate.parse(expectedOnlineDateText));
        } catch (Exception e) {
            context.setErrorMessage("Expected online date is invalid");
            return context;
        }

        Map<String, DictParamItem> businessMap = loadActiveDictItemMap(BUSINESS_LINE_PARAM_ID);
        DictParamItem businessItem = businessMap.get(context.getBusinessLineCode());
        if (businessItem == null) {
            context.setErrorMessage("Business line is invalid");
            return context;
        }
        context.setBusinessLineName(businessItem.getItemName());

        Map<String, DictParamItem> devTeamMap = loadActiveDictItemMap(DEV_TEAM_PARAM_ID);
        DictParamItem devTeamItem = devTeamMap.get(context.getDevTeamCode());
        if (devTeamItem == null) {
            context.setErrorMessage("Dev team is invalid");
            return context;
        }
        context.setDevTeamName(devTeamItem.getItemName());
        return context;
    }

    private Map<String, DictParamItem> loadActiveDictItemMap(String paramId) {
        List<DictParamItem> items = dictParamItemRepository.findByParamIdAndStatusOrderByIdAsc(paramId, 0);
        Map<String, DictParamItem> map = new LinkedHashMap<>();
        for (DictParamItem item : items) {
            if (item.getItemCode() != null) {
                map.put(item.getItemCode(), item);
            }
        }
        return map;
    }

    private boolean isResubmitChanged(TrackRequirement requirement, ValidationContext validation) {
        return !Objects.equals(requirement.getTitle(), validation.getTitle())
                || !Objects.equals(requirement.getBusinessLineCode(), validation.getBusinessLineCode())
                || !Objects.equals(requirement.getPriority(), validation.getPriority())
                || !Objects.equals(requirement.getExpectedOnlineDate(), validation.getExpectedOnlineDate())
                || !Objects.equals(requirement.getDevTeamCode(), validation.getDevTeamCode())
                || !Objects.equals(normalizeNullable(requirement.getDescription()), validation.getDescription());
    }

    private void writeLog(String requirementId,
                          String actionType,
                          String fromStatus,
                          String toStatus,
                          String opinion,
                          User operator,
                          LocalDateTime operateTime) {
        TrackLog log = new TrackLog();
        log.setLogType(LOG_TYPE_REQUIREMENT_MANAGE);
        log.setRequirementId(requirementId);
        log.setActionType(actionType);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setOpinion(normalizeNullable(opinion));
        log.setOperatorId(operator.getId());
        log.setOperatorName(resolveUserDisplayName(operator));
        log.setOperateTime(operateTime == null ? LocalDateTime.now() : operateTime);
        trackLogRepository.save(log);
    }

    private User getCurrentUser() {
        Long userId = permissionChecker.getCurrentUserId();
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId).orElse(null);
    }

    private String resolveUserDisplayName(User user) {
        if (user == null) {
            return "unknown";
        }
        String nickname = normalize(user.getNickname());
        if (nickname != null) {
            return nickname;
        }
        String username = normalize(user.getUsername());
        return username == null ? "unknown" : username;
    }

    private String resolveDepartmentName(User user) {
        if (user == null || user.getPrimaryDeptId() == null) {
            return "";
        }
        DictParamItem dept = dictParamItemRepository.findById(user.getPrimaryDeptId()).orElse(null);
        return dept == null || dept.getItemName() == null ? "" : dept.getItemName();
    }

    private boolean isAdmin() {
        return permissionChecker.hasAnyRole("admin");
    }

    private boolean isDeveloper() {
        return permissionChecker.hasAnyRole("developer");
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static class ValidationContext {
        private String title;
        private String businessLineCode;
        private String businessLineName;
        private String priority;
        private java.time.LocalDate expectedOnlineDate;
        private String devTeamCode;
        private String devTeamName;
        private String description;
        private String errorMessage;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getBusinessLineCode() {
            return businessLineCode;
        }

        public void setBusinessLineCode(String businessLineCode) {
            this.businessLineCode = businessLineCode;
        }

        public String getBusinessLineName() {
            return businessLineName;
        }

        public void setBusinessLineName(String businessLineName) {
            this.businessLineName = businessLineName;
        }

        public String getPriority() {
            return priority;
        }

        public void setPriority(String priority) {
            this.priority = priority;
        }

        public java.time.LocalDate getExpectedOnlineDate() {
            return expectedOnlineDate;
        }

        public void setExpectedOnlineDate(java.time.LocalDate expectedOnlineDate) {
            this.expectedOnlineDate = expectedOnlineDate;
        }

        public String getDevTeamCode() {
            return devTeamCode;
        }

        public void setDevTeamCode(String devTeamCode) {
            this.devTeamCode = devTeamCode;
        }

        public String getDevTeamName() {
            return devTeamName;
        }

        public void setDevTeamName(String devTeamName) {
            this.devTeamName = devTeamName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}
