package com.ruoyi.fire.mapper;

import java.util.List;
import com.ruoyi.fire.domain.FireFaultRepair;

/**
 * 故障报修Mapper接口
 * 
 * @author ruoyi
 */
public interface FireFaultRepairMapper {
    /**
     * 查询故障报修
     * 
     * @param repairId 故障报修ID
     * @return 故障报修
     */
    public FireFaultRepair selectFireFaultRepairById(Long repairId);

    /**
     * 查询故障报修列表
     * 
     * @param fireFaultRepair 故障报修
     * @return 故障报修集合
     */
    public List<FireFaultRepair> selectFireFaultRepairList(FireFaultRepair fireFaultRepair);

    /**
     * 新增故障报修
     * 
     * @param fireFaultRepair 故障报修
     * @return 结果
     */
    public int insertFireFaultRepair(FireFaultRepair fireFaultRepair);

    /**
     * 修改故障报修
     * 
     * @param fireFaultRepair 故障报修
     * @return 结果
     */
    public int updateFireFaultRepair(FireFaultRepair fireFaultRepair);

    /**
     * 删除故障报修
     * 
     * @param repairId 故障报修ID
     * @return 结果
     */
    public int deleteFireFaultRepairById(Long repairId);

    /**
     * 批量删除故障报修
     * 
     * @param repairIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteFireFaultRepairByIds(String[] repairIds);

    /**
     * 生成报修单号
     * 
     * @return 报修单号
     */
    public String generateRepairNo();
}
