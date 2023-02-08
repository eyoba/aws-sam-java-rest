package model.request;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonAutoDetect
public class CreateOrderRequest {
    private String customerId;
    private Long preTaxAmount;
    private Long postTaxAmount;
}
