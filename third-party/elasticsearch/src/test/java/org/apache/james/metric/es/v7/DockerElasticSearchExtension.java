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

package org.apache.james.metric.es.v7;

import org.apache.james.backends.opensearch.DockerOpenSearch;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class DockerElasticSearchExtension implements AfterAllCallback, BeforeAllCallback, BeforeEachCallback, AfterEachCallback, ParameterResolver {
    private final DockerOpenSearch elasticSearch;

    DockerElasticSearchExtension(DockerOpenSearch elasticSearch) {
        this.elasticSearch = elasticSearch;
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        elasticSearch.start();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        if (!elasticSearch.isRunning()) {
            elasticSearch.unpause();
        }
        elasticSearch.flushIndices();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        elasticSearch.cleanUpData();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        elasticSearch.stop();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return (parameterContext.getParameter().getType() == DockerOpenSearch.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return elasticSearch;
    }
}
