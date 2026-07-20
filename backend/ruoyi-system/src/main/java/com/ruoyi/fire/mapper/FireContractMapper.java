package com.ruoyi.fire.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.fire.domain.FireContract;

/**
 * 维保合同 Mapper
 */
public interface FireContractMapper {

    /**
     * 查询合同详情
     */
    FireContract selectFireContractById(Long contractId);

    /**
     * 查询合同列表
     */
    List<FireContract> selectFireContractList(FireContract fireContract);

    /**
     * 新增合同
     */
    int insertFireContract(FireContract fireContract);

    /**
     * 修改合同
     */
    int updateFireContract(FireContract fireContract);

    /**
     * 逻辑删除合同
     */
    int deleteFireContractByIds(Long[] contractIds);

    /**
     * 批量查询合同（导出勾选）
     */
    List<FireContract> selectFireContractByIds(Long[] contractIds);

    /**
     * 查询状态对象池统计
     */
    Map<String, Object> selectContractPoolStats();
}
