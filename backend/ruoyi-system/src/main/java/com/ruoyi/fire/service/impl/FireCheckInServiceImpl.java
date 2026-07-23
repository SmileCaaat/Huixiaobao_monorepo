package com.ruoyi.fire.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.fire.domain.FireCheckIn;
import com.ruoyi.fire.domain.FireCheckInImage;
import com.ruoyi.fire.domain.FireCompany;
import com.ruoyi.fire.domain.FireMaintenanceTask;
import com.ruoyi.fire.mapper.FireCheckInMapper;
import com.ruoyi.fire.service.IFireCheckInService;
import com.ruoyi.fire.service.IFireCompanyService;
import com.ruoyi.fire.service.IFireMaintenanceTaskService;
import com.ruoyi.system.service.ISysUserService;

/**
 * 维保签到Service实现
 *
 * @author ruoyi
 */
@Service
public class FireCheckInServiceImpl implements IFireCheckInService {
    @Autowired
    private FireCheckInMapper fireCheckInMapper;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private IFireMaintenanceTaskService taskService;

    @Autowired
    private IFireCompanyService companyService;

    @Override
    public FireCheckIn selectFireCheckInById(Long checkInId) {
        FireCheckIn checkIn = fireCheckInMapper.selectFireCheckInById(checkInId);
        if (checkIn != null) {
            List<FireCheckInImage> images = fireCheckInMapper.selectCheckInImages(checkInId);
            checkIn.setImages(images);
        }
        return checkIn;
    }

    @Override
    public List<FireCheckIn> selectFireCheckInList(FireCheckIn fireCheckIn) {
        return fireCheckInMapper.selectFireCheckInList(fireCheckIn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertFireCheckIn(FireCheckIn fireCheckIn) {
        // 事务内再次校验，降低并发重复签到/无签到签退的竞态
        validateCheckInPairFlow(fireCheckIn);
        int rows = fireCheckInMapper.insertFireCheckIn(fireCheckIn);
        if (fireCheckIn.getImages() != null && fireCheckIn.getImages().size() > 0) {
            for (FireCheckInImage image : fireCheckIn.getImages()) {
                image.setCheckInId(fireCheckIn.getCheckInId());
                fireCheckInMapper.insertCheckInImage(image);
            }
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateFireCheckIn(FireCheckIn fireCheckIn) {
        fireCheckInMapper.deleteCheckInImagesByCheckInId(fireCheckIn.getCheckInId());
        if (fireCheckIn.getImages() != null && fireCheckIn.getImages().size() > 0) {
            for (FireCheckInImage image : fireCheckIn.getImages()) {
                image.setCheckInId(fireCheckIn.getCheckInId());
                fireCheckInMapper.insertCheckInImage(image);
            }
        }
        return fireCheckInMapper.updateFireCheckIn(fireCheckIn);
    }

    @Override
    public int deleteFireCheckInByIds(Long[] checkInIds) {
        for (Long checkInId : checkInIds) {
            fireCheckInMapper.deleteCheckInImagesByCheckInId(checkInId);
        }
        return fireCheckInMapper.deleteFireCheckInByIds(checkInIds);
    }

    @Override
    public int deleteFireCheckInById(Long checkInId) {
        fireCheckInMapper.deleteCheckInImagesByCheckInId(checkInId);
        return fireCheckInMapper.deleteFireCheckInById(checkInId);
    }

    @Override
    public List<FireCheckIn> selectPairCheckIns(Long taskId, Long userId, Long excludeId) {
        List<FireCheckIn> list = fireCheckInMapper.selectPairCheckIns(taskId, userId, excludeId);
        for (FireCheckIn checkIn : list) {
            List<FireCheckInImage> images = fireCheckInMapper.selectCheckInImages(checkIn.getCheckInId());
            checkIn.setImages(images);
        }
        return list;
    }

    @Override
    public Map<String, FireCheckIn> resolvePairRecords(FireCheckIn current) {
        Map<String, FireCheckIn> result = new HashMap<>();
        if (current == null) {
            return result;
        }
        FireCheckIn checkInRecord = null;
        FireCheckIn checkOutRecord = null;
        if ("0".equals(current.getCheckInType())) {
            checkInRecord = current;
        } else if ("1".equals(current.getCheckInType())) {
            checkOutRecord = current;
        }

        // 历史未关联任务：不自动配对
        if (current.getTaskId() == null || current.getUserId() == null) {
            result.put("checkInRecord", checkInRecord);
            result.put("checkOutRecord", checkOutRecord);
            return result;
        }

        if ("0".equals(current.getCheckInType())) {
            FireCheckIn pair = fireCheckInMapper.selectNearestCheckOut(
                    current.getTaskId(), current.getUserId(), current.getCheckInTime(), current.getCheckInId());
            if (pair != null) {
                pair.setImages(fireCheckInMapper.selectCheckInImages(pair.getCheckInId()));
                checkOutRecord = pair;
            }
        } else if ("1".equals(current.getCheckInType())) {
            FireCheckIn pair = fireCheckInMapper.selectNearestCheckIn(
                    current.getTaskId(), current.getUserId(), current.getCheckInTime(), current.getCheckInId());
            if (pair != null) {
                pair.setImages(fireCheckInMapper.selectCheckInImages(pair.getCheckInId()));
                checkInRecord = pair;
            }
        }

        result.put("checkInRecord", checkInRecord);
        result.put("checkOutRecord", checkOutRecord);
        return result;
    }

    @Override
    public void prepareMobileInsert(FireCheckIn checkIn, boolean requireTaskMembership) {
        if (checkIn == null) {
            throw new ServiceException("签到数据不能为空");
        }
        // 移动端不信任请求体中的 userId / userName
        Long sessionUserId = ShiroUtils.getUserId();
        checkIn.setUserId(sessionUserId);
        applyAccountUserName(checkIn, sessionUserId);
        checkIn.setCreateBy(ShiroUtils.getLoginName());
        if (checkIn.getCheckInTime() == null) {
            checkIn.setCheckInTime(new Date());
        }
        validateAndBindTask(checkIn, requireTaskMembership ? sessionUserId : null, true);
        validateAddressForMobile(checkIn);
        validateCheckInPairFlow(checkIn);
    }

    @Override
    public void prepareAdminInsert(FireCheckIn checkIn, String addressMode) {
        if (checkIn == null) {
            throw new ServiceException("签到数据不能为空");
        }
        if (checkIn.getUserId() == null) {
            throw new ServiceException("请选择签到人");
        }
        applyAccountUserName(checkIn, checkIn.getUserId());
        checkIn.setCreateBy(ShiroUtils.getLoginName());
        if (checkIn.getCheckInTime() == null) {
            checkIn.setCheckInTime(new Date());
        }
        validateAndBindTask(checkIn, null, false);
        applyAddressMode(checkIn, addressMode);
        validateCheckInPairFlow(checkIn);
    }

    @Override
    public void prepareAdminUpdate(FireCheckIn checkIn, String addressMode) {
        if (checkIn == null || checkIn.getCheckInId() == null) {
            throw new ServiceException("签到ID不能为空");
        }
        if (checkIn.getUserId() == null) {
            throw new ServiceException("请选择签到人");
        }
        applyAccountUserName(checkIn, checkIn.getUserId());
        checkIn.setUpdateBy(ShiroUtils.getLoginName());
        validateAndBindTask(checkIn, null, false);
        applyAddressMode(checkIn, addressMode);
    }

    private void applyAccountUserName(FireCheckIn checkIn, Long userId) {
        SysUser user = userService.selectUserById(userId);
        if (user == null || "2".equals(user.getDelFlag())) {
            throw new ServiceException("签到人不存在或已删除");
        }
        if ("1".equals(user.getStatus())) {
            throw new ServiceException("签到人账号已停用");
        }
        checkIn.setUserId(user.getUserId());
        checkIn.setUserName(user.getUserName());
    }

    private void validateAndBindTask(FireCheckIn checkIn, Long membershipUserId, boolean requireMembership) {
        if (checkIn.getTaskId() == null) {
            throw new ServiceException("请选择维保任务");
        }
        FireMaintenanceTask task = taskService.selectFireMaintenanceTaskByTaskId(checkIn.getTaskId());
        if (task == null) {
            throw new ServiceException("维保任务不存在或已删除");
        }
        if (checkIn.getCompanyId() != null && task.getCompanyId() != null
                && !task.getCompanyId().equals(checkIn.getCompanyId())) {
            throw new ServiceException("任务与所属客户不匹配");
        }
        // 以任务数据回填客户，不信任客户端 companyName
        checkIn.setCompanyId(task.getCompanyId());
        if (StringUtils.isNotEmpty(task.getCompanyName())) {
            checkIn.setCompanyName(task.getCompanyName());
        } else if (task.getCompanyId() != null) {
            FireCompany company = companyService.selectFireCompanyById(task.getCompanyId());
            if (company != null) {
                checkIn.setCompanyName(company.getCompanyName());
            }
        }
        if (requireMembership && membershipUserId != null && !isTaskRelated(task, membershipUserId)
                && !isCurrentUserAdmin()) {
            throw new ServiceException("当前用户不是该任务的负责人、执行人员或操作人员");
        }
    }

    private boolean isCurrentUserAdmin() {
        try {
            SysUser user = ShiroUtils.getSysUser();
            return user != null && user.isAdmin();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTaskRelated(FireMaintenanceTask task, Long userId) {
        if (task == null || userId == null) {
            return false;
        }
        if (userId.equals(task.getManagerId()) || userId.equals(task.getExecutorId())) {
            return true;
        }
        String operatorIds = task.getOperatorIds();
        if (StringUtils.isEmpty(operatorIds)) {
            return false;
        }
        String[] ids = operatorIds.split(",");
        for (String id : ids) {
            if (String.valueOf(userId).equals(id.trim())) {
                return true;
            }
        }
        return false;
    }

    private void applyAddressMode(FireCheckIn checkIn, String addressMode) {
        String mode = StringUtils.isNotEmpty(addressMode) ? addressMode.trim() : checkIn.getAddressMode();
        if (StringUtils.isEmpty(mode)) {
            throw new ServiceException("请选择地址录入方式");
        }
        if (!"auto".equals(mode) && !"manual".equals(mode)) {
            throw new ServiceException("地址录入方式无效");
        }
        checkIn.setAddressMode(mode);
        if ("auto".equals(mode)) {
            String located = StringUtils.isNotEmpty(checkIn.getLocatedAddress())
                    ? checkIn.getLocatedAddress().trim()
                    : (checkIn.getAddress() == null ? "" : checkIn.getAddress().trim());
            if (checkIn.getLongitude() == null || checkIn.getLatitude() == null
                    || StringUtils.isEmpty(located)) {
                throw new ServiceException("请获取当前位置或手动输入签到地址");
            }
            validateCoordinate(checkIn.getLongitude(), checkIn.getLatitude());
            checkIn.setAddress(located);
            checkIn.setManualAddress(null);
        } else {
            String manual = StringUtils.isNotEmpty(checkIn.getManualAddress())
                    ? checkIn.getManualAddress().trim()
                    : (checkIn.getAddress() == null ? "" : checkIn.getAddress().trim());
            if (StringUtils.isEmpty(manual)) {
                throw new ServiceException("请获取当前位置或手动输入签到地址");
            }
            checkIn.setAddress(manual);
            checkIn.setLongitude(null);
            checkIn.setLatitude(null);
            checkIn.setLocatedAddress(null);
        }
    }

    private void validateAddressForMobile(FireCheckIn checkIn) {
        if (StringUtils.isEmpty(checkIn.getAddress()) || checkIn.getAddress().trim().isEmpty()) {
            throw new ServiceException("签到地址不能为空");
        }
        checkIn.setAddress(checkIn.getAddress().trim());
        if (checkIn.getLongitude() != null || checkIn.getLatitude() != null) {
            if (checkIn.getLongitude() == null || checkIn.getLatitude() == null) {
                throw new ServiceException("经纬度不完整");
            }
            validateCoordinate(checkIn.getLongitude(), checkIn.getLatitude());
        }
    }

    private void validateCoordinate(Double longitude, Double latitude) {
        if (longitude < -180 || longitude > 180) {
            throw new ServiceException("经度超出有效范围[-180,180]");
        }
        if (latitude < -90 || latitude > 90) {
            throw new ServiceException("纬度超出有效范围[-90,90]");
        }
    }

    private void validateCheckInPairFlow(FireCheckIn checkIn) {
        String type = checkIn.getCheckInType();
        if (StringUtils.isEmpty(type)) {
            throw new ServiceException("请选择签到类型");
        }
        FireCheckIn open = fireCheckInMapper.selectOpenCheckIn(checkIn.getTaskId(), checkIn.getUserId());
        if ("0".equals(type)) {
            if (open != null) {
                throw new ServiceException("该任务已有未签退的签到记录，不能重复签到");
            }
        } else if ("1".equals(type)) {
            if (open == null) {
                throw new ServiceException("该任务尚未签到，不能签退");
            }
        } else {
            throw new ServiceException("签到类型无效");
        }
    }
}
