package az.sima.simajava.dto.sima.requests;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CallbackRequestDto {
    private String type;
    private String operationId;
    private String dataSignature;
    private String signedDataHash;
    private String algName;


}
