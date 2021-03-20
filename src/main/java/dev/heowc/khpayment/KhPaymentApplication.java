package dev.heowc.khpayment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
public class KhPaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(KhPaymentApplication.class, args);
    }

}
