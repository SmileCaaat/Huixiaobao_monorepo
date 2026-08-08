package com.ruoyi.fire.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.RepairStatus;
import com.ruoyi.common.enums.UrgencyLevel;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.fire.domain.FireCompany;
import com.ruoyi.fire.domain.FireFaultRepair;
import com.ruoyi.fire.domain.FireFaultRepairLog;
import com.ruoyi.fire.domain.FireUserCompany;
import com.ruoyi.fire.mapper.FireFaultRepairLogMapper;
import com.ruoyi.fire.mapper.FireFaultRepairMapper;
import com.ruoyi.fire.service.IFireCompanyService;
import com.ruoyi.fire.service.IFireDataPermissionService;
import com.ruoyi.fire.service.IFireFaultRepairService;
import com.ruoyi.system.service.ISysUserService;

/**
 * 故障报修 Service 实现。
 */
@Service
public class FireFaultRepairServiceImpl implements IFireFaultRepairService {
    @Autowired
    private FireFaultRepairMapper fireFaultRepairMapper;

    @Autowired
    private FireFaultRepairLogMapper fireFaultRepairLogMapper;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private IFireCompanyService companyService;

    @Autowired
    private IFireDataPermissionService fireDataPermissionService;

    @Override
    public FireFaultRepair selectFireFaultRepairById(Long repairId) {
        return fireFaultRepairMapper.selectFireFaultRepairById(repairId);
    }

    @Override
    public List<FireFaultRepair> selectFireFaultRepairList(FireFaultRepair fireFaultRepair) {
        return fireFaultRepairMapper.selectFireFaultRepairList(fireFaultRepair);
    }

    @Override
    public int insertFireFaultRepair(FireFaultRepair fireFaultRepair) {
        validateUrgencyLevel(fireFaultRepair.getUrgencyLevel());
        fireFaultRepair.setRepairNo(fireFaultRepairMapper.generateRepairNo());
        if (StringUtils.isEmpty(fireFaultRepair.getStatus())) {
            fireFaultRepair.setStatus("0");
        }
        if (fireFaultRepair.getFoundTime() == null) {
            fireFaultRepair.setFoundTime(new Date());
        }

        fillReporterInfo(fireFaultRepair);
        fillCompanyInfo(fireFaultRepair);
        // 上报人自动获得第一处理权，无需管理员下派
        assignReporterAsHandler(fireFaultRepair);

        int rows = fireFaultRepairMapper.insertFireFaultRepair(fireFaultRepair);
        if (rows > 0) {
            appendLog(fireFaultRepair.getRepairId(), "create",
                    displayName(fireFaultRepair.getReporterName()) + "提交了维修工单",
                    fireFaultRepair.getReporterId(), fireFaultRepair.getReporterName());
            if (fireFaultRepair.getRepairUserId() != null) {
                appendLog(fireFaultRepair.getRepairId(), "dispatch",
                        displayName(fireFaultRepair.getReporterName()) + "自动接单处理",
                        fireFaultRepair.getReporterId(), fireFaultRepair.getReporterName());
            }
        }
        return rows;
    }

    @Override
    public int updateFireFaultRepair(FireFaultRepair fireFaultRepair) {
        FireFaultRepair existing = getRequiredRepair(fireFaultRepair.getRepairId());
        if (RepairStatus.COMPLETED.getCode().equals(existing.getRepairStatus())) {
            throw new ServiceException("已完成的报修单不能编辑");
        }
        if (hasStartedWork(existing)) {
            throw new ServiceException("工单已开始处理，不能编辑报修信息");
        }
        validateUrgencyLevel(fireFaultRepair.getUrgencyLevel());
        fillCompanyInfo(fireFaultRepair);
        return fireFaultRepairMapper.updateFireFaultRepair(fireFaultRepair);
    }

    @Override
    public int deleteFireFaultRepairByIds(String ids) {
        String[] repairIds = Convert.toStrArray(ids);
        for (String repairId : repairIds) {
            ensurePending(Long.parseLong(repairId));
        }
        return fireFaultRepairMapper.deleteFireFaultRepairByIds(repairIds);
    }

    @Override
    public int deleteFireFaultRepairById(Long repairId) {
        ensurePending(repairId);
        return fireFaultRepairMapper.deleteFireFaultRepairById(repairId);
    }

    @Override
    public List<SysUser> selectDispatchUsers(Long repairId) {
        FireFaultRepair repair = getRequiredRepair(repairId);
        validateDispatchAuthority(repair);
        // 全部已注册、状态正常、未删除的系统用户
        List<SysUser> users = userService.selectActiveRegisteredUserList();
        if (users == null) {
            return java.util.Collections.emptyList();
        }
        return users.stream()
                .filter(u -> u != null && u.isDispatchableUser() && "0".equals(u.getAuditStatus()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int dispatchRepair(Long repairId, Long repairUserId, String dispatchBy) {
        FireFaultRepair repair = getRequiredRepair(repairId);
        ensureNotCompleted(repair);
        validateDispatchAuthority(repair);
        if (repairUserId == null) {
            throw new ServiceException("处理人不能为空");
        }

        // 已派发给其他人时拒绝静默覆盖
        if (repair.getRepairUserId() != null
                && !repairUserId.equals(repair.getRepairUserId())
                && RepairStatus.IN_PROGRESS.getCode().equals(repair.getRepairStatus())) {
            throw new ServiceException("该报修单已派发给其他人，不能重复派发");
        }

        SysUser repairUser = userService.selectUserById(repairUserId);
        fireDataPermissionService.assertUserDispatchable(repairUser);

        validateDispatchUser(repair, repairUserId);

        Date now = new Date();
        FireFaultRepair update = new FireFaultRepair();
        update.setRepairId(repairId);
        update.setRepairStatus(RepairStatus.IN_PROGRESS.getCode());
        update.setRepairUserId(repairUserId);
        update.setRepairPerson(repairUser.getUserName());
        update.setRepairPhone(repairUser.getPhonenumber());
        update.setDispatchBy(dispatchBy);
        update.setDispatchTime(now);
        update.setAcceptTime(now);
        update.setUpdateBy(dispatchBy);
        int rows = fireFaultRepairMapper.updateFireFaultRepair(update);
        if (rows > 0) {
            SysUser operator = ShiroUtils.getSysUser();
            String opName = operator != null ? operator.getUserName() : dispatchBy;
            Long opId = operator != null ? operator.getUserId() : null;
            appendLog(repairId, "dispatch",
                    displayName(opName) + "派发给" + displayName(repairUser.getUserName()),
                    opId, opName);
        }
        return rows;
    }

    @Override
    public int acceptRepair(Long repairId, String repairPerson, String repairPhone) {
        FireFaultRepair repair = getRequiredRepair(repairId);
        ensureNotCompleted(repair);
        if (!RepairStatus.PENDING.getCode().equals(repair.getRepairStatus())) {
            throw new ServiceException("只有待处理状态的报修单才能受理");
        }
        if (StringUtils.isEmpty(repairPerson)) {
            throw new ServiceException("处理人不能为空");
        }

        FireFaultRepair update = new FireFaultRepair();
        update.setRepairId(repairId);
        update.setRepairStatus(RepairStatus.IN_PROGRESS.getCode());
        update.setRepairPerson(repairPerson);
        update.setRepairPhone(repairPhone);
        update.setAcceptTime(new Date());
        update.setUpdateBy(ShiroUtils.getLoginName());
        return fireFaultRepairMapper.updateFireFaultRepair(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int startRepair(Long repairId) {
        SysUser current = requireCurrentUser();
        claimByReporterIfNeeded(repairId);
        FireFaultRepair repair = getRequiredRepair(repairId);
        fireDataPermissionService.assertCanProcessRepair(current, repair);

        if (repair.getStartTime() != null) {
            // 幂等：已开始则不覆盖原 start_time
            return 1;
        }

        int rows = fireFaultRepairMapper.startFireFaultRepair(
                repairId, current.getUserId(), current.getLoginName());
        if (rows <= 0) {
            FireFaultRepair latest = getRequiredRepair(repairId);
            if (latest.getStartTime() != null && current.getUserId().equals(latest.getRepairUserId())) {
                return 1;
            }
            throw new ServiceException("工单状态已变化，无法开始处理");
        }
        appendLog(repairId, "start",
                displayName(current.getUserName()) + "开始处理工单",
                current.getUserId(), current.getUserName());
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int completeRepair(FireFaultRepair fireFaultRepair) {
        SysUser current = requireCurrentUser();
        if (fireFaultRepair == null || fireFaultRepair.getRepairId() == null) {
            throw new ServiceException("repairId 不能为空");
        }
        claimByReporterIfNeeded(fireFaultRepair.getRepairId());
        FireFaultRepair repair = getRequiredRepair(fireFaultRepair.getRepairId());
        fireDataPermissionService.assertCanProcessRepair(current, repair);

        if (RepairStatus.COMPLETED.getCode().equals(repair.getRepairStatus())
                || repair.getCompleteTime() != null) {
            throw new ServiceException("工单已完成，不能重复提交");
        }
        if (repair.getStartTime() == null) {
            FireFaultRepair startUpdate = new FireFaultRepair();
            startUpdate.setRepairId(repair.getRepairId());
            startUpdate.setStartTime(fireFaultRepair.getStartTime() != null
                    ? fireFaultRepair.getStartTime() : new Date());
            startUpdate.setUpdateBy(current.getLoginName());
            fireFaultRepairMapper.updateFireFaultRepair(startUpdate);
            repair = getRequiredRepair(fireFaultRepair.getRepairId());
        }

        String description = fireFaultRepair.getRepairDescription();
        if (StringUtils.isEmpty(description)) {
            throw new ServiceException("处理说明不能为空");
        }
        if (description.length() > 500) {
            throw new ServiceException("处理说明不能超过500字");
        }

        String updateBy = StringUtils.isNotEmpty(fireFaultRepair.getUpdateBy())
                ? fireFaultRepair.getUpdateBy()
                : current.getLoginName();

        int rows = fireFaultRepairMapper.completeFireFaultRepair(
                repair.getRepairId(),
                current.getUserId(),
                description,
                fireFaultRepair.getRepairImages(),
                fireFaultRepair.getCompleteTime(),
                updateBy);
        if (rows <= 0) {
            throw new ServiceException("工单状态已变化，请刷新后重试");
        }
        appendLog(repair.getRepairId(), "complete",
                displayName(current.getUserName()) + "完成了维修",
                current.getUserId(), current.getUserName());
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveRepairProgress(FireFaultRepair fireFaultRepair) {
        SysUser current = requireCurrentUser();
        if (fireFaultRepair == null || fireFaultRepair.getRepairId() == null) {
            throw new ServiceException("repairId 不能为空");
        }
        claimByReporterIfNeeded(fireFaultRepair.getRepairId());
        FireFaultRepair repair = getRequiredRepair(fireFaultRepair.getRepairId());
        fireDataPermissionService.assertCanProcessRepair(current, repair);
        if (RepairStatus.COMPLETED.getCode().equals(repair.getRepairStatus())) {
            throw new ServiceException("工单已完成，不能再保存维修信息");
        }

        String description = fireFaultRepair.getRepairDescription();
        if (description != null && description.length() > 500) {
            throw new ServiceException("维修说明不能超过500字");
        }

        FireFaultRepair update = new FireFaultRepair();
        update.setRepairId(repair.getRepairId());
        if (description != null) {
            update.setRepairDescription(description);
        }
        if (fireFaultRepair.getRepairImages() != null) {
            update.setRepairImages(fireFaultRepair.getRepairImages());
        }
        if (repair.getStartTime() == null) {
            update.setStartTime(fireFaultRepair.getStartTime() != null
                    ? fireFaultRepair.getStartTime() : new Date());
        } else if (fireFaultRepair.getStartTime() != null) {
            update.setStartTime(fireFaultRepair.getStartTime());
        }
        update.setUpdateBy(current.getLoginName());
        int rows = fireFaultRepairMapper.updateFireFaultRepair(update);
        if (rows > 0) {
            appendLog(repair.getRepairId(), "save",
                    displayName(current.getUserName()) + "保存了维修信息",
                    current.getUserId(), current.getUserName());
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int transferRepair(Long repairId, Long targetUserId) {
        SysUser current = requireCurrentUser();
        FireFaultRepair repair = getRequiredRepair(repairId);
        if (!RepairStatus.IN_PROGRESS.getCode().equals(repair.getRepairStatus())) {
            throw new ServiceException("仅处理中的工单可以转派");
        }
        if (repair.getRepairUserId() == null || !repair.getRepairUserId().equals(current.getUserId())) {
            throw new ServiceException("仅当前处理人可以转派");
        }
        if (targetUserId == null) {
            throw new ServiceException("请选择转派对象");
        }
        if (targetUserId.equals(current.getUserId())) {
            throw new ServiceException("不能转派给自己");
        }
        if (current.getDeptId() == null) {
            throw new ServiceException("当前账号未归属部门，无法转派");
        }

        SysUser target = userService.selectUserById(targetUserId);
        validateDispatchUser(repair, targetUserId);
        if (target.getDeptId() == null || !current.getDeptId().equals(target.getDeptId())) {
            throw new ServiceException("只能转派给同部门人员");
        }

        FireFaultRepair update = new FireFaultRepair();
        update.setRepairId(repairId);
        update.setRepairUserId(targetUserId);
        update.setRepairPerson(target.getUserName());
        update.setRepairPhone(target.getPhonenumber());
        update.setDispatchBy(current.getLoginName());
        update.setDispatchTime(new Date());
        update.setUpdateBy(current.getLoginName());
        int rows = fireFaultRepairMapper.updateFireFaultRepair(update);
        if (rows <= 0) {
            throw new ServiceException("转派失败，请刷新后重试");
        }
        appendLog(repairId, "transfer",
                displayName(current.getUserName()) + "转派给" + displayName(target.getUserName()),
                current.getUserId(), current.getUserName());
        return rows;
    }

    @Override
    public List<SysUser> selectTransferUsers(Long repairId) {
        SysUser current = requireCurrentUser();
        FireFaultRepair repair = getRequiredRepair(repairId);
        if (repair.getRepairUserId() == null || !repair.getRepairUserId().equals(current.getUserId())) {
            throw new ServiceException("仅当前处理人可查看转派名单");
        }
        if (current.getDeptId() == null) {
            throw new ServiceException("当前账号未归属部门");
        }
        List<SysUser> users = userService.selectActiveRegisteredUserList();
        if (users == null || users.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        Long deptId = current.getDeptId();
        Long selfId = current.getUserId();
        return users.stream()
                .filter(u -> u != null && u.getUserId() != null)
                .filter(u -> !u.getUserId().equals(selfId))
                .filter(u -> deptId.equals(u.getDeptId()))
                .filter(u -> "0".equals(u.getStatus()) && !"2".equals(u.getDelFlag()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<FireFaultRepairLog> selectRepairLogs(Long repairId) {
        getRequiredRepair(repairId);
        try {
            List<FireFaultRepairLog> logs = fireFaultRepairLogMapper.selectLogsByRepairId(repairId);
            return logs != null ? logs : java.util.Collections.emptyList();
        } catch (Exception e) {
            // 未执行 upgrade_repair_self_handle_transfer_log.sql 时表可能不存在，页面仍可打开
            org.slf4j.LoggerFactory.getLogger(getClass()).warn(
                    "select repair logs failed repairId={}, fallback empty: {}", repairId, e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int claimByReporterIfNeeded(Long repairId) {
        SysUser current = requireCurrentUser();
        FireFaultRepair repair = getRequiredRepair(repairId);
        if (!RepairStatus.PENDING.getCode().equals(repair.getRepairStatus())) {
            return 0;
        }
        if (repair.getRepairUserId() != null) {
            return 0;
        }
        if (!current.getUserId().equals(repair.getReporterId())) {
            return 0;
        }
        FireFaultRepair claim = new FireFaultRepair();
        claim.setRepairId(repairId);
        claim.setRepairUserId(current.getUserId());
        claim.setRepairPerson(current.getUserName());
        claim.setRepairPhone(current.getPhonenumber());
        claim.setRepairStatus(RepairStatus.IN_PROGRESS.getCode());
        claim.setDispatchBy(current.getLoginName());
        claim.setDispatchTime(new Date());
        claim.setAcceptTime(new Date());
        claim.setUpdateBy(current.getLoginName());
        int rows = fireFaultRepairMapper.updateFireFaultRepair(claim);
        if (rows > 0) {
            appendLog(repairId, "dispatch",
                    displayName(current.getUserName()) + "自动接单处理",
                    current.getUserId(), current.getUserName());
        }
        return rows;
    }

    private SysUser requireCurrentUser() {
        SysUser current = ShiroUtils.getSysUser();
        if (current == null || current.getUserId() == null) {
            throw new ServiceException("未登录或登录状态已失效");
        }
        return current;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int recallDispatch(Long repairId, String recallBy) {
        FireFaultRepair repair = getRequiredRepair(repairId);
        validateDispatchAuthority(repair);

        if (RepairStatus.COMPLETED.getCode().equals(repair.getRepairStatus())
                || repair.getCompleteTime() != null) {
            throw new ServiceException("该工单已完成，不能撤回。");
        }
        if (RepairStatus.PENDING.getCode().equals(repair.getRepairStatus())
                || repair.getRepairUserId() == null) {
            throw new ServiceException("工单状态已发生变化，请刷新后重试。");
        }
        if (!RepairStatus.IN_PROGRESS.getCode().equals(repair.getRepairStatus())) {
            throw new ServiceException("工单状态已发生变化，请刷新后重试。");
        }
        if (hasStartedWork(repair)) {
            throw new ServiceException("该工单已开始处理，不能撤回。");
        }

        Long previousUserId = repair.getRepairUserId();
        String previousPerson = repair.getRepairPerson();
        String beforeStatus = repair.getRepairStatus();

        int rows = fireFaultRepairMapper.recallFireFaultRepair(repairId, recallBy);
        if (rows <= 0) {
            throw new ServiceException("工单状态已发生变化，请刷新后重试。");
        }

        org.slf4j.LoggerFactory.getLogger(getClass()).info(
                "recallDispatch repairId={}, previousUserId={}, previousPerson={}, operator={}, beforeStatus={}, afterStatus={}",
                repairId, previousUserId, previousPerson, recallBy, beforeStatus,
                RepairStatus.PENDING.getCode());
        SysUser operator = ShiroUtils.getSysUser();
        appendLog(repairId, "recall",
                displayName(operator != null ? operator.getUserName() : recallBy) + "撤回了派发",
                operator != null ? operator.getUserId() : null,
                operator != null ? operator.getUserName() : recallBy);
        return rows;
    }

    /** 是否已产生处理业务数据（开始维修/结果/附件等） */
    private boolean hasStartedWork(FireFaultRepair repair) {
        if (repair == null) {
            return false;
        }
        if (repair.getStartTime() != null) {
            return true;
        }
        if (StringUtils.isNotEmpty(repair.getRepairDescription())) {
            return true;
        }
        if (StringUtils.isNotEmpty(repair.getRepairImages())) {
            return true;
        }
        return false;
    }

    private void fillReporterInfo(FireFaultRepair fireFaultRepair) {
        SysUser currentUser = ShiroUtils.getSysUser();
        if (currentUser == null) {
            return;
        }
        if (fireFaultRepair.getReporterId() == null) {
            fireFaultRepair.setReporterId(currentUser.getUserId());
        }
        if (StringUtils.isEmpty(fireFaultRepair.getReporterName())) {
            fireFaultRepair.setReporterName(currentUser.getUserName());
        }
        if (StringUtils.isEmpty(fireFaultRepair.getReporterPhone())) {
            fireFaultRepair.setReporterPhone(currentUser.getPhonenumber());
        }
    }

    /**
     * 上报人自动成为第一处理人（status=处理中），无需管理员下派。
     */
    private void assignReporterAsHandler(FireFaultRepair fireFaultRepair) {
        if (fireFaultRepair.getRepairUserId() != null) {
            if (StringUtils.isEmpty(fireFaultRepair.getRepairStatus())) {
                fireFaultRepair.setRepairStatus(RepairStatus.IN_PROGRESS.getCode());
            }
            return;
        }
        Long reporterId = fireFaultRepair.getReporterId();
        if (reporterId == null) {
            if (StringUtils.isEmpty(fireFaultRepair.getRepairStatus())) {
                fireFaultRepair.setRepairStatus(RepairStatus.PENDING.getCode());
            }
            return;
        }
        SysUser reporter = userService.selectUserById(reporterId);
        if (reporter == null) {
            SysUser current = ShiroUtils.getSysUser();
            if (current != null && reporterId.equals(current.getUserId())) {
                reporter = current;
            }
        }
        Date now = new Date();
        fireFaultRepair.setRepairUserId(reporterId);
        fireFaultRepair.setRepairPerson(reporter != null ? reporter.getUserName() : fireFaultRepair.getReporterName());
        fireFaultRepair.setRepairPhone(reporter != null ? reporter.getPhonenumber() : fireFaultRepair.getReporterPhone());
        fireFaultRepair.setRepairStatus(RepairStatus.IN_PROGRESS.getCode());
        if (StringUtils.isEmpty(fireFaultRepair.getDispatchBy())) {
            fireFaultRepair.setDispatchBy(ShiroUtils.getLoginName() != null ? ShiroUtils.getLoginName() : "self");
        }
        if (fireFaultRepair.getDispatchTime() == null) {
            fireFaultRepair.setDispatchTime(now);
        }
        if (fireFaultRepair.getAcceptTime() == null) {
            fireFaultRepair.setAcceptTime(now);
        }
    }

    private void appendLog(Long repairId, String actionType, String content, Long operatorId, String operatorName) {
        if (repairId == null || fireFaultRepairLogMapper == null) {
            return;
        }
        try {
            FireFaultRepairLog log = new FireFaultRepairLog();
            log.setRepairId(repairId);
            log.setActionType(actionType);
            log.setActionContent(content);
            log.setOperatorId(operatorId);
            log.setOperatorName(operatorName);
            log.setCreateTime(new Date());
            fireFaultRepairLogMapper.insertFireFaultRepairLog(log);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(getClass()).warn(
                    "append repair log failed repairId={}, action={}", repairId, actionType, e);
        }
    }

    private String displayName(String name) {
        return StringUtils.isNotEmpty(name) ? name : "系统";
    }

    private void fillCompanyInfo(FireFaultRepair fireFaultRepair) {
        if (fireFaultRepair.getCompanyId() == null || StringUtils.isNotEmpty(fireFaultRepair.getCompanyName())) {
            return;
        }
        FireCompany company = companyService.selectFireCompanyById(fireFaultRepair.getCompanyId());
        if (company != null) {
            fireFaultRepair.setCompanyName(company.getCompanyName());
        }
    }

    private FireFaultRepair getRequiredRepair(Long repairId) {
        FireFaultRepair repair = fireFaultRepairMapper.selectFireFaultRepairById(repairId);
        if (repair == null) {
            throw new ServiceException("报修单不存在");
        }
        return repair;
    }

    private void ensurePending(Long repairId) {
        FireFaultRepair repair = getRequiredRepair(repairId);
//        if (!RepairStatus.PENDING.getCode().equals(repair.getRepairStatus())) {
//            throw new ServiceException("只有待处理状态的报修单才能删除");
//        }
    }

    private void ensureNotCompleted(FireFaultRepair repair) {
        if (RepairStatus.COMPLETED.getCode().equals(repair.getRepairStatus())) {
            throw new ServiceException("已完成的报修单不能再次处理");
        }
    }

    private void validateUrgencyLevel(String urgencyLevel) {
        if (StringUtils.isEmpty(urgencyLevel) || !UrgencyLevel.isValid(urgencyLevel)) {
            throw new ServiceException("紧急程度参数无效");
        }
    }

    private void validateDispatchUser(FireFaultRepair repair, Long repairUserId) {
        SysUser repairUser = userService.selectUserById(repairUserId);
        if (repairUser == null || "2".equals(repairUser.getDelFlag())) {
            throw new ServiceException("处理人不存在或已删除");
        }
        if (!"0".equals(repairUser.getStatus())) {
            throw new ServiceException("处理人账号已停用");
        }
    }

    private void validateDispatchAuthority(FireFaultRepair repair) {
        SysUser current = ShiroUtils.getSysUser();
        if (current == null) {
            throw new ServiceException("未登录或登录状态已失效");
        }
        // 超管、系统角色项目负责人：与超管同等派发/指派处理人权限
        if (canDispatchLikeAdmin(current)) {
            return;
        }
        if (repair.getCompanyId() == null) {
            throw new ServiceException("报修单未关联单位，无法派发");
        }
        List<FireUserCompany> companyUsers = companyService
                .selectActiveUserListByCompanyId(repair.getCompanyId());
        boolean allowed = companyUsers != null && companyUsers.stream()
                .anyMatch(item -> current.getUserId().equals(item.getUserId())
                        && ("1".equals(item.getRoleType()) || "2".equals(item.getRoleType())));
        if (!allowed) {
            throw new ServiceException("您无权派发该单位的报修任务");
        }
    }

    /**
     * 超管或系统角色 project_manager 可全局派发（不依赖单位成员表绑定）。
     * 同时检查会话用户角色与 Shiro 授权角色，避免 principal 未带 roles 时误判。
     */
    private boolean canDispatchLikeAdmin(SysUser current) {
        if (current.isAdmin()) {
            return true;
        }
        if (fireDataPermissionService.hasGlobalBizDataScope(current)) {
            return true;
        }
        try {
            return org.apache.shiro.SecurityUtils.getSubject().hasRole("project_manager");
        } catch (Exception e) {
            return false;
        }
    }
}
