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
package org.apache.camel.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.support.CamelContextHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MainBeansTest {

    private static Main main;
    private static MyFoo myFoo;

    @BeforeAll
    static void setup() {
        myFoo = new MyFoo();

        main = new Main();
        main.configure().addRoutesBuilder(new MyRouteBuilder());
        main.bind("myFoolish", myFoo);

        // create by class
        main.addProperty("camel.beans.foo", "#class:org.apache.camel.main.MySedaBlockingQueueFactory");
        main.addProperty("camel.beans.foo.counter", "123");

        // lookup by type
        main.addProperty("camel.beans.myfoo", "#type:org.apache.camel.main.MyFoo");
        main.addProperty("camel.beans.myfoo.name", "Donkey");
        main.addProperty("camel.beans.myfoo.map[key1]", "value1");
        main.addProperty("camel.beans.myfoo.map[key2]", "value2");
        main.addProperty("camel.beans.myfoo.map[key3]", "value3");

        main.start();
    }

    @AfterAll
    static void tearDown() {
        main.stop();
    }

    @Test
    public void testBindBeans() throws Exception {
        CamelContext camelContext = main.getCamelContext();
        assertNotNull(camelContext);

        Object foo = camelContext.getRegistry().lookupByName("foo");
        assertNotNull(foo);

        MySedaBlockingQueueFactory myBQF = camelContext.getRegistry().findByType(MySedaBlockingQueueFactory.class).iterator().next();
        assertSame(foo, myBQF);

        assertEquals(123, myBQF.getCounter());
        assertEquals("Donkey", myFoo.getName());
        assertEquals(3, myFoo.getMap().size());
        assertTrue(myFoo.getMap().containsKey("key1"));
        assertTrue(myFoo.getMap().containsKey("key2"));
        assertTrue(myFoo.getMap().containsKey("key3"));
        assertEquals("value1", myFoo.getMap().get("key1"));
        assertEquals("value2", myFoo.getMap().get("key2"));
        assertEquals("value3", myFoo.getMap().get("key3"));
    }

    @Test
    public void testLookupAndConvert() {
        CamelContext camelContext = main.getCamelContext();
        assertNotNull(camelContext);

        String valueNoHash = "#bean:myfoo?method=getName";
        String str = CamelContextHelper.lookupAndConvert(camelContext, valueNoHash, String.class);
        System.out.println(str);
    }

    public static class MyRouteBuilder extends RouteBuilder {
        @Override
        public void configure() throws Exception {
            from("direct:start").to("mock:foo");
        }
    }

}
