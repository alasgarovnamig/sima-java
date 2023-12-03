package az.sima.simajava.dto.sima.simaContract;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignableContainer {
    private ProtoInfo ProtoInfo;
    private OperationInfo OperationInfo;
    private ClientInfo ClientInfo;
    private DataInfo DataInfo;

}
