package com.ruoyi.fire.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.fire.domain.FireContract;

/**
 * 维保合同 Service 接口
 */
public interface IFireContractService {

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
     * 删除合同
     */
    int deleteFireContractByIds(String ids);

    /**
     * 勾选导出合同
     */
    List<FireContract> selectFireContractByIds(String ids);

    /**
     * 查询状态对象池统计
     */
    Map<String, Integer> selectContractPoolStats();

    /**
     * 终止合同（移入未生效对象池）
     */
    int terminateContract(Long contractId, String updateBy);
}
