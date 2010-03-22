/**
 * Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

package com.googlecode.commonspringaspects.aspects;


import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.core.Ordered;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * Monitors (and logs) JAMon statistics.
 * 
 * Does similar work as org.springframework.aop.interceptor.JamonPerformanceMonitorInterceptor,
 * but is implemented as Advice instead of Interceptor.
 * 
 * @version $Revision: 1.5 $
 */
public class JamonAspect implements Ordered {

    private static final Logger LOGGER = Logger.getLogger(JamonAspect.class);

    private int logThresholdMilliseconds = 0;

    private boolean enabled = true;

    private int order = Ordered.LOWEST_PRECEDENCE;

    /**
     * Monitors method calls using Jamon.
     * 
     * @param pjp
     * @return
     * @throws Throwable
     */
    public Object monitor(ProceedingJoinPoint pjp) throws Throwable {
        if (!isEnabled()) {
            return pjp.proceed();
        }
        
        String methodSignature = createInvocationTraceName(pjp);
        Monitor monitor = MonitorFactory.start(methodSignature);
        try {
            return pjp.proceed();
        } finally {
            monitor.stop();
            if (monitor.getLastValue() > getLogThresholdMilliseconds()) {
                LOGGER.warn(monitor.getLastValue() + " ms. " + monitor);
            } else if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(monitor.getLastValue() + " ms. " + monitor);
            }
        }
    }

    private String createInvocationTraceName(ProceedingJoinPoint pjp) {
        String longSignatureString = pjp.getSignature().toLongString();
        
        int lastIndexOfThrows = longSignatureString.lastIndexOf("throws");
        if (lastIndexOfThrows > 0) {
            longSignatureString = longSignatureString.substring(0, lastIndexOfThrows);    
        }
        
        String[] split = longSignatureString.split(" ");
        return split[split.length - 1];
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setLogThresholdMilliseconds(int logThresholdMilliseconds) {
        this.logThresholdMilliseconds = logThresholdMilliseconds;
    }

    /**
     * Sets priority order for the aspect.
     * 
     * @param order
     */
    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    protected boolean isEnabled() {
        return enabled;
    }

    protected int getLogThresholdMilliseconds() {
        return logThresholdMilliseconds;
    }

}
