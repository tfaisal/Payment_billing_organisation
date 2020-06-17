package com.abcfinancial.api.billing;

import com.abcfinancial.api.billing.utility.common.AppConstants;
import com.abcfinancial.api.common.annotation.AbcService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Clock;

@Slf4j
@AbcService
@EnableWebSecurity
@EnableResourceServer
@EnableGlobalMethodSecurity( prePostEnabled = true )
public class BillingApplication extends ResourceServerConfigurerAdapter implements WebMvcConfigurer
{
    public static void main( String[] args )
    {
        SpringApplication.run( BillingApplication.class, args );
    }

    @Override
    public void configure( ResourceServerSecurityConfigurer resources )
    {
        resources.resourceId( "billing" );
    }

    @Override
    public void configure( HttpSecurity http ) throws Exception
    {
        http.authorizeRequests( )
            .antMatchers( "/" ).permitAll( )
            .antMatchers( "/docs/**" ).permitAll( )
            .antMatchers( "/actuator/health" ).permitAll( ) // can we tighten this up?
            .anyRequest( ).authenticated( ); //individual services use annotations
    }

    @Override
    public void addViewControllers( ViewControllerRegistry registry )
    {
        registry.addViewController( "/" ).setViewName( "forward:/docs/index.html" );
    }

    @Bean
    public Clock clock( )
    {
        return Clock.systemUTC( );
    }

    @Bean
    public RestTemplate restTemplate( )
    {
        return new RestTemplate( )
        {
            {
                setRequestFactory( new HttpComponentsClientHttpRequestFactory( HttpClientBuilder
                    .create( )
                    .setConnectionManager( new PoolingHttpClientConnectionManager( )
                    {
                        {
                            setDefaultMaxPerRoute( 20 );
                            setMaxTotal( AppConstants.MAXCONNECTIONCOUNT );
                        }
                    } ).setConnectionManagerShared( true ).build( ) ) );
            }
        };
    }
}
