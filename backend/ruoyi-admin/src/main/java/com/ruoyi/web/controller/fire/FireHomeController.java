package com.ruoyi.web.controller.fire;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.fire.domain.FireEquipment;
import com.ruoyi.fire.service.IFireBuildingService;
import com.ruoyi.fire.service.IFireCompanyService;
import com.ruoyi.fire.service.IFireEquipmentService;
import com.ruoyi.fire.service.IFireSystemTypeService;

/**
 * 消防首页控制器
 * 
 * @author ruoyi
 */
@Controller
@RequestMapping("/fire")
public class FireHomeController extends BaseController {
    @Autowired
    private IFireBuildingService buildingService;

    @Autowired
    private IFireEquipmentService equipmentService;

    @Autowired
    private IFireCompanyService companyService;

    @Autowired
    private IFireSystemTypeService systemTypeService;

    /**
     * 消防首页
     */
    /**
     * 消防首页
     */
    @GetMapping("/home")
    public String home(ModelMap mmap) {
        return "fire_main";
    }

    /**
     * 获取首页统计数据（AJAX）
     */
    @GetMapping("/stats/home")
    @ResponseBody
    public AjaxResult getHomeStats() {
        AjaxResult result = AjaxResult.success();
        
        // 公司总数
        int companyCount = companyService.countCompany();
        result.put("companyCount", companyCount);
        
        // 建筑总数
        int buildingCount = buildingService.countBuilding();
        result.put("buildingCount", buildingCount);
        
        // 设备总数
        int equipmentCount = equipmentService.countEquipment();
        result.put("equipmentCount", equipmentCount);
        
        // 即将过期设备数（30天内）
        int expiringSoonCount = equipmentService.countExpiringSoon();
        result.put("expiringSoonCount", expiringSoonCount);

        // 有效期内设备
        int inDateCount = equipmentService.countInDate();
        result.put("inDateCount", inDateCount);
        
        // 已过期设备
        int expiredCount = equipmentService.countExpired();
        result.put("expiredCount", expiredCount);

        // 计算百分比
        if (equipmentCount > 0) {
            result.put("inDatePercent", String.format("%.1f", inDateCount * 100.0 / equipmentCount));
            result.put("expiringSoonPercent", String.format("%.1f", expiringSoonCount * 100.0 / equipmentCount));
            result.put("expiredPercent", String.format("%.1f", expiredCount * 100.0 / equipmentCount));
        } else {
            result.put("inDatePercent", "0");
            result.put("expiringSoonPercent", "0");
            result.put("expiredPercent", "0");
        }

        // 即将过期设备列表
        List<FireEquipment> expiringSoonList = equipmentService.selectExpiringSoonList();
        result.put("expiringSoonList", expiringSoonList);
        
        // 已过期设备列表
        List<FireEquipment> expiredList = equipmentService.selectExpiredList();
        result.put("expiredList", expiredList);
        
        return result;
    }
}
