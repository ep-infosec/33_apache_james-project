/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/
package org.apache.james;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import org.apache.james.backends.cassandra.DockerCassandra;
import org.apache.james.backends.cassandra.init.configuration.ClusterConfiguration;
import org.apache.james.modules.TestJMAPServerModule;
import org.apache.james.modules.protocols.ImapGuiceProbe;
import org.apache.james.util.Host;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.DockerClientFactory;

class CassandraNodeConfTest {
    private static final int CASSANDRA_PORT = 9042;

    private static JamesServerBuilder extensionBuilder() {
        return TestingDistributedJamesServerBuilder.withSearchConfiguration(SearchConfiguration.openSearch())
            .extension(new DockerOpenSearchExtension())
            .extension(new CassandraExtension())
            .server(configuration -> CassandraJamesServerMain.createServer(configuration)
                .overrideWith(new TestJMAPServerModule()))
            .disableAutoStart();
    }

    private static String getDockerHostIp() {
        DockerClientFactory clientFactory = DockerClientFactory.instance();
        clientFactory.client();
        return clientFactory.dockerHostIpAddress();
    }

    private SocketChannel socketChannel;

    @BeforeEach
    void setUp() throws IOException {
        socketChannel = SocketChannel.open();
    }

    @AfterEach
    void after() throws IOException {
        socketChannel.close();
    }

    @Nested
    class NormalBehaviour {
        @RegisterExtension
        JamesServerExtension testExtension = extensionBuilder().build();

        @Test
        void serverShouldStartServiceWhenNodeIsReachable(GuiceJamesServer server) throws Exception {
            assertThatServerStartCorrectly(server);
        }
    }

    @Nested
    class OneFailedNode {
        String unreachableNode = "10.2.3.42";

        private final DockerCassandraRule cassandra = new DockerCassandraRule();

        @RegisterExtension
        JamesServerExtension testExtension = extensionBuilder()
            .overrideServerModule(binder -> binder.bind(ClusterConfiguration.class)
                .toInstance(DockerCassandra.configurationBuilder(
                        Host.from(unreachableNode, 9042),
                        cassandra.getHost())
                    .build()))
            .build();

        @Test
        void serverShouldStartServiceWhenNodeIsReachable(GuiceJamesServer server) throws Exception {
            assertThatServerStartCorrectly(server);
        }
    }

    @Nested
    class UseMappedPort {
        private final DockerCassandraRule cassandra = new DockerCassandraRule();

        @RegisterExtension
        JamesServerExtension testExtension =  extensionBuilder()
            .overrideServerModule(binder -> binder.bind(ClusterConfiguration.class)
                .toInstance(DockerCassandra.configurationBuilder(
                        Host.from(getDockerHostIp(), cassandra.getMappedPort(CASSANDRA_PORT)))
                    .build()))
            .build();

        @Test
        void configShouldWorkWithNonDefaultPort(GuiceJamesServer server) throws Exception {
            assertThatServerStartCorrectly(server);
        }
    }

    private void assertThatServerStartCorrectly(GuiceJamesServer server) throws Exception {
        server.start();
        socketChannel.connect(new InetSocketAddress("127.0.0.1", server.getProbe(ImapGuiceProbe.class).getImapPort()));
        assertThat(getServerConnectionResponse(socketChannel)).startsWith("* OK JAMES IMAP4rev1 Server");
    }

    private String getServerConnectionResponse(SocketChannel socketChannel) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1000);
        socketChannel.read(byteBuffer);
        byte[] bytes = byteBuffer.array();
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
