package az.sima.simajava.controller;

import az.sima.simajava.constants.SimaConstants;
import az.sima.simajava.service.SimaService;
import az.sima.simajava.utils.FileUtils;
import az.sima.simajava.utils.enums.FileType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.io.IOException;


@Controller
@RequiredArgsConstructor
public class SimaQRController {
    private final SimaService simaService;

    @GetMapping("/")
    public String getQRCode(Model model) throws IOException {
        String filename = FileUtils.GenerateFileNameAccordingToLocalDateTime(FileType.SIMA_QR);
        String filaPath = String.format("%s/%s", SimaConstants.QR_IMAGE_DIRECTORY, filename);
        String url = simaService.generateQrUrl();
        boolean result = FileUtils.GenerateQrImage(url, filaPath, 350, 350);
        if(!result){
            return "Error";
        }
        String qrBase64 = FileUtils.FileToBase64(filaPath);
        boolean deleteFileResult = FileUtils.DeleteFile(filaPath);
        if (!deleteFileResult){
            System.out.println(String.format("URI: %s file can not delete !!!!!",filaPath));
        }

        model.addAttribute("qrcode", qrBase64);
        return "QR_Code";

    }
}
