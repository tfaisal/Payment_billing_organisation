package com.abcfinancial.api.billing.utility.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource( "classpath:ErrorMessage.properties" )

public class ApplicationConfiguration {
    @Autowired
    private Environment env;

    public String getValue( String key ) {
     return env.getProperty( key );
    }
}
