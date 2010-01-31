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

import java.util.List;

import com.googlecode.easiest.cache.ever.MethodCall;


/**
 * Interface to allow various cache key-generation
 * strategies
 * 
 * @author Brad Cupit
 */
public interface KeyGenerator {
	/**
	 * Generates a unique method key for the method being executed.
	 * If the cache were implemented as a Map of Maps, this key would
	 * be the key for the first Map.
	 * 
	 * If the cache were implemented as a Map of Maps, this key would
	 * be the first key. Psuedo-cache definition:
	 * Map<MethodKey, Map<ParameterKey, CachedData>>
	 */
	String generateMethodKey(MethodCall methodCall);

	/**
	 * Generates a unique parameter key (or null for 0 parameter methods)
	 * for the parameters passed in when the current method was invoked.
	 * 
	 * We cache the result based on the parameters passed in (so we
	 * only return the cached result if the method is being called again
	 * with the same parameters).
	 * 
	 * If the cache were implemented as a Map of Maps, this key would
	 * be the second key. Psuedo-cache definition:
	 * Map<MethodKey, Map<ParameterKey, CachedData>>
	 */
	String generateParameterKey(List<?> arguments);
}
