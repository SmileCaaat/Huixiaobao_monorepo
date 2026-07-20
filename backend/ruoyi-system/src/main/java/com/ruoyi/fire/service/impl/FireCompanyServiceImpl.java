package com.ruoyi.fire.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.fire.domain.FireBuilding;
import com.ruoyi.fire.domain.FireCompany;
import com.ruoyi.fire.domain.FireUserCompany;
import com.ruoyi.fire.mapper.FireBuildingMapper;
import com.ruoyi.fire.mapper.FireCompanyMapper;
import com.ruoyi.fire.service.IFireCompanyService;

/**
 * 巡检公司Service实现
 * 
 * @author ruoyi
 */
@Service
public class FireCompanyServiceImpl implements IFireCompanyService {

    @Autowired
    private FireCompanyMapper companyMapper;

    @Autowired
    private FireBuildingMapper buildingMapper;

    @Override
    public List<FireCompany> selectFireCompanyList(FireCompany fireCompany) {
        return companyMapper.selectFireCompanyList(fireCompany);
    }

    @Override
    public List<FireCompany> selectCustomerManageList(FireCompany fireCompany) {
        return companyMapper.selectCustomerManageList(fireCompany);
    }

    @Override
    public List<FireCompany> selectCustomerManageByIds(Long[] companyIds) {
        return companyMapper.selectCustomerManageByIds(companyIds);
    }

    @Override
    public List<FireCompany> selectCompanyAll() {
        return companyMapper.selectCompanyAll();
    }

    @Override
    public FireCompany selectFireCompanyById(Long companyId) {
        return companyMapper.selectFireCompanyById(companyId);
    }

    @Override
    @Transactional
    public int insertFireCompany(FireCompany fireCompany) {
        fillDefaultFields(fireCompany);
        // 自动生成公司编码
        if (StringUtils.isEmpty(fireCompany.getCompanyCode())) {
            fireCompany.setCompanyCode(generateCompanyCode());
        }
        int rows = companyMapper.insertFireCompany(fireCompany);

        // 保存关联的建筑信息到 fire_building 表
        List<FireBuilding> buildingList = fireCompany.getBuildingList();
        if (buildingList != null && !buildingList.isEmpty()) {
            int buildingCount = buildingMapper.countBuilding();
            for (int i = 0; i < buildingList.size(); i++) {
                FireBuilding b = buildingList.get(i);
                b.setCompanyId(fireCompany.getCompanyId());
                b.setCompanyName(fireCompany.getCompanyName());
                if (StringUtils.isEmpty(b.getBuildingCode())) {
                    b.setBuildingCode(String.format("B%04d", buildingCount + i + 1));
                }
                if (StringUtils.isEmpty(b.getStatus())) {
                    b.setStatus("0");
                }
                b.setDelFlag("0");
                b.setCreateBy(fireCompany.getCreateBy());
            }
            buildingMapper.batchInsertBuildings(buildingList);
        }
        return rows;
    }

    @Override
    @Transactional
    public int updateFireCompany(FireCompany fireCompany) {
        fillDefaultFields(fireCompany);
        int rows = companyMapper.updateFireCompany(fireCompany);

        // 处理关联的建筑列表：全删全增（只对前台有传 buildingList 时处理，保证兼容性）
        List<FireBuilding> buildingList = fireCompany.getBuildingList();
        if (buildingList != null) {
            // 先根据公司ID逻辑删除原有联结建筑
            buildingMapper.deleteBuildingByCompanyId(fireCompany.getCompanyId());
            // 如果列表不为空，则批量重新插入
            if (!buildingList.isEmpty()) {
                int buildingCount = buildingMapper.countBuilding();
                for (int i = 0; i < buildingList.size(); i++) {
                    FireBuilding b = buildingList.get(i);
                    b.setCompanyId(fireCompany.getCompanyId());
                    b.setCompanyName(fireCompany.getCompanyName());
                    if (StringUtils.isEmpty(b.getBuildingCode())) {
                        b.setBuildingCode(String.format("B%04d", buildingCount + i + 1));
                    }
                    if (StringUtils.isEmpty(b.getStatus())) {
                        b.setStatus("0");
                    }
                    b.setDelFlag("0");
                    b.setCreateBy(fireCompany.getUpdateBy() != null ? fireCompany.getUpdateBy() : fireCompany.getCreateBy());
                }
                buildingMapper.batchInsertBuildings(buildingList);
            }
        }
        return rows;
    }

    @Override
    @Transactional
    public int deleteFireCompanyById(Long companyId) {
        // 删除用户公司关联
        companyMapper.deleteUserCompanyByCompanyId(companyId);
        return companyMapper.deleteFireCompanyById(companyId);
    }

    @Override
    @Transactional
    public int deleteFireCompanyByIds(Long[] companyIds) {
        for (Long companyId : companyIds) {
            companyMapper.deleteUserCompanyByCompanyId(companyId);
        }
        return companyMapper.deleteFireCompanyByIds(companyIds);
    }

    @Override
    public boolean checkCompanyCodeUnique(FireCompany fireCompany) {
        Long companyId = fireCompany.getCompanyId() == null ? -1L : fireCompany.getCompanyId();
        FireCompany info = companyMapper.checkCompanyCodeUnique(fireCompany.getCompanyCode());
        if (info != null && !info.getCompanyId().equals(companyId)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean checkCompanyNameUnique(FireCompany fireCompany) {
        Long companyId = fireCompany.getCompanyId() == null ? -1L : fireCompany.getCompanyId();
        FireCompany info = companyMapper.checkCompanyNameUnique(fireCompany.getCompanyName());
        if (info != null && !info.getCompanyId().equals(companyId)) {
            return false;
        }
        return true;
    }

    @Override
    public List<FireCompany> selectCompanyListByUserId(Long userId) {
        return companyMapper.selectCompanyListByUserId(userId);
    }

    @Override
    public List<FireUserCompany> selectUserListByCompanyId(Long companyId) {
        return companyMapper.selectUserListByCompanyId(companyId);
    }

    @Override
    @Transactional
    public int assignUsers(Long companyId, Long[] userIds, String roleType, String createBy) {
        // 先删除原有关联
        companyMapper.deleteUserCompanyByCompanyId(companyId);

        // 批量新增
        if (userIds != null && userIds.length > 0) {
            List<FireUserCompany> list = new ArrayList<>();
            for (Long userId : userIds) {
                FireUserCompany uc = new FireUserCompany();
                uc.setCompanyId(companyId);
                uc.setUserId(userId);
                uc.setRoleType(roleType != null ? roleType : "0");
                uc.setCreateBy(createBy);
                list.add(uc);
            }
            return companyMapper.batchInsertUserCompany(list);
        }
        return 0;
    }

    @Override
    public int cancelUserCompany(Long companyId, Long userId) {
        FireUserCompany uc = new FireUserCompany();
        uc.setCompanyId(companyId);
        uc.setUserId(userId);
        return companyMapper.deleteUserCompany(uc);
    }

    @Override
    public int countCompany() {
        return companyMapper.countCompany();
    }

    /**
     * 生成公司编码
     */
    private String generateCompanyCode() {
        int count = companyMapper.countCompany();
        return String.format("C%04d", count + 1);
    }

    private void fillDefaultFields(FireCompany fireCompany) {
        if (fireCompany == null) {
            return;
        }
        if (StringUtils.isEmpty(fireCompany.getIsKeyUnit())) {
            fireCompany.setIsKeyUnit("0");
        }
        if (StringUtils.isEmpty(fireCompany.getCompanyType())) {
            fireCompany.setCompanyType("00");
        }
        if (StringUtils.isEmpty(fireCompany.getStatus())) {
            fireCompany.setStatus("0");
        }
        if (fireCompany.getCheckInRadius() == null) {
            fireCompany.setCheckInRadius(500);
        }
        if (StringUtils.isEmpty(fireCompany.getSignRequirement())) {
            fireCompany.setSignRequirement("0");
        }
        if (StringUtils.isEmpty(fireCompany.getAutoFireSystem())) {
            fireCompany.setAutoFireSystem("1");
        }
        if (StringUtils.isEmpty(fireCompany.getMaintenanceStandard())) {
            fireCompany.setMaintenanceStandard("《建筑消防设施的维护管理》(GB25201-2010)");
        }
        if (StringUtils.isEmpty(fireCompany.getReportMode())) {
            fireCompany.setReportMode("0");
        }
    }
}
