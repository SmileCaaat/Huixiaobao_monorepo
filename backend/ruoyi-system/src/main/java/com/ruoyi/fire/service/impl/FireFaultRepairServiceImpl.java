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
import com.ruoyi.fire.domain.FireUserCompany;
import com.ruoyi.fire.mapper.FireFaultRepairMapper;
import com.ruoyi.fire.service.IFireCompanyService;
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
    private ISysUserService userService;

    @Autowired
    private IFireCompanyService companyService;

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
        if (StringUtils.isEmpty(fireFaultRepair.getRepairStatus())) {
            fireFaultRepair.setRepairStatus(RepairStatus.PENDING.getCode());
        }
        if (StringUtils.isEmpty(fireFaultRepair.getStatus())) {
            fireFaultRepair.setStatus("0");
        }
        if (fireFaultRepair.getFoundTime() == null) {
            fireFaultRepair.setFoundTime(new Date());
        }

        fillReporterInfo(fireFaultRepair);
        fillCompanyInfo(fireFaultRepair);

        return fireFaultRepairMapper.insertFireFaultRepair(fireFaultRepair);
    }

    @Override
    public int updateFireFaultRepair(FireFaultRepair fireFaultRepair) {
        FireFaultRepair existing = getRequiredRepair(fireFaultRepair.getRepairId());
        if (!RepairStatus.PENDING.getCode().equals(existing.getRepairStatus())) {
            throw new ServiceException("只有待处理状态的报修单才能编辑");
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
    public List<FireUserCompany> selectDispatchUsers(Long repairId) {
        FireFaultRepair repair = getRequiredRepair(repairId);
        validateDispatchAuthority(repair);
        if (repair.getCompanyId() == null) {
            throw new ServiceException("报修单未关联单位，无法加载处理人");
        }
        // 仅返回该公司下：已注册、status=0、del_flag=0 的关联员工（见 selectActiveUserListByCompanyId）
        List<FireUserCompany> users = companyService.selectActiveUserListByCompanyId(repair.getCompanyId());
        return users != null ? users : java.util.Collections.emptyList();
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
        if (repairUser == null || "2".equals(repairUser.getDelFlag())) {
            throw new ServiceException("处理人不存在或已删除");
        }
        if ("1".equals(repairUser.getStatus())) {
            throw new ServiceException("处理人账号已停用");
        }

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
        return fireFaultRepairMapper.updateFireFaultRepair(update);
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
    public int startRepair(Long repairId) {
        FireFaultRepair repair = getRequiredRepair(repairId);
        if (!RepairStatus.IN_PROGRESS.getCode().equals(repair.getRepairStatus())) {
            throw new ServiceException("只有处理中状态的报修单才能开始处理");
        }

        FireFaultRepair update = new FireFaultRepair();
        update.setRepairId(repairId);
        update.setStartTime(new Date());
        update.setUpdateBy(ShiroUtils.getLoginName());
        return fireFaultRepairMapper.updateFireFaultRepair(update);
    }

    @Override
    public int completeRepair(FireFaultRepair fireFaultRepair) {
        FireFaultRepair repair = getRequiredRepair(fireFaultRepair.getRepairId());
        if (!RepairStatus.IN_PROGRESS.getCode().equals(repair.getRepairStatus())) {
            throw new ServiceException("只有处理中状态的报修单才能完成");
        }

        FireFaultRepair update = new FireFaultRepair();
        update.setRepairId(fireFaultRepair.getRepairId());
        update.setRepairStatus(RepairStatus.COMPLETED.getCode());
        if (repair.getStartTime() == null) {
            update.setStartTime(new Date());
        }
        update.setCompleteTime(new Date());
        update.setRepairDescription(fireFaultRepair.getRepairDescription());
        update.setRepairImages(fireFaultRepair.getRepairImages());
        update.setUpdateBy(fireFaultRepair.getUpdateBy());
        return fireFaultRepairMapper.updateFireFaultRepair(update);
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
        if (repair.getCompanyId() == null) {
            throw new ServiceException("报修单未关联单位，无法派发");
        }

        List<FireUserCompany> companyUsers = companyService.selectActiveUserListByCompanyId(repair.getCompanyId());
        if (companyUsers == null || companyUsers.isEmpty()) {
            throw new ServiceException("该单位还没有配置可派发的处理人员");
        }

        boolean matched = companyUsers.stream().anyMatch(item -> repairUserId.equals(item.getUserId()));
        if (!matched) {
            throw new ServiceException("当前用户无权处理该单位工单");
        }
    }

    private void validateDispatchAuthority(FireFaultRepair repair) {
        SysUser current = ShiroUtils.getSysUser();
        if (current == null) {
            throw new ServiceException("未登录或登录状态已失效");
        }
        if (current.isAdmin()) {
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
}
