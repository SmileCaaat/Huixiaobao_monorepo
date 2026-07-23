package com.ruoyi.fire.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.fire.domain.FireCheckIn;

/**
 * 维保签到Service接口
 *
 * @author ruoyi
 */
public interface IFireCheckInService {
    public FireCheckIn selectFireCheckInById(Long checkInId);

    public List<FireCheckIn> selectFireCheckInList(FireCheckIn fireCheckIn);

    public int insertFireCheckIn(FireCheckIn fireCheckIn);

    public int updateFireCheckIn(FireCheckIn fireCheckIn);

    public int deleteFireCheckInByIds(Long[] checkInIds);

    public int deleteFireCheckInById(Long checkInId);

    /**
     * 按任务+人员查询可配对的签到/签退记录
     */
    public List<FireCheckIn> selectPairCheckIns(Long taskId, Long userId, Long excludeId);

    /**
     * 组装详情页签到/签退配对（同 taskId + userId；历史无 taskId 不配对）
     */
    public Map<String, FireCheckIn> resolvePairRecords(FireCheckIn current);

    /**
     * 移动端新增前规范化与校验（强制会话用户，拒绝请求体姓名）
     */
    public void prepareMobileInsert(FireCheckIn checkIn, boolean requireTaskMembership);

    /**
     * 后台新增前规范化与校验（按 userId 回查姓名；校验任务与地址模式）
     */
    public void prepareAdminInsert(FireCheckIn checkIn, String addressMode);

    /**
     * 后台编辑前规范化与校验
     */
    public void prepareAdminUpdate(FireCheckIn checkIn, String addressMode);
}
