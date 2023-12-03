package az.sima.simajava.dto.sima.simaContract;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientInfo {
    private  int ClientId;
    private String ClientName;
    private String IconURI;
    private String Callback;
    private String[] HostName;
    private String RedirectURI;
}
