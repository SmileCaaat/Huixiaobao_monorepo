package com.ruoyi.fire.service.impl;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.text.Convert;
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
    @Autowired
    private FireBuildingMapper buildingMapper;

    /**
     * 查询建筑信息列表
     * 
     * @param building 建筑信息
     * @return 建筑信息集合
     */
    @Override
    public List<FireBuilding> selectBuildingList(FireBuilding building) {
        List<FireBuilding> list = buildingMapper.selectBuildingList(building);
        // 为每个建筑添加类别翻译
        for (FireBuilding b : list) {
            b.setBuildingTypeText(convertBuildingType(b.getBuildingType()));
        }
        return list;
    }

    /**
     * 查询所有建筑信息
     * 
     * @return 建筑信息集合
     */
    @Override
    public List<FireBuilding> selectBuildingAll() {
        List<FireBuilding> list = buildingMapper.selectBuildingAll();
        // 为每个建筑添加类别翻译
        for (FireBuilding b : list) {
            b.setBuildingTypeText(convertBuildingType(b.getBuildingType()));
        }
        return list;
    }

    /**
     * 根据建筑ID查询建筑信息
     * 
     * @param buildingId 建筑ID
     * @return 建筑信息
     */
    @Override
    public FireBuilding selectBuildingById(Long buildingId) {
        FireBuilding building = buildingMapper.selectBuildingById(buildingId);
        if (building != null) {
            building.setBuildingTypeText(convertBuildingType(building.getBuildingType()));
        }
        return building;
    }

    /**
     * 校验建筑编码是否唯一
     * 
     * @param building 建筑信息
     * @return 结果
     */
    @Override
    public boolean checkBuildingCodeUnique(FireBuilding building) {
        Long buildingId = StringUtils.isNull(building.getBuildingId()) ? -1L : building.getBuildingId();
        FireBuilding info = buildingMapper.checkBuildingCodeUnique(building.getBuildingCode());
        if (StringUtils.isNotNull(info) && info.getBuildingId().longValue() != buildingId.longValue()) {
            return false;
        }
        return true;
    }

    /**
     * 校验建筑名称是否唯一
     * 
     * @param building 建筑信息
     * @return 结果
     */
    @Override
    public boolean checkBuildingNameUnique(FireBuilding building) {
        Long buildingId = StringUtils.isNull(building.getBuildingId()) ? -1L : building.getBuildingId();
        FireBuilding info = buildingMapper.checkBuildingNameUnique(building.getBuildingName());
        if (StringUtils.isNotNull(info) && info.getBuildingId().longValue() != buildingId.longValue()) {
            return false;
        }
        return true;
    }

    /**
     * 新增建筑信息
     * 
     * @param building 建筑信息
     * @return 结果
     */
    @Override
    public int insertBuilding(FireBuilding building) {
        return buildingMapper.insertBuilding(building);
    }

    /**
     * 修改建筑信息
     * 
     * @param building 建筑信息
     * @return 结果
     */
    @Override
    public int updateBuilding(FireBuilding building) {
        return buildingMapper.updateBuilding(building);
    }

    /**
     * 批量删除建筑信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteBuildingByIds(String ids) {
        return buildingMapper.deleteBuildingByIds(Convert.toLongArray(ids));
    }

    /**
     * 统计建筑数量
     * 
     * @return 建筑数量
     */
    @Override
    public int countBuilding() {
        return buildingMapper.countBuilding();
    }

    /**
     * 按建筑类型统计数量
     * 
     * @return 统计结果
     */
    @Override
    public List<Map<String, Object>> countBuildingByType() {
        return buildingMapper.countBuildingByType();
    }
    
    /**
     * 转换建筑类型编码为中文名称
     * 
     * @param typeCode 类型编码
     * @return 中文名称
     */
    private String convertBuildingType(String typeCode) {
        if (typeCode == null) return "";
        switch (typeCode) {
            case "type1_high_rise_civil": return "一类高层民用建筑";
            case "type2_high_rise_civil": return "二类高层民用建筑";
            case "high_rise_factory": return "高层厂房";
            case "high_rise_warehouse": return "高层库房";
            case "single_multi_civil": return "单、多层民用建筑";
            case "single_multi_factory": return "单、多层厂房";
            case "single_multi_warehouse": return "单、多层库房";
            case "underground": return "地下建筑";
            case "tunnel_culvert": return "隧道、涵洞";
            case "other": return "其他建筑";
            default: return typeCode;
        }
    }
}
