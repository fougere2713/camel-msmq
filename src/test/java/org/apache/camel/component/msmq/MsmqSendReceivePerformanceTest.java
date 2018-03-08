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
public class MsmqSendReceivePerformanceTest extends ContextTestSupport {

    private CountDownLatch latch;
    private CountDownLatch latchStart;

    private int bufferSize = 1024;

    public void testMsmqSendReceivePerformance() throws Exception {

        try {
            MsmqQueue.createQueue(".\\Private$\\Test");
        } catch (Exception ex) {

        }
        ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
        int nummsg = 1000;
        latchStart = new CountDownLatch(1);
        for (int i = 0; i < nummsg; ++i) {
            template.sendBody("direct:input", buffer);
        }
        latchStart.countDown();
        latch = new CountDownLatch(nummsg);
        long start = System.currentTimeMillis();
        latch.await();
        long stop = System.currentTimeMillis();
        System.out.println(nummsg / ((stop - start) / (float)1000));
        MsmqQueue.deleteQueue("DIRECT=OS:localhost\\private$\\test");
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {

                from("direct:input").to("msmq:DIRECT=OS:localhost\\private$\\test?deliveryPersistent=true");
                from("msmq:DIRECT=OS:localhost\\private$\\test?concurrentConsumers=16&initialBufferSize=1024").process(new Processor() {

                    public void process(Exchange exc) throws Exception {
                        latchStart.await();
                        Assert.assertTrue(((Long)exc.getIn().getHeader(MsmqConstants.BODY_SIZE)).longValue() == bufferSize);
                        latch.countDown();
                    }
                });
            }
        };
    }
}
