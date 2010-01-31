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

import java.util.List;

import org.junit.Test;

/**
 * Unit test for {@link MethodCall}
 * 
 * @author Brad Cupit
 */
public class MethodCallTest {
	@Test
	public void convertsParameterTypesArrayToList() throws Exception {
		Class<?>[] parameterTypesArray = { Integer.class, String.class };

		MethodCall methodCall = new MethodCall(null, null, parameterTypesArray, new Object[0]);
		List<Class<?>> parameterTypesList = methodCall.getParameterTypes();

		assertEquals(parameterTypesArray.length, parameterTypesList.size());
		assertEquals(parameterTypesArray[0], parameterTypesList.get(0));
		assertEquals(parameterTypesArray[1], parameterTypesList.get(1));
	}

	@Test
	public void convertsParameterArrayToList() throws Exception {
		Object[] parameterArray = { 1, "abc" };

		MethodCall methodCall = new MethodCall(null, null, new Class<?>[0], parameterArray);
		List<Object> parameterList = methodCall.getParameters();

		assertEquals(parameterArray.length, parameterList.size());
		assertEquals(parameterArray[0], parameterList.get(0));
		assertEquals(parameterArray[1], parameterList.get(1));
	}

	@Test
	public void testGetFullMethodNameWithParameters() throws Exception {
		String className = "com.something.ClassName";
		String methodName = "methodName";
		Class<?>[] parameterTypesArray = { Integer.class, String.class };

		String expectedFullMethodName = className + "." + methodName + "(" + parameterTypesArray[0].getName() + ","
					+ parameterTypesArray[1].getName() + ")";

		MethodCall methodCall = new MethodCall(className, methodName, parameterTypesArray, new Object[0]);

		assertEquals(expectedFullMethodName, methodCall.getFullMethodNameWithParameters());
	}
}
