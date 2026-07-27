package com.ruoyi.fire.service;

import java.util.List;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.fire.domain.FireCheckIn;
import com.ruoyi.fire.domain.FireFaultRepair;
import com.ruoyi.fire.domain.FireMaintenanceTask;
import com.ruoyi.fire.domain.FireUserCompany;
import com.ruoyi.system.domain.FireReportRecord;

/**
 * 消防业务统一数据权限
 */
public interface IFireDataPermissionService {

    /**
     * �Ƿ�߱�ȫ��ҵ�����ݷ�Χ����������Ա����Ŀ�����ˣ���
     * ������ϵͳ��������Ա����Ȩ�ޡ�
     */
    boolean hasGlobalBizDataScope(SysUser user);

    /**
     * @deprecated ʹ�� {@link #hasGlobalBizDataScope(SysUser)}��fire_dept_admin ��ͣ��
     */
    @Deprecated
    default boolean isFireDeptAdmin(SysUser user) {
        return hasGlobalBizDataScope(user);
    }

    boolean canAccessCompany(Long userId, Long companyId);

    boolean canAccessCompany(SysUser user, Long companyId);

    boolean canManageCompanyMembers(Long userId, Long companyId);

    boolean canManageCompanyMembers(SysUser user, Long companyId);

    boolean canAccessTask(Long userId, FireMaintenanceTask task);

    boolean canAccessTask(SysUser user, FireMaintenanceTask task);

    boolean canDispatchTask(Long userId, FireMaintenanceTask task);

    boolean canDispatchTask(SysUser user, FireMaintenanceTask task);

    boolean canAccessRepair(Long userId, FireFaultRepair repair);

    boolean canAccessRepair(SysUser user, FireFaultRepair repair);

    /**
     * 处理权限（开始处理 / 完成）：仅当前派发处理人，不可用查看权限冒用。
     */
    boolean canProcessRepair(SysUser user, FireFaultRepair repair);

    void assertCanProcessRepair(SysUser user, FireFaultRepair repair);

    /**
     * 是否员工工作台视图（非超管、非系统项目经理、无派发权限）。
     */
    boolean isRepairEmployeeWorkbench(SysUser user);

    /**
     * 清洗客户端越权参数后注入列表范围，并按白名单工作台分类过滤。
     *
     * @param workbenchCategory assignedPending|processing|completed|reported，管理视图可忽略
     */
    void prepareRepairListQuery(FireFaultRepair query, SysUser user, String workbenchCategory);

    boolean canAccessReport(Long userId, FireReportRecord report);

    boolean canAccessReport(SysUser user, FireReportRecord report);

    void assertCanAccessCompany(Long userId, Long companyId);

    void assertCanAccessCompany(SysUser user, Long companyId);

    void assertCanAccessTask(Long userId, FireMaintenanceTask task);

    void assertCanAccessTask(SysUser user, FireMaintenanceTask task);

    void assertCanDispatchTask(Long userId, FireMaintenanceTask task);

    void assertCanDispatchTask(SysUser user, FireMaintenanceTask task);

    void assertCanAccessRepair(Long userId, FireFaultRepair repair);

    void assertCanAccessRepair(SysUser user, FireFaultRepair repair);

    void assertCanAccessReport(Long userId, FireReportRecord report);

    void assertCanAccessReport(SysUser user, FireReportRecord report);

    /** \u4e3a\u4efb\u52a1\u5217\u8868\u67e5\u8be2\u6ce8\u5165\u53ef\u89c1\u8303\u56f4\uff08\u975e\u7ba1\u7406\u5458\uff09 */
    void applyTaskListScope(FireMaintenanceTask query, SysUser user);

    /** \u4e3a\u62a5\u4fee\u5217\u8868\u67e5\u8be2\u6ce8\u5165\u53ef\u89c1\u8303\u56f4\uff08\u975e\u7ba1\u7406\u5458\uff09 */
    void applyRepairListScope(FireFaultRepair query, SysUser user);

    /** 为报告列表查询注入可见范围（非管理员） */
    void applyReportListScope(FireReportRecord query, SysUser user);

    /** 为签到列表/导出查询注入可见范围（非管理员） */
    void applyCheckInListScope(FireCheckIn query, SysUser user);

    boolean canAccessCheckIn(Long userId, FireCheckIn checkIn);

    boolean canAccessCheckIn(SysUser user, FireCheckIn checkIn);

    void assertCanAccessCheckIn(Long userId, FireCheckIn checkIn);

    void assertCanAccessCheckIn(SysUser user, FireCheckIn checkIn);

    FireUserCompany getMembership(Long userId, Long companyId);

    List<Long> listAccessibleCompanyIds(Long userId);

    /**
     * 当前用户负责的项目/客户 ID：fire_user_company 负责人角色 ∪ 任务 manager_id 负责客户
     */
    List<Long> listLeadCompanyIds(Long userId);

    List<Long> listLeadCompanyIds(SysUser user);
}
