package az.sima.simajava.dto.sima.simaContract;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {
    private  Header Header;
    private  SignableContainer SignableContainer;
}