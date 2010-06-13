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
package com.googlecode.easiest.cache.ever;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.googlecode.easiest.cache.ever.caches.CacheService;
import com.googlecode.easiest.cache.ever.caches.CachedValue;
import com.googlecode.easiest.cache.ever.keys.DefaultKeyGenerator;
import com.thoughtworks.xstream.XStream;

/**
 * unit test for {@link CacheAspect}
 * 
 * @author Brad Cupit
 */
public class CacheAspectTest {
    private final String methodName = "methodName";
    private final String fullMethodName = TestClass.class.getName() + "." + methodName;
    private final Object[] zeroParams = new Object[0];
    private final Class<?>[] zeroParamClasses = new Class<?>[0];
    private final Object[] oneParams = new Object[] { "string" };
    private final Class<?>[] oneParamClasses = new Class<?>[] { String.class };
    private final CachedValue cachedValue = CachedValue.create(new Object());
    private final ProceedingJoinPoint mockJoinPoint = mock(ProceedingJoinPoint.class);
    private final MethodSignature mockMethodSignature = mock(MethodSignature.class);
    private final CacheReturnValue mockCacheAnnotation = mock(CacheReturnValue.class);
    private final CacheService mockCacheService = mock(CacheService.class);
    private final CacheAspect cacheAspect = new CacheAspect();

    @Before
    public void before() {
        DefaultKeyGenerator keyGenerator = new DefaultKeyGenerator();
        keyGenerator.setXstream(new XStream());
        cacheAspect.setKeyGenerator(keyGenerator);
        cacheAspect.setCacheService(mockCacheService);
    }

    @Test
    public void aroundAdviceForMethodAnnotationShouldInvokeMethodWhenReturnValueNotYetCached() throws Throwable {
        setupMocksForZeroInputParamCacheMethod();
        Object objectToCache = new Object();

        when(mockJoinPoint.proceed()).thenReturn(objectToCache);
        when(mockCacheService.retrieve(anyString(), anyString())).thenReturn(CachedValue.notFound());

        cacheAspect.aroundAdviceForMethodAnnotation(mockJoinPoint, mockCacheAnnotation);

        verify(mockJoinPoint).proceed();
    }

    @Test
    public void aroundAdviceForMethodAnnotationShouldCacheReturnValueWhenNotYetCached() throws Throwable {
        setupMocksForZeroInputParamCacheMethod();
        Object objectToCache = new Object();

        when(mockJoinPoint.proceed()).thenReturn(objectToCache);
        when(mockCacheService.retrieve(anyString(), anyString())).thenReturn(CachedValue.notFound());

        cacheAspect.aroundAdviceForMethodAnnotation(mockJoinPoint, mockCacheAnnotation);

        verify(mockCacheService).add(fullMethodName, null, objectToCache);
    }

    @Test
    public void aroundAdviceForMethodAnnotationShouldNotInvokeMethodWhenReturnValueAlreadyCached() throws Throwable {
        setupMocksForZeroInputParamCacheMethod();
        when(mockCacheService.retrieve(anyString(), anyString())).thenReturn(cachedValue);

        cacheAspect.aroundAdviceForMethodAnnotation(mockJoinPoint, mockCacheAnnotation);

        verify(mockJoinPoint, never()).proceed();
    }

    @Test
    public void aroundAdviceForMethodAnnotationShouldNotCacheReturnValueWhenAlreadyCached() throws Throwable {
        setupMocksForZeroInputParamCacheMethod();
        when(mockCacheService.retrieve(anyString(), anyString())).thenReturn(cachedValue);

        cacheAspect.aroundAdviceForMethodAnnotation(mockJoinPoint, mockCacheAnnotation);

        verify(mockCacheService, never()).add(anyString(), anyString(), anyObject());
    }

    /**
     * prove the users can set defaults via Spring xml config
     */
    @Test
    public void setDefaultMaxSizeShouldShouldOverrideTheDefaultSetting() throws Throwable {
        setupMocksForOneInputParamCacheMethod();
        when(mockCacheService.retrieve(anyString(), anyString())).thenReturn(cachedValue);
        when(mockCacheAnnotation.maxSize()).thenReturn(CacheConstants.UNSET_MAX_SIZE);

        int expectedMaxSize = 1200;
        cacheAspect.setDefaultMaxSize(expectedMaxSize);

        cacheAspect.aroundAdviceForMethodAnnotation(mockJoinPoint, mockCacheAnnotation);
        ArgumentCaptor<CacheConfig> argument = ArgumentCaptor.forClass(CacheConfig.class);
        verify(mockCacheService).createCacheIfNecessary(anyString(), argument.capture());
        assertThat(argument.getValue().getMaxSize(), is(expectedMaxSize));
    }

    /**
     * prove the users can set defaults via Spring xml config
     */
    @Test
    public void setDefaultExpirationTimeShouldShouldOverrideTheDefaultSetting() throws Throwable {
        setupMocksForOneInputParamCacheMethod();
        when(mockCacheService.retrieve(anyString(), anyString())).thenReturn(cachedValue);
        when(mockCacheAnnotation.expirationTime()).thenReturn(CacheConstants.UNSET_EXPIRATION_TIME);

        int expectedExpirationTime = 52;
        cacheAspect.setDefaultExpirationTime(expectedExpirationTime);

        cacheAspect.aroundAdviceForMethodAnnotation(mockJoinPoint, mockCacheAnnotation);
        ArgumentCaptor<CacheConfig> argument = ArgumentCaptor.forClass(CacheConfig.class);
        verify(mockCacheService).createCacheIfNecessary(anyString(), argument.capture());
        assertThat(argument.getValue().getExpirationTime(), is(expectedExpirationTime));
    }

    /**
     * prove the users can set defaults via Spring xml config
     */
    @Test
    public void setDefaultUnitShouldShouldOverrideTheDefaultSetting() throws Throwable {
        setupMocksForOneInputParamCacheMethod();
        when(mockCacheService.retrieve(anyString(), anyString())).thenReturn(cachedValue);
        when(mockCacheAnnotation.unit()).thenReturn(Time.UNSET);

        Time expectedUnit = Time.MICROSECONDS;
        cacheAspect.setDefaultUnit(expectedUnit);

        cacheAspect.aroundAdviceForMethodAnnotation(mockJoinPoint, mockCacheAnnotation);
        ArgumentCaptor<CacheConfig> argument = ArgumentCaptor.forClass(CacheConfig.class);
        verify(mockCacheService).createCacheIfNecessary(anyString(), argument.capture());
        assertThat(argument.getValue().getUnit(), is(expectedUnit));
    }

    /**
     * This test ensures our cache names are unique and that two separate methods (which
     * happen to implement the same interface) still get unique cache names (and therefore
     * unique caches with separate settings). We accomplish with this pattern:
     * fully.qualified.ConcreteClassName.methodName (we cannot use the fully qualified
     * interface name).
     * 
     * {@link Signature#getDeclaringTypeName()} on a JDK proxy returns
     * the interface name not the concrete class name, so we use {@link ProceedingJoinPoint#getTarget()}.getClass().getName()
     * instead. A similar thing happens for {@link MethodSignature#getMethod()} with
     * JDK proxies vs CGLIB proxies.
     */
    @Test
    public void aroundAdviceForMethodAnnotationShouldGenerateCacheNameWithConcreteClassNameNotInterfaceName() throws Throwable {
        setupMocksForZeroInputParamCacheMethod();
        when(mockCacheService.retrieve(anyString(), anyString())).thenReturn(cachedValue);

        String fullMethodNameOnConcreteClass = TestClass.class.getName() + "." + methodName;
        String fullMethodNameOnInterface = TestInterface.class.getName() + "." + methodName;

        // setup AspectJ's MethodSignature to behave like it does in a real AOP setting
        when(mockMethodSignature.getName()).thenReturn(methodName);
        when(mockMethodSignature.getDeclaringTypeName()).thenReturn(fullMethodNameOnInterface);

        cacheAspect.aroundAdviceForMethodAnnotation(mockJoinPoint, mockCacheAnnotation);

        verify(mockCacheService).retrieve(eq(fullMethodNameOnConcreteClass), anyString());
        verify(mockCacheService, never()).retrieve(eq(fullMethodNameOnInterface), anyString());
    }

    /**
     * test a small optimization. if the intercepted method takes no parameters,
     * it will always return the same value, so maxSize will be 1, regardless
     * of configuration.
     */
    @Test
    public void aroundAdviceForMethodAnnotationShouldSetMaxSizeToOneForZeroParamMethods() throws Throwable {
        setupMocksForZeroInputParamCacheMethod();

        int one = 1;
        int somethingOtherThanOne = 1000;

        when(mockCacheService.retrieve(anyString(), anyString())).thenReturn(cachedValue);
        when(mockCacheAnnotation.maxSize()).thenReturn(somethingOtherThanOne);

        cacheAspect.aroundAdviceForMethodAnnotation(mockJoinPoint, mockCacheAnnotation);
        ArgumentCaptor<CacheConfig> argument = ArgumentCaptor.forClass(CacheConfig.class);
        verify(mockCacheService).createCacheIfNecessary(anyString(), argument.capture());
        assertThat(argument.getValue().getMaxSize(), is(one));
    }

    /**
     * {@link CacheService#retrieve(String, String)} used to
     * return null when the value was not in cache AND when
     * the value was cached but was null. So, cached 'null' was the same
     * as an uncached value (and would incorrectly invoke the method again).
     * This test proves that inefficiency is gone.
     */
    @Test
    public void aroundAdviceForMethodAnnotationShouldNotInvokeMethodWhenNullWasCached() throws Throwable {
        setupMocksForZeroInputParamCacheMethod();

        CachedValue nullValue = CachedValue.create(null);
        when(mockCacheService.retrieve(anyString(), anyString())).thenReturn(nullValue);

        cacheAspect.aroundAdviceForMethodAnnotation(mockJoinPoint, mockCacheAnnotation);

        verify(mockJoinPoint, never()).proceed();
    }

    private void setupMocksForZeroInputParamCacheMethod() {
        setupCommonMockInvocations();

        when(mockJoinPoint.getArgs()).thenReturn(zeroParams);
        when(mockMethodSignature.getParameterTypes()).thenReturn(zeroParamClasses);
    }

    private void setupMocksForOneInputParamCacheMethod() {
        setupCommonMockInvocations();

        when(mockJoinPoint.getArgs()).thenReturn(oneParams);
        when(mockMethodSignature.getParameterTypes()).thenReturn(oneParamClasses);
    }

    private void setupCommonMockInvocations() {
        when(mockJoinPoint.getTarget()).thenReturn(new TestClass());
        when(mockJoinPoint.getSignature()).thenReturn(mockMethodSignature);

        when(mockMethodSignature.getName()).thenReturn(methodName);
    }

    /**
     * utility test class
     * 
     * @author Brad Cupit
     */
    public static class TestClass implements TestInterface {
    }

    /**
     * utility test interface
     * 
     * @author Brad Cupit
     */
    public interface TestInterface {
    }
}
