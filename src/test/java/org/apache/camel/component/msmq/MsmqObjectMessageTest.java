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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
public class MsmqObjectMessageTest extends ContextTestSupport {

    private CountDownLatch latch;

    public void testMsmqSendReceive() throws Exception {
        try {
            MsmqQueue.createQueue(".\\Private$\\Test");
        } catch (Exception ex) {
        }
        Person person = new Person();
        person.setFirstName("Antonio");
        person.setSecondName("Pusceddu");
        ByteArrayOutputStream bostream = new ByteArrayOutputStream();
        ObjectOutputStream ostream = new ObjectOutputStream(bostream);
        ostream.writeObject(person);
        ostream.close();
        ByteBuffer buffer = ByteBuffer.allocateDirect(bostream.size());
        buffer.put(bostream.toByteArray());
        latch = new CountDownLatch(1);
        template.sendBody("direct:input", buffer);
        latch.await();
        MsmqQueue.deleteQueue("DIRECT=OS:localhost\\private$\\Test");
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {

                from("direct:input").to("msmq:DIRECT=OS:localhost\\private$\\Test");
                from("msmq:DIRECT=OS:localhost\\private$\\Test?concurrentConsumers=1").process(new Processor() {

                    public void process(Exchange exc) throws Exception {
                        ByteBuffer buffer = (ByteBuffer)exc.getIn().getBody();
                        int size = ((Long)exc.getIn().getHeader(MsmqConstants.BODY_SIZE)).intValue();
                        byte[] body = new byte[size];
                        buffer.rewind();
                        buffer.get(body, 0, size);
                        ByteArrayInputStream bis = new ByteArrayInputStream(body);
                        ObjectInputStream istream = new ObjectInputStream(bis);
                        Person person = (Person)istream.readObject();
                        Assert.assertTrue(person.getFirstName().equals("Antonio") && person.getSecondName().equals("Pusceddu"));
                        latch.countDown();
                    }
                });
            }
        };
    }
}
