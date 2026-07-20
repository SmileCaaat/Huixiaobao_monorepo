package com.ruoyi.fire.service;

import java.util.List;
import com.ruoyi.fire.domain.FireCheckIn;

/**
 * 维保签到Service接口
 * 
 * @author ruoyi
 */
public interface IFireCheckInService {
    /**
     * 查询签到
     */
    public FireCheckIn selectFireCheckInById(Long checkInId);

    /**
     * 查询签到列表
     */
    public List<FireCheckIn> selectFireCheckInList(FireCheckIn fireCheckIn);

    /**
     * 新增签到
     */
    public int insertFireCheckIn(FireCheckIn fireCheckIn);

    /**
     * 修改签到
     */
    public int updateFireCheckIn(FireCheckIn fireCheckIn);

    /**
     * 批量删除签到
     */
    public int deleteFireCheckInByIds(Long[] checkInIds);

    /**
     * 删除签到
     */
    public int deleteFireCheckInById(Long checkInId);

    /**
     * 查询同一用户同一天的配对签到/签退记录
     */
    public List<FireCheckIn> selectPairCheckIns(Long userId, String checkInDate, Long excludeId);
}
