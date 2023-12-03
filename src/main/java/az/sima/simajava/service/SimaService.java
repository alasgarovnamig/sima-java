package az.sima.simajava.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.security.cert.CertificateException;

public interface SimaService {
    ResponseEntity<Object> getData(String tsQuery, String tsCert, String tsSignAlg, String tsSign);

    ResponseEntity<Object> callback(String tsCert, String tsSignAlg, String tsSign);

    String generateQrUrl();

    String generateAppRedirectUrl();
}
