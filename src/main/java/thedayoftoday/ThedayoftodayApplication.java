package thedayoftoday;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ThedayoftodayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThedayoftodayApplication.class, args);
    }
}