package com.ruoyi.fire.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.fire.domain.FireCheckIn;
import com.ruoyi.fire.domain.FireCheckInImage;

/**
 * 维保签到Mapper接口
 * 
 * @author ruoyi
 */
public interface FireCheckInMapper {
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
     * 删除签到
     */
    public int deleteFireCheckInById(Long checkInId);

    /**
     * 批量删除签到
     */
    public int deleteFireCheckInByIds(Long[] checkInIds);

    /**
     * 查询签到图片列表
     */
    public List<FireCheckInImage> selectCheckInImages(Long checkInId);

    /**
     * 新增签到图片
     */
    public int insertCheckInImage(FireCheckInImage image);

    /**
     * 删除签到图片
     */
    public int deleteCheckInImagesByCheckInId(Long checkInId);

    /**
     * 查询同一任务同一人员的配对签到/签退记录
     */
    public List<FireCheckIn> selectPairCheckIns(@Param("taskId") Long taskId, @Param("userId") Long userId,
            @Param("excludeId") Long excludeId);

    /**
     * 查询尚未签退的签到记录
     */
    public FireCheckIn selectOpenCheckIn(@Param("taskId") Long taskId, @Param("userId") Long userId);

    /**
     * 查询时间上最近的后续签退
     */
    public FireCheckIn selectNearestCheckOut(@Param("taskId") Long taskId, @Param("userId") Long userId,
            @Param("checkInTime") java.util.Date checkInTime, @Param("excludeId") Long excludeId);

    /**
     * 查询时间上最近的前序签到
     */
    public FireCheckIn selectNearestCheckIn(@Param("taskId") Long taskId, @Param("userId") Long userId,
            @Param("checkInTime") java.util.Date checkInTime, @Param("excludeId") Long excludeId);
}
