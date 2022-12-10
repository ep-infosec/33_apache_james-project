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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensearch.action.admin.cluster.health.ClusterHealthResponse;
import org.opensearch.cluster.ClusterName;
import org.opensearch.cluster.ClusterState;
import org.opensearch.cluster.block.ClusterBlocks;
import org.opensearch.cluster.health.ClusterHealthStatus;
import org.opensearch.cluster.node.DiscoveryNodes;
import org.opensearch.cluster.routing.RoutingTable;

import com.google.common.collect.ImmutableSet;

class OpenSearchHealthCheckTest {
    private OpenSearchHealthCheck healthCheck;

    @BeforeEach
    void setup() {
        healthCheck = new OpenSearchHealthCheck(null, ImmutableSet.of());
    }

    @Test
    void checkShouldReturnHealthyWhenOpenSearchClusterHealthStatusIsGreen() {
        FakeClusterHealthResponse response = new FakeClusterHealthResponse(ClusterHealthStatus.GREEN);

        assertThat(healthCheck.toHealthCheckResult(response).isHealthy()).isTrue();
    }

    @Test
    void checkShouldReturnUnHealthyWhenOpenSearchClusterHealthStatusIsRed() {
        FakeClusterHealthResponse response = new FakeClusterHealthResponse(ClusterHealthStatus.RED);

        assertThat(healthCheck.toHealthCheckResult(response).isUnHealthy()).isTrue();
    }

    @Test
    void checkShouldReturnHealthyWhenOpenSearchClusterHealthStatusIsYellow() {
        FakeClusterHealthResponse response = new FakeClusterHealthResponse(ClusterHealthStatus.YELLOW);

        assertThat(healthCheck.toHealthCheckResult(response).isHealthy()).isTrue();
    }

    private static class FakeClusterHealthResponse extends ClusterHealthResponse {
        private final ClusterHealthStatus status;

        private FakeClusterHealthResponse(ClusterHealthStatus clusterHealthStatus) {
            super("fake-cluster", new String[0],
                new ClusterState(new ClusterName("fake-cluster"), 0, null, null, RoutingTable.builder().build(),
                    DiscoveryNodes.builder().build(),
                    ClusterBlocks.builder().build(), null, 0, false));
            this.status = clusterHealthStatus;
        }

        @Override
        public ClusterHealthStatus getStatus() {
            return this.status;
        }
    }
}
