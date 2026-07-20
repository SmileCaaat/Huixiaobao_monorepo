package com.ruoyi.fire.service;

import java.util.List;
import com.ruoyi.fire.domain.FireMaintenanceRecord;

/**
 * 维保任务检查记录Service接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public interface IFireMaintenanceRecordService {
    /**
     * 查询维保任务检查记录
     * 
     * @param recordId 维保任务检查记录主键
     * @return 维保任务检查记录
     */
    public FireMaintenanceRecord selectFireMaintenanceRecordByRecordId(Long recordId);

    /**
     * 查询维保任务检查记录列表
     * 
     * @param fireMaintenanceRecord 维保任务检查记录
     * @return 维保任务检查记录集合
     */
    public List<FireMaintenanceRecord> selectFireMaintenanceRecordList(FireMaintenanceRecord fireMaintenanceRecord);

    /**
     * 查询一级列表
     * 
     * @param taskId 任务ID
     * @return 维保任务检查记录集合
     */
    public List<FireMaintenanceRecord> selectLevel1List(Long taskId);

    /**
     * 查询二级列表
     * 
     * @param taskId         任务ID
     * @param parentRecordId 父级记录ID
     * @return 维保任务检查记录集合
     */
    public List<FireMaintenanceRecord> selectLevel2List(Long taskId, Long parentRecordId);

    /**
     * 查询三级列表
     * 
     * @param taskId         任务ID
     * @param parentRecordId 父级记录ID
     * @return 维保任务检查记录集合
     */
    public List<FireMaintenanceRecord> selectLevel3List(Long taskId, Long parentRecordId);

    /**
     * 新增维保任务检查记录
     * 
     * @param fireMaintenanceRecord 维保任务检查记录
     * @return 结果
     */
    public int insertFireMaintenanceRecord(FireMaintenanceRecord fireMaintenanceRecord);

    /**
     * 修改维保任务检查记录
     * 
     * @param fireMaintenanceRecord 维保任务检查记录
     * @return 结果
     */
    public int updateFireMaintenanceRecord(FireMaintenanceRecord fireMaintenanceRecord);

    /**
     * 更新检查结果（自动更新任务统计）
     * 
     * @param fireMaintenanceRecord 维保任务检查记录
     * @return 结果
     */
    public int updateCheckResult(FireMaintenanceRecord fireMaintenanceRecord);

    /**
     * 批量删除维保任务检查记录
     * 
     * @param recordIds 需要删除的维保任务检查记录主键集合
     * @return 结果
     */
    public int deleteFireMaintenanceRecordByRecordIds(Long[] recordIds);

    /**
     * 删除维保任务检查记录信息
     * 
     * @param recordId 维保任务检查记录主键
     * @return 结果
     */
    public int deleteFireMaintenanceRecordByRecordId(Long recordId);

    /**
     * 一键完成：将任务的所有未完成检查项标记为正常
     * 
     * @param taskId 任务ID
     * @return 结果
     */
    public int completeAllByTaskId(Long taskId);
}
