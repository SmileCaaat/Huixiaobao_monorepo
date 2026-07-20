package com.ruoyi.fire.service;

import java.util.List;
import com.ruoyi.fire.domain.FireInspection;

/**
 * 巡检登记Service接口
 * 
 * @author ruoyi
 */
public interface IFireInspectionService {

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
     * 批量删除巡检登记
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteFireInspectionByIds(String ids);

    /**
     * 删除巡检登记信息
     * 
     * @param inspectionId 巡检ID
     * @return 结果
     */
    public int deleteFireInspectionById(Long inspectionId);

    /**
     * 根据公司ID查询巡检列表
     * 
     * @param companyId 公司ID
     * @return 巡检登记集合
     */
    public List<FireInspection> selectFireInspectionByCompanyId(Long companyId);

    /**
     * 查询公司近一个月的巡检测试记录
     * 
     * @param companyId 公司ID
     * @return 巡检登记集合
     */
    public List<FireInspection> selectRecentInspectionTests(Long companyId);
}
