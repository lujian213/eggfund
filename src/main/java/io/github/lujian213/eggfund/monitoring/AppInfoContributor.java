package io.github.lujian213.eggfund.monitoring;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

@Component
public class AppInfoContributor implements InfoContributor {
    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("app", "eggfund");
        builder.withDetail("author", "Lu Jian");
        builder.withDetail("email", "lujian213@msn.com");
    }
}