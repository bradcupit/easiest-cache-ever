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

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;

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
	public void runsMethodAndCachesResults() throws Throwable {
		setupMocksForZeroInputParamCacheMethod();
		Object objectToCache = new Object();

		when(mockJoinPoint.proceed()).thenReturn(objectToCache);
		when(mockCacheService.retrieve(anyString(), anyString())).thenReturn(CachedValue.notFound());

		cacheAspect.setKeyGenerator(new DefaultKeyGenerator());
		cacheAspect.aroundAdviceForMethodAnnotation(mockJoinPoint, mockCacheAnnotation);

		verify(mockCacheService).add(fullMethodName, null, objectToCache);
	}

	@Test
	public void doesNotRunMethodWhenResultIsAlreadyCached() throws Throwable {
		setupMocksForZeroInputParamCacheMethod();
		when(mockCacheService.retrieve(anyString(), anyString())).thenReturn(cachedValue);

		cacheAspect.setKeyGenerator(new DefaultKeyGenerator());
		cacheAspect.aroundAdviceForMethodAnnotation(mockJoinPoint, mockCacheAnnotation);

		verify(mockCacheService).retrieve(fullMethodName, null);
		verify(mockJoinPoint, never()).proceed();
		verify(mockCacheService, never()).add(anyString(), anyString(), anyObject());
	}

	@Test
	public void canOverrideCacheDefaults() throws Throwable {
		setupMocksForOneInputParamCacheMethod();
		when(mockCacheService.retrieve(anyString(), anyString())).thenReturn(cachedValue);

		when(mockCacheAnnotation.maxSize()).thenReturn(CacheConstants.UNSET_MAX_SIZE);
		when(mockCacheAnnotation.expirationTime()).thenReturn(CacheConstants.UNSET_EXPIRATION_TIME);
		when(mockCacheAnnotation.unit()).thenReturn(Time.UNSET);

		int expectedMaxSize = 1200;
		int expectedExpirationTime = -52;
		Time expectedUnit = Time.MICROSECONDS;

		cacheAspect.setDefaultMaxSize(expectedMaxSize);
		cacheAspect.setDefaultExpirationTime(expectedExpirationTime);
		cacheAspect.setDefaultUnit(expectedUnit);

		cacheAspect.aroundAdviceForMethodAnnotation(mockJoinPoint, mockCacheAnnotation);

		CacheConfig expectedCacheConfig = new CacheConfig(expectedMaxSize, expectedExpirationTime, expectedUnit);
		verify(mockCacheService).createCacheIfNecessary(anyString(), refEq(expectedCacheConfig));
	}

	/**
	 * {@link Signature#getDeclaringTypeName()} on a JDK proxy returns
	 * the interface name not the concrete class name, so we use {@link ProceedingJoinPoint#getTarget()}.getClass().getName()
	 * instead. A similar thing happens for {@link MethodSignature#getMethod()} with
	 * JDK proxies vs CGLIB proxies.
	 */
	@Test
	public void createsMethodCallWithConcreteClassNameNotInterfaceName() throws Throwable {
		setupMocksForZeroInputParamCacheMethod();
		when(mockCacheService.retrieve(anyString(), anyString())).thenReturn(cachedValue);

		String fullMethodNameOnConcreteClass = TestClass.class.getName() + "." + methodName;
		String fullMethodNameOnInterface = TestInterface.class.getName() + "." + methodName;

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
	public void maxSizeIsOneForZeroParamMethods() throws Throwable {
		setupMocksForZeroInputParamCacheMethod();

		when(mockCacheService.retrieve(anyString(), anyString())).thenReturn(cachedValue);
		when(mockCacheAnnotation.maxSize()).thenReturn(1000);

		cacheAspect.aroundAdviceForMethodAnnotation(mockJoinPoint, mockCacheAnnotation);

		CacheConfig expectedCacheConfig = new CacheConfig(1, 0, null);
		verify(mockCacheService).createCacheIfNecessary(anyString(), refEq(expectedCacheConfig));
	}

	/**
	 * {@link CacheService#retrieve(String, String)} used to
	 * return null when the value was not in cache AND when
	 * the value was cached but was null. So, cached 'null' was same
	 * as an uncached value. This test proves that inefficiency is gone.
	 */
	@Test
	public void doesNotInvokeJoinPointWhenNullWasCached() throws Throwable {
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

	public static class TestClass implements TestInterface {
	}

	public interface TestInterface {
	}
}
