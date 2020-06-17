package com.abcfinancial.api.billing.generalledger.payment.valueobject;

import com.abcfinancial.api.billing.generalledger.enums.EventType;
import com.abcfinancial.api.billing.generalledger.enums.PaymentType;
import com.abcfinancial.api.billing.generalledger.enums.Status;
import com.abcfinancial.api.billing.utility.common.Validation;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude( JsonInclude.Include.NON_NULL )
public class Payload
{
    /**
     * eventType
     */
    @Enumerated( EnumType.STRING )
    private EventType eventType;
    /**
     * status
     */
    @NotNull( message = "not allowed for status" )
    private Status status;
    /**
     * message
     */
    private String message;
    /**
     * messageCode
     */
    private String messageCode;
    /**
     * transactionType
     */
    @NotNull( message = "not allowed for transactionType" )
    private PaymentType transactionType;
    /**
     * transactionId
     */
    private String transactionId;
    /**
     * referencedId
     */
    @NotBlank( message = "not allowed for referenceId" )
    @Size( max = 50 )
    private String referencedId;
    /**
     * requested
     */
    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE_TIME )
    @JsonFormat( pattern = "MM-dd-yyyy HH:mm:ss" )
    private LocalDateTime requested;
    /**
     * Amount in the merchant's configured currency to attempt.
     */
    private BigDecimal amount;
    /**
     * Human readable source .
     */
    private String source;
    /**
     * companyId
     */
    private UUID companyId;
    /**
     * merchantId
     */
    private UUID merchantId;

    public void setReferencedId( String referencedId )
    {
        this.referencedId = Validation.trim( referencedId );
    }

}
