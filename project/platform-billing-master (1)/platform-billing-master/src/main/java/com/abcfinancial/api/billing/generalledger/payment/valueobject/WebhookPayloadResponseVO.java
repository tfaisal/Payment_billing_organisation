package com.abcfinancial.api.billing.generalledger.payment.valueobject;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude( JsonInclude.Include.NON_NULL )
public class WebhookPayloadResponseVO
{
    /**
     * companyId
     */
    private UUID companyId;
    /**
     * eventTime
     */
    @JsonFormat( pattern = "MM-dd-yyyy HH:mm:ss" )
    private LocalDateTime eventTime;
    /**
     * eventId
     */
    private UUID eventId;

    @JsonProperty( "payloadList" )
    @NotNull( message = "not allowed for payloadList" )
    private List<Payload> payloadList;
}
