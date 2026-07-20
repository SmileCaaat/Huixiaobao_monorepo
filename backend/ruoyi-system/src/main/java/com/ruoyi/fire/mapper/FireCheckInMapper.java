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
     * 查询同一用户同一天的配对签到/签退记录
     */
    public List<FireCheckIn> selectPairCheckIns(@Param("userId") Long userId, @Param("checkInDate") String checkInDate,
            @Param("excludeId") Long excludeId);
}
