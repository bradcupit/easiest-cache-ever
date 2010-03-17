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

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Simple servlet to test if caching is working.
 * Forwards to main.jsp for rendering.
 * 
 * @author Brad Cupit
 */
public class MainServlet extends HttpServlet {
    private static final String CACHE_IS_WORKING_KEY = "cacheIsWorking";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletContext servletContext = request.getSession().getServletContext();
        WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);

        CacheTester cacheTester = webApplicationContext.getBean(CacheTester.class);

        int originalCachedValue = cacheTester.incrementAndReturn();
        int newCachedValue = cacheTester.incrementAndReturn();

        if (originalCachedValue == newCachedValue) {
            request.setAttribute(CACHE_IS_WORKING_KEY, true);
        } else {
            request.setAttribute(CACHE_IS_WORKING_KEY, false);
        }

        request.getRequestDispatcher("main.jsp").forward(request, response);
    }
}
