package com.ruoyi.fire.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.fire.domain.FireContract;
import com.ruoyi.fire.mapper.FireContractMapper;
import com.ruoyi.fire.service.IFireContractService;

/**
 * 维保合同 Service 实现
 */
@Service
public class FireContractServiceImpl implements IFireContractService {

    @Autowired
    private FireContractMapper fireContractMapper;

    @Override
    public FireContract selectFireContractById(Long contractId) {
        return fireContractMapper.selectFireContractById(contractId);
    }

    @Override
    public List<FireContract> selectFireContractList(FireContract fireContract) {
        return fireContractMapper.selectFireContractList(fireContract);
    }

    @Override
    public int insertFireContract(FireContract fireContract) {
        if (fireContract.getTerminateFlag() == null) {
            fireContract.setTerminateFlag("0");
        }
        return fireContractMapper.insertFireContract(fireContract);
    }

    @Override
    public int updateFireContract(FireContract fireContract) {
        return fireContractMapper.updateFireContract(fireContract);
    }

    @Override
    public int deleteFireContractByIds(String ids) {
        Long[] contractIds = Convert.toLongArray(ids);
        if (contractIds == null || contractIds.length == 0) {
            return 0;
        }
        return fireContractMapper.deleteFireContractByIds(contractIds);
    }

    @Override
    public List<FireContract> selectFireContractByIds(String ids) {
        Long[] contractIds = Convert.toLongArray(ids);
        if (contractIds == null || contractIds.length == 0) {
            return java.util.Collections.emptyList();
        }
        return fireContractMapper.selectFireContractByIds(contractIds);
    }

    @Override
    public Map<String, Integer> selectContractPoolStats() {
        Map<String, Object> raw = fireContractMapper.selectContractPoolStats();
        Map<String, Integer> result = new HashMap<>();
        result.put("validCount", toInt(raw.get("validCount")));
        result.put("expiringCount", toInt(raw.get("expiringCount")));
        result.put("expiredCount", toInt(raw.get("expiredCount")));
        result.put("inactiveCount", toInt(raw.get("inactiveCount")));
        return result;
    }

    @Override
    public int terminateContract(Long contractId, String updateBy) {
        FireContract contract = new FireContract();
        contract.setContractId(contractId);
        contract.setTerminateFlag("1");
        contract.setUpdateBy(updateBy);
        return fireContractMapper.updateFireContract(contract);
    }

    private int toInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
