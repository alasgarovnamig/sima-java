package az.sima.simajava.dto.sima.simaContract;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Header {
    private String AlgorithmName;
    private byte[] Signature;
}
