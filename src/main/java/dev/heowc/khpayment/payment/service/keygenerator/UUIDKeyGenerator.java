package dev.heowc.khpayment.payment.service.keygenerator;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
class UUIDKeyGenerator implements KeyGenerator {


    @Override
    public String generate() {
        return UUID.randomUUID().toString().substring(0, 20);
    }
}
