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

/**
 * POJO to hold cache configuration data, like
 * max elements in memory or max time to live.
 * 
 * @author Brad Cupit
 */
public class CacheConfig {
    private final int maxSize;
    private final int expirationTime;
    private final Time unit;

    public CacheConfig(int maxSize, int expirationTime, Time unit) {
        this.maxSize = maxSize;
        this.expirationTime = expirationTime;
        this.unit = unit;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int getExpirationTime() {
        return expirationTime;
    }

    public Time getUnit() {
        return unit;
    }
}
