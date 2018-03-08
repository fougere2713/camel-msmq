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
import java.util.concurrent.CountDownLatch;

import junit.framework.Assert;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.msmq.native_support.MsmqQueue;

/**
 * @version $Revision: 14846 $
 */
public class MsmqSendReceiveStressTest extends ContextTestSupport {

    private int nummsg = 10;
    private CountDownLatch latch = new CountDownLatch(nummsg);;
    private int bufferSize = 1024 * 1024;

    public void testMsmqSendReceive() throws Exception {
        try {
            MsmqQueue.createQueue(".\\Private$\\Test");
        } catch (Exception ex) {

        }
        ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
        for (int i = 0; i < nummsg; ++i) {
            template.sendBody("direct:input", buffer);
        }
        latch.await();
        MsmqQueue.deleteQueue("DIRECT=OS:localhost\\private$\\test");
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {

                from("direct:input").to("msmq:DIRECT=OS:localhost\\private$\\test");
                from("msmq:DIRECT=OS:localhost\\private$\\test?concurrentConsumers=1&initialBufferSize=1048576").process(new Processor() {

                    public void process(Exchange exc) throws Exception {
                        Assert.assertTrue(((Long)exc.getIn().getHeader(MsmqConstants.BODY_SIZE)).intValue() == bufferSize);
                        latch.countDown();
                    }
                });
            }
        };
    }
}
