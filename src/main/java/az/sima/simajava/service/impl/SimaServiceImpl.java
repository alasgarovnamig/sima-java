package az.sima.simajava.service.impl;

import az.sima.simajava.utils.HeaderDeserializer;
import az.sima.simajava.constants.SimaConstants;
import az.sima.simajava.dto.sima.SimaPersonalData;
import az.sima.simajava.dto.sima.requests.CallbackRequestDto;
import az.sima.simajava.dto.sima.responses.CallbackResponseDto;
import az.sima.simajava.dto.sima.responses.GetDataErrorResponseDto;
import az.sima.simajava.dto.sima.responses.GetDataResponseDto;
import az.sima.simajava.dto.sima.simaContract.*;
import az.sima.simajava.dto.sima.simaContract.enums.OperationType;
import az.sima.simajava.service.SimaService;
import com.google.gson.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SimaServiceImpl implements SimaService {

    private final HttpServletRequest httpServletRequest;
//    private final Gson gson;

    @Override
    public ResponseEntity<Object> getData(String tsQuery, String tsCert, String tsSignAlg, String tsSign) {
        // Request convert to byte array
        byte[] requestBytes = getRequestPathAndQueryStringAsBytes();
        // Request Validation
        if (!tsCertValidation(tsCert, tsSign, requestBytes))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GetDataErrorResponseDto.builder()
                    .errorMessage("This request has not been addressed by the Sima application")
                    .build());
        // tsQuery convert to Sima Contract Object
        Contract simaContract = tsQueryConvertToContract(tsQuery);

        // For Auth
        if (simaContract.getSignableContainer().getOperationInfo().getType().equals("Auth")) {
            return ResponseEntity.ok(GetDataResponseDto.builder()
                    .fileName(SimaConstants.AUTH_FILENAME) //Must be Constant file name in Auth
                    .data(generateUUIDAsBase64ForAuth()) // Generated UUID As Base64 For Auth
                    .build());
        }

        // Actions to be taken in accordance with the contract object

        // For Sign
        return ResponseEntity.ok(GetDataResponseDto.builder()
                .fileName(SimaConstants.DUMMY_FILENAME) // For Sign Document Name
                .data(SimaConstants.DUMMY_FILE_BASE64) // For Sign Document as Base64
                .build());
    }

    @Override
    public ResponseEntity<Object> callback(String tsCert, String tsSignAlg, String tsSign){
        try {
            // Request body convert to String
            String body = getRequestBodyAsString(httpServletRequest);
            // Request Validation
            if (!tsCertValidation(tsCert, tsSign, body.getBytes(StandardCharsets.UTF_8)))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GetDataErrorResponseDto.builder()
                        .errorMessage("This request has not been addressed by the Sima application")
                        .build());
            // Request Body
            CallbackRequestDto dto = new Gson().fromJson(body, CallbackRequestDto.class);
            // Sign Document User Information
            SimaPersonalData signDocumentUserInformation= certToPersonalData(tsCert);
            return ResponseEntity.ok(CallbackResponseDto.builder().status("success").build());
        }catch (Exception ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CallbackResponseDto.builder().status("failed").build());
        }
    }

    @Override
    public String generateQrUrl() {
        String operationId = "10000000000000000000000000001";
		OperationType operationType = OperationType.AUTH;
		Contract contract = createContract(operationId,operationType);
//		Gson gson = new Gson();
//		String contractJsonString = gson.toJson(contract).trim();
		byte[] signature = createSignature(contract.getSignableContainer());
		contract.getHeader().setSignature(signature);
//		String encodedContract= encodeContract(contract);
        return String.format("%s%s",SimaConstants.QR_URI_PREFIX,encodeContract(contract));
    }

    @Override
    public String generateAppRedirectUrl() {
        String operationId = "10000000000000000000000000001";
        OperationType operationType = OperationType.AUTH;
        Contract contract = createContract(operationId,operationType);
//        Gson gson = new Gson();
//        String contractJsonString = gson.toJson(contract).trim();
        byte[] signature = createSignature(contract.getSignableContainer());
        contract.getHeader().setSignature(signature);
//        String encodedContract= encodeContract(contract);
        return String.format("%s%s",SimaConstants.APP_URI_PREFIX,encodeContract(contract));
    }


    private byte[] getRequestPathAndQueryStringAsBytes() {
        String requestPathAndQuery = String.format("%s?%s",
                httpServletRequest.getRequestURI(),
                httpServletRequest.getQueryString());
        return requestPathAndQuery.getBytes(StandardCharsets.UTF_8);
    }

    private String getRequestBodyAsString(HttpServletRequest request) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }

    private boolean tsCertValidation(String tsCert, String tsSign, byte[] processBuffer) {
        try {
            X509Certificate cert = parseX509Certificate(Base64.getDecoder().decode(tsCert));
            Signature signer = Signature.getInstance("SHA256withECDSA");
            signer.initVerify(cert.getPublicKey());
            signer.update(processBuffer);
            return signer.verify(Base64.getDecoder().decode(tsSign));
        } catch (Exception e) {
            e.printStackTrace(); // Handle the exception according to your needs
            return false;
        }
    }

    private X509Certificate parseX509Certificate(byte[] certBytes) throws CertificateException {
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certBytes));
    }

    private Contract tsQueryConvertToContract(String tsQuery) {
        byte[] byteArray = Base64.getDecoder().decode(tsQuery);
        String json = new String(byteArray, StandardCharsets.UTF_8);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Header.class, new HeaderDeserializer());
        Gson gson = gsonBuilder.create();
        return gson.fromJson(json, Contract.class);
    }

    private String generateUUIDAsBase64ForAuth() {
        // Generate UUID
        UUID uuid = UUID.randomUUID();

        // Convert UUID to bytes
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());

        // Encode bytes to base64 and return
        return Base64.getEncoder().encodeToString(byteBuffer.array());
    }

    public SimaPersonalData certToPersonalData(String cert) throws CertificateException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        byte[] certBytes = Base64.getDecoder().decode(cert);
        Certificate certificate = certificateFactory.generateCertificate(new ByteArrayInputStream(certBytes));
        String subject = ((X509Certificate) certificate).getSubjectDN().getName();
        return SimaPersonalData.builder()
                .fin(extractValueFromSubject(subject, "SERIALNUMBER"))
                .name(extractValueFromSubject(subject, "GIVENNAME"))
                .surname(extractValueFromSubject(subject, "SURNAME"))
                .fullName(extractValueFromSubject(subject, "CN"))
                .country(extractValueFromSubject(subject, "C"))
                .build();
    }

    private String extractValueFromSubject(String input, String fieldName) {
        String regex = fieldName + "=([^,]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1).trim();
        } else {
            return "N/A";
        }
    }
    private  Contract createContract(String operationId, OperationType operationType) {
        return Contract.builder()
                .Header(Header.builder()
                        .AlgorithmName("HmacSHA256")
                        .build())
                .SignableContainer(SignableContainer.builder()
                        .ProtoInfo(ProtoInfo.builder()
                                .Name("web2app")
                                .Version("1.3")
                                .build())
                        .OperationInfo(OperationInfo.builder()
                                .OperationId(operationId)
                                .Type(operationType.equals(OperationType.AUTH) ? "Auth" : "Sign")
                                .NbfUTC(System.currentTimeMillis())
                                .ExpUTC(System.currentTimeMillis() + (200 * 60))
                                .Assignee(new ArrayList<>())
                                .build())
                        .ClientInfo(ClientInfo.builder()
                                .ClientId(1)
                                .ClientName("ScanMe APP")
                                .IconURI("Icon Pulic URL")
                                .Callback("callbackURL")
                                .RedirectURI("redirectionURL")
                                .HostName(null)
                                .build())
                        .DataInfo(DataInfo.builder().build())
                        .build())
                .build();
    }

    private   String encodeContract(Contract model){
        Gson gson = new Gson();
        String json = gson.toJson(model);
        return Base64.getEncoder().encodeToString(json.getBytes());
    }
    @SneakyThrows
    public byte[] createSignature(SignableContainer model) {
        String json = new Gson().toJson(model).trim();
        byte[] computedHashAsByte = computeSha256HashAsByte(json);
        byte[] hMac = getHMAC(computedHashAsByte, "SecretKey");
        return hMac;
    }

    @SneakyThrows
    private static byte[] computeSha256HashAsByte(String input) {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    @SneakyThrows
    private static byte[] getHMAC(byte[] data, String key) {
        Mac sha256HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256HMAC.init(secretKey);
        return sha256HMAC.doFinal(data);
    }
}
