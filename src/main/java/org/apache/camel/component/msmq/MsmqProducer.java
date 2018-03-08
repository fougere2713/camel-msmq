/**
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
package org.apache.camel.component.msmq;

import org.apache.camel.Exchange;
import org.apache.camel.Producer;
import org.apache.camel.component.msmq.native_support.MsmqMessage;
import org.apache.camel.component.msmq.native_support.MsmqQueue;
import org.apache.camel.component.msmq.native_support.msmq_native_support;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * A {@link Producer} implementation for MSMQ
 * 
 * @version $Revision: 15484 $
 */
public class MsmqProducer extends DefaultProducer {
    protected final Logger LOG = LoggerFactory.getLogger(MsmqProducer.class);

    private final MsmqQueue queue;
    private boolean deliveryPersistent;
    private int timeToLive = msmq_native_support.INFINITE;
    private int priority = 3;

    public MsmqProducer(MsmqEndpoint endpoint) {
        super(endpoint);
        this.queue = new MsmqQueue();

        this.deliveryPersistent = endpoint.getDeliveryPersistent();
        this.timeToLive = endpoint.getTimeToLive();
        this.priority = endpoint.getPriority();
    }

    public void process(Exchange exchange) throws Exception {
        if (!queue.isOpen()) {
            openConnection();
        }

        Object obj = exchange.getIn().getBody();
        ByteBuffer body = null;

        if (obj instanceof ByteBuffer) {
            body = (ByteBuffer)obj;
            if (!body.isDirect()) {
                ByteBuffer outBuffer;
                outBuffer = ByteBuffer.allocateDirect(body.remaining());
                outBuffer.put(body);
                outBuffer.flip();
                body = outBuffer;
            }
        }

        if (obj instanceof String) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(((String)obj).length() * 2);
            buffer.asCharBuffer().put((String)obj);
            body = buffer;
        }

        if (body == null) {
            LOG.warn("No payload for exchange: " + exchange);
        } else {
            MsmqMessage msmqMessage = new MsmqMessage();
            msmqMessage.setMsgBodyWithByteBuffer(body);
            if (deliveryPersistent) {
                msmqMessage.setDelivery(msmq_native_support.MQMSG_DELIVERY_RECOVERABLE);
            }
            msmqMessage.setTimeToBeReceived(timeToLive);
            msmqMessage.setPriority(priority);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Sending body: " + body);
            }
            queue.sendMessage(msmqMessage);
        }
    }

    @Override
    protected void doStart() throws Exception {
    }

    @Override
    protected void doStop() throws Exception {
        if (queue.isOpen()) {
            queue.close();
        }
    }

    private void openConnection() {
        queue.open(((MsmqEndpoint)getEndpoint()).getRemaining(), msmq_native_support.MQ_SEND_ACCESS);
    }

}
