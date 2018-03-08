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
package org.apache.camel.component.msmq.native_support;

import junit.framework.Assert;
import junit.framework.TestCase;

public class NativeSupportTest extends TestCase {

    public void testSendReceive() {
        MsmqQueue sendQueue = new MsmqQueue();
        MsmqQueue receiveQueue = new MsmqQueue();

        sendQueue.open("DIRECT=OS:localhost\\private$\\test", msmq_native_support.MQ_SEND_ACCESS);
        receiveQueue.open("DIRECT=OS:localhost\\private$\\test", msmq_native_support.MQ_RECEIVE_ACCESS);

        String str = new String("Hello David");

        MsmqMessage message1 = new MsmqMessage();
        ByteArray sendbuffer = new ByteArray(str.length());
        message1.setMsgBody(sendbuffer.cast());
        message1.setBodySize(str.length());
        for (int i = 0; i < str.length(); ++i) {
            sendbuffer.setitem(i, str.getBytes()[i]);
        }

        sendQueue.sendMessage(message1);

        MsmqMessage message2 = new MsmqMessage();
        ByteArray recvbuffer = new ByteArray(str.length());
        message2.setMsgBody(recvbuffer.cast());
        message2.setBodySize(str.length());
        receiveQueue.receiveMessage(message2, -1);

        byte[] buffer = new byte[str.length()];
        for (int i = 0; i < str.length(); ++i) {
            buffer[i] = recvbuffer.getitem(i);
        }

        Assert.assertTrue(new String(buffer).equals(str));

        sendQueue.close();
        receiveQueue.close();
    }

    public void testSendReceiveWithSettingProperties() {
        MsmqQueue sendQueue = new MsmqQueue();
        MsmqQueue receiveQueue = new MsmqQueue();

        sendQueue.open("DIRECT=OS:localhost\\private$\\test", msmq_native_support.MQ_SEND_ACCESS);
        receiveQueue.open("DIRECT=OS:localhost\\private$\\test", msmq_native_support.MQ_RECEIVE_ACCESS);

        String str = new String("Hello David");

        MsmqMessage message1 = new MsmqMessage();

        message1.setAppSpecifc(1234);
        message1.setDelivery(msmq_native_support.MQMSG_DELIVERY_RECOVERABLE);
        message1.setCorrelationId("01234567890123456789".getBytes());
        message1.setPriority(5);
        message1.setTimeToBeReceived(1000);

        ByteArray sendbuffer = new ByteArray(str.length());
        message1.setMsgBody(sendbuffer.cast());
        message1.setBodySize(str.length());
        for (int i = 0; i < str.length(); ++i) {
            sendbuffer.setitem(i, str.getBytes()[i]);
        }

        sendQueue.sendMessage(message1);

        MsmqMessage message2 = new MsmqMessage();
        ByteArray recvbuffer = new ByteArray(str.length());
        message2.setMsgBody(recvbuffer.cast());
        message2.setBodySize(str.length());
        receiveQueue.receiveMessage(message2, -1);

        byte[] buffer = new byte[str.length()];
        for (int i = 0; i < str.length(); ++i) {
            buffer[i] = recvbuffer.getitem(i);
        }

        Assert.assertTrue(new String(buffer).equals(str));
        Assert.assertTrue(message2.getAppSpecific() == 1234);
        Assert.assertTrue(message2.getDelivery() == msmq_native_support.MQMSG_DELIVERY_RECOVERABLE);
        byte[] cid = new byte[msmq_native_support.PROPID_M_CORRELATIONID_SIZE];
        message2.getCorrelationId(cid);
        Assert.assertTrue(new String(cid).equals("01234567890123456789"));
        Assert.assertTrue(message2.getPriority() == 5);
        Assert.assertTrue(message2.getTimeToBeReceived() == 1000);

        sendQueue.close();
        receiveQueue.close();
    }

    public void testSendReceiveWithMsgBodySizeAdjustment() {
        MsmqQueue sendQueue = new MsmqQueue();
        MsmqQueue receiveQueue = new MsmqQueue();

        sendQueue.open("DIRECT=OS:localhost\\private$\\test", msmq_native_support.MQ_SEND_ACCESS);
        receiveQueue.open("DIRECT=OS:localhost\\private$\\test", msmq_native_support.MQ_RECEIVE_ACCESS);

        String str = new String("Hello David");

        MsmqMessage message1 = new MsmqMessage();
        ByteArray sendbuffer = new ByteArray(str.length());
        message1.setMsgBody(sendbuffer.cast());
        message1.setBodySize(str.length());
        for (int i = 0; i < str.length(); ++i) {
            sendbuffer.setitem(i, str.getBytes()[i]);
        }

        sendQueue.sendMessage(message1);

        MsmqMessage message2 = new MsmqMessage();
        int initsize = 1;
        boolean cont = true;
        ByteArray recvbuffer = new ByteArray(initsize);
        message2.setMsgBody(recvbuffer.cast());
        message2.setBodySize(initsize);

        while (cont) {
            try {
                receiveQueue.receiveMessage(message2, -1);
                cont = false;
            } catch (RuntimeException ex) {
                if (ex.getMessage().equals("Message body too big")) {
                    initsize += 5;
                    recvbuffer = new ByteArray(initsize);
                    message2.setMsgBody(recvbuffer.cast());
                    message2.setBodySize(initsize);
                } else {
                    throw ex;
                }
            }
        }

        byte[] buffer = new byte[str.length()];
        for (int i = 0; i < str.length(); ++i) {
            buffer[i] = recvbuffer.getitem(i);
        }

        Assert.assertTrue(new String(buffer).equals(str));
        Assert.assertTrue(message2.getBodySize() == str.length());

        sendQueue.close();
        receiveQueue.close();
    }

    @Override
    public void setUp() {
        MsmqQueue.createQueue(".\\Private$\\Test");
    }

    @Override
    public void tearDown() {
        MsmqQueue.deleteQueue("DIRECT=OS:localhost\\private$\\test");
    }
}
