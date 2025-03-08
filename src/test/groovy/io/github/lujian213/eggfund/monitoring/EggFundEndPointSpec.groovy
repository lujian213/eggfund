package io.github.lujian213.eggfund.monitoring

import io.github.lujian213.eggfund.config.SecurityConfig
import io.github.lujian213.eggfund.model.FundInfo
import io.github.lujian213.eggfund.model.FundRTValue
import io.github.lujian213.eggfund.service.FundDataService
import io.github.lujian213.eggfund.utils.Constants
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.autoconfigure.beans.BeansEndpointAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(EggFundEndPoint)
@ContextConfiguration(classes = [SecurityConfig.class, EggFundEndPoint.class])
@WithMockUser(roles="USER")
/**
 * https://github.com/spring-projects/spring-boot/issues/40574
 */
@ImportAutoConfiguration([EndpointAutoConfiguration, WebEndpointAutoConfiguration,
        BeansEndpointAutoConfiguration, ManagementContextAutoConfiguration])
class EggFundEndPointSpec extends Specification {

    @SpringBean
    FundDataService fundDataService = Mock(FundDataService)
    @Autowired
    MockMvc mockMvc

    def "GetFundRTValues"() {
        given:
        def fundInfo1 = new FundInfo(id: "1", name: "Fund1", alias: "Alias1", priority: 2)
        def fundInfo2 = new FundInfo(id: "2", name: "Fund2", alias: null, priority: 1)
        def funRTValue1 = new FundRTValue("2025-01-01 15:00", 100.0, 0.0)
        def funRTValue2 = new FundRTValue("2025-01-01 15:00", 200.0, 0.0d)
        def returnValue = ["2": [name: "Fund2", unitValue: 200.0], "1": [name: "Alias1", unitValue: 100.0]]
        fundDataService.getAllFunds() >> [fundInfo1, fundInfo2]
        fundDataService.getFundRTValues(["1", "2"]) >> ["1": funRTValue1, "2": funRTValue2]

        expect:
        mockMvc.perform(get("/actuator/eggfund"))
                .andExpect(status().isOk())
                .andExpect(content().json(Constants.MAPPER.writeValueAsString(returnValue)))
    }
}