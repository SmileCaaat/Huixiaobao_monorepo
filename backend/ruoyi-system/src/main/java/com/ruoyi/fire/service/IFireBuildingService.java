package com.ruoyi.fire.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.fire.domain.FireBuilding;

/**
 * 建筑信息 服务层
 * 
 * @author ruoyi
 */
public interface IFireBuildingService {
    /**
     * 查询建筑信息列表
     * 
     * @param building 建筑信息
     * @return 建筑信息集合
     */
    public List<FireBuilding> selectBuildingList(FireBuilding building);

    /**
     * 查询所有建筑信息
     * 
     * @return 建筑信息集合
     */
    public List<FireBuilding> selectBuildingAll();

    /**
     * 根据建筑ID查询建筑信息
     * 
     * @param buildingId 建筑ID
     * @return 建筑信息
     */
    public FireBuilding selectBuildingById(Long buildingId);

    /**
     * 校验建筑编码是否唯一
     * 
     * @param building 建筑信息
     * @return 结果
     */
    public boolean checkBuildingCodeUnique(FireBuilding building);

    /**
     * 校验建筑名称是否唯一
     * 
     * @param building 建筑信息
     * @return 结果
     */
    public boolean checkBuildingNameUnique(FireBuilding building);

    /**
     * 新增建筑信息
     * 
     * @param building 建筑信息
     * @return 结果
     */
    public int insertBuilding(FireBuilding building);

    /**
     * 修改建筑信息
     * 
     * @param building 建筑信息
     * @return 结果
     */
    public int updateBuilding(FireBuilding building);

    /**
     * 批量删除建筑信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteBuildingByIds(String ids);

    /**
     * 统计建筑数量
     * 
     * @return 建筑数量
     */
    public int countBuilding();

    /**
     * 按建筑类型统计数量
     * 
     * @return 统计结果
     */
    public List<Map<String, Object>> countBuildingByType();
}
