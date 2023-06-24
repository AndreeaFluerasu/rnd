package rnd.poc.oauth2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OAUTH2ClientAppMain {

    public static void main(String[] args) {
        SpringApplication.run(OAUTH2ClientAppMain.class, args);

        System.out.println("Hello world!");
    }
}