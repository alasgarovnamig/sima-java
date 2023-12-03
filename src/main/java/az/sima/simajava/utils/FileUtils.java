package az.sima.simajava.utils;

import az.sima.simajava.utils.enums.FileType;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class FileUtils {
    public static String GenerateFileNameAccordingToLocalDateTime(FileType type) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");
        switch (type) {
            case SIMA_QR:
                return String.format("sima_qr_%s.%s", now.format(formatter), "png");
            default:
                return "";
        }
    }

    public static boolean GenerateQrImage(String data, String filePath, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix bitMatrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, width, height, hints);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.setColor(Color.BLACK);
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (bitMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }
            return ImageIO.write(image, "png", new File(filePath));
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static String FileToBase64(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        byte[] fileContent = Files.readAllBytes(path);
        return Base64.getEncoder().encodeToString(fileContent);
    }

    public static boolean DeleteFile(String filePath){
        Path path = Paths.get(filePath);

        try {
            if (Files.exists(path)) {
                if (!Files.isDirectory(path)) {
                    Files.delete(path);
                    System.out.println("Delete Successfully");
                    return true;
                }
                System.out.println("The path is directory");
                return false;
            } else {
                System.out.println("File or directory does not exist.");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
