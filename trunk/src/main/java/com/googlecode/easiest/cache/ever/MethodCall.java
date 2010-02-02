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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * POJO to hold information about the intercepted method.
 * This way we don't need to propagate AOP specific classes
 * throughout the codebase.
 * 
 * @author Brad Cupit
 */
public class MethodCall {
    private final String className;
    private final String methodName;
    private final List<Class<?>> parameterTypes;
    private final List<Object> parameters;
    private final String fullMethodNameWithParameters;

    public MethodCall(String className, String methodName, Class<?>[] parameterTypes, Object[] parameters) {
        this.className = className;
        this.methodName = methodName;
        this.parameterTypes = Collections.unmodifiableList(Arrays.asList(parameterTypes));
        this.parameters = Collections.unmodifiableList(Arrays.asList(parameters));
        this.fullMethodNameWithParameters = buildFullMethodNameWithParameters(className, methodName, this.parameterTypes);
    }

    public String getMethodName() {
        return methodName;
    }

    public String getClassName() {
        return className;
    }

    public List<Class<?>> getParameterTypes() {
        return parameterTypes;
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public String getFullMethodNameWithParameters() {
        return fullMethodNameWithParameters;
    }

    private String buildFullMethodNameWithParameters(String className, String methodName, List<Class<?>> parameterTypes) {
        String fullyQualifiedMethodName = className + "." + methodName;

        if (parameterTypes.size() == 0) {
            return fullyQualifiedMethodName;
        } else {
            List<String> paramClassNames = new ArrayList<String>();

            for (Class<?> clazz : parameterTypes) {
                paramClassNames.add(clazz.getName());
            }

            return fullyQualifiedMethodName + "(" + toCommaSeparatedString(paramClassNames) + ")";
        }
    }

    private String toCommaSeparatedString(List<String> classNamesOfParameters) {
        StringBuilder stringBuilder = new StringBuilder();

        boolean first = true;

        for (String parameterClassName : classNamesOfParameters) {
            if (first) {
                first = false;
            } else {
                stringBuilder.append(",");
            }

            stringBuilder.append(parameterClassName);
        }

        return stringBuilder.toString();
    }
}
