package lab.s2jh.core.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.common.collect.Maps;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QrcodeUtils {

    private static final int BLACK = 0xff000000;
    private static final int RED = 0xffff0000;
    private static final int WHITE = 0xFFFFFFFF;

    /**
     * Generate two-dimensional code
     * @param str
     * @param height
     * @return
     */
    public static BufferedImage createQrcode(String content, Integer height) {
        return createQrcode(content, height, null);
    }

    public static BufferedImage createQrcode(String content, Integer height, String logoPath) {
        if (height == null || height < 100) {
            height = 200;
        }

        int logoHeight = height / 5;

        try {
            Map<EncodeHintType, Object> hints = Maps.newHashMap();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 0);
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, height, height, hints);

            int width = bitMatrix.getWidth();
            BufferedImage image = new BufferedImage(bitMatrix.getWidth(), bitMatrix.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.setRGB(x, y, bitMatrix.get(x, y) ? RED : WHITE);
                }
            }

            if (logoPath == null) {
                return image;
            }

            Graphics2D g = image.createGraphics();

            BufferedImage logo = scale(logoPath, logoHeight, logoHeight, false);

            int widthLogo = logo.getWidth();
            int heightLogo = logo.getHeight();

            // 计算图片放置位置
            int x = (image.getWidth() - widthLogo) / 2;
            int y = (image.getHeight() - logo.getHeight()) / 2;

            //开始绘制图片
            g.drawImage(logo, x, y, widthLogo, heightLogo, null);
            g.drawRoundRect(x, y, widthLogo, heightLogo, 15, 15);
            g.setStroke(new BasicStroke(1));
            g.setColor(Color.white);
            g.drawRect(x, y, widthLogo, heightLogo);

            g.dispose();

            return image;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * The original image is passed by the height and width are scaled to meet the requirements generated image
     * @param SrcImageFile source file address
     * @param Height target height
     * @param Width the width of the target
     * @param HasFiller when the ratio does not need padding : true padding ; false no padding
     * @return
     * @throws IOException
     */
    private static BufferedImage scale(String srcImageFile, int height, int width, boolean hasFiller) throws IOException {
        double ratio = 0;//scaling ratio
        File file = new File(srcImageFile);
        BufferedImage srcImage = ImageIO.read(file);
        Image destImage = srcImage.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);

     // Calculate the ratio
        if (srcImage.getHeight() > height || srcImage.getWidth() > width) {
            if (srcImage.getHeight() > srcImage.getWidth()) {
                ratio = Integer.valueOf(height).doubleValue() / srcImage.getHeight();
            } else {
                ratio = Integer.valueOf(width).doubleValue() / srcImage.getWidth();
            }
        } else {
            ratio = 1;
        }

        AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(ratio, ratio), null);
        destImage = op.filter(srcImage, null);


     // Whether padding
        if (hasFiller) {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics2d = image.createGraphics();
            graphics2d.setColor(Color.white);
            graphics2d.fillRect(0, 0, width, height);
            if (width == destImage.getWidth(null)) {
                graphics2d.drawImage(destImage, 0, (height - destImage.getHeight(null)) / 2, destImage.getWidth(null), destImage.getHeight(null),
                        Color.white, null);
            } else {
                graphics2d.drawImage(destImage, 0, (width - destImage.getWidth(null)) / 2, destImage.getWidth(null), destImage.getHeight(null),
                        Color.white, null);
            }

            graphics2d.dispose();
            destImage = image;
        }

        return (BufferedImage) destImage;
    }

    /**
     * Generate two-dimensional code image
     * @param Content dimensional code content
     * @param Height the height of the two-dimensional code
     * @param File image address
     * @throws IOException
     */
    public static void createQrcodeImage(String content, Integer height, File file) throws IOException {
        BufferedImage image = createQrcode(content, height);
        ImageIO.write(image, "png", file);
    }

    /**
     * Two-dimensional code parsing content
     * @param File image address
     * @return
     */
    public static String decodeQrcode(File file) {
        BufferedImage image;
        try {
            if (file == null || file.exists() == false) {
                throw new Exception(" File not found:" + file.getPath());
            }

            image = ImageIO.read(file);

            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Map<DecodeHintType, String> hints = new HashMap<DecodeHintType, String>();
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
            Result result = new QRCodeReader().decode(bitmap, hints);

            return result.getText();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void main(String[] args) throws Exception {
     

        String srcFile = "d://head-portrait.jpg";
        String destFile = "d://kfcLogo.png";
        String content = "KFC good taste";
        BufferedImage image = createQrcode(content, 600, srcFile);
        ImageIO.write(image, "png", new File(destFile));
        System.out.println("-----Health- success----");

        String result = QrcodeUtils.decodeQrcode(new File(destFile));
        if (content.endsWith(result)) {
            System.out.println("-----Successfully resolved----");
        }

    }
}
