package com.ruoyi.fire.service.impl;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.fire.domain.FireInspection;
import com.ruoyi.fire.mapper.FireInspectionMapper;
import com.ruoyi.fire.service.IFireInspectionService;

/**
 * 巡检登记Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class FireInspectionServiceImpl implements IFireInspectionService {

    @Autowired
    private FireInspectionMapper fireInspectionMapper;

    /**
     * 查询巡检登记
     * 
     * @param inspectionId 巡检ID
     * @return 巡检登记
     */
    @Override
    public FireInspection selectFireInspectionById(Long inspectionId) {
        FireInspection inspection = fireInspectionMapper.selectFireInspectionById(inspectionId);
        if (inspection != null && StringUtils.isNotEmpty(inspection.getImageUrls())) {
            inspection.setImages(Arrays.asList(inspection.getImageUrls().split(",")));
        }
        return inspection;
    }

    /**
     * 查询巡检登记列表
     * 
     * @param fireInspection 巡检登记
     * @return 巡检登记
     */
    @Override
    public List<FireInspection> selectFireInspectionList(FireInspection fireInspection) {
        List<FireInspection> list = fireInspectionMapper.selectFireInspectionList(fireInspection);
        for (FireInspection inspection : list) {
            if (StringUtils.isNotEmpty(inspection.getImageUrls())) {
                inspection.setImages(Arrays.asList(inspection.getImageUrls().split(",")));
            }
        }
        return list;
    }

    /**
     * 新增巡检登记
     * 
     * @param fireInspection 巡检登记
     * @return 结果
     */
    @Override
    public int insertFireInspection(FireInspection fireInspection) {
        return fireInspectionMapper.insertFireInspection(fireInspection);
    }

    /**
     * 修改巡检登记
     * 
     * @param fireInspection 巡检登记
     * @return 结果
     */
    @Override
    public int updateFireInspection(FireInspection fireInspection) {
        return fireInspectionMapper.updateFireInspection(fireInspection);
    }

    /**
     * 删除巡检登记对象
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteFireInspectionByIds(String ids) {
        String[] strIds = ids.split(",");
        Long[] longIds = new Long[strIds.length];
        for (int i = 0; i < strIds.length; i++) {
            longIds[i] = Long.parseLong(strIds[i].trim());
        }
        return fireInspectionMapper.deleteFireInspectionByIds(longIds);
    }

    /**
     * 删除巡检登记信息
     * 
     * @param inspectionId 巡检ID
     * @return 结果
     */
    @Override
    public int deleteFireInspectionById(Long inspectionId) {
        return fireInspectionMapper.deleteFireInspectionById(inspectionId);
    }

    /**
     * 根据公司ID查询巡检列表
     * 
     * @param companyId 公司ID
     * @return 巡检登记集合
     */
    @Override
    public List<FireInspection> selectFireInspectionByCompanyId(Long companyId) {
        List<FireInspection> list = fireInspectionMapper.selectFireInspectionByCompanyId(companyId);
        for (FireInspection inspection : list) {
            if (StringUtils.isNotEmpty(inspection.getImageUrls())) {
                inspection.setImages(Arrays.asList(inspection.getImageUrls().split(",")));
            }
        }
        return list;
    }

    /**
     * 查询公司当月的巡检测试记录
     * 
     * @param companyId 公司ID
     * @return 巡检登记集合
     */
    @Override
    public List<FireInspection> selectRecentInspectionTests(Long companyId) {
        // 计算当月第一天的时间
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        java.util.Date startTime = cal.getTime();
        
        List<FireInspection> list = fireInspectionMapper.selectInspectionTestByCompanyIdAndTimeRange(companyId, startTime);
        // 处理图片URL
        for (FireInspection inspection : list) {
            if (StringUtils.isNotEmpty(inspection.getImageUrls())) {
                inspection.setImages(Arrays.asList(inspection.getImageUrls().split(",")));
            }
        }
        return list;
    }
}
