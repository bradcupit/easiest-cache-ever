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

import java.io.CharArrayWriter;
import java.util.List;

import com.googlecode.easiest.cache.ever.MethodCall;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.CompactWriter;


/**
 * Default, out-of-the-box implementation of {@link KeyGenerator}.
 * This implementation generates extremely unique keys, at the
 * expense of generating longer keys, which take up more memory.
 * It covers edge cases to prevent cache-bugs in client applications.
 * 
 * Other, {@link KeyGenerator} implementations are free to create shorter keys
 * with a slightly weaker guarantee of uniqueness.
 * 
 * @author Brad Cupit
 */
public class DefaultKeyGenerator implements KeyGenerator {
	private XStream xstream;

	public void setXstream(XStream xstream) {
		this.xstream = xstream;
	}

	/**
	 * Generates a unique method key for the method being executed.
	 * If the cache were implemented as a Map of Maps, this key would
	 * be the key for the first Map.
	 * 
	 * If the cache were implemented as a Map of Maps, this key would
	 * be the first key. Psuedo-cache definition:
	 * Map<MethodKey, Map<ParameterKey, CachedData>>
	 * 
	 * Example keys:
	 * com.fully.qualified.ClassName.noParamsMethod
	 * com.fully.qualified.ClassName.oneParamMethod(java.lang.String)
	 * com.fully.qualified.ClassName.twoParamMethod(com.fully.qualified.ParameterName,com.fully.qualified.ParameterName)
	*/
	public String generateMethodKey(MethodCall methodCall) {
		return methodCall.getFullMethodNameWithParameters();
	}

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
	 * 
	 * Example keys:
	 * (0 parameters)			null
	 * (1 simple parameter)		<string>value</string>
	 * (2 complex parameters)	<object-array><com.fully.qualified.ParameterName><field>value1</field></com.fully.qualified.ParameterName><com.fully.qualified.ParameterName><field>value2</field></com.fully.qualified.ParameterName></object-array>
	 */
	public String generateParameterKey(List<?> parameters) {
		if (parameters.size() == 0) {
			return null;
		} else {
			Object dataToSerialze;

			if (parameters.size() == 1) {
				// small optimization for 1 param methods
				dataToSerialze = parameters.get(0);
			} else {
				dataToSerialze = parameters.toArray();
			}

			CharArrayWriter charArrayWriter = new CharArrayWriter();
			CompactWriter compactWriter = new CompactWriter(charArrayWriter);
			xstream.marshal(dataToSerialze, compactWriter);

			return charArrayWriter.toString();
		}
	}
}
