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

import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.james.core.healthcheck.ComponentName;
import org.apache.james.core.healthcheck.HealthCheck;
import org.apache.james.core.healthcheck.Result;
import org.opensearch.action.admin.cluster.health.ClusterHealthRequest;
import org.opensearch.action.admin.cluster.health.ClusterHealthResponse;
import org.opensearch.client.Requests;

import com.google.common.annotations.VisibleForTesting;

import reactor.core.publisher.Mono;

public class OpenSearchHealthCheck implements HealthCheck {
    private static final ComponentName COMPONENT_NAME = new ComponentName("OpenSearch Backend");

    private final Set<IndexName> indexNames;
    private final ReactorOpenSearchClient client;

    @Inject
    OpenSearchHealthCheck(ReactorOpenSearchClient client, Set<IndexName> indexNames) {
        this.client = client;
        this.indexNames = indexNames;
    }

    @Override
    public ComponentName componentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Mono<Result> check() {
        String[] indices = indexNames.stream()
            .map(IndexName::getValue)
            .toArray(String[]::new);
        ClusterHealthRequest request = Requests.clusterHealthRequest(indices);

        return client.health(request)
            .map(this::toHealthCheckResult)
            .onErrorResume(e -> Mono.just(Result.unhealthy(COMPONENT_NAME, "Error while contacting cluster", e)));
    }

    @VisibleForTesting
    Result toHealthCheckResult(ClusterHealthResponse response) {
        switch (response.getStatus()) {
            case GREEN:
            case YELLOW:
                return Result.healthy(COMPONENT_NAME);
            case RED:
                return Result.unhealthy(COMPONENT_NAME, response.getClusterName() + " status is RED");
            default:
                throw new NotImplementedException("Un-handled OpenSearch cluster status");
        }
    }
}
