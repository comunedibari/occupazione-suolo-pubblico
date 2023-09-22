
package it.fincons.osp.payload.protocollazione.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProtocolloResponse {
    private String numeroProtocollo;
    private String anno;
    private LocalDateTime dataProtocollo;
}
