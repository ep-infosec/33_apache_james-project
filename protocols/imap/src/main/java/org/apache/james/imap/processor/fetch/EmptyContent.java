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
package org.apache.james.imap.processor.fetch;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apache.james.mailbox.model.Content;
import org.reactivestreams.Publisher;

import reactor.core.publisher.Flux;

/**
 * Just an Empty {@link Content}
 */
public class EmptyContent implements Content {

    private static final byte[] EMPTY_ARRAY = new byte[0];

    /**
     * Return 0 as this {@link Content} is empty
     */
    @Override
    public long size() {
        return 0;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(EMPTY_ARRAY);
    }

    @Override
    public Publisher<ByteBuffer> reactiveBytes() {
        return Flux.just(EMPTY_ARRAY)
            .map(ByteBuffer::wrap);
    }
}
