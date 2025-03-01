package io.github.lujian213.eggfund.config;

import io.github.lujian213.eggfund.service.FundDataService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SystemPreparation {
    @Bean
    public CommandLineRunner prepare(FundDataService fundDataService) {
        return args -> {
            fundDataService.updateFundRTValues();
            fundDataService.updateFundsValues();
        };
    }
}