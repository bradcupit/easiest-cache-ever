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
package com.googlecode.easiest.cache.ever.caches;

/**
 * Simple wrapper class which tells us if the value
 * was found in the cache.
 * 
 * Previous versions used 'null' to determine if the value
 * was in the cache, however, there was no way to distinguish
 * if the value was not found vs. if the value was found but
 * was null.
 * 
 * @author Brad Cupit
 */
public class CachedValue {
	private final boolean found;
	private final Object value;

	/**
	 * private constructor. use the builder methods below
	 * to instantiate.
	 */
	private CachedValue(boolean found, Object value) {
		this.found = found;
		this.value = value;
	}

	/**
	 * builder method. Used when the object was
	 * found in the cache. This is simpler,
	 * and more readable than calling the
	 * constructor directly.
	 */
	public static CachedValue create(Object value) {
		return new CachedValue(true, value);
	}

	/**
	 * builder method. Used when the object was
	 * not found in the cache. This is simpler,
	 * and more readable than calling the
	 * constructor directly.
	 */
	public static CachedValue notFound() {
		return new CachedValue(false, null);
	}

	/**
	 * return the value as found in the cache.
	 * If {@link #wasFound()} is false, this will
	 * always be null.
	 * If {@link #wasFound()} is true, this can be
	 * null (meaning, null was stored in the cache).
	 */
	public Object value() {
		return value;
	}

	/**
	 * was the object found in the cache?
	 */
	public boolean wasFound() {
		return found;
	}
}
