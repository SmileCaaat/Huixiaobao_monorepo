package com.ruoyi.fire.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.enums.RepairStatus;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import org.apache.shiro.SecurityUtils;
import com.ruoyi.fire.domain.FireCheckIn;
import com.ruoyi.fire.domain.FireCompany;
import com.ruoyi.fire.domain.FireFaultRepair;
import com.ruoyi.fire.domain.FireInspection;
import com.ruoyi.fire.domain.FireMaintenanceTask;
import com.ruoyi.fire.domain.FireUserCompany;
import com.ruoyi.fire.mapper.FireCompanyMapper;
import com.ruoyi.fire.mapper.FireMaintenanceTaskMapper;
import com.ruoyi.fire.service.IFireDataPermissionService;
import com.ruoyi.fire.service.IFireMaintenanceTaskService;
import com.ruoyi.system.domain.FireReportRecord;
import com.ruoyi.system.service.ISysUserService;

/**
 * 消防业务数据权限：超级管理员 / 系统项目经理(全局) / 项目负责人 / 项目成员 / 任务干系人
 */
@Service
public class FireDataPermissionServiceImpl implements IFireDataPermissionService {

    /** 系统角色：项目经理（全量业务数据） */
    private static final String ROLE_PROJECT_MANAGER = "project_manager";

    @Autowired
    private FireCompanyMapper companyMapper;

    @Autowired
    private FireMaintenanceTaskMapper taskMapper;

    @Autowired(required = false)
    private IFireMaintenanceTaskService taskService;

    @Autowired
    private ISysUserService userService;

    @Override
    public boolean hasGlobalBizDataScope(SysUser user) {
        if (user == null) {
            return false;
        }
        if (user.isAdmin()) {
            return true;
        }
        List<SysRole> roles = user.getRoles();
        if (roles == null) {
            return false;
        }
        return roles.stream().anyMatch(r -> ROLE_PROJECT_MANAGER.equals(r.getRoleKey())
                && !"1".equals(r.getStatus())
                && !"2".equals(r.getDelFlag()));
    }

    @Override
    public FireUserCompany getMembership(Long userId, Long companyId) {
        if (userId == null || companyId == null) {
            return null;
        }
        return companyMapper.selectUserCompany(userId, companyId);
    }

    @Override
    public List<Long> listAccessibleCompanyIds(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<FireUserCompany> list = companyMapper.selectActiveMembershipsByUserId(userId);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().map(FireUserCompany::getCompanyId).distinct().collect(Collectors.toList());
    }

    @Override
    public boolean canAccessCompany(Long userId, Long companyId) {
        if (userId == null || companyId == null) {
            return false;
        }
        if (SysUser.isAdmin(userId)) {
            return true;
        }
        return getMembership(userId, companyId) != null;
    }

    @Override
    public boolean canAccessCompany(SysUser user, Long companyId) {
        if (user == null || companyId == null) {
            return false;
        }
        if (user.isAdmin() || hasGlobalBizDataScope(user)) {
            return true;
        }
        return canAccessCompany(user.getUserId(), companyId);
    }

    @Override
    public boolean canManageCompanyMembers(Long userId, Long companyId) {
        if (SysUser.isAdmin(userId)) {
            return true;
        }
        FireUserCompany m = getMembership(userId, companyId);
        if (m == null) {
            return false;
        }
        return FireUserCompany.ROLE_PROJECT_MANAGER.equals(m.getRoleType())
                || FireUserCompany.ROLE_COMPANY_ADMIN.equals(m.getRoleType());
    }

    @Override
    public boolean canManageCompanyMembers(SysUser user, Long companyId) {
        if (user == null) {
            return false;
        }
        if (user.isAdmin() || hasGlobalBizDataScope(user)) {
            return true;
        }
        return canManageCompanyMembers(user.getUserId(), companyId);
    }

    private boolean isTaskAssignee(Long userId, FireMaintenanceTask task) {
        if (userId == null || task == null) {
            return false;
        }
        if (userId.equals(task.getManagerId()) || userId.equals(task.getExecutorId())) {
            return true;
        }
        String operatorIds = task.getOperatorIds();
        if (StringUtils.isEmpty(operatorIds)) {
            return false;
        }
        for (String id : operatorIds.split(",")) {
            if (userId.toString().equals(id.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean isCompanyLead(FireUserCompany m) {
        if (m == null) {
            return false;
        }
        return FireUserCompany.ROLE_PROJECT_MANAGER.equals(m.getRoleType())
                || FireUserCompany.ROLE_COMPANY_ADMIN.equals(m.getRoleType())
                || FireUserCompany.ROLE_TEAM_LEADER.equals(m.getRoleType());
    }

    @Override
    public boolean canAccessTask(Long userId, FireMaintenanceTask task) {
        if (userId == null || task == null) {
            return false;
        }
        if (SysUser.isAdmin(userId)) {
            return true;
        }
        if (isTaskAssignee(userId, task)) {
            return true;
        }
        if (task.getCompanyId() != null) {
            return isCompanyLead(getMembership(userId, task.getCompanyId()));
        }
        return false;
    }

    @Override
    public boolean canAccessTask(SysUser user, FireMaintenanceTask task) {
        if (user == null || task == null) {
            return false;
        }
        if (user.isAdmin() || hasGlobalBizDataScope(user)) {
            return true;
        }
        return canAccessTask(user.getUserId(), task);
    }

    @Override
    public boolean canDispatchTask(Long userId, FireMaintenanceTask task) {
        if (!canAccessTask(userId, task)) {
            return false;
        }
        if (SysUser.isAdmin(userId)) {
            return true;
        }
        if (task.getCompanyId() == null) {
            return userId.equals(task.getManagerId());
        }
        FireUserCompany m = getMembership(userId, task.getCompanyId());
        return isCompanyLead(m) || userId.equals(task.getManagerId());
    }

    @Override
    public boolean canDispatchTask(SysUser user, FireMaintenanceTask task) {
        if (user == null || task == null) {
            return false;
        }
        if (user.isAdmin() || hasGlobalBizDataScope(user)) {
            return true;
        }
        return canDispatchTask(user.getUserId(), task);
    }

    @Override
    public boolean canAccessRepair(Long userId, FireFaultRepair repair) {
        if (userId == null || repair == null) {
            return false;
        }
        if (SysUser.isAdmin(userId)) {
            return true;
        }
        if (userId.equals(repair.getReporterId()) || userId.equals(repair.getRepairUserId())) {
            return true;
        }
        if (repair.getCompanyId() != null) {
            return isCompanyLead(getMembership(userId, repair.getCompanyId()));
        }
        return false;
    }

    @Override
    public boolean canAccessRepair(SysUser user, FireFaultRepair repair) {
        if (user == null || repair == null) {
            return false;
        }
        if (user.isAdmin() || hasGlobalBizDataScope(user)) {
            return true;
        }
        return canAccessRepair(user.getUserId(), repair);
    }

    @Override
    public boolean canProcessRepair(SysUser user, FireFaultRepair repair) {
        if (user == null || repair == null || user.getUserId() == null) {
            return false;
        }
        if ("1".equals(user.getStatus()) || "2".equals(user.getDelFlag())) {
            return false;
        }
        if (repair.getRepairUserId() == null) {
            return false;
        }
        if (!user.getUserId().equals(repair.getRepairUserId())) {
            return false;
        }
        if (!RepairStatus.IN_PROGRESS.getCode().equals(repair.getRepairStatus())) {
            return false;
        }
        // 已派发处理人可处理；不因单位成员关系二次拦截（任务干系人/转派场景）
        return true;
    }

    @Override
    public void assertCanProcessRepair(SysUser user, FireFaultRepair repair) {
        if (user == null) {
            throw new ServiceException("未登录或登录状态已失效");
        }
        if (repair == null) {
            throw new ServiceException("报修单不存在");
        }
        if (repair.getRepairUserId() == null) {
            throw new ServiceException("工单尚未派发，不能处理");
        }
        if (!user.getUserId().equals(repair.getRepairUserId())) {
            throw new ServiceException("只有当前派发处理人可以处理该工单");
        }
        if ("1".equals(user.getStatus()) || "2".equals(user.getDelFlag())) {
            throw new ServiceException("当前账号不可用，不能处理工单");
        }
        if (!RepairStatus.IN_PROGRESS.getCode().equals(repair.getRepairStatus())) {
            throw new ServiceException("工单状态已变化，无法处理");
        }
    }

    @Override
    public boolean isRepairEmployeeWorkbench(SysUser user) {
        if (user == null || user.isAdmin() || hasGlobalBizDataScope(user)) {
            return false;
        }
        try {
            return !SecurityUtils.getSubject().isPermitted("fire:repair:accept");
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public void prepareRepairListQuery(FireFaultRepair query, SysUser user, String workbenchCategory) {
        if (query == null || user == null) {
            return;
        }
        sanitizeRepairClientScope(query, user);
        applyRepairListScope(query, user);
        if (isRepairEmployeeWorkbench(user)) {
            query.setRepairUserId(null);
            query.setReporterId(null);
            applyRepairWorkbenchCategory(query, user, workbenchCategory);
        }
    }

    private void sanitizeRepairClientScope(FireFaultRepair query, SysUser user) {
        if (query.getParams() == null) {
            query.setParams(new HashMap<>());
        }
        query.getParams().remove("relatedUserId");
        query.getParams().remove("leadCompanyIds");
        query.getParams().remove("scopeMode");
        query.getParams().remove("scopeUserId");
        query.getParams().remove("workbenchCategory");
        if (isRepairEmployeeWorkbench(user)) {
            query.setRepairUserId(null);
            query.setReporterId(null);
        }
    }

    private void applyRepairWorkbenchCategory(FireFaultRepair query, SysUser user, String category) {
        String normalized = normalizeWorkbenchCategory(category);
        if (query.getParams() == null) {
            query.setParams(new HashMap<>());
        }
        query.getParams().put("workbenchCategory", normalized);
        query.getParams().put("scopeUserId", user.getUserId());
    }

    private String normalizeWorkbenchCategory(String category) {
        // processing 分类已下线，并入待我处理
        if ("processing".equals(category)) {
            return "assignedPending";
        }
        if ("completed".equals(category) || "reported".equals(category)
                || "assignedPending".equals(category)) {
            return category;
        }
        return "assignedPending";
    }

    @Override
    public boolean canAccessReport(Long userId, FireReportRecord report) {
        if (userId == null || report == null) {
            return false;
        }
        if (SysUser.isAdmin(userId)) {
            return true;
        }
        if (taskService == null || report.getTaskId() == null) {
            return false;
        }
        FireMaintenanceTask task = taskService.selectFireMaintenanceTaskByTaskId(report.getTaskId());
        return canAccessTask(userId, task);
    }

    @Override
    public boolean canAccessReport(SysUser user, FireReportRecord report) {
        if (user == null || report == null) {
            return false;
        }
        if (user.isAdmin() || hasGlobalBizDataScope(user)) {
            return true;
        }
        return canAccessReport(user.getUserId(), report);
    }

    @Override
    public void assertCanAccessCompany(Long userId, Long companyId) {
        if (SysUser.isAdmin(userId)) {
            return;
        }
        if (!canAccessCompany(userId, companyId)) {
            throw new ServiceException("\u65e0\u6743\u8bbf\u95ee\u8be5\u5ba2\u6237\u6570\u636e");
        }
    }

    @Override
    public void assertCanAccessCompany(SysUser user, Long companyId) {
        if (user == null) {
            throw new ServiceException("\u672a\u767b\u5f55");
        }
        if (!canAccessCompany(user, companyId)) {
            throw new ServiceException("\u65e0\u6743\u8bbf\u95ee\u8be5\u5ba2\u6237\u6570\u636e");
        }
    }

    @Override
    public boolean canAccessCompanyContext(SysUser user, Long companyId, FireMaintenanceTask linkedTask) {
        if (canAccessCompany(user, companyId)) {
            return true;
        }
        if (user == null || companyId == null || linkedTask == null) {
            return false;
        }
        if (linkedTask.getCompanyId() == null || !companyId.equals(linkedTask.getCompanyId())) {
            return false;
        }
        return canAccessTask(user, linkedTask);
    }

    @Override
    public void assertCanAccessCompanyContext(SysUser user, Long companyId, FireMaintenanceTask linkedTask) {
        if (user == null) {
            throw new ServiceException("\u672a\u767b\u5f55");
        }
        if (!canAccessCompanyContext(user, companyId, linkedTask)) {
            throw new ServiceException("\u65e0\u6743\u8bbf\u95ee\u8be5\u5ba2\u6237\u6570\u636e");
        }
    }

    /** Whether user is manager/executor/operator on any task of the company. */
    private boolean canAccessCompanyViaTaskStakeholder(Long userId, Long companyId) {
        if (userId == null || companyId == null || taskMapper == null) {
            return false;
        }
        List<FireCompany> companies = taskMapper.selectCompanyListByTaskUserId(userId);
        if (companies == null || companies.isEmpty()) {
            return false;
        }
        for (FireCompany company : companies) {
            if (company != null && companyId.equals(company.getCompanyId())) {
                return true;
            }
        }
        return false;
    }

    private List<Long> listTaskStakeholderCompanyIds(Long userId) {
        if (userId == null || taskMapper == null) {
            return Collections.emptyList();
        }
        List<FireCompany> companies = taskMapper.selectCompanyListByTaskUserId(userId);
        if (companies == null || companies.isEmpty()) {
            return Collections.emptyList();
        }
        return companies.stream()
                .filter(c -> c != null && c.getCompanyId() != null)
                .map(FireCompany::getCompanyId)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public void assertCanAccessTask(Long userId, FireMaintenanceTask task) {
        if (task == null) {
            throw new ServiceException("\u4efb\u52a1\u4e0d\u5b58\u5728");
        }
        if (!canAccessTask(userId, task)) {
            throw new ServiceException("\u65e0\u6743\u8bbf\u95ee\u8be5\u4efb\u52a1");
        }
    }

    @Override
    public void assertCanAccessTask(SysUser user, FireMaintenanceTask task) {
        if (task == null) {
            throw new ServiceException("\u4efb\u52a1\u4e0d\u5b58\u5728");
        }
        if (!canAccessTask(user, task)) {
            throw new ServiceException("\u65e0\u6743\u8bbf\u95ee\u8be5\u4efb\u52a1");
        }
    }

    @Override
    public void assertCanDispatchTask(Long userId, FireMaintenanceTask task) {
        if (task == null) {
            throw new ServiceException("\u4efb\u52a1\u4e0d\u5b58\u5728");
        }
        if (!canDispatchTask(userId, task)) {
            throw new ServiceException("\u65e0\u6743\u6d3e\u53d1\u8be5\u4efb\u52a1");
        }
    }

    @Override
    public void assertCanDispatchTask(SysUser user, FireMaintenanceTask task) {
        if (task == null) {
            throw new ServiceException("\u4efb\u52a1\u4e0d\u5b58\u5728");
        }
        if (!canDispatchTask(user, task)) {
            throw new ServiceException("\u65e0\u6743\u6d3e\u53d1\u8be5\u4efb\u52a1");
        }
    }

    @Override
    public void assertCanAccessRepair(Long userId, FireFaultRepair repair) {
        if (repair == null) {
            throw new ServiceException("\u62a5\u4fee\u5355\u4e0d\u5b58\u5728");
        }
        if (!canAccessRepair(userId, repair)) {
            throw new ServiceException("\u65e0\u6743\u8bbf\u95ee\u8be5\u62a5\u4fee\u5355");
        }
    }

    @Override
    public void assertCanAccessRepair(SysUser user, FireFaultRepair repair) {
        if (repair == null) {
            throw new ServiceException("\u62a5\u4fee\u5355\u4e0d\u5b58\u5728");
        }
        if (!canAccessRepair(user, repair)) {
            throw new ServiceException("\u65e0\u6743\u8bbf\u95ee\u8be5\u62a5\u4fee\u5355");
        }
    }

    @Override
    public void assertCanAccessReport(Long userId, FireReportRecord report) {
        if (report == null) {
            throw new ServiceException("\u62a5\u544a\u4e0d\u5b58\u5728");
        }
        if (!canAccessReport(userId, report)) {
            throw new ServiceException("\u65e0\u6743\u8bbf\u95ee\u8be5\u62a5\u544a");
        }
    }

    @Override
    public void assertCanAccessReport(SysUser user, FireReportRecord report) {
        if (report == null) {
            throw new ServiceException("\u62a5\u544a\u4e0d\u5b58\u5728");
        }
        if (!canAccessReport(user, report)) {
            throw new ServiceException("\u65e0\u6743\u8bbf\u95ee\u8be5\u62a5\u544a");
        }
    }

    @Override
    public void assertUserDispatchable(Long targetUserId) {
        if (targetUserId == null) {
            throw new ServiceException("\u5904\u7406\u4eba\u4e0d\u80fd\u4e3a\u7a7a");
        }
        assertUserDispatchable(userService.selectUserById(targetUserId));
    }

    @Override
    public void assertUserDispatchable(SysUser targetUser) {
        if (targetUser == null || "2".equals(targetUser.getDelFlag())) {
            throw new ServiceException("\u5904\u7406\u4eba\u4e0d\u5b58\u5728\u6216\u5df2\u5220\u9664");
        }
        if ("1".equals(targetUser.getStatus())) {
            throw new ServiceException("\u5904\u7406\u4eba\u8d26\u53f7\u5df2\u505c\u7528");
        }
        String audit = targetUser.getAuditStatus();
        if (StringUtils.isNotEmpty(audit) && !"0".equals(audit)) {
            throw new ServiceException("\u8be5\u7528\u6237\u5c1a\u672a\u901a\u8fc7\u5ba1\u6838\uff0c\u4e0d\u53ef\u6d3e\u5de5");
        }
        if (!targetUser.isDispatchableUser()) {
            throw new ServiceException("\u8be5\u7528\u6237\u4e0d\u53ef\u6d3e\u5de5");
        }
    }

    @Override
    public void applyTaskListScope(FireMaintenanceTask query, SysUser user) {
        if (query == null || user == null || user.isAdmin() || hasGlobalBizDataScope(user)) {
            return;
        }
        Long companyId = query.getCompanyId();
        if (companyId != null) {
            FireUserCompany m = getMembership(user.getUserId(), companyId);
            if (isCompanyLead(m)) {
                return;
            }
            if (m == null) {
                query.setManagerId(user.getUserId());
                return;
            }
        }
        query.setManagerId(user.getUserId());
    }

    @Override
    public void applyRepairListScope(FireFaultRepair query, SysUser user) {
        if (query == null || user == null || user.isAdmin() || hasGlobalBizDataScope(user)) {
            return;
        }
        Long companyId = query.getCompanyId();
        if (companyId != null && isCompanyLead(getMembership(user.getUserId(), companyId))) {
            return;
        }
        List<FireUserCompany> memberships = companyMapper.selectActiveMembershipsByUserId(user.getUserId());
        List<Long> leadCompanyIds = new ArrayList<>();
        if (memberships != null) {
            for (FireUserCompany m : memberships) {
                if (isCompanyLead(m) && m.getCompanyId() != null) {
                    leadCompanyIds.add(m.getCompanyId());
                }
            }
        }
        if (query.getParams() == null) {
            query.setParams(new HashMap<>());
        }
        if (!leadCompanyIds.isEmpty()) {
            query.getParams().put("leadCompanyIds", leadCompanyIds);
            query.getParams().put("relatedUserId", user.getUserId());
            query.getParams().put("scopeMode", "leadOrRelated");
        } else {
            query.getParams().put("relatedUserId", user.getUserId());
        }
    }

    @Override
    public void applyReportListScope(FireReportRecord query, SysUser user) {
        if (query == null || user == null || user.isAdmin() || hasGlobalBizDataScope(user)) {
            return;
        }
        if (query.getParams() == null) {
            query.setParams(new HashMap<>());
        }
        query.getParams().put("scopeUserId", user.getUserId());
    }

    @Override
    public List<Long> listLeadCompanyIds(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        Set<Long> ids = new LinkedHashSet<>();
        List<FireUserCompany> memberships = companyMapper.selectActiveMembershipsByUserId(userId);
        if (memberships != null) {
            for (FireUserCompany m : memberships) {
                if (isCompanyLead(m) && m.getCompanyId() != null) {
                    ids.add(m.getCompanyId());
                }
            }
        }
        List<Long> managed = taskMapper.selectManagedCompanyIdsByUserId(userId);
        if (managed != null) {
            for (Long companyId : managed) {
                if (companyId != null) {
                    ids.add(companyId);
                }
            }
        }
        return new ArrayList<>(ids);
    }

    @Override
    public List<Long> listLeadCompanyIds(SysUser user) {
        if (user == null) {
            return Collections.emptyList();
        }
        return listLeadCompanyIds(user.getUserId());
    }

    private boolean isCheckInSignerInProject(Long signerUserId, Long companyId, Long taskId) {
        if (signerUserId == null || companyId == null) {
            return false;
        }
        if (getMembership(signerUserId, companyId) != null) {
            return true;
        }
        if (taskId == null) {
            return false;
        }
        FireMaintenanceTask task = taskService != null
                ? taskService.selectFireMaintenanceTaskByTaskId(taskId)
                : taskMapper.selectFireMaintenanceTaskByTaskId(taskId);
        if (task == null || task.getCompanyId() == null || !companyId.equals(task.getCompanyId())) {
            return false;
        }
        return isTaskAssignee(signerUserId, task);
    }

    @Override
    public boolean canAccessCheckIn(Long userId, FireCheckIn checkIn) {
        if (userId == null || checkIn == null) {
            return false;
        }
        if (SysUser.isAdmin(userId)) {
            return true;
        }
        if (userId.equals(checkIn.getUserId())) {
            return true;
        }
        Long companyId = checkIn.getCompanyId();
        if (companyId == null) {
            return false;
        }
        List<Long> leadIds = listLeadCompanyIds(userId);
        if (leadIds.isEmpty() || !leadIds.contains(companyId)) {
            return false;
        }
        return isCheckInSignerInProject(checkIn.getUserId(), companyId, checkIn.getTaskId());
    }

    @Override
    public boolean canAccessCheckIn(SysUser user, FireCheckIn checkIn) {
        if (user == null || checkIn == null) {
            return false;
        }
        if (user.isAdmin() || hasGlobalBizDataScope(user)) {
            return true;
        }
        return canAccessCheckIn(user.getUserId(), checkIn);
    }

    @Override
    public void assertCanAccessCheckIn(Long userId, FireCheckIn checkIn) {
        if (checkIn == null) {
            throw new ServiceException("签到记录不存在");
        }
        if (!canAccessCheckIn(userId, checkIn)) {
            throw new ServiceException("无权限访问该签到记录");
        }
    }

    @Override
    public void assertCanAccessCheckIn(SysUser user, FireCheckIn checkIn) {
        if (checkIn == null) {
            throw new ServiceException("签到记录不存在");
        }
        if (user == null) {
            throw new ServiceException("未登录");
        }
        if (!canAccessCheckIn(user, checkIn)) {
            throw new ServiceException("无权限访问该签到记录");
        }
    }

    @Override
    public void applyCheckInListScope(FireCheckIn query, SysUser user) {
        if (query == null || user == null || user.isAdmin() || hasGlobalBizDataScope(user)) {
            return;
        }
        if (query.getParams() == null) {
            query.setParams(new HashMap<>());
        } else {
            // 禁止前端伪造 scope 参数
            query.getParams().remove("scopeMode");
            query.getParams().remove("leadCompanyIds");
            query.getParams().remove("scopeUserId");
        }
        List<Long> leadCompanyIds = listLeadCompanyIds(user);
        query.getParams().put("scopeUserId", user.getUserId());
        if (leadCompanyIds.isEmpty()) {
            query.getParams().put("scopeMode", "self");
            return;
        }
        query.getParams().put("scopeMode", "leadOrSelf");
        query.getParams().put("leadCompanyIds", leadCompanyIds);
    }

    @Override
    public void applyInspectionListScope(FireInspection query, SysUser user) {
        if (query == null || user == null || user.isAdmin() || hasGlobalBizDataScope(user)) {
            return;
        }
        if (query.getParams() == null) {
            query.setParams(new HashMap<>());
        } else {
            query.getParams().remove("companyIds");
            query.getParams().remove("scopeMode");
        }
        Long requestedCompanyId = query.getCompanyId();
        if (requestedCompanyId != null) {
            if (!canAccessCompany(user.getUserId(), requestedCompanyId)
                    && !canAccessCompanyViaTaskStakeholder(user.getUserId(), requestedCompanyId)) {
                query.getParams().put("scopeMode", "none");
                query.setCompanyId(null);
            }
            return;
        }
        Set<Long> companyIdSet = new LinkedHashSet<>();
        List<Long> memberCompanyIds = listAccessibleCompanyIds(user.getUserId());
        if (memberCompanyIds != null) {
            companyIdSet.addAll(memberCompanyIds);
        }
        companyIdSet.addAll(listTaskStakeholderCompanyIds(user.getUserId()));
        if (companyIdSet.isEmpty()) {
            query.getParams().put("scopeMode", "none");
            return;
        }
        query.getParams().put("companyIds", new ArrayList<>(companyIdSet));
    }

    @Override
    public boolean canAccessInspection(Long userId, FireInspection inspection) {
        if (inspection == null || userId == null) {
            return false;
        }
        if (canAccessCompany(userId, inspection.getCompanyId())) {
            return true;
        }
        if (userId.equals(inspection.getInspectorId())) {
            return true;
        }
        if (inspection.getTaskId() != null) {
            FireMaintenanceTask task = taskMapper != null
                    ? taskMapper.selectFireMaintenanceTaskByTaskId(inspection.getTaskId())
                    : null;
            return canAccessTask(userId, task);
        }
        return canAccessCompanyViaTaskStakeholder(userId, inspection.getCompanyId());
    }

    @Override
    public boolean canAccessInspection(SysUser user, FireInspection inspection) {
        if (inspection == null || user == null) {
            return false;
        }
        if (user.isAdmin() || hasGlobalBizDataScope(user)) {
            return true;
        }
        return canAccessInspection(user.getUserId(), inspection);
    }

    @Override
    public void assertCanAccessInspection(Long userId, FireInspection inspection) {
        if (inspection == null) {
            throw new ServiceException("巡检测试记录不存在");
        }
        if (!canAccessInspection(userId, inspection)) {
            throw new ServiceException("无权限访问该巡检测试记录");
        }
    }

    @Override
    public void assertCanAccessInspection(SysUser user, FireInspection inspection) {
        if (inspection == null) {
            throw new ServiceException("巡检测试记录不存在");
        }
        if (user == null) {
            throw new ServiceException("未登录");
        }
        if (!canAccessInspection(user, inspection)) {
            throw new ServiceException("无权限访问该巡检测试记录");
        }
    }
}
