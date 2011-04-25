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

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.core.Ordered;

/**
 * @author Albert Tumanov
 * @version $Revision: $
 * @since Nov 3, 2009
 */
public class CachingAspect implements Ordered {
    static private final Logger LOGGER = Logger.getLogger(CachingAspect.class);

    private Ehcache cache;

    private boolean enabled = true;

    private int order = Ordered.HIGHEST_PRECEDENCE;

    /**
     * This advice caches the results of the advised method invocation and returns cached object whenever possible.
     * 
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    public Object cachedObject(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!enabled) {
            return joinPoint.proceed();
        }

        Object value;
        String cacheKey = getCacheKey(joinPoint);

        synchronized (this) {
            Element element = (Element) cache.get(cacheKey);

            if (element != null) {

                value = element.getObjectValue();

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Got from cache: key = " + cacheKey + ", value = " + value);
                }

            } else {

                value = joinPoint.proceed();
                cache.put(new Element(cacheKey, value));

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Added to cache: key = " + cacheKey + ", value = " + value);
                }
            }
        }
        return value;
    }

    private String getCacheKey(ProceedingJoinPoint pjp) {
        String targetName = pjp.getTarget().getClass().getSimpleName();
        String methodName = pjp.getSignature().getName();
        Object[] arguments = pjp.getArgs();
        StringBuilder key = new StringBuilder();
        key.append(targetName).append(".").append(methodName);
        if (arguments != null) {
            for (Object argument : arguments) {
                key.append(".").append(argument);
            }
        }
        return key.toString();
    }

    public void setCache(Ehcache cache) {
        this.cache = cache;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
