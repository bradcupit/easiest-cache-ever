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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to signal the return value of the method
 * should be cached.
 * 
 * If the annotation is applied to a method, the return
 * value of that method will be cached.
 * 
 * If the annotation is applied to a class, then every
 * public, non-void method in the class will have it's results
 * cached.
 * 
 * If the annotation is applied to a method and a class,
 * the annotation settings on the method take precedence
 * just for that annotated method. For the rest of the methods,
 * the class annotation settings are used. 
 * 
 * When the annotation is applied to a class, the settings
 * ({@link #maxSize()}, {@link #expirationTime()}, etc.)
 * are applied individually to each method. Meaning,
 * <code>
 * 	@CacheReturnValue(maxSize=12)
 * 	public class MyClass { ... }
 * </code>
 * Results in an individual {@link #maxSize()} of 12 per method.
 * Not a {@link #maxSize()} of 12 which all methods share.
 * 
 * Configuration
 * -------------
 * Values specified on the annotation itself will always take precedence. Example:
 * @CacheReturnValue(maxSize = 50)
 * maxSize will always be 50.
 * 
 * If no values are specified, the cache will use the default settings,
 * specified in a Spring xml config file. Example:
 * @CacheReturnValue
 * Here, maxSize will be the value set in the Spring xml config file:
 * 
 *	<bean class="com.googlecode.easiest.cache.ever.CacheAspect" scope="singleton">
 *		<property name="defaultMaxSize" value="100"/>
 *		<property name="defaultExpirationTime" value="1"/>
 *		<property name="defaultUnit" value="WEEKS"/>
 *		...
 *      <!-- or, for no expiration -->
 *		<property name="defaultExpirationTime">
 *			<util:constant static-field="com.googlecode.easiest.cache.ever.CacheConstants.NO_EXPIRATION"/>
 *		</property>
 *	</bean> 
 *
 * If defaults are not set in a Spring xml config file,
 * the hardcoded defaults in {@link CacheAspect} are used.
 * 
 * @author Brad Cupit
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD, ElementType.TYPE })
public @interface CacheReturnValue {
	/**
	 * The maximum number of elements to cache.
	 * If this limit is reached, the Least Recently
	 * Used element will be pushed out of the cache.
	 */
	int maxSize() default CacheConstants.UNSET_MAX_SIZE; // NOTE: the real default value is configured in a spring xml file

	/**
	 * Amount of time that passes before the
	 * element is expired and removed from
	 * the cache. See {@link #unit()} for
	 * the units this time is in.
	 * 
	 * when set to {@link CacheConstants#NO_EXPIRATION}, the elements do not expire.
	 */
	int expirationTime() default CacheConstants.UNSET_EXPIRATION_TIME; // NOTE: the real default value is configured in a spring xml file

	/**
	 * units/measurement for duration specified in {@link #expirationTime()}
	 */
	Time unit() default Time.UNSET; // NOTE: the real default value is configured in a spring xml file
}
