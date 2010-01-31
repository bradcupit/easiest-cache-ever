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

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit test for {@link Time}
 * 
 * @author Brad Cupit
 */
public class TimeTest {
	@Test
	public void convertToSeconds() throws Exception {
		assertEquals(1, Time.NANOSECONDS.toSeconds(1000 * 1000 * 1000));
		assertEquals(1, Time.MICROSECONDS.toSeconds(1000 * 1000));
		assertEquals(1, Time.MILLISECONDS.toSeconds(1000));
		assertEquals(1, Time.SECONDS.toSeconds(1));
		assertEquals(60, Time.MINUTES.toSeconds(1));
		assertEquals(60 * 60, Time.HOURS.toSeconds(1));
		assertEquals(60 * 60 * 24, Time.DAYS.toSeconds(1));
		assertEquals(60 * 60 * 24 * 7, Time.WEEKS.toSeconds(1));
	}

	@Test(expected = IllegalArgumentException.class)
	public void toSecondsThrowsForUnset() throws Exception {
		Time.UNSET.toSeconds(0);
	}
}
