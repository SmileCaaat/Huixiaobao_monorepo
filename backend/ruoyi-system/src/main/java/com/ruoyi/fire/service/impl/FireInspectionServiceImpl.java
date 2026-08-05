package com.ruoyi.fire.service.impl;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.fire.domain.FireBuilding;
import com.ruoyi.fire.domain.FireCompany;
import com.ruoyi.fire.domain.FireInspection;
import com.ruoyi.fire.domain.dto.FireInspectionTemplateCategoryVO;
import com.ruoyi.fire.domain.dto.FireInspectionTemplateEquipmentVO;
import com.ruoyi.fire.mapper.FireInspectionMapper;
import com.ruoyi.fire.service.IFireBuildingService;
import com.ruoyi.fire.service.IFireCompanyService;
import com.ruoyi.fire.service.IFireInspectionService;
import com.ruoyi.fire.service.IFireMaintenanceTemplateCategoryService;

/**
 * 巡检测试Service业务层处理
 */
@Service
public class FireInspectionServiceImpl implements IFireInspectionService {

    @Autowired
    private FireInspectionMapper fireInspectionMapper;

    @Autowired
    private IFireCompanyService companyService;

    @Autowired
    private IFireBuildingService buildingService;

    @Autowired
    private IFireMaintenanceTemplateCategoryService templateCategoryService;

    @Override
    public FireInspection selectFireInspectionById(Long inspectionId) {
        FireInspection inspection = fireInspectionMapper.selectFireInspectionById(inspectionId);
        fillImages(inspection);
        return inspection;
    }

    @Override
    public List<FireInspection> selectFireInspectionList(FireInspection fireInspection) {
        List<FireInspection> list = fireInspectionMapper.selectFireInspectionList(fireInspection);
        for (FireInspection inspection : list) {
            fillImages(inspection);
        }
        return list;
    }

    @Override
    public int insertFireInspection(FireInspection fireInspection) {
        enrichSnapshots(fireInspection, true);
        if (StringUtils.isEmpty(fireInspection.getDelFlag())) {
            fireInspection.setDelFlag("0");
        }
        return fireInspectionMapper.insertFireInspection(fireInspection);
    }

    @Override
    public int updateFireInspection(FireInspection fireInspection) {
        enrichSnapshots(fireInspection, false);
        return fireInspectionMapper.updateFireInspection(fireInspection);
    }

    @Override
    public int deleteFireInspectionByIds(String ids) {
        String[] strIds = ids.split(",");
        Long[] longIds = new Long[strIds.length];
        for (int i = 0; i < strIds.length; i++) {
            longIds[i] = Long.parseLong(strIds[i].trim());
        }
        return fireInspectionMapper.deleteFireInspectionByIds(longIds);
    }

    @Override
    public int deleteFireInspectionById(Long inspectionId) {
        return fireInspectionMapper.deleteFireInspectionById(inspectionId);
    }

    @Override
    public List<FireInspection> selectFireInspectionByCompanyId(Long companyId) {
        List<FireInspection> list = fireInspectionMapper.selectFireInspectionByCompanyId(companyId);
        for (FireInspection inspection : list) {
            fillImages(inspection);
        }
        return list;
    }

    @Override
    public List<FireInspection> selectRecentInspectionTests(Long companyId) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        java.util.Date startTime = cal.getTime();

        List<FireInspection> list = fireInspectionMapper.selectInspectionTestByCompanyIdAndTimeRange(companyId, startTime);
        for (FireInspection inspection : list) {
            fillImages(inspection);
        }
        return list;
    }

    @Override
    public List<FireInspection> selectReportInspectionRecords(
            Long taskId, Long companyId, java.util.Date startTime, java.util.Date endTime) {
        if (taskId == null || companyId == null || startTime == null || endTime == null) {
            return java.util.Collections.emptyList();
        }
        List<FireInspection> list = fireInspectionMapper.selectReportInspectionRecords(
                taskId, companyId, startTime, endTime);
        for (FireInspection inspection : list) {
            fillImages(inspection);
        }
        return list;
    }

    /**
     * 校验并回填公司/建筑/模板类目名称与维保标准快照，不信任前端提交的名称，
     * 也不再关联 fire_system_type.typeId。
     */
    private void enrichSnapshots(FireInspection inspection, boolean requireCompany) {
        if (inspection == null) {
            return;
        }
        if (inspection.getCompanyId() == null) {
            if (requireCompany) {
                throw new ServiceException("请选择单位");
            }
        } else {
            FireCompany company = companyService.selectFireCompanyById(inspection.getCompanyId());
            if (company == null || !"0".equals(StringUtils.nvl(company.getDelFlag(), "0"))) {
                throw new ServiceException("单位不存在或已删除");
            }
            inspection.setCompanyName(company.getCompanyName());
            if (StringUtils.isEmpty(inspection.getMaintenanceStandard())) {
                inspection.setMaintenanceStandard(company.getMaintenanceStandard());
            }
        }

        if (inspection.getBuildingId() != null) {
            FireBuilding building = buildingService.selectBuildingById(inspection.getBuildingId());
            if (building == null) {
                throw new ServiceException("建筑不存在或已删除");
            }
            if (inspection.getCompanyId() != null && building.getCompanyId() != null
                    && !inspection.getCompanyId().equals(building.getCompanyId())) {
                throw new ServiceException("建筑不属于所选单位");
            }
            inspection.setBuildingName(building.getBuildingName());
            if (inspection.getCompanyId() == null) {
                inspection.setCompanyId(building.getCompanyId());
            }
        }

        if (StringUtils.isNotEmpty(inspection.getCategoryKey())) {
            FireInspectionTemplateCategoryVO category =
                    templateCategoryService.findLevel1ByCategoryKey(inspection.getCategoryKey());
            if (category == null) {
                throw new ServiceException("系统名称类目无效");
            }
            inspection.setSystemName(category.getCategoryName());
            inspection.setSystemType(category.getCategoryKey());
            // 明确不写入 fire_system_type 的 typeId
            inspection.setSystemTypeId(null);
        }

        if (StringUtils.isNotEmpty(inspection.getEquipmentKey())) {
            if (StringUtils.isEmpty(inspection.getCategoryKey())) {
                throw new ServiceException("请先选择系统名称");
            }
            FireInspectionTemplateEquipmentVO equipment =
                    templateCategoryService.findEquipment(inspection.getCategoryKey(), inspection.getEquipmentKey());
            if (equipment == null) {
                throw new ServiceException("设备名称不属于所选系统");
            }
            inspection.setEquipmentName(equipment.getEquipmentName());
            inspection.setEquipmentTypeId(null);
        }

        if (inspection.getEquipmentCount() == null || inspection.getEquipmentCount() < 1) {
            inspection.setEquipmentCount(1);
        }
        if (StringUtils.isEmpty(inspection.getEquipmentStatus())) {
            inspection.setEquipmentStatus("0");
        }
    }

    private void fillImages(FireInspection inspection) {
        if (inspection != null && StringUtils.isNotEmpty(inspection.getImageUrls())) {
            inspection.setImages(Arrays.asList(inspection.getImageUrls().split(",")));
        }
    }
}
