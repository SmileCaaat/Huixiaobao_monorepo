package com.ruoyi.fire.service;

import java.util.List;
import com.ruoyi.fire.domain.FireInspectionSession;
import com.ruoyi.fire.domain.FireCompany;

/**
 * 巡检会话Service接口
 * 
 * @author ruoyi
 */
public interface IFireInspectionSessionService {

    /**
     * 查询会话列表
     */
    List<FireInspectionSession> selectSessionList(FireInspectionSession session);

    /**
     * 根据ID查询会话
     */
    FireInspectionSession selectSessionById(Long sessionId);

    /**
     * 获取用户当前有效的会话
     */
    FireInspectionSession getCurrentSession(Long userId);

    /**
     * 签到
     * 
     * @param userId    用户ID
     * @param userName  用户名
     * @param companyId 公司ID
     * @param longitude 经度
     * @param latitude  纬度
     * @param address   地址
     * @param remark    备注
     * @return 签到结果
     */
    FireInspectionSession checkIn(Long userId, String userName, Long companyId,
            Double longitude, Double latitude, String address, String remark);

    /**
     * 签退
     * 
     * @param userId    用户ID
     * @param longitude 经度
     * @param latitude  纬度
     * @param address   地址
     * @param remark    备注
     * @return 操作结果
     */
    int checkOut(Long userId, Double longitude, Double latitude, String address, String remark);

    /**
     * 切换公司（先签退再签到）
     */
    FireInspectionSession switchCompany(Long userId, String userName, Long newCompanyId,
            Double longitude, Double latitude, String address);

    /**
     * 验证是否在打卡范围内
     */
    boolean validateCheckInRange(FireCompany company, Double longitude, Double latitude);

    /**
     * 计算两点间距离（米）
     */
    double calculateDistance(double lat1, double lon1, double lat2, double lon2);

    /**
     * 查询用户会话历史
     */
    List<FireInspectionSession> selectSessionHistory(Long userId, Integer limit);

    /**
     * 统计某公司今日签到人数
     */
    int countTodayCheckIn(Long companyId);
}
