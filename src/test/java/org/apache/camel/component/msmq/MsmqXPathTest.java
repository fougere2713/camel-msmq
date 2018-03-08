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
public class MsmqXPathTest extends ContextTestSupport {

    private CountDownLatch latch;

    public void testMsmqSendReceive() throws Exception {
        try {
            MsmqQueue.createQueue(".\\Private$\\Test");
        } catch (Exception ex) {
        }

        String str1 = new String("<person name='David' city='Rome'/>");
        ByteBuffer buffer = ByteBuffer.allocateDirect(str1.length() * 2);
        buffer.asCharBuffer().put(str1);
        template.sendBody("direct:input", buffer);

        String str2 = new String("<person name='James' city='London'/>");
        buffer = ByteBuffer.allocateDirect(str2.length() * 2);
        buffer.asCharBuffer().put(str2);
        template.sendBody("direct:input", buffer);
        latch = new CountDownLatch(1);
        latch.await();
        MsmqQueue.deleteQueue("DIRECT=OS:localhost\\private$\\Test");
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {

                from("direct:input").to("msmq:DIRECT=OS:localhost\\private$\\Test");

                from("msmq:DIRECT=OS:localhost\\private$\\Test?concurrentConsumers=1").process(new Processor() {
                    public void process(Exchange exchange) {
                        int size = ((Long)exchange.getIn().getHeader(MsmqConstants.BODY_SIZE)).intValue();
                        ByteBuffer buffer = (ByteBuffer)exchange.getIn().getBody();
                        exchange.getIn().setBody(buffer.asCharBuffer().subSequence(0, size / 2).toString());
                    }
                }).to("direct:output");

                from("direct:output").filter().xpath("/person[@name='James']").process(new Processor() {
                    public void process(Exchange exc) throws Exception {
                        Assert.assertTrue(exc.getIn().getBody(String.class).equals("<person name='James' city='London'/>"));
                        latch.countDown();
                    }
                });
            }
        };
    }
}
