package az.sima.simajava.dto.sima;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SimaPersonalData {
    private String fin;
    private String name;
    private String surname;
    private String fullName;
    private  String country;
}
