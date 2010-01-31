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

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import com.googlecode.easiest.cache.ever.caches.CacheService;
import com.googlecode.easiest.cache.ever.caches.CachedValue;
import com.googlecode.easiest.cache.ever.keys.KeyGenerator;


/**
 * AOP implementation for adding caching to methods/classes
 * containing the {@link CacheReturnValue @CacheReturnValue} annotation. 
 * 
 * @author Brad Cupit
 */
@Aspect
public class CacheAspect {
	private CacheService cacheService;
	private KeyGenerator keyGenerator;
	private int defaultMaxSize = 1024;
	private int defaultExpirationTime = CacheConstants.NO_EXPIRATION;
	private Time defaultUnit = Time.DAYS;

	public void setCacheService(CacheService cache) {
		this.cacheService = cache;
	}

	public void setKeyGenerator(KeyGenerator keyGenerator) {
		this.keyGenerator = keyGenerator;
	}

	/**
	 * see documentation in {@link CacheReturnValue#maxSize()}
	 */
	public void setDefaultMaxSize(int maxSize) {
		this.defaultMaxSize = maxSize;
	}

	/**
	 * see documentation in {@link CacheReturnValue#expirationTime()}
	 */
	public void setDefaultExpirationTime(int expirationTime) {
		this.defaultExpirationTime = expirationTime;
	}

	/**
	 * Unit/measurement for the duration specified in {@link #setDefaultExpirationTime(int)}
	 */
	public void setDefaultUnit(Time unit) {
		this.defaultUnit = unit;
	}

	@Around("annotatedMethod(methodAnnotation) && methodWithReturnValue()")
	public Object aroundAdviceForMethodAnnotation(ProceedingJoinPoint joinPoint, CacheReturnValue methodAnnotation)
				throws Throwable {
		return aroundAdvice(joinPoint, methodAnnotation);
	}

	@Around("annotatedClass(classAnnotation) && methodNotAnnotated() && publicMethod() && methodWithReturnValue()")
	public Object aroundAdviceForClassAnnotation(ProceedingJoinPoint joinPoint, CacheReturnValue classAnnotation)
				throws Throwable {
		return aroundAdvice(joinPoint, classAnnotation);
	}

	private Object aroundAdvice(ProceedingJoinPoint joinPoint, CacheReturnValue cacheAnnotation) throws Throwable {
		final MethodCall methodCall = buildMethodCall(joinPoint);
		final String cacheId = keyGenerator.generateMethodKey(methodCall);
		final String key = keyGenerator.generateParameterKey(methodCall.getParameters());

		final CacheConfig cacheConfig = buildCacheConfig(cacheAnnotation, methodCall.getParameters().size());
		cacheService.createCacheIfNecessary(cacheId, cacheConfig);

		final CachedValue cachedValue = cacheService.retrieve(cacheId, key);

		if (cachedValue.wasFound()) {
			return cachedValue.value();
		} else {
			final Object returnValue = joinPoint.proceed();
			cacheService.add(cacheId, key, returnValue);
			return returnValue;
		}
	}

	@Pointcut("execution(!void *(..))")
	protected void methodWithReturnValue() {
	}

	@Pointcut("execution(public * *(..))")
	protected void publicMethod() {
	}

	@Pointcut("@within(classAnnotation)")
	protected void annotatedClass(CacheReturnValue classAnnotation) {
	}

	@Pointcut("@annotation(methodAnnotation)")
	protected void annotatedMethod(CacheReturnValue methodAnnotation) {
	}

	@Pointcut("!@annotation(com.googlecode.easiest.cache.ever.CacheReturnValue)")
	protected void methodNotAnnotated() {
	}

	private MethodCall buildMethodCall(ProceedingJoinPoint joinPoint) {
		final MethodSignature methodSignature;

		if (joinPoint.getSignature() instanceof MethodSignature) {
			methodSignature = (MethodSignature) joinPoint.getSignature();
		} else {
			throw new RuntimeException("Spring can only join on methods, so casting to MethodSignature should always work.");
		}

		final String concreteClassName = joinPoint.getTarget().getClass().getName();

		return new MethodCall(concreteClassName, methodSignature.getName(), methodSignature.getParameterTypes(),
					joinPoint.getArgs());
	}

	private CacheConfig buildCacheConfig(CacheReturnValue cacheAnnotation, int numParameters) {
		final int maxSize;
		if (numParameters == 0) {
			maxSize = 1;
		} else if (cacheAnnotation.maxSize() == CacheConstants.UNSET_MAX_SIZE) {
			maxSize = defaultMaxSize;
		} else {
			maxSize = cacheAnnotation.maxSize();
		}

		final int expirationTime;
		if (cacheAnnotation.expirationTime() == CacheConstants.UNSET_EXPIRATION_TIME) {
			expirationTime = defaultExpirationTime;
		} else {
			expirationTime = cacheAnnotation.expirationTime();
		}

		final Time unit;
		if (cacheAnnotation.unit() == Time.UNSET) {
			unit = defaultUnit;
		} else {
			unit = cacheAnnotation.unit();
		}

		return new CacheConfig(maxSize, expirationTime, unit);
	}
}
