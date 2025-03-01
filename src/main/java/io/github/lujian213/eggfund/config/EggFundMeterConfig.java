package io.github.lujian213.eggfund.config;

import io.github.lujian213.eggfund.model.Investor;
import io.github.lujian213.eggfund.service.InvestService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class EggFundMeterConfig {
    private final InvestService investService;

    public EggFundMeterConfig(InvestService investService) {
        this.investService = investService;
    }

    @Bean
    public MeterBinder createEggFundMeterBinder() {
        return this::bind;
    }

    protected void bind(MeterRegistry meterRegistry) {
        investService.getAllInvestors().forEach(investor -> bindTo(meterRegistry, investService, investor));
    }

    protected void bindTo(MeterRegistry meterRegistry, InvestService investService, Investor investor) {
        meterRegistry.gauge("eggfund.invested.funds", Collections.singleton(Tag.of("investor", investor.getName())),
                investService, service -> service.getUserInvestedFunds(investor.getId()).size());
    }
}