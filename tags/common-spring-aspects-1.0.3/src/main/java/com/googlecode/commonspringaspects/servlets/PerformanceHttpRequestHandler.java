// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlecode.commonspringaspects.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.HttpRequestHandler;

import com.jamonapi.MonitorFactory;

/**
 * Shows web page with Jamon statistics.
 *
 * @version $Revision: $
 */
public class PerformanceHttpRequestHandler implements HttpRequestHandler {

    private boolean enabled = true;

    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {

        if (!enabled) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (request.getMethod().equals("POST")) {

            MonitorFactory.reset();
            response.sendRedirect(request.getRequestURI());

        } else {

            response.setContentType("text/html");
            PrintWriter writer = response.getWriter();
            writer.print("<html>");
            writer.print("<body><form action='' method='post'> <input type='submit' value='Reset JAMon'> </form>");
            writer.print(MonitorFactory.getRootMonitor().getReport(3, "desc"));
            writer.print("</body></html>");

        }
    }

    public void setEnabled(boolean productionMode) {
        this.enabled = productionMode;
    }
}
