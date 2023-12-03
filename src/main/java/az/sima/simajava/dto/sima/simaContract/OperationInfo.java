package az.sima.simajava.dto.sima.simaContract;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationInfo {
    private String Type;
    private String OperationId;
    private Long NbfUTC;
    private Long ExpUTC;
    private List<String> Assignee;
}
