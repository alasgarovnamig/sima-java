package az.sima.simajava.dto.sima.simaContract;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataInfo {
    private String DataURI;
    private String AlgName;
    private String FingerPrint;
}
