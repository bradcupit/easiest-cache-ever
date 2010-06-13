/*
 * Copyright 2010 Brad Cupit
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.easiest.cache.ever.keys;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.googlecode.easiest.cache.ever.MethodCall;
import com.thoughtworks.xstream.XStream;

/**
 * unit test for {@link DefaultKeyGenerator}
 * 
 * @author Brad Cupit
 */
public class DefaultKeyGeneratorTest {
    private final DefaultKeyGenerator keyGenerator = buildDefaultKeyGenerator();

    private final Class<?>[] zeroParamTypes = new Class<?>[0];
    private final Object[] zeroParams = new Object[0];
    private final List<Object> zeroParamsList = new ArrayList<Object>();
    private final String methodName = "methodName";
    private final String className = TestClass.class.getName();

    /**
     * One of the ways we get a unique cache key is to include the fully-qualified
     * method name (com.something.MyClass.someMethod), as the JVM will ensure this is
     * a unique name.
     * 
     * note: this only applies to zero-arg methods. Methods with arguments need
     * the argument types (to differentiate overloaded methods).
     */
    @Test
    public void generateMethodKeyShouldReturnFullyQualifiedMethodNameForZeroParams() throws Exception {
        MethodCall methodCall = new MethodCall(className, methodName, zeroParamTypes, zeroParams);

        String methodKey = keyGenerator.generateMethodKey(methodCall);

        String fullyQualifiedMethodName = className + "." + methodName;
        assertEquals(fullyQualifiedMethodName, methodKey);
    }

    /**
     * a fully qualified, unique method name must also include the argument types
     * (to differentiate between overloaded methods).
     * 
     * Do we really need this?
     * -----------------------
     * To get a unique cache key, we need to include the arguments passed in.
     * We serialize those arguments with xstream, which includes the fully-qualified
     * class name in the xml: <com.something.MyClass> ... </com.something.MyClass>
     * 
     * Q: Since the xml already includes the class name, why do we also need it as part
     * of the fully-qualified-method-name? Isn't it duplicated?
     * 
     * A: Yes, but it's necessary to cover a very rare edge case, where the same argument
     * is passed to two different overloaded methods:
     * 
     * public void run(String s) {}
     * public void run(Object o) {}
     * 
     * run("hi");           // calls run(String s)
     * run((Object) "hi");  // calls run(Object o)
     * 
     * If we didn't have the below test and corresponding code, the two cache names and
     * keys would be the same, resulting in the same result for two different method calls:
     *   cache name:  com.something.SomeClass.run
     *   cache key:   <string>hi</string>
     * 
     * With this feature, though the cache keys are the same (<string>hi</string>) the cache
     * names are different:
     *   com.something.SomeClass.run(java.lang.String)
     *   com.something.SomeClass.run(java.lang.Object)
     */
    @Test
    public void generateMethodKeyShouldReturnKeyEndingWithFullyQualifiedClassesOfParams() throws Exception {
        Class<?>[] twoArgsTypes = new Class<?>[] { TestArgument1.class, TestArgument2.class };
        Object[] twoArgs = new Object[] { new TestArgument1(), new TestArgument2() };
        MethodCall methodCall = new MethodCall(className, methodName, twoArgsTypes, twoArgs);

        String methodKey = keyGenerator.generateMethodKey(methodCall);

        String fullyQualifiedMethodArgs = "(" + TestArgument1.class.getName() + "," + TestArgument2.class.getName() + ")";
        assertThat(methodKey, endsWith(fullyQualifiedMethodArgs));
    }

    /**
     * the "method key" is the name of the cache, and the "parameter key" is the
     * key in that cache. Also, for zero parameters there will only be one entry
     * in that cache. Null makes more sense than "" in that case.
     */
    @Test
    public void generateParameterKeyShouldReturnNullForZeroParams() throws Exception {
        String parameterKey = keyGenerator.generateParameterKey(zeroParamsList);

        assertNull(parameterKey);
    }

    /**
     * test it handles multiple arguments, and arguments are serialized
     * as an array, not a collection (xstream's serialized array is shorter
     * than serialized collections)
     */
    @Test
    public void generateParameterKeyShouldEndWithSerializedParams() throws Exception {
        String firstParameter = "hi";
        Integer secondParameter = 42;
        List<?> parameters = Arrays.asList((Object) firstParameter, (Object) secondParameter);

        String cacheKey = keyGenerator.generateParameterKey(parameters);

        assertThat(cacheKey, endsWith("<object-array><string>" + firstParameter + "</string><int>" + secondParameter
                    + "</int></object-array>"));
    }

    /**
     * with only one parameter there is no need to take up extra space with
     * <object-array> and </object-array> so we save memory by discarding it
     */
    @Test
    public void parameterKeyShouldEndWithOptimizedArgumentsForOneArgMethods() throws Exception {
        String argument = "hi";
        String cacheKey = keyGenerator.generateParameterKey(Arrays.asList(argument));

        assertThat(cacheKey, endsWith("<string>" + argument + "</string>"));
    }

    private DefaultKeyGenerator buildDefaultKeyGenerator() {
        DefaultKeyGenerator defaultKeyGenerator = new DefaultKeyGenerator();
        defaultKeyGenerator.setXstream(new XStream());

        return defaultKeyGenerator;
    }

    /**
     * empty class used for testing
     * 
     * @author Brad Cupit
     */
    public static class TestClass {
    }

    /**
     * empty class used for testing
     * 
     * @author Brad Cupit
     */
    public static class TestArgument1 {
    }

    /**
     * empty class used for testing
     * 
     * @author Brad Cupit
     */
    public static class TestArgument2 {
    }
}
