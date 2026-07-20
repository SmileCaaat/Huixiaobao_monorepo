package com.ruoyi.common.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * 二维码生成工具类
 * 
 * @author ruoyi
 */
public class QrCodeUtils {

    /** 默认二维码宽度 */
    private static final int DEFAULT_WIDTH = 300;

    /** 默认二维码高度 */
    private static final int DEFAULT_HEIGHT = 300;

    /** 默认图片格式 */
    private static final String DEFAULT_FORMAT = "PNG";

    /** 默认字符编码 */
    private static final String DEFAULT_CHARSET = StandardCharsets.UTF_8.name();

    /**
     * 生成二维码图片
     *
     * @param content 二维码内容
     * @return BufferedImage 二维码图片
     */
    public static BufferedImage generateQrCode(String content) {
        return generateQrCode(content, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 生成二维码图片
     *
     * @param content 二维码内容
     * @param width   宽度
     * @param height  高度
     * @return BufferedImage 二维码图片
     */
    public static BufferedImage generateQrCode(String content, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, DEFAULT_CHARSET);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (WriterException e) {
            throw new RuntimeException("生成二维码失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成带样式的二维码图片（圆角、颜色等）
     *
     * @param content         二维码内容
     * @param width           宽度
     * @param height          高度
     * @param foregroundColor 前景色
     * @param backgroundColor 背景色
     * @return BufferedImage 二维码图片
     */
    public static BufferedImage generateStyledQrCode(String content, int width, int height,
            Color foregroundColor, Color backgroundColor) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, DEFAULT_CHARSET);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 2);

            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();

            // 启用抗锯齿
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 填充背景
            graphics.setColor(backgroundColor);
            graphics.fillRect(0, 0, width, height);

            // 绘制二维码
            graphics.setColor(foregroundColor);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (bitMatrix.get(x, y)) {
                        graphics.fillRect(x, y, 1, 1);
                    }
                }
            }

            graphics.dispose();
            return image;
        } catch (WriterException e) {
            throw new RuntimeException("生成二维码失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成带边框和标题的二维码图片
     *
     * @param content  二维码内容
     * @param title    标题文字
     * @param subtitle 副标题文字
     * @return BufferedImage 带边框的二维码图片
     */
    public static BufferedImage generateQrCodeWithBorder(String content, String title, String subtitle) {
        int qrSize = 280;
        int padding = 30;
        int titleHeight = 50;
        int subtitleHeight = 30;
        int totalWidth = qrSize + padding * 2;
        int totalHeight = qrSize + padding * 2 + titleHeight + subtitleHeight;

        BufferedImage qrImage = generateQrCode(content, qrSize, qrSize);
        BufferedImage result = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();

        // 启用抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 填充白色背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, totalWidth, totalHeight);

        // 绘制边框
        g.setColor(new Color(24, 144, 255)); // 蓝色边框
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(5, 5, totalWidth - 10, totalHeight - 10, 15, 15);

        // 获取支持中文的字体
        java.awt.Font chineseFont = getChineseFont(java.awt.Font.BOLD, 16);

        // 绘制标题
        g.setColor(new Color(50, 50, 50));
        g.setFont(chineseFont);
        java.awt.FontMetrics fm = g.getFontMetrics();
        int titleX = (totalWidth - fm.stringWidth(title)) / 2;
        g.drawString(title, titleX, padding + 20);

        // 绘制二维码
        g.drawImage(qrImage, padding, padding + titleHeight, null);

        // 绘制副标题
        g.setColor(new Color(100, 100, 100));
        g.setFont(getChineseFont(java.awt.Font.PLAIN, 12));
        fm = g.getFontMetrics();
        int subtitleX = (totalWidth - fm.stringWidth(subtitle)) / 2;
        g.drawString(subtitle, subtitleX, totalHeight - 15);

        g.dispose();
        return result;
    }

    /**
     * 缓存加载的字体
     */
    private static java.awt.Font cachedFont = null;

    /**
     * 获取支持中文的字体
     * 优先从classpath加载内嵌字体，失败则尝试系统字体
     */
    private static java.awt.Font getChineseFont(int style, int size) {
        // 尝试使用缓存的字体
        if (cachedFont != null) {
            return cachedFont.deriveFont(style, (float) size);
        }
        
        // 尝试从classpath加载内嵌字体
        try {
            java.io.InputStream fontStream = QrCodeUtils.class.getClassLoader()
                .getResourceAsStream("fonts/SimHei.ttf");
            if (fontStream != null) {
                cachedFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, fontStream);
                fontStream.close();
                return cachedFont.deriveFont(style, (float) size);
            }
        } catch (Exception e) {
            // 加载失败，继续尝试系统字体
        }
        
        // 尝试系统中文字体（Windows和Linux兼容）
        String[] fontNames = {
            "Microsoft YaHei",      // Windows
            "SimHei",               // Windows 黑体
            "SimSun",               // Windows 宋体
            "WenQuanYi Micro Hei",  // Linux
            "WenQuanYi Zen Hei",    // Linux
            "Noto Sans CJK SC",     // Linux
            "Droid Sans Fallback",  // Linux/Android
        };
        
        java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] availableFonts = ge.getAvailableFontFamilyNames();
        java.util.Set<String> fontSet = new java.util.HashSet<>(java.util.Arrays.asList(availableFonts));
        
        for (String fontName : fontNames) {
            if (fontSet.contains(fontName)) {
                cachedFont = new java.awt.Font(fontName, style, size);
                return cachedFont;
            }
        }
        
        // 如果都没有，使用默认的SansSerif（中文会乱码）
        return new java.awt.Font("SansSerif", style, size);
    }

    /**
     * 将二维码图片转换为Base64字符串
     *
     * @param content 二维码内容
     * @return Base64编码的图片字符串
     */
    public static String generateQrCodeBase64(String content) {
        return generateQrCodeBase64(content, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 将二维码图片转换为Base64字符串
     *
     * @param content 二维码内容
     * @param width   宽度
     * @param height  高度
     * @return Base64编码的图片字符串
     */
    public static String generateQrCodeBase64(String content, int width, int height) {
        try {
            BufferedImage image = generateQrCode(content, width, height);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, DEFAULT_FORMAT, baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("生成二维码Base64失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将二维码图片保存到文件
     *
     * @param content  二维码内容
     * @param filePath 文件路径
     */
    public static void saveQrCodeToFile(String content, String filePath) {
        saveQrCodeToFile(content, filePath, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 将二维码图片保存到文件
     *
     * @param content  二维码内容
     * @param filePath 文件路径
     * @param width    宽度
     * @param height   高度
     */
    public static void saveQrCodeToFile(String content, String filePath, int width, int height) {
        try {
            BufferedImage image = generateQrCode(content, width, height);
            File file = new File(filePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            ImageIO.write(image, DEFAULT_FORMAT, file);
        } catch (IOException e) {
            throw new RuntimeException("保存二维码到文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将带边框的二维码图片保存到文件
     *
     * @param content  二维码内容
     * @param title    标题
     * @param subtitle 副标题
     * @param filePath 文件路径
     */
    public static void saveQrCodeWithBorderToFile(String content, String title, String subtitle, String filePath) {
        try {
            BufferedImage image = generateQrCodeWithBorder(content, title, subtitle);
            File file = new File(filePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            ImageIO.write(image, DEFAULT_FORMAT, file);
        } catch (IOException e) {
            throw new RuntimeException("保存二维码到文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将BufferedImage转换为字节数组
     *
     * @param image BufferedImage
     * @return 字节数组
     */
    public static byte[] toBytes(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, DEFAULT_FORMAT, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("转换图片失败: " + e.getMessage(), e);
        }
    }
}
