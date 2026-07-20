package com.ruoyi.fire.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.fire.domain.FireInspection;

/**
 * 巡检登记Mapper接口
 * 
 * @author ruoyi
 */
public interface FireInspectionMapper {

    /**
     * 查询巡检登记
     * 
     * @param inspectionId 巡检ID
     * @return 巡检登记
     */
    public FireInspection selectFireInspectionById(Long inspectionId);

    /**
     * 查询巡检登记列表
     * 
     * @param fireInspection 巡检登记
     * @return 巡检登记集合
     */
    public List<FireInspection> selectFireInspectionList(FireInspection fireInspection);

    /**
     * 新增巡检登记
     * 
     * @param fireInspection 巡检登记
     * @return 结果
     */
    public int insertFireInspection(FireInspection fireInspection);

    /**
     * 修改巡检登记
     * 
     * @param fireInspection 巡检登记
     * @return 结果
     */
    public int updateFireInspection(FireInspection fireInspection);

    /**
     * 删除巡检登记
     * 
     * @param inspectionId 巡检ID
     * @return 结果
     */
    public int deleteFireInspectionById(Long inspectionId);

    /**
     * 批量删除巡检登记
     * 
     * @param inspectionIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteFireInspectionByIds(Long[] inspectionIds);

    /**
     * 根据公司ID查询巡检列表
     * 
     * @param companyId 公司ID
     * @return 巡检登记集合
     */
    public List<FireInspection> selectFireInspectionByCompanyId(Long companyId);

    /**
     * 按公司ID和时间范围查询巡检测试记录
     * 
     * @param companyId 公司ID
     * @param startTime 开始时间
     * @return 巡检登记集合
     */
    public List<FireInspection> selectInspectionTestByCompanyIdAndTimeRange(
        @Param("companyId") Long companyId, 
        @Param("startTime") java.util.Date startTime);
}
