package rnd.poc.db.locking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DBLockingAppMain {

    public static void main(String[] args) {
        SpringApplication.run(DBLockingAppMain.class, args);

        System.out.println("Hello world!");
    }
}