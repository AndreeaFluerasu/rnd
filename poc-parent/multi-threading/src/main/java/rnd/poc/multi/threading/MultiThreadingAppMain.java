package rnd.poc.multi.threading;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MultiThreadingAppMain {
    public static void main(String[] args) {
        SpringApplication.run(MultiThreadingAppMain.class);
        System.out.println("Hello world!");
    }
}