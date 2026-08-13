package com.paiyucun.util;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * 图片处理工具
 *
 * 发送到 AI 前压缩图片，控制流量与成本。全部在内存中完成，不产生临时文件。
 */
public class ImageUtil {

    /** 发送给 AI 的图片最长边（像素），超过则等比缩小 */
    public static final int DEFAULT_MAX_DIMENSION = 1024;

    /** JPEG 压缩质量 0~1 */
    private static final float JPEG_QUALITY = 0.7f;

    /**
     * 将图片压缩为 JPEG（最长边不超过 maxDimension）。
     *
     * @param input        原始图片字节
     * @param maxDimension 最长边上限
     * @return 压缩后的 JPEG 字节；若图片无法解码（如 WebP）或处理失败则返回 null，由调用方按原图发送
     */
    public static byte[] compressToJpeg(byte[] input, int maxDimension) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(input));
            if (image == null) {
                return null;
            }
            image = scaleDown(image, maxDimension);
            return encodeJpeg(image, JPEG_QUALITY);
        } catch (Exception e) {
            return null;
        }
    }

    /** 等比缩小图片，使最长边不超过 maxDimension */
    private static BufferedImage scaleDown(BufferedImage src, int maxDimension) {
        int w = src.getWidth();
        int h = src.getHeight();
        if (Math.max(w, h) <= maxDimension) {
            return src;
        }
        double scale = (double) maxDimension / Math.max(w, h);
        int newW = Math.max(1, (int) (w * scale));
        int newH = Math.max(1, (int) (h * scale));

        BufferedImage scaled = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, newW, newH, null);
        g.dispose();
        return scaled;
    }

    /** 编码为 JPEG 字节 */
    private static byte[] encodeJpeg(BufferedImage image, float quality) throws Exception {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(bos)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
        return bos.toByteArray();
    }
}
