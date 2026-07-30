package com.ruoyi.web.controller.fire;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.fire.domain.FireEquipment;
import com.ruoyi.fire.service.IFireEquipmentService;

/**
 * 公开访问控制器 - 用于手机扫码查看设备信息
 * 无需登录即可访问
 * 
 * @author ruoyi
 */
@Controller
@RequestMapping("/public")
public class PublicEquipmentController {

    @Autowired
    private IFireEquipmentService equipmentService;

    /**
     * 手机扫码查看设备信息页面（精美移动端页面）
     * 
     * @param equipmentCode 设备编码
     * @param mmap          模型
     * @return 移动端设备详情页面
     */
    @GetMapping("/equipment/{equipmentCode}")
    public String viewEquipment(@PathVariable("equipmentCode") String equipmentCode, ModelMap mmap) {
        FireEquipment equipment = equipmentService.selectEquipmentByCode(equipmentCode);
        if (equipment == null) {
            mmap.put("errorMsg", "设备信息不存在或已删除");
            return "public/equipment/error";
        }
        mmap.put("equipment", equipment);
        return "public/equipment/detail";
    }

    /**
     * API接口 - 获取设备详情（JSON格式）
     * 浏览器直接访问旧二维码地址时，兼容跳转到设备信息展示页
     * 
     * @param equipmentCode 设备编码
     * @return 设备信息
     */
    @GetMapping("/api/equipment/{equipmentCode}")
    @ResponseBody
    public ResponseEntity<AjaxResult> getEquipmentInfo(@PathVariable("equipmentCode") String equipmentCode,
            HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains(MediaType.TEXT_HTML_VALUE)) {
            String detailPath = request.getContextPath() + "/public/equipment/" + equipmentCode;
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(detailPath)).build();
        }

        FireEquipment equipment = equipmentService.selectEquipmentByCode(equipmentCode);
        if (equipment == null) {
            return ResponseEntity.ok(AjaxResult.error("设备信息不存在"));
        }
        return ResponseEntity.ok(AjaxResult.success(equipment));
    }
}
