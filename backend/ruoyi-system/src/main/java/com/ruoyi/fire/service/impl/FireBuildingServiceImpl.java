package com.ruoyi.fire.service.impl;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.fire.domain.FireBuilding;
import com.ruoyi.fire.mapper.FireBuildingMapper;
import com.ruoyi.fire.service.IFireBuildingService;

/**
 * 建筑信息 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class FireBuildingServiceImpl implements IFireBuildingService {
    private static final int CODE_GEN_MAX_RETRY = 8;
    private static final Object CODE_LOCK = new Object();

    @Autowired
    private FireBuildingMapper buildingMapper;

    @Override
    public List<FireBuilding> selectBuildingList(FireBuilding building) {
        List<FireBuilding> list = buildingMapper.selectBuildingList(building);
        for (FireBuilding b : list) {
            b.setBuildingTypeText(convertBuildingType(b.getBuildingType()));
        }
        return list;
    }

    @Override
    public List<FireBuilding> selectBuildingAll() {
        List<FireBuilding> list = buildingMapper.selectBuildingAll();
        for (FireBuilding b : list) {
            b.setBuildingTypeText(convertBuildingType(b.getBuildingType()));
        }
        return list;
    }

    @Override
    public FireBuilding selectBuildingById(Long buildingId) {
        FireBuilding building = buildingMapper.selectBuildingById(buildingId);
        if (building != null) {
            building.setBuildingTypeText(convertBuildingType(building.getBuildingType()));
        }
        return building;
    }

    @Override
    public boolean checkBuildingCodeUnique(FireBuilding building) {
        Long buildingId = StringUtils.isNull(building.getBuildingId()) ? -1L : building.getBuildingId();
        FireBuilding info = buildingMapper.checkBuildingCodeUnique(building.getBuildingCode());
        if (StringUtils.isNotNull(info) && info.getBuildingId().longValue() != buildingId.longValue()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean checkBuildingNameUnique(FireBuilding building) {
        if (StringUtils.isNull(building.getCompanyId())) {
            return false;
        }
        FireBuilding info = buildingMapper.checkBuildingNameUnique(building);
        return StringUtils.isNull(info);
    }

    /**
     * 新增建筑：始终由后端生成建筑编码，忽略请求中的 buildingCode。
     */
    @Override
    @Transactional
    public int insertBuilding(FireBuilding building) {
        if (building == null) {
            throw new ServiceException("建筑信息不能为空");
        }
        if (building.getCompanyId() == null) {
            throw new ServiceException("请选择所属客户");
        }
        if (StringUtils.isEmpty(building.getBuildingName())) {
            throw new ServiceException("建筑名称不能为空");
        }
        if (!checkBuildingNameUnique(building)) {
            throw new ServiceException("同一客户下建筑名称不能重复");
        }
        if (StringUtils.isEmpty(building.getStatus())) {
            building.setStatus("0");
        }
        if (StringUtils.isEmpty(building.getDelFlag())) {
            building.setDelFlag("0");
        }

        // 明确清空前端传入编码，防止绕过；在锁内分配并插入，降低并发重复
        building.setBuildingCode(null);
        synchronized (CODE_LOCK) {
            Long maxSeq = buildingMapper.selectMaxBuildingCodeSeq();
            long next = (maxSeq == null ? 0L : maxSeq.longValue()) + 1L;
            for (int attempt = 0; attempt < CODE_GEN_MAX_RETRY; attempt++) {
                String code = formatBuildingCode(next + attempt);
                FireBuilding exists = buildingMapper.checkBuildingCodeUnique(code);
                if (exists != null) {
                    continue;
                }
                building.setBuildingCode(code);
                try {
                    int rows = buildingMapper.insertBuilding(building);
                    if (rows > 0) {
                        return rows;
                    }
                } catch (DuplicateKeyException e) {
                    building.setBuildingCode(null);
                    // 继续尝试下一个序号
                }
            }
        }
        throw new ServiceException("建筑编码生成冲突，请稍后重试");
    }

    /**
     * 修改建筑：强制保留数据库原编码。
     */
    @Override
    @Transactional
    public int updateBuilding(FireBuilding building) {
        if (building == null || building.getBuildingId() == null) {
            throw new ServiceException("建筑不存在");
        }
        FireBuilding db = buildingMapper.selectBuildingById(building.getBuildingId());
        if (db == null) {
            throw new ServiceException("建筑不存在或已被删除");
        }
        // 请求中的 buildingCode 一律忽略，写回库中原值（XML 也不再更新该列）
        building.setBuildingCode(db.getBuildingCode());
        if (building.getCompanyId() == null) {
            building.setCompanyId(db.getCompanyId());
        }
        if (!checkBuildingNameUnique(building)) {
            throw new ServiceException("同一客户下建筑名称不能重复");
        }
        return buildingMapper.updateBuilding(building);
    }

    /**
     * 统一生成建筑编码：B + 至少4位数字；超过4位自然扩展。
     * 仅解析符合 B+数字 的历史编码，忽略纯数字等旧格式。
     */
    @Override
    public String generateNextBuildingCode() {
        synchronized (CODE_LOCK) {
            Long maxSeq = buildingMapper.selectMaxBuildingCodeSeq();
            long next = (maxSeq == null ? 0L : maxSeq.longValue()) + 1L;
            if (next < 1L) {
                next = 1L;
            }
            // 跳过已占用编码（含逻辑删除行）
            for (int i = 0; i < CODE_GEN_MAX_RETRY; i++) {
                String code = formatBuildingCode(next + i);
                if (buildingMapper.checkBuildingCodeUnique(code) == null) {
                    return code;
                }
            }
            return formatBuildingCode(next + CODE_GEN_MAX_RETRY);
        }
    }

    private static String formatBuildingCode(long seq) {
        if (seq < 1L) {
            seq = 1L;
        }
        String num = Long.toString(seq);
        if (num.length() < 4) {
            num = String.format("%04d", seq);
        }
        return "B" + num;
    }

    @Override
    public int deleteBuildingByIds(String ids) {
        return buildingMapper.deleteBuildingByIds(Convert.toLongArray(ids));
    }

    @Override
    public int countBuilding() {
        return buildingMapper.countBuilding();
    }

    @Override
    public List<Map<String, Object>> countBuildingByType() {
        return buildingMapper.countBuildingByType();
    }

    private String convertBuildingType(String typeCode) {
        if (typeCode == null) {
            return "";
        }
        switch (typeCode) {
            case "type1_high_rise_civil":
                return "一类高层民用建筑";
            case "type2_high_rise_civil":
                return "二类高层民用建筑";
            case "high_rise_factory":
                return "高层厂房";
            case "high_rise_warehouse":
                return "高层库房";
            case "single_multi_civil":
                return "单、多层民用建筑";
            case "single_multi_factory":
                return "单、多层厂房";
            case "single_multi_warehouse":
                return "单、多层库房";
            case "underground":
                return "地下建筑";
            case "tunnel_culvert":
                return "隧道、涵洞";
            case "other":
                return "其他建筑";
            default:
                return typeCode;
        }
    }
}
