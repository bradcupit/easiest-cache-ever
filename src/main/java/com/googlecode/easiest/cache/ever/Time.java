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

import java.util.concurrent.TimeUnit;

/**
 * just like {@link TimeUnit}, except also
 * contains {@link #UNSET} and {@link #WEEKS}
 * 
 * @author Brad Cupit
 */
public enum Time {
	UNSET, NANOSECONDS, MICROSECONDS, MILLISECONDS, SECONDS, MINUTES, HOURS, DAYS, WEEKS;

	private static final int DAYS_IN_WEEK = 7;

	/**
	 * converts input parameter from the current unit to seconds.
	 * note: positive input values can still result in a 0 return value. example:
	 * 10 MILLISCONDS => 0 SECONDS
	 */
	public long toSeconds(long expirationTime) {
		switch (this) {
		case NANOSECONDS:
			return TimeUnit.NANOSECONDS.toSeconds(expirationTime);
		case MICROSECONDS:
			return TimeUnit.MICROSECONDS.toSeconds(expirationTime);
		case MILLISECONDS:
			return TimeUnit.MILLISECONDS.toSeconds(expirationTime);
		case SECONDS:
			return TimeUnit.SECONDS.toSeconds(expirationTime);
		case MINUTES:
			return TimeUnit.MINUTES.toSeconds(expirationTime);
		case HOURS:
			return TimeUnit.HOURS.toSeconds(expirationTime);
		case DAYS:
			return TimeUnit.DAYS.toSeconds(expirationTime);
		case WEEKS:
			return DAYS_IN_WEEK * TimeUnit.DAYS.toSeconds(expirationTime);
		case UNSET:
			throw new IllegalArgumentException(Time.class.getName() + " enum is unset. Unable to convert to seconds");
		default:
			throw new IllegalArgumentException(
						"a new enum must have been added without a corresponding entry in the switch statement: "
									+ Time.class.getName() + "." + this.name());
		}
	}
}
