package az.sima.simajava.controller;

import az.sima.simajava.dto.sima.requests.CallbackRequestDto;
import az.sima.simajava.dto.sima.responses.GetDataResponseDto;
import az.sima.simajava.service.SimaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.cert.CertificateException;

@Slf4j
@RestController
@RequestMapping("/sima")
@RequiredArgsConstructor
public class SimaController {

    private final SimaService simaService;

    @GetMapping("/getfile")
    public ResponseEntity<Object> getData(
            @RequestParam String tsquery,
            @RequestHeader(name = "ts-cert") String tsCert,
            @RequestHeader(name = "ts-sign-alg") String tsSignAlg,
            @RequestHeader(name = "ts-sign") String tsSign

    ) {
        return simaService.getData(tsquery, tsCert, tsSignAlg, tsSign);
    }


    @PostMapping("/callback")
    public ResponseEntity<Object> callback(
            @RequestHeader(name = "ts-cert") String tsCert,
            @RequestHeader(name = "ts-sign-alg") String tsSignAlg,
            @RequestHeader(name = "ts-sign") String tsSign
    ) {
        return simaService.callback(tsCert, tsSignAlg, tsSign);
    }

    @GetMapping("/appURI")
    public String getSimaAppURI() {
        return simaService.generateAppRedirectUrl();
    }

}
