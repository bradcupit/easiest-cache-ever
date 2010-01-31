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

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import com.googlecode.easiest.cache.ever.CacheConfig;
import com.googlecode.easiest.cache.ever.CacheConstants;
import com.rits.cloning.Cloner;


/**
 * The default, out-of-the-box {@link CacheService}
 * implementation.
 * 
 * @author Brad Cupit
 */
public class DefaultCacheService implements CacheService {
	protected static final boolean OVERFLOW_TO_DISK = false;
	protected static final boolean DISK_PERSISTENT = false;
	private static final int EHCACHE_NO_EXPIRATION = 0;
	private static final int EHCACHE_SMALLEST_TIMEOUT_IN_SECONDS = 1;
	private static final Object NULL_KEY = new Object();

	private Cloner cloner;
	private CacheManager ehcacheManager;
	private final Object addNewCacheLock = new Object();

	public void setCloner(Cloner cloner) {
		this.cloner = cloner;
	}

	public void setEhcacheManager(CacheManager ehCacheManager) {
		this.ehcacheManager = ehCacheManager;
	}

	/**
	 * 
	 * Creates a new cache which can be configured separately
	 * from all other caches.
	 * 
	 * Think of the {@link CacheService} as a Map of Maps: Map<cacheId, Map<key, value>>
	 */
	public void createCacheIfNecessary(String cacheId, CacheConfig cacheConfig) {
		// Most of the time, the cache will already exist. Avoid the
		// synchronization bottleneck by checking for existence first
		if (!ehcacheManager.cacheExists(cacheId)) {
			boolean eternal = isEternal(cacheConfig);
			long timeToLiveSeconds = getTimeToLiveInSeconds(cacheConfig);

			// synchronize to avoid ObjectExistsException, thrown when the same cache
			// is added twice. See CacheManager#addCacheNoCheck(Ehcache)
			//
			// Without the synchronization, two threads could check if
			// the cache exists, both receive 'false', then both try to add
			// the same cache id, and an exception is thrown for one of the threads.
			//
			// With synchronization, the first thread would add the cache while
			// the other waits. When the original thread releases the synchronization,
			// the cache has been added and calls to CacheManager#cacheExists(..)
			// for the same cache id return true. The 2nd thread does not create
			// the cache and safely returns;
			synchronized (addNewCacheLock) {
				// This may look like double-checked locking, but it's different.
				// Double-checked locking is problematic with non-volatile fields.
				// We're not using a field, we're putting a value in a
				// synchronized Map (which Ehcache manages internally).
				//
				// Double-checked locking can be fixed by synchronizing reads.
				// We have synchronized reads since the Ehcache internal
				// Map uses synchronization.
				if (!ehcacheManager.cacheExists(cacheId)) {
					Ehcache ehcache = new Cache(cacheId, cacheConfig.getMaxSize(), MemoryStoreEvictionPolicy.LRU,
								OVERFLOW_TO_DISK, null, eternal, timeToLiveSeconds, 0, DISK_PERSISTENT, 0, null);

					ehcacheManager.addCache(ehcache);
				}
			}
		}
	}

	/**
	 * Add an object to the cache. The object does not need to be serializable.
	 * 
	 * @param cacheId name of the cache to add the object to. If the cache were a Map of Maps,
	 *                this would be the key in the first map. Example: a fully qualified method name.
	 * @param key     a unique key for the corresponding value. If the cache were a Map of Maps,
	 *                this would be the key in the second map. Example: serialized parameters to a method,
	 *                uniquely identifying that invocation of the method.
	 * @param value   the object to be cached.
	 */
	public void add(String cacheId, String key, Object value) {
		Ehcache ehcache = ehcacheManager.getEhcache(cacheId);

		final Object convertedKey = convertNullKey(key);
		final Object threadSafeValue = makeThreadSafe(value);

		ehcache.put(new Element(convertedKey, threadSafeValue));
	}

	/**
	 * Gets an object from the cache.
	 * 
	 * @param cacheId name of the cache to get the object from. If the cache were a Map of Maps,
	 *                this would be the key in the first map. Example: a fully qualified method name.
	 * @param key     a unique key for the cached-object-to-return. If the cache were a Map of Maps,
	 *                this would be the key in the second map. Example: serialized parameters to a method,
	 *                uniquely identifying that invocation of the method.
	 * @return instance of {@link CachedValue}, which wraps the cached object and provides a method signaling
	 *         if the object was found in the cache.
	 */
	public CachedValue retrieve(String cacheId, String key) {
		Ehcache ehcache = ehcacheManager.getEhcache(cacheId);

		if (ehcache == null) {
			return null;
		}

		Element element = ehcache.get(convertNullKey(key));

		if (element == null) {
			return CachedValue.notFound();
		}

		Object rawCachedValue = element.getObjectValue();
		return CachedValue.create(makeThreadSafe(rawCachedValue));
	}

	private Object makeThreadSafe(Object value) {
		return cloner.deepClone(value);
	}

	private long getTimeToLiveInSeconds(CacheConfig cacheConfig) {
		if (cacheConfig.getExpirationTime() <= 0) {
			return EHCACHE_NO_EXPIRATION;
		} else {
			long seconds = cacheConfig.getUnit().toSeconds(cacheConfig.getExpirationTime());
			if (seconds < EHCACHE_SMALLEST_TIMEOUT_IN_SECONDS) {
				seconds = EHCACHE_SMALLEST_TIMEOUT_IN_SECONDS;
			}

			return seconds;
		}
	}

	private boolean isEternal(CacheConfig cacheConfig) {
		if (cacheConfig.getExpirationTime() == CacheConstants.NO_EXPIRATION) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Trick Ehcache into supporting null keys.
	 * Use a unique Object representing null,
	 * and use this method to consistently
	 * convert null to our unique Object.
	 */
	private Object convertNullKey(String stringKey) {
		if (stringKey == null) {
			return NULL_KEY;
		} else {
			return stringKey;
		}
	}
}
