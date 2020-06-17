package com.abcfinancial.api.billing.subscriptionmanagement.agreement.valueobject;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@JsonInclude( JsonInclude.Include.NON_DEFAULT )

public class AgreementCancelVO
{
    /**
     * Agreement cancellation date must be a date in the present or in the future
     */
    @DateTimeFormat( pattern = "MM-dd-yyyy", iso = DateTimeFormat.ISO.DATE )
    @JsonFormat( pattern = "MM-dd-yyyy" )
    @JsonProperty( "agrmCancellationDate" )
    @NotNull
    private LocalDate agrmCancellationDate;
}
