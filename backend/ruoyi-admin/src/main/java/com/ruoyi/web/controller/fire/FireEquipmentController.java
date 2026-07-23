package com.ruoyi.web.controller.fire;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.common.utils.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.QrCodeUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.fire.domain.FireBuilding;
import com.ruoyi.fire.domain.FireEquipment;
import com.ruoyi.fire.service.IFireBuildingService;
import com.ruoyi.fire.service.IFireEquipmentService;

/**
 * 消防设备操作处理
 * 
 * @author ruoyi
 */
@Controller
@RequestMapping("/fire/equipment")
public class FireEquipmentController extends BaseController {
    private String prefix = "fire/equipment";

    @Autowired
    private IFireEquipmentService equipmentService;

    @Autowired
    private IFireBuildingService buildingService;

    @Value("${ruoyi.addressPath:http://localhost}")
    private String serverAddress;

    @RequiresPermissions("fire:equipment:view")
    @GetMapping()
    public String equipment() {
        return prefix + "/equipment";
    }

    @RequiresPermissions("fire:equipment:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(FireEquipment equipment) {
        startPage();
        List<FireEquipment> list = equipmentService.selectEquipmentList(equipment);
        return getDataTable(list);
    }

    @Log(title = "设备管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions("fire:equipment:export")
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(FireEquipment equipment) {
        List<FireEquipment> list = equipmentService.selectEquipmentList(equipment);
        ExcelUtil<FireEquipment> util = new ExcelUtil<FireEquipment>(FireEquipment.class);
        return util.exportExcel(list, "消防设备数据");
    }

    @RequiresPermissions("fire:equipment:remove")
    @Log(title = "设备管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(equipmentService.deleteEquipmentByIds(ids));
    }

    /**
     * 新增设备
     */
    @RequiresPermissions("fire:equipment:add")
    @GetMapping("/add")
    public String add(ModelMap mmap) {
        mmap.put("systemNames", com.ruoyi.fire.enums.FireEquipmentCategory.allLabels());
        return prefix + "/add";
    }

    /**
     * 新增保存设备
     * 必填字段：equipmentName（设备名称）、location（具体位置）
     * 可选字段：buildingId、floorNo、systemName、projectCategory、manufacturer、
     * expireDate、quantity、model、image
     */
    @RequiresPermissions("fire:equipment:add")
    @Log(title = "设备管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated FireEquipment equipment) {
        // 只在用户输入了设备编码时才验证唯一性
        if (StringUtils.isNotEmpty(equipment.getEquipmentCode())
                && !equipmentService.checkEquipmentCodeUnique(equipment)) {
            return error("新增设备'" + equipment.getEquipmentName() + "'失败，设备编码已存在");
        }
        if (equipment.getBuildingId() != null) {
            if (equipment.getCompanyId() == null) {
                return error("请选择所属客户");
            }
            FireBuilding building = buildingService.selectBuildingById(equipment.getBuildingId());
            if (building == null) {
                return error("所选建筑不存在或已删除");
            }
            if (building.getCompanyId() == null
                    || !building.getCompanyId().equals(equipment.getCompanyId())) {
                return error("所选建筑不属于当前客户，请重新选择");
            }
        }
        equipment.setCreateBy(getLoginName());
        return toAjax(equipmentService.insertEquipment(equipment));
    }

    @Log(title = "设备管理", businessType = BusinessType.IMPORT)
    @RequiresPermissions("fire:equipment:import")
    @PostMapping("/importData")
    @ResponseBody
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception {
        ExcelUtil<FireEquipment> util = new ExcelUtil<FireEquipment>(FireEquipment.class);
        List<FireEquipment> equipmentList = util.importExcel(file.getInputStream());
        String message = equipmentService.importEquipment(equipmentList, updateSupport, getLoginName());
        return AjaxResult.success(message);
    }

    @RequiresPermissions("fire:equipment:view")
    @GetMapping("/importTemplate")
    @ResponseBody
    public AjaxResult importTemplate() {
        ExcelUtil<FireEquipment> util = new ExcelUtil<FireEquipment>(FireEquipment.class);
        return util.importTemplateExcel("消防设备数据");
    }

    /**
     * 修改设备
     */
    @RequiresPermissions("fire:equipment:edit")
    @GetMapping("/edit/{equipmentId}")
    public String edit(@PathVariable("equipmentId") Long equipmentId, ModelMap mmap) {
        mmap.put("equipment", equipmentService.selectEquipmentById(equipmentId));
        mmap.put("buildings", buildingService.selectBuildingAll());
        mmap.put("systemNames", com.ruoyi.fire.enums.FireEquipmentCategory.allLabels());
        return prefix + "/edit";
    }

    /**
     * 修改保存设备
     * 可修改字段：buildingId、floorNo、systemName、projectCategory、equipmentName、
     * manufacturer、expireDate、quantity、location、model、image、
     * equipmentStatus、remark
     */
    @RequiresPermissions("fire:equipment:edit")
    @Log(title = "设备管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated FireEquipment equipment) {
        if (!equipmentService.checkEquipmentCodeUnique(equipment)) {
            return error("修改设备'" + equipment.getEquipmentName() + "'失败，设备编码已存在");
        }
        equipment.setUpdateBy(getLoginName());
        return toAjax(equipmentService.updateEquipment(equipment));
    }

    /**
     * 校验设备编码
     */
    @PostMapping("/checkEquipmentCodeUnique")
    @ResponseBody
    public boolean checkEquipmentCodeUnique(FireEquipment equipment) {
        return equipmentService.checkEquipmentCodeUnique(equipment);
    }

    // ============================== 二维码相关功能 ==============================

    /**
     * 查看设备二维码弹窗
     * 生成设备二维码，二维码内容为：{baseUrl}/public/api/equipment/{equipmentCode}
     * 小程序扫码后可通过 /api/fire/equipment/scan/{equipmentCode} 接口获取设备信息
     */
    @RequiresPermissions("fire:equipment:view")
    @GetMapping("/qrcode/{equipmentId}")
    public String qrcode(@PathVariable("equipmentId") Long equipmentId, ModelMap mmap, HttpServletRequest request) {
        FireEquipment equipment = equipmentService.selectEquipmentById(equipmentId);
        mmap.put("equipment", equipment);

        // 生成二维码访问URL - 指向返回JSON数据的API接口
        String baseUrl = getBaseUrl(request);
        String qrUrl = baseUrl + "/public/api/equipment/" + equipment.getEquipmentCode();
        mmap.put("qrUrl", qrUrl);

        // 生成二维码Base64
        String qrBase64 = QrCodeUtils.generateQrCodeBase64(qrUrl, 200, 200);
        mmap.put("qrBase64", qrBase64);

        return prefix + "/qrcode";
    }

    /**
     * 下载单个设备二维码图片
     * 生成带设备名称和编码的二维码PNG图片
     * 文件名格式：{equipmentCode}_qrcode.png
     */
    @RequiresPermissions("fire:equipment:view")
    @GetMapping("/qrcode/download/{equipmentId}")
    public void downloadQrCode(@PathVariable("equipmentId") Long equipmentId,
            HttpServletRequest request, HttpServletResponse response) {
        try {
            FireEquipment equipment = equipmentService.selectEquipmentById(equipmentId);
            String baseUrl = getBaseUrl(request);
            String qrUrl = baseUrl + "/public/api/equipment/" + equipment.getEquipmentCode();

            // 生成带标题的二维码（包含设备名称和编码）
            BufferedImage qrImage = QrCodeUtils.generateQrCodeWithBorder(
                    qrUrl,
                    equipment.getEquipmentName(),
                    "编码: " + equipment.getEquipmentCode());

            // 设置响应头 - 使用RFC 5987规范的UTF-8编码解决中文乱码
            String fileName = equipment.getEquipmentCode() + "_qrcode.png";
            response.setContentType("image/png");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''"
                            + URLEncoder.encode(fileName, "UTF-8"));

            // 输出图片
            OutputStream out = response.getOutputStream();
            ImageIO.write(qrImage, "PNG", out);
            out.flush();
            out.close();
        } catch (Exception e) {
            logger.error("下载二维码失败", e);
        }
    }

    /**
     * 批量导出二维码（打包成ZIP）
     * 根据筛选条件批量生成设备二维码，打包成ZIP文件下载
     * ZIP文件名格式：qrcode_{timestamp}.zip
     * 每个二维码文件名格式：{equipmentCode}.png
     */
    @Log(title = "设备管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions("fire:equipment:export")
    @GetMapping("/qrcode/batchExport")
    public void batchExportQrCode(FireEquipment equipment, HttpServletRequest request, HttpServletResponse response) {
        try {
            List<FireEquipment> list = equipmentService.selectEquipmentList(equipment);
            if (list == null || list.isEmpty()) {
                return;
            }

            String baseUrl = getBaseUrl(request);

            // 设置响应头 - 使用RFC 5987规范的UTF-8编码
            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String fileName = "qrcode_" + timestamp + ".zip";
            response.setContentType("application/zip");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''"
                            + URLEncoder.encode(fileName, "UTF-8"));

            // 创建ZIP输出流
            ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());

            for (FireEquipment eq : list) {
                String qrUrl = baseUrl + "/public/api/equipment/" + eq.getEquipmentCode();

                // 生成带标题的二维码（包含设备名称和编码）
                BufferedImage qrImage = QrCodeUtils.generateQrCodeWithBorder(
                        qrUrl,
                        eq.getEquipmentName(),
                        "编码: " + eq.getEquipmentCode());

                // 转换为字节数组
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(qrImage, "PNG", baos);
                byte[] imageBytes = baos.toByteArray();

                // 添加到ZIP - 使用设备编码作为文件名
                String entryName = eq.getEquipmentCode() + ".png";
                ZipEntry entry = new ZipEntry(entryName);
                zos.putNextEntry(entry);
                zos.write(imageBytes);
                zos.closeEntry();
            }

            zos.finish();
            zos.close();
        } catch (IOException e) {
            logger.error("批量导出二维码失败", e);
        }
    }

    /**
     * 获取服务器基础URL
     * 优先使用配置的 ruoyi.addressPath，如果未配置则使用请求中的地址
     * 用于生成设备二维码的完整访问URL
     */
    private String getBaseUrl(HttpServletRequest request) {
        // 如果配置了外部访问地址，优先使用配置的地址
        if (StringUtils.isNotEmpty(serverAddress) && !serverAddress.equals("http://localhost")) {
            return serverAddress;
        }

        // 否则使用请求中的地址（适用于局域网访问）
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        if ((scheme.equals("http") && serverPort != 80) ||
                (scheme.equals("https") && serverPort != 443)) {
            url.append(":").append(serverPort);
        }
        url.append(contextPath);

        return url.toString();
    }
}
