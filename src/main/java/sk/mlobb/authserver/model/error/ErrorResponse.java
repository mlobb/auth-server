package sk.mlobb.authserver.model.error;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {

    private String info;
}
