/*
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
package org.apache.camel.quarkus.component.micrometer.deployment;

import java.util.List;
import java.util.Optional;

import io.quarkus.test.QuarkusUnitTest;
import jakarta.inject.Inject;
import org.apache.camel.CamelContext;
import org.apache.camel.component.micrometer.eventnotifier.MicrometerExchangeEventNotifier;
import org.apache.camel.component.micrometer.eventnotifier.MicrometerRouteEventNotifier;
import org.apache.camel.component.micrometer.eventnotifier.MicrometerRouteEventNotifierNamingStrategy;
import org.apache.camel.component.micrometer.routepolicy.MicrometerRoutePolicyConfiguration;
import org.apache.camel.component.micrometer.routepolicy.MicrometerRoutePolicyFactory;
import org.apache.camel.component.micrometer.routepolicy.MicrometerRoutePolicyNamingStrategy;
import org.apache.camel.component.micrometer.spi.InstrumentedThreadPoolFactory;
import org.apache.camel.impl.engine.DefaultMessageHistoryFactory;
import org.apache.camel.spi.EventNotifier;
import org.apache.camel.spi.MessageHistoryFactory;
import org.apache.camel.spi.RoutePolicyFactory;
import org.apache.camel.spi.ThreadPoolFactory;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MicrometerMetricsConfigDefaultsTest {

    @RegisterExtension
    static final QuarkusUnitTest CONFIG = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

    @Inject
    CamelContext context;

    @Test
    public void testMicrometerMetricsConfiguration() {
        List<RoutePolicyFactory> routePolicyFactories = context.getRoutePolicyFactories();
        assertEquals(1, routePolicyFactories.size());
        RoutePolicyFactory routePolicyFactory = routePolicyFactories.get(0);
        assertInstanceOf(MicrometerRoutePolicyFactory.class, routePolicyFactory);
        MicrometerRoutePolicyFactory micrometerRoutePolicyFactory = (MicrometerRoutePolicyFactory) routePolicyFactory;
        assertEquals(MicrometerRoutePolicyNamingStrategy.DEFAULT, micrometerRoutePolicyFactory.getNamingStrategy());

        MicrometerRoutePolicyConfiguration policyConfiguration = micrometerRoutePolicyFactory.getPolicyConfiguration();
        assertTrue(policyConfiguration.isContextEnabled());
        assertTrue(policyConfiguration.isRouteEnabled());
        assertNull(policyConfiguration.getExcludePattern());

        MessageHistoryFactory messageHistoryFactory = context.getMessageHistoryFactory();
        assertNotNull(messageHistoryFactory);
        assertInstanceOf(DefaultMessageHistoryFactory.class, messageHistoryFactory);

        List<EventNotifier> eventNotifiers = context.getManagementStrategy()
                .getEventNotifiers()
                .stream()
                .filter(eventNotifier -> !eventNotifier.getClass().getName().contains("BaseMainSupport"))
                .toList();
        assertEquals(3, eventNotifiers.size());

        Optional<EventNotifier> optionalExchangeEventNotifier = context.getManagementStrategy()
                .getEventNotifiers()
                .stream()
                .filter(eventNotifier -> eventNotifier.getClass().equals(MicrometerExchangeEventNotifier.class))
                .findFirst();
        assertTrue(optionalExchangeEventNotifier.isPresent());

        MicrometerExchangeEventNotifier micrometerExchangeEventNotifier = (MicrometerExchangeEventNotifier) optionalExchangeEventNotifier
                .get();
        assertTrue(micrometerExchangeEventNotifier.getNamingStrategy().isBaseEndpointURI());

        Optional<EventNotifier> optionalRouteEventNotifier = context.getManagementStrategy()
                .getEventNotifiers()
                .stream()
                .filter(eventNotifier -> eventNotifier.getClass().equals(MicrometerRouteEventNotifier.class))
                .findFirst();
        assertTrue(optionalRouteEventNotifier.isPresent());

        MicrometerRouteEventNotifier micrometerRouteEventNotifier = (MicrometerRouteEventNotifier) optionalRouteEventNotifier
                .get();
        assertEquals(MicrometerRouteEventNotifierNamingStrategy.DEFAULT, micrometerRouteEventNotifier.getNamingStrategy());

        ThreadPoolFactory threadPoolFactory = context.getExecutorServiceManager().getThreadPoolFactory();
        assertNotNull(threadPoolFactory);
        assertFalse(threadPoolFactory instanceof InstrumentedThreadPoolFactory);
    }
}
