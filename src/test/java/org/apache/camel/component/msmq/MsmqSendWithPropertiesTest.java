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

import java.nio.ByteBuffer;

import junit.framework.Assert;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Producer;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.msmq.native_support.MsmqMessage;
import org.apache.camel.component.msmq.native_support.MsmqQueue;
import org.apache.camel.component.msmq.native_support.msmq_native_support;

/**
 * @version $Revision: 14846 $
 */
public class MsmqSendWithPropertiesTest extends ContextTestSupport {

    public void testMsmqSendReceive() throws Exception {
        try {
            MsmqQueue.createQueue(".\\Private$\\Test");
        } catch (Exception ex) {

        }
        String str = new String("Hello David");
        ByteBuffer buffer = ByteBuffer.allocateDirect(str.length() * 2);
        buffer.asCharBuffer().put(str);
        template.sendBody("direct:input", buffer);

        MsmqQueue receiveQueue = new MsmqQueue();
        receiveQueue.open("DIRECT=OS:localhost\\private$\\test", msmq_native_support.MQ_RECEIVE_ACCESS);

        MsmqMessage message2 = new MsmqMessage();
        ByteBuffer recvbuffer = ByteBuffer.allocateDirect(str.length() * 2);
        message2.setMsgBodyWithByteBuffer(recvbuffer);
        receiveQueue.receiveMessage(message2, -1);

        Assert.assertEquals(str, recvbuffer.asCharBuffer().subSequence(0, (int)message2.getBodySize() / 2).toString());
        Assert.assertTrue(message2.getPriority() == 5);
        Assert.assertTrue(message2.getTimeToBeReceived() == 10);
        Assert.assertTrue(message2.getDelivery() == msmq_native_support.MQMSG_DELIVERY_RECOVERABLE);
        receiveQueue.close();
        MsmqQueue.deleteQueue("DIRECT=OS:localhost\\private$\\test");
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("direct:input").to("msmq:DIRECT=OS:localhost\\private$\\test?deliveryPersistent=true&priority=5&timeToLive=10");
            }
        };
    }
}
