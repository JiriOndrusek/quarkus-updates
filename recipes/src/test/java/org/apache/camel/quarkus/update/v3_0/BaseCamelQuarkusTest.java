/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.quarkus.update.v3_0;

import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.SourceSpecs;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.openrewrite.maven.Assertions.pomXml;

public class BaseCamelQuarkusTest implements RewriteTest {

    SourceSpecs[] withCamel(SourceSpecs... sourceSpecs) {
        return Stream.concat(Stream.of(camelPom()), Arrays.stream(sourceSpecs)).toArray(SourceSpecs[]::new);
    }

    private SourceSpecs camelPom() {
        return pomXml(
                "<project>\n" +
                        "                        <modelVersion>4.0.0</modelVersion>\n" +
                        "\n" +
                        "                        <artifactId>test</artifactId>\n" +
                        "                        <groupId>org.apache.camel.quarkus.test</groupId>\n" +
                        "                        <version>1.0.0</version>\n" +
                        "\n" +
                        "                        <properties>\n" +
                        "                        <quarkus.platform.version>2.13.3.Final</quarkus.platform.version>\n" +
                        "                        </properties>\n" +
                        "\n" +
                        "                        <dependencyManagement>\n" +
                        "                        <dependencies>\n" +
                        "                           <dependency>\n" +
                        "                               <groupId>io.quarkus.platform</groupId>\n" +
                        "                               <artifactId>quarkus-camel-bom</artifactId>\n" +
                        "                               <version>2.13.7.Final</version>\n" +
                        "                               <type>pom</type>\n" +
                        "                               <scope>import</scope>\n" +
                        "                           </dependency>\n" +
                        "                        </dependencies>\n" +
                        "                        </dependencyManagement>\n" +
                        "\n" +
                        "                        <dependencies>\n" +
                        "                        <dependency>\n" +
                        "                           <groupId>org.apache.camel.quarkus</groupId>\n" +
                        "                           <artifactId>camel-quarkus-activemq</artifactId>\n" +
                        "                        </dependency>\n" +
                        "                        </dependencies>\n" +
                        "\n" +
                        "                        </project>",

                "<project>\n" +
                        "                        <modelVersion>4.0.0</modelVersion>\n" +
                        "\n" +
                        "                        <artifactId>test</artifactId>\n" +
                        "                        <groupId>org.apache.camel.quarkus.test</groupId>\n" +
                        "                        <version>1.0.0</version>\n" +
                        "\n" +
                        "                        <properties>\n" +
                        "                        <quarkus.platform.version>2.13.3.Final</quarkus.platform.version>\n" +
                        "                        </properties>\n" +
                        "\n" +
                        "                        <dependencyManagement>\n" +
                        "                        <dependencies>\n" +
                        "                           <dependency>\n" +
                        "                               <groupId>io.quarkus.platform</groupId>\n" +
                        "                               <artifactId>quarkus-camel-bom</artifactId>\n" +
                        "                               <version>2.13.7.Final</version>\n" +
                        "                               <type>pom</type>\n" +
                        "                               <scope>import</scope>\n" +
                        "                           </dependency>\n" +
                        "                        </dependencies>\n" +
                        "                        </dependencyManagement>\n" +
                        "\n" +
                        "                        <dependencies>\n" +
                        "                        <!--Extension camel-quarkus-activemq was removed, consider camel-quarkus-jms or camel-quarkus-sjms or camel-quarkus-amqp instead. \n" +
                        "                        <dependency>\n" +
                        "                           <groupId>org.apache.camel.quarkus</groupId>\n" +
                        "                           <artifactId>camel-quarkus-activemq</artifactId>\n" +
                        "                        </dependency>-->\n" +
                        "                        </dependencies>\n" +
                        "\n" +
                        "                        </project>");
    }

}
