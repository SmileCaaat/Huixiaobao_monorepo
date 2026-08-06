package com.ruoyi.fire.service;

import java.util.List;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.fire.domain.FireFaultRepair;

/**
 * 故障报修Service接口
 *
 * @author ruoyi
 */
public interface IFireFaultRepairService {
    /**
     * 查询故障报修
     *
     * @param repairId 故障报修ID
     * @return 故障报修
     */
    FireFaultRepair selectFireFaultRepairById(Long repairId);

    /**
     * 查询故障报修列表
     *
     * @param fireFaultRepair 故障报修
     * @return 故障报修集合
     */
    List<FireFaultRepair> selectFireFaultRepairList(FireFaultRepair fireFaultRepair);

    /**
     * 新增故障报修
     *
     * @param fireFaultRepair 故障报修
     * @return 结果
     */
    int insertFireFaultRepair(FireFaultRepair fireFaultRepair);

    /**
     * 修改故障报修
     *
     * @param fireFaultRepair 故障报修
     * @return 结果
     */
    int updateFireFaultRepair(FireFaultRepair fireFaultRepair);

    /**
     * 批量删除故障报修
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteFireFaultRepairByIds(String ids);

    /**
     * 删除故障报修信息
     *
     * @param repairId 故障报修ID
     * @return 结果
     */
    int deleteFireFaultRepairById(Long repairId);

    /**
     * 派发报修单（指定处理人，供小程序自助受理等场景）
     *
     * @param repairId 报修ID
     * @param repairUserId 处理人用户ID
     * @param dispatchBy 派发人
     * @return 结果
     */
    int dispatchRepair(Long repairId, Long repairUserId, String dispatchBy);

    /**
     * 查询可派发的处理人（全部已注册且正常的系统用户）
     */
    List<SysUser> selectDispatchUsers(Long repairId);

    /**
     * 接受报修
     *
     * @param repairId 报修ID
     * @param repairPerson 维修人员
     * @param repairPhone 维修电话
     * @return 结果
     */
    int acceptRepair(Long repairId, String repairPerson, String repairPhone);

    /**
     * 开始维修
     *
     * @param repairId 报修ID
     * @return 结果
     */
    int startRepair(Long repairId);

    /**
     * 完成维修
     *
     * @param fireFaultRepair 故障报修（包含维修说明和图片）
     * @return 结果
     */
    int completeRepair(FireFaultRepair fireFaultRepair);

    /**
     * 撤回派发（仅已派发且尚未开始处理）
     *
     * @param repairId 报修ID
     * @param recallBy 撤回操作人
     * @return 结果
     */
    int recallDispatch(Long repairId, String recallBy);

    /**
     * 当前处理人转派给同部门同事
     */
    int transferRepair(Long repairId, Long targetUserId);

    /**
     * 可转派的同部门人员（姓名+电话）
     */
    List<SysUser> selectTransferUsers(Long repairId);

    /**
     * 工单日志
     */
    List<com.ruoyi.fire.domain.FireFaultRepairLog> selectRepairLogs(Long repairId);

    /**
     * 上报人对历史待处理单自动接单（不写入开始时间）
     */
    int claimByReporterIfNeeded(Long repairId);

    /**
     * 保存维修进度（说明/图片/维修时间），不完成工单
     */
    int saveRepairProgress(FireFaultRepair fireFaultRepair);
}
