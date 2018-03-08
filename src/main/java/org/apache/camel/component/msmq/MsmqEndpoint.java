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

import java.util.Map;

import org.apache.camel.Consumer;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.component.msmq.native_support.msmq_native_support;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.impl.DefaultExchange;

/**
 * @version $Revision: 14846 $
 */
public class MsmqEndpoint extends DefaultEndpoint {

    private final String remaining;
    private final Map parameters;

    private boolean deliveryPersistent;
    private int timeToLive = msmq_native_support.INFINITE;
    private int priority = 3;

    private int concurrentConsumers = 1;
    private int initialBufferSize = 128;
    private int incrementBufferSize = 128;

    public MsmqEndpoint(String endpointUri, String remaining, Map parameters, MsmqComponent component) {
        super(endpointUri, component);
        this.remaining = remaining;
        this.parameters = parameters;
        setExchangePattern(ExchangePattern.InOnly);
    }

    public Producer createProducer() throws Exception {
        return new MsmqProducer(this);
    }

    @Override
    public DefaultExchange createExchange() {
        return new DefaultExchange(getCamelContext(), getExchangePattern());
    }

    public boolean isSingleton() {
        return true;
    }

    public String getRemaining() {
        return remaining;
    }

    public Map getParameters() {
        return parameters;
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        return new MsmqConsumer(this, processor);
    }

    public void setDeliveryPersistent(boolean deliveryPersistent) {
        this.deliveryPersistent = deliveryPersistent;
    }

    public boolean getDeliveryPersistent() {
        return deliveryPersistent;
    }

    public void setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public void setConcurrentConsumers(int concurrentConsumers) {
        this.concurrentConsumers = concurrentConsumers;
    }

    public int getConcurrentConsumers() {
        return concurrentConsumers;
    }

    public void setInitialBufferSize(int initialBufferSize) {
        this.initialBufferSize = initialBufferSize;
    }

    public int getInitialBufferSize() {
        return initialBufferSize;
    }

    public void setIncrementBufferSize(int incrementBufferSize) {
        this.incrementBufferSize = incrementBufferSize;
    }

    public int getIncrementBufferSize() {
        return incrementBufferSize;
    }

}
