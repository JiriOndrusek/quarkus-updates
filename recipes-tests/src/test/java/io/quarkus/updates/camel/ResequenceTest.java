package io.quarkus.updates.camel;

import org.apache.camel.BindToRegistry;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.engine.FileStateRepository;
import org.apache.camel.impl.engine.MemoryStateRepository;

import java.io.File;

public class ResequenceTest extends RouteBuilder {
    @Override
    public void configure() {
        from("direct:start")

                .resequence().body()
                .to("mock:result");
    }


}
