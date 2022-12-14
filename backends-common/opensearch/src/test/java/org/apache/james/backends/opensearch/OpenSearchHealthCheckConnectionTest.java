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
package org.apache.james.backends.opensearch;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.google.common.collect.ImmutableSet;

class OpenSearchHealthCheckConnectionTest {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    @RegisterExtension
    public DockerOpenSearchExtension openSearch = new DockerOpenSearchExtension(DockerOpenSearchExtension.CleanupStrategy.NONE);
    private OpenSearchHealthCheck openSearchHealthCheck;

    @BeforeEach
    void setUp() {
        ReactorOpenSearchClient client = openSearch.getDockerOpenSearch().clientProvider(REQUEST_TIMEOUT).get();

        openSearchHealthCheck = new OpenSearchHealthCheck(client, ImmutableSet.of());
    }

    @Test
    void checkShouldSucceedWhenOpenSearchIsRunning() {
        assertThat(openSearchHealthCheck.check().block().isHealthy()).isTrue();
    }

    @Test
    void checkShouldFailWhenOpenSearchIsPaused() {

        openSearch.getDockerOpenSearch().pause();

        try {
            assertThat(openSearchHealthCheck.check().block().isUnHealthy()).isTrue();
        } finally {
            openSearch.getDockerOpenSearch().unpause();
        }
    }
}
