package com.ruoyi.fire.mapper;

import java.util.List;
import com.ruoyi.fire.domain.FireInspectionSession;
import org.apache.ibatis.annotations.Param;

/**
 * 巡检会话Mapper接口
 * 
 * @author ruoyi
 */
public interface FireInspectionSessionMapper {

    /**
     * 查询会话列表
     */
    List<FireInspectionSession> selectSessionList(FireInspectionSession session);

    /**
     * 根据ID查询会话
     */
    FireInspectionSession selectSessionById(Long sessionId);

    /**
     * 查询用户当前有效的会话（状态为已签到）
     */
    FireInspectionSession selectCurrentSession(Long userId);

    /**
     * 查询用户今日在某公司的会话
     */
    FireInspectionSession selectTodaySession(@Param("userId") Long userId, @Param("companyId") Long companyId);

    /**
     * 新增会话
     */
    int insertSession(FireInspectionSession session);

    /**
     * 修改会话
     */
    int updateSession(FireInspectionSession session);

    /**
     * 删除会话
     */
    int deleteSessionById(Long sessionId);

    /**
     * 查询用户的会话历史
     */
    List<FireInspectionSession> selectSessionHistory(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 统计某公司今日签到人数
     */
    int countTodayCheckIn(Long companyId);
}
