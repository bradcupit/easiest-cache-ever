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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.ObjectExistsException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.googlecode.easiest.cache.ever.CacheConfig;
import com.googlecode.easiest.cache.ever.CacheConstants;
import com.googlecode.easiest.cache.ever.Time;
import com.rits.cloning.Cloner;

/**
 * unit test for {@link DefaultCacheService}
 * 
 * @author Brad Cupit
 */
public class DefaultCacheServiceTest {
    /** some number large enough that size won't be a factor in the test */
    private static final int DONT_CARE_ABOUT_SIZE = 1000;

    private final CacheConfig cacheConfig = new CacheConfig(DONT_CARE_ABOUT_SIZE, CacheConstants.NO_EXPIRATION, null);
    private final String cacheId = "cacheId";
    private final String cacheKey = "cacheKey";
    private final String expectedValue = "expected value";
    private final CacheManager ehCacheManager = new CacheManager();
    private DefaultCacheService cacheService;
    private final CacheManager mockEhcacheManager = mock(CacheManager.class);
    private final Ehcache mockEhcache = mock(Ehcache.class);

    @Before
    public void before() {
        cacheService = new DefaultCacheService();
        cacheService.setCloner(new Cloner());
        cacheService.setEhcacheManager(ehCacheManager);
    }

    @After
    public void after() {
        ehCacheManager.shutdown();
    }

    @Test
    public void createCacheIfNecessaryShouldCreateNewCacheWhenCacheIdNotFound() throws Exception {
        cacheService.setEhcacheManager(mockEhcacheManager);
        cacheService.createCacheIfNecessary(cacheId, cacheConfig);

        verify(mockEhcacheManager).addCache(Mockito.any(Ehcache.class));
    }

    @Test
    public void createCacheIfNecessaryShouldNotCreateCacheWhenItAlreadyExists() throws Exception {
        when(mockEhcacheManager.cacheExists(cacheId)).thenReturn(true);

        cacheService.setEhcacheManager(mockEhcacheManager);
        cacheService.createCacheIfNecessary(cacheId, cacheConfig);

        verify(mockEhcacheManager, never()).addCache((Ehcache) null);
        verify(mockEhcacheManager, never()).addCache((Cache) null);
        verify(mockEhcacheManager, never()).addCache((String) null);
    }

    @Test
    public void retrieveShouldRemoveLeastRecentlyUsedItemWhenMaxSizeReached() throws Exception {
        int oneItemMax = 1;
        CacheConfig cacheConfig = new CacheConfig(oneItemMax, CacheConstants.NO_EXPIRATION, null);

        cacheService.createCacheIfNecessary(cacheId, cacheConfig);

        String firstCacheKey = "1";
        String firstCachedValue = "1st val to be cached";
        cacheService.add(cacheId, firstCacheKey, firstCachedValue);

        assertNotNull(cacheService.retrieve(cacheId, firstCacheKey).value());
        cacheService.add(cacheId, "2nd cache key", "this value should push the 1st value out of the cache");
        assertNull(cacheService.retrieve(cacheId, firstCacheKey).value());
    }

    @Test
    public void retrieveShouldExpireCachedElementsWithAnExpirationTime() throws Exception {
        int expirationTimeInSeconds = 1;
        CacheConfig cacheConfig = new CacheConfig(DONT_CARE_ABOUT_SIZE, expirationTimeInSeconds, Time.SECONDS);

        cacheService.createCacheIfNecessary(cacheId, cacheConfig);

        String valueToBeCached = "value that will be added to the cache";
        cacheService.add(cacheId, cacheKey, valueToBeCached);

        assertNotNull(cacheService.retrieve(cacheId, cacheKey).value());
        TimeUnit.SECONDS.sleep(expirationTimeInSeconds + 1);
        assertNull(cacheService.retrieve(cacheId, cacheKey).value());
    }

    /**
     * when ehcache's timeToLiveSeconds = 0, the element will never expire.
     * if the user configures a very small timeout (like 10 milliseconds)
     * that may convert to 0 seconds, and therefore never expire. Instead,
     * we should round up to 1 second.
     * If the user wants eternal, they should use {@link CacheConstants#NO_EXPIRATION}
     */
    @Test
    public void createCacheIfNecessaryShouldNotSetZeroSecondExpirationTimeForVerySmallTimeValue() throws Exception {
        CacheConfig cacheConfig = new CacheConfig(100, 10, Time.MILLISECONDS);

        cacheService.createCacheIfNecessary(cacheId, cacheConfig);

        Cache cache = ehCacheManager.getCache(cacheId);
        assertTrue(!cache.getCacheConfiguration().isEternal());
        int roundedUpTime = 1;
        assertEquals(roundedUpTime, cache.getCacheConfiguration().getTimeToLiveSeconds());
    }

    public void addShouldAddObjectToCache() {
        cacheService.createCacheIfNecessary(cacheId, cacheConfig);

        cacheService.add(cacheId, cacheKey, expectedValue);
        String retrieved = (String) cacheService.retrieve(cacheId, cacheKey).value();

        assertThat(expectedValue, equalTo(retrieved));
    }

    public void addShouldPutObjectInEhcache() {
        cacheService.setEhcacheManager(mockEhcacheManager);
        when(mockEhcacheManager.getEhcache(cacheId)).thenReturn(mockEhcache);

        cacheService.add(cacheId, cacheKey, expectedValue);

        verify(mockEhcache).put(new Element(cacheKey, expectedValue));
    }

    /**
     * tests a clone of the object is put in the cache.
     * 
     * This is for thread safety. Example:
     *   thread 1 puts object in cache
     *   thread 2 gets object from cache
     *   thread 1 modifies the object it put in the cache
     *   thread 2 sees modifications <-- thread safety issue (unless object-in-cache is thread safe, which is unlikely) 
     */
    @Test
    public void addShouldStoreClonedObjectForThreadSafety() throws Exception {
        String initialValue = "initial value";

        Cacheable original = new Cacheable();
        original.field = initialValue;

        cacheService.createCacheIfNecessary(cacheId, cacheConfig);
        cacheService.add(cacheId, cacheKey, original);
        original.field = "new value doesn't change value in cache";
        Cacheable retrieved = (Cacheable) cacheService.retrieve(cacheId, cacheKey).value();

        assertNotSame(original, retrieved);
        assertEquals(initialValue, retrieved.field);
    }

    public void retrieveShouldReturnCachedValueWithWasFoundEqualToTrueWhenObjectInCache() {
        cacheService.createCacheIfNecessary(cacheId, cacheConfig);
        cacheService.add(cacheId, cacheKey, "value doesn't matter just that something is actually cached");

        CachedValue cachedValue = cacheService.retrieve(cacheId, cacheKey);

        assertThat(cachedValue.wasFound(), is(true));
    }

    public void retrieveShouldReturnCachedValueWithWasFoundEqualToFalseWhenObjectNotInCache() {
        cacheService.createCacheIfNecessary(cacheId, cacheConfig);

        CachedValue cachedValue = cacheService.retrieve(cacheId, cacheKey);

        assertThat(cachedValue.wasFound(), is(false));
    }

    public void retrieveShouldGetObjectFromEhcache() {
        cacheService.setEhcacheManager(mockEhcacheManager);
        when(mockEhcacheManager.getEhcache(cacheId)).thenReturn(mockEhcache);

        cacheService.retrieve(cacheId, cacheKey);

        verify(mockEhcache).get(cacheKey);
    }

    /**
     * tests the retrieved object is a clone of the object in the cache
     * 
     * This is for thread safety. Example:
     *   thread 1 gets object from cache
     *   thread 2 gets object from cache
     *   thread 1 modifies the object it got
     *   thread 2 sees modifications <-- thread safety issue (unless object-in-cache is thread safe, which is unlikely) 
     */
    @Test
    public void retrieveShouldReturnClonedObjectForThreadSafety() throws Exception {
        Cacheable original = new Cacheable();
        original.field = "initial value";

        cacheService.createCacheIfNecessary(cacheId, cacheConfig);
        cacheService.add(cacheId, cacheKey, original);

        Cacheable firstRetrieved = (Cacheable) cacheService.retrieve(cacheId, cacheKey).value();
        firstRetrieved.field = "new value doesn't change value in cache";
        Cacheable secondRetrieved = (Cacheable) cacheService.retrieve(cacheId, cacheKey).value();

        assertNotSame(firstRetrieved, secondRetrieved);
        assertTrue(!firstRetrieved.field.equals(secondRetrieved.field));
        assertEquals(original.field, secondRetrieved.field);
    }

    /**
     * test {@link DefaultCacheService#retrieve(String, String)} handles nulls
     */
    @Test
    public void retrieveShouldReturnNullValueWhenGivenNullValue() throws Exception {
        final String nullCacheValue = null;

        cacheService.createCacheIfNecessary(cacheId, cacheConfig);
        cacheService.add(cacheId, cacheKey, nullCacheValue);
        CachedValue cachedValue = cacheService.retrieve(cacheId, cacheKey);

        assertTrue(cachedValue.wasFound());
        assertNull(cachedValue.value());
    }

    /**
     * ehcache 1.6 does not allow null keys (since they switched to
     * {@link ConcurrentHashMap}, which also doesn't support null
     * keys. We need that support. For example: if we're caching
     * a method with one input param, and that parameter is null.
     * We need a single entry in the cache with a key of 'null'
     * in order to cache all input parameters.
     * 
     * We added special code to work around the lack of null keys.
     * This test proves that special code is working.
     */
    @Test
    public void retrieveShouldFindCorrectValueForNullKey() throws Exception {
        final String nullCacheKey = null;

        cacheService.createCacheIfNecessary(cacheId, cacheConfig);
        cacheService.add(cacheId, nullCacheKey, expectedValue);
        CachedValue cachedValue = cacheService.retrieve(cacheId, nullCacheKey);

        assertTrue(cachedValue.wasFound());
        assertNotNull(cachedValue.value());
        assertEquals(expectedValue, cachedValue.value());
    }

    /**
     * This is a thread-safety test. If this library had a thread-safety issue,
     * this test may incorrectly pass sometimes, however, if it fails
     * even once due to an {@link ObjectExistsException}, it means there
     * is a thread-safety issue (our synchronization isn't working).
     */
    @Test
    public void createCacheIfNecessaryShouldNotThrowThreadSafetyException() throws Exception {
        cacheService.setEhcacheManager(new CacheManager() {
            @Override
            public boolean cacheExists(String cacheName) throws IllegalStateException {
                boolean cacheExists = super.cacheExists(cacheName);
                // make our data stale. won't do anything in properly synchronized
                // code, but in unsynchronized code, will increase chances of an error
                letAnotherThreadRun();
                return cacheExists;
            }
        });

        int numThreads = 100;
        List<? extends Callable<Void>> eachThreadDoesTheSameThing = Collections.nCopies(numThreads, new Callable<Void>() {
            public Void call() throws Exception {
                cacheService.createCacheIfNecessary(cacheId, cacheConfig);
                return null;
            }
        });

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<Future<Void>> results = executorService.invokeAll(eachThreadDoesTheSameThing);
        rethrowAnyExceptionsThatOccurred(results);
    }

    private void rethrowAnyExceptionsThatOccurred(List<Future<Void>> results) throws InterruptedException, ExecutionException {
        for (Future<Void> future : results) {
            future.get();
        }
    }

    private void letAnotherThreadRun() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Helper class for unit tests
     * 
     * @author Brad Cupit
     */
    private static class Cacheable {
        private String field;
    }
}
