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

package org.apache.james.modules.mailbox;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.james.backends.opensearch.ReactorOpenSearchClient;
import org.apache.james.lifecycle.api.StartUpCheck;
import org.opensearch.Version;
import org.opensearch.client.RequestOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenSearchStartUpCheck implements StartUpCheck {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenSearchStartUpCheck.class);

    private static final Version RECOMMENDED_ES_VERSION = Version.V_2_0_0;
    private static final String VERSION_CHECKING_ERROR_MESSAGE = "Error when checking ES version";

    public static final String CHECK_NAME = "OpenSearchStartUpCheck";

    private final ReactorOpenSearchClient client;

    @Inject
    private OpenSearchStartUpCheck(ReactorOpenSearchClient client) {
        this.client = client;
    }

    @Override
    public CheckResult check() {
        try {

            Version esVersion = Version.fromString(client.info(RequestOptions.DEFAULT).getVersion().getNumber());

            if (esVersion.isCompatible(RECOMMENDED_ES_VERSION)) {
                return CheckResult.builder()
                    .checkName(checkName())
                    .resultType(ResultType.GOOD)
                    .build();
            }

            String esVersionCompatibilityWarn = String.format(
                "ES version(%s) is not compatible with the recommendation(%s)",
                esVersion.toString(),
                RECOMMENDED_ES_VERSION.toString());
            LOGGER.warn(esVersionCompatibilityWarn);

            return CheckResult.builder()
                .checkName(checkName())
                .resultType(ResultType.BAD)
                .description(esVersionCompatibilityWarn)
                .build();
        } catch (IOException e) {
            LOGGER.error(VERSION_CHECKING_ERROR_MESSAGE, e);
            return CheckResult.builder()
                .checkName(checkName())
                .resultType(ResultType.BAD)
                .description(VERSION_CHECKING_ERROR_MESSAGE + ": " + e.getMessage())
                .build();
        }
    }

    @Override
    public String checkName() {
        return CHECK_NAME;
    }
}
