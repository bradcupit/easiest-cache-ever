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
package com.googlecode.easiest.cache.ever.spring.aop.test;

import static com.googlecode.easiest.cache.ever.CacheConstants.*;

import com.googlecode.easiest.cache.ever.CacheReturnValue;

public class CacheTester {
    int value = 0;

    /**
     * If the value never increments, then we know caching is working.
     */
    @CacheReturnValue(expirationTime = NO_EXPIRATION)
    public int incrementAndReturn() {
        value++;
        return value;
    }
}
