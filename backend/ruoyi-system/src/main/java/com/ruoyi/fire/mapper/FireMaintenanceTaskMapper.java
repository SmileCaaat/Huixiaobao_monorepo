package com.ruoyi.fire.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.fire.domain.FireCompany;
import com.ruoyi.fire.domain.FireMaintenanceTask;

/**
 * 维保任务Mapper接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public interface FireMaintenanceTaskMapper 
{
    /**
     * 查询维保任务
     * 
     * @param taskId 维保任务主键
     * @return 维保任务
     */
    public FireMaintenanceTask selectFireMaintenanceTaskByTaskId(Long taskId);

    /**
     * 查询维保任务列表
     * 
     * @param fireMaintenanceTask 维保任务
     * @return 维保任务集合
     */
    public List<FireMaintenanceTask> selectFireMaintenanceTaskList(FireMaintenanceTask fireMaintenanceTask);

    /**
     * 新增维保任务
     * 
     * @param fireMaintenanceTask 维保任务
     * @return 结果
     */
    public int insertFireMaintenanceTask(FireMaintenanceTask fireMaintenanceTask);

    /**
     * 修改维保任务
     * 
     * @param fireMaintenanceTask 维保任务
     * @return 结果
     */
    public int updateFireMaintenanceTask(FireMaintenanceTask fireMaintenanceTask);

    /**
     * 删除维保任务
     * 
     * @param taskId 维保任务主键
     * @return 结果
     */
    public int deleteFireMaintenanceTaskByTaskId(Long taskId);

    /**
     * 批量删除维保任务
     * 
     * @param taskIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteFireMaintenanceTaskByTaskIds(Long[] taskIds);

    /**
     * 查询用户作为负责人或操作员参与的任务所关联的公司列表（去重）
     *
     * @param userId 用户ID
     * @return 公司列表
     */
    public List<FireCompany> selectCompanyListByTaskUserId(@Param("userId") Long userId);
}
