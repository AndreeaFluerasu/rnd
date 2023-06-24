package rnd.poc.oauth2.authorization.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OAUTH2AuthServerAppMain {
    public static void main(String[] args) {
        SpringApplication.run(OAUTH2AuthServerAppMain.class);
        System.out.println("Hello world!");
    }
}