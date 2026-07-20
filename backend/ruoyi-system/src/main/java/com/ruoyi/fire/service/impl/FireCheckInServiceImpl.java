package com.ruoyi.fire.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.fire.mapper.FireCheckInMapper;
import com.ruoyi.fire.domain.FireCheckIn;
import com.ruoyi.fire.domain.FireCheckInImage;
import com.ruoyi.fire.service.IFireCheckInService;

/**
 * 维保签到Service实现
 * 
 * @author ruoyi
 */
@Service
public class FireCheckInServiceImpl implements IFireCheckInService {
    @Autowired
    private FireCheckInMapper fireCheckInMapper;

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
    @Transactional
    public int insertFireCheckIn(FireCheckIn fireCheckIn) {
        int rows = fireCheckInMapper.insertFireCheckIn(fireCheckIn);
        // 插入签到图片
        if (fireCheckIn.getImages() != null && fireCheckIn.getImages().size() > 0) {
            for (FireCheckInImage image : fireCheckIn.getImages()) {
                image.setCheckInId(fireCheckIn.getCheckInId());
                fireCheckInMapper.insertCheckInImage(image);
            }
        }
        return rows;
    }

    @Override
    @Transactional
    public int updateFireCheckIn(FireCheckIn fireCheckIn) {
        // 先删除原有的签到图片
        fireCheckInMapper.deleteCheckInImagesByCheckInId(fireCheckIn.getCheckInId());
        // 插入新的签到图片
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
        // 删除关联的图片
        for (Long checkInId : checkInIds) {
            fireCheckInMapper.deleteCheckInImagesByCheckInId(checkInId);
        }
        return fireCheckInMapper.deleteFireCheckInByIds(checkInIds);
    }

    @Override
    public int deleteFireCheckInById(Long checkInId) {
        // 删除关联的图片
        fireCheckInMapper.deleteCheckInImagesByCheckInId(checkInId);
        return fireCheckInMapper.deleteFireCheckInById(checkInId);
    }

    @Override
    public List<FireCheckIn> selectPairCheckIns(Long userId, String checkInDate, Long excludeId) {
        List<FireCheckIn> list = fireCheckInMapper.selectPairCheckIns(userId, checkInDate, excludeId);
        // 为每条记录加载图片
        for (FireCheckIn checkIn : list) {
            List<FireCheckInImage> images = fireCheckInMapper.selectCheckInImages(checkIn.getCheckInId());
            checkIn.setImages(images);
        }
        return list;
    }
}
