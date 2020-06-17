package com.abcfinancial.api.billing.utility.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )
@JsonPropertyOrder( {
                        "code", 
                        "message"
                    } )

public class CustomResponse
{
    private Integer code;
    private String message;
    private Object details;
    private Long timestamp;
    @JsonIgnore
    private HttpStatus errorStatusCode;

    public CustomResponse( )
    {
    }

    public CustomResponse( Integer code, String message )
    {
        this.code = code;
        this.message = message;
    }

    public CustomResponse( Integer code, String message, HttpStatus errorStatusCode )
    {
        this.code = code;
        this.message = message;
        this.errorStatusCode = errorStatusCode;
    }

    public CustomResponse( Integer code, String message, Object details, Long timestamp )
    {
        this.code = code;
        this.message = message;
        this.details = details;
        this.timestamp = timestamp;
    }

    @Override
    public String toString( )
    {
        return "ErrorResponse{" +
               "code = " + code +
               ", message = '" + message + '\'' +
               ", details = " + details +
               ", timestamp = " + timestamp +
               '}';
    }
}
