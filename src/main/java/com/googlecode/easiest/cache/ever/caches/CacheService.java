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

import com.googlecode.easiest.cache.ever.CacheConfig;

/**
 * A {@link CacheService} provides common operations
 * abstracting which backend cache is being used.
 * For example, there may be a CacheService implementation
 * for Ehcache, and another for OSCache, etc.
 * 
 * @author Brad Cupit
 */
public interface CacheService {
    /**
     * 
     * Creates a new cache, which can be configured separately
     * from all other caches.
     * 
     * Think of the {@link CacheService} as a Map of Maps: Map<cacheId, Map<key, value>>
     */
    public void createCacheIfNecessary(String cacheId, CacheConfig cacheConfig);

    /**
     * Add an object to the cache.
     * 
     * @param cacheId name of the cache to add the object to. If the cache were a Map of Maps,
     *                this would be the key in the first map. Example: a fully qualified method name.
     * @param key     a unique key for the corresponding value. If the cache were a Map of Maps,
     *                this would be the key in the second map. Example: serialized parameters to a method,
     *                uniquely identifying that invocation of the method.
     * @param value   the object to be cached.
     */
    void add(String cacheId, String key, Object value);

    /**
     * Gets an object from the cache.
     * 
     * @param cacheId name of the cache to get the object from. If the cache were a Map of Maps,
     *                this would be the key in the first map. Example: a fully qualified method name.
     * @param key     a unique key for the cached-object-to-return. If the cache were a Map of Maps,
     *                this would be the key in the second map. Example: serialized parameters to a method,
     *                uniquely identifying that invocation of the method.
     * @return instance of {@link CachedValue} (will not be null). Wraps the cached object and provides
     *         a method signaling if the object was found in the cache. 
     */
    CachedValue retrieve(String cacheId, String key);
}
