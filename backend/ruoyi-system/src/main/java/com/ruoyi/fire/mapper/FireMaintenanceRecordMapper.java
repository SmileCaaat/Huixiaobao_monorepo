package com.ruoyi.fire.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.fire.domain.FireMaintenanceRecord;

/**
 * 维保任务检查记录Mapper接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public interface FireMaintenanceRecordMapper {
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
     * 根据任务ID和层级查询记录
     * 
     * @param taskId 任务ID
     * @param level  层级
     * @return 维保任务检查记录集合
     */
    public List<FireMaintenanceRecord> selectByTaskIdAndLevel(@Param("taskId") Long taskId,
            @Param("level") Integer level);

    /**
     * 根据任务ID、父级记录ID和层级查询记录
     * 
     * @param taskId         任务ID
     * @param parentRecordId 父级记录ID
     * @param level          层级
     * @return 维保任务检查记录集合
     */
    public List<FireMaintenanceRecord> selectByTaskIdAndParentId(@Param("taskId") Long taskId,
            @Param("parentRecordId") Long parentRecordId,
            @Param("level") Integer level);

    /**
     * 查询任务的所有三级记录
     * 
     * @param taskId 任务ID
     * @return 维保任务检查记录集合
     */
    public List<FireMaintenanceRecord> selectLevel3ByTaskId(Long taskId);

    /**
     * 根据任务ID查询所有记录
     * 
     * @param taskId 任务ID
     * @return 维保任务检查记录集合
     */
    public List<FireMaintenanceRecord> selectRecordsByTaskId(Long taskId);

    /**
     * 新增维保任务检查记录
     * 
     * @param fireMaintenanceRecord 维保任务检查记录
     * @return 结果
     */
    public int insertFireMaintenanceRecord(FireMaintenanceRecord fireMaintenanceRecord);

    /**
     * 批量新增维保任务检查记录
     * 
     * @param list 维保任务检查记录集合
     * @return 结果
     */
    public int batchInsertFireMaintenanceRecord(List<FireMaintenanceRecord> list);

    /**
     * 修改维保任务检查记录
     * 
     * @param fireMaintenanceRecord 维保任务检查记录
     * @return 结果
     */
    public int updateFireMaintenanceRecord(FireMaintenanceRecord fireMaintenanceRecord);

    /**
     * 删除维保任务检查记录
     * 
     * @param recordId 维保任务检查记录主键
     * @return 结果
     */
    public int deleteFireMaintenanceRecordByRecordId(Long recordId);

    /**
     * 批量删除维保任务检查记录
     * 
     * @param recordIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteFireMaintenanceRecordByRecordIds(Long[] recordIds);

    /**
     * 根据任务ID删除记录
     * 
     * @param taskId 任务ID
     * @return 结果
     */
    public int deleteFireMaintenanceRecordByTaskId(Long taskId);

    /**
     * 一键完成：将任务的所有未完成三级检查项标记为正常
     * 
     * @param taskId 任务ID
     * @return 结果
     */
    public int completeAllByTaskId(Long taskId);
}
