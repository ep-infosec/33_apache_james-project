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


package org.apache.james.mpt.imapmailbox.suite;

import java.util.Locale;

import org.apache.james.mpt.api.ImapFeatures;
import org.apache.james.mpt.api.ImapHostSystem;
import org.apache.james.mpt.imapmailbox.ImapTestConstants;
import org.apache.james.mpt.imapmailbox.suite.base.BasicImapCommands;
import org.apache.james.mpt.script.SimpleScriptedTestProtocol;
import org.junit.Assume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class MailboxAnnotation implements ImapTestConstants {

    protected abstract ImapHostSystem createImapHostSystem();

    private SimpleScriptedTestProtocol simpleScriptedTestProtocol;

    @BeforeEach
    public void setUp() throws Exception {
        ImapHostSystem system = createImapHostSystem();
        Assume.assumeTrue(system.supports(ImapFeatures.Feature.ANNOTATION_SUPPORT));
        simpleScriptedTestProtocol = new SimpleScriptedTestProtocol("/org/apache/james/imap/scripts/", system)
                .withUser(USER, PASSWORD)
                .withLocale(Locale.US);
        BasicImapCommands.welcome(simpleScriptedTestProtocol);
        BasicImapCommands.authenticate(simpleScriptedTestProtocol);
        BasicImapCommands.prepareMailbox(simpleScriptedTestProtocol);
    }
    
    @Test
    public void testMetadataUS() throws Exception {
        simpleScriptedTestProtocol
            .withLocale(Locale.US)
            .run("Metadata");
    }

    @Test
    public void testMetadataWithLimitationUS() throws Exception {
        simpleScriptedTestProtocol
            .withLocale(Locale.US)
            .run("MetadataWithLimitation");
    }

    @Test
    public void testMetadataWithBinaryValue() throws Exception {
        simpleScriptedTestProtocol
            .withLocale(Locale.US)
            .run("MetadataWithBinaryData");
    }
}
