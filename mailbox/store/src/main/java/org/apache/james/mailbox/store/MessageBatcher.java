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

package org.apache.james.mailbox.store;

import java.util.List;

import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.MessageRange;

import com.github.fge.lambdas.Throwing;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import reactor.core.publisher.Flux;

public class MessageBatcher {

    public static final int NO_BATCH_SIZE = 0;

    public interface BatchedOperation {
        List<MessageRange> execute(MessageRange messageRange) throws MailboxException;
    }

    public interface ReactiveBatchedOperation {
        Flux<MessageRange> execute(MessageRange messageRange);
    }

    private final int batchSize;

    public MessageBatcher(int batchSize) {
        Preconditions.checkArgument(batchSize >= NO_BATCH_SIZE);
        this.batchSize = batchSize;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public List<MessageRange> batchMessages(MessageRange set, BatchedOperation batchedOperation) throws MailboxException {
        if (batchSize > 0) {
            return set.split(batchSize)
                .stream()
                .flatMap(Throwing.function(range -> batchedOperation.execute(range)
                    .stream()))
                .collect(ImmutableList.toImmutableList());
        } else {
            return batchedOperation.execute(set);
        }
    }

    public Flux<MessageRange> batchMessagesReactive(MessageRange set, ReactiveBatchedOperation batchedOperation) {
        if (batchSize > 0) {
           return Flux.fromIterable(set.split(batchSize))
                .flatMap(batchedOperation::execute);
        } else {
            return batchedOperation.execute(set);
        }
    }

}
