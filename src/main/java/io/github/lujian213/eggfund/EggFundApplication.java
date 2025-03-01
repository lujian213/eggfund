package io.github.lujian213.eggfund;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EggFundApplication {

    private static final Logger log = LoggerFactory.getLogger(EggFundApplication.class);

    public static void main(String[] args) {
        log.info("start...");
        SpringApplication.run(EggFundApplication.class, args);
    }
}
