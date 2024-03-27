package io.quarkus.updates.camel.camel40;

import org.apache.camel.builder.RouteBuilder;

public class Test extends RouteBuilder {
    @Override
    public void configure() {
        from("direct:in").choice().when().xquery("text", Object.class, null, "header")
                .to("mock:premium");
    }
}
