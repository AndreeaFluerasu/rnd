package rnd.poc.oauth2.resource.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class OAUTH2ResourceServerAppMain {

    public static void main(String[] args) {
        SpringApplication.run(OAUTH2ResourceServerAppMain.class);
        System.out.println("Hello world!");
    }
}