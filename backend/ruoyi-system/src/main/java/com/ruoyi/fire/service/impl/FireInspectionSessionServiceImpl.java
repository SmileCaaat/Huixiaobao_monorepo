package com.ruoyi.fire.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.fire.domain.FireCompany;
import com.ruoyi.fire.domain.FireInspectionSession;
import com.ruoyi.fire.mapper.FireCompanyMapper;
import com.ruoyi.fire.mapper.FireInspectionSessionMapper;
import com.ruoyi.fire.service.IFireInspectionSessionService;

/**
 * 巡检会话Service实现
 * 
 * @author ruoyi
 */
@Service
public class FireInspectionSessionServiceImpl implements IFireInspectionSessionService {

    @Autowired
    private FireInspectionSessionMapper sessionMapper;

    @Autowired
    private FireCompanyMapper companyMapper;

    @Override
    public List<FireInspectionSession> selectSessionList(FireInspectionSession session) {
        return sessionMapper.selectSessionList(session);
    }

    @Override
    public FireInspectionSession selectSessionById(Long sessionId) {
        return sessionMapper.selectSessionById(sessionId);
    }

    @Override
    public FireInspectionSession getCurrentSession(Long userId) {
        return sessionMapper.selectCurrentSession(userId);
    }

    @Override
    @Transactional
    public FireInspectionSession checkIn(Long userId, String userName, Long companyId,
            Double longitude, Double latitude, String address, String remark) {

        // 1. 查询公司信息
        FireCompany company = companyMapper.selectFireCompanyById(companyId);
        if (company == null) {
            throw new ServiceException("公司不存在");
        }

        // 2. 验证打卡范围（如果公司设置了打卡区域且用户有位置信息）
        if (company.getCheckInLongitude() != null && company.getCheckInLatitude() != null
                && longitude != null && latitude != null) {
            if (!validateCheckInRange(company, longitude, latitude)) {
                double distance = calculateDistance(
                        company.getCheckInLatitude(), company.getCheckInLongitude(),
                        latitude, longitude);
                throw new ServiceException(
                        "不在打卡范围内，距离打卡点" + (int) distance + "米，允许范围" + company.getCheckInRadius() + "米");
            }
        }

        // 3. 检查是否已有未签退的会话
        FireInspectionSession currentSession = sessionMapper.selectCurrentSession(userId);
        if (currentSession != null) {
            if (currentSession.getCompanyId().equals(companyId)) {
                // 同一公司，返回当前会话
                return currentSession;
            } else {
                // 不同公司，需要先签退
                throw new ServiceException("请先从【" + currentSession.getCompanyName() + "】签退后再签到其他公司");
            }
        }

        // 4. 创建新会话
        FireInspectionSession session = new FireInspectionSession();
        session.setUserId(userId);
        session.setUserName(userName);
        session.setCompanyId(companyId);
        session.setCompanyName(company.getCompanyName());
        session.setCheckInTime(new Date());
        session.setCheckInLongitude(longitude);
        session.setCheckInLatitude(latitude);
        session.setCheckInAddress(address);
        session.setCheckInRemark(remark);
        session.setStatus("0"); // 已签到
        session.setSessionDate(new Date());

        sessionMapper.insertSession(session);

        return session;
    }

    @Override
    @Transactional
    public int checkOut(Long userId, Double longitude, Double latitude, String address, String remark) {
        // 查询当前会话
        FireInspectionSession currentSession = sessionMapper.selectCurrentSession(userId);
        if (currentSession == null) {
            throw new ServiceException("当前没有签到的会话");
        }

        // 更新签退信息
        currentSession.setCheckOutTime(new Date());
        currentSession.setCheckOutLongitude(longitude);
        currentSession.setCheckOutLatitude(latitude);
        currentSession.setCheckOutAddress(address);
        currentSession.setCheckOutRemark(remark);
        currentSession.setStatus("1"); // 已签退

        return sessionMapper.updateSession(currentSession);
    }

    @Override
    @Transactional
    public FireInspectionSession switchCompany(Long userId, String userName, Long newCompanyId,
            Double longitude, Double latitude, String address) {

        // 1. 先签退当前会话
        FireInspectionSession currentSession = sessionMapper.selectCurrentSession(userId);
        if (currentSession != null) {
            checkOut(userId, longitude, latitude, address, "切换公司自动签退");
        }

        // 2. 签到新公司
        return checkIn(userId, userName, newCompanyId, longitude, latitude, address, "切换公司");
    }

    @Override
    public boolean validateCheckInRange(FireCompany company, Double longitude, Double latitude) {
        if (company.getCheckInLongitude() == null || company.getCheckInLatitude() == null) {
            // 未设置打卡区域，允许任何位置签到
            return true;
        }

        if (longitude == null || latitude == null) {
            // 用户位置为空，允许签到（无法验证）
            return true;
        }

        double distance = calculateDistance(
                company.getCheckInLatitude(), company.getCheckInLongitude(),
                latitude, longitude);

        int allowedRadius = company.getCheckInRadius() != null ? company.getCheckInRadius() : 500;

        return distance <= allowedRadius;
    }

    @Override
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // 地球半径（米）

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    @Override
    public List<FireInspectionSession> selectSessionHistory(Long userId, Integer limit) {
        return sessionMapper.selectSessionHistory(userId, limit);
    }

    @Override
    public int countTodayCheckIn(Long companyId) {
        return sessionMapper.countTodayCheckIn(companyId);
    }
}
