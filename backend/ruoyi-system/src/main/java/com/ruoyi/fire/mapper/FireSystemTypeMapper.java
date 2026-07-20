package com.ruoyi.fire.mapper;

import java.util.List;
import com.ruoyi.fire.domain.FireSystemType;

/**
 * 消防系统类型配置Mapper接口
 * 
 * @author ruoyi
 */
public interface FireSystemTypeMapper {

    /**
     * 查询消防系统类型
     * 
     * @param typeId 类型ID
     * @return 消防系统类型
     */
    public FireSystemType selectFireSystemTypeById(Long typeId);

    /**
     * 查询消防系统类型列表
     * 
     * @param fireSystemType 消防系统类型
     * @return 消防系统类型集合
     */
    public List<FireSystemType> selectFireSystemTypeList(FireSystemType fireSystemType);

    /**
     * 查询所有正常状态的系统类型
     * 
     * @return 消防系统类型集合
     */
    public List<FireSystemType> selectFireSystemTypeAll();

    /**
     * 新增消防系统类型
     * 
     * @param fireSystemType 消防系统类型
     * @return 结果
     */
    public int insertFireSystemType(FireSystemType fireSystemType);

    /**
     * 修改消防系统类型
     * 
     * @param fireSystemType 消防系统类型
     * @return 结果
     */
    public int updateFireSystemType(FireSystemType fireSystemType);

    /**
     * 删除消防系统类型
     * 
     * @param typeId 类型ID
     * @return 结果
     */
    public int deleteFireSystemTypeById(Long typeId);

    /**
     * 批量删除消防系统类型
     * 
     * @param typeIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteFireSystemTypeByIds(Long[] typeIds);

    /**
     * 校验类型编码是否唯一
     * 
     * @param typeCode 类型编码
     * @return 结果
     */
    public FireSystemType checkTypeCodeUnique(String typeCode);

    /**
     * 校验类型名称是否唯一
     * 
     * @param typeName 类型名称
     * @return 结果
     */
    public FireSystemType checkTypeNameUnique(String typeName);
}
