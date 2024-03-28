package io.quarkus.updates.camel.camel40;

import org.apache.camel.builder.RouteBuilder;

public class ThrottleEIPTest extends RouteBuilder {
    @Override
    public void configure() {
        long maxRequestsPerPeriod = 100L;
        Long maxRequests = Long.valueOf(maxRequestsPerPeriod);

        from("seda:a")
                .throttle(maxRequestsPerPeriod).timePeriodMillis(500).asyncDelayed()
                .to("seda:b");

        from("seda:a")
                .throttle(maxRequestsPerPeriod).timePeriodMillis(500)
                .to("seda:b");

        from("seda:c")
                .throttle(maxRequestsPerPeriod)
                .to("seda:d");

        from("seda:a")
                .throttle(maxRequests).timePeriodMillis(500).asyncDelayed()
                .to("seda:b");

        from("seda:a")
                .throttle(maxRequests).timePeriodMillis(500)
                .to("seda:b");

        from("seda:c")
                .throttle(maxRequests)
                .to("seda:d");
    }
}