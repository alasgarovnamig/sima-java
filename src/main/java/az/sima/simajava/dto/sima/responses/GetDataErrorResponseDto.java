package az.sima.simajava.dto.sima.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetDataErrorResponseDto {
    private String errorMessage;
}
