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

import org.apache.commons.lang.Validate;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.core.task.TaskExecutor;

public class ForkingAspect {
    private boolean enabled = true;

    private TaskExecutor taskExecutor;

    public ForkingAspect(TaskExecutor taskExecutor) {
        Validate.notNull(taskExecutor);
        this.taskExecutor = taskExecutor;
    }

    public void fork(final ProceedingJoinPoint joinPoint) throws Throwable {
        if (!enabled) {
            joinPoint.proceed();
            return;
        }

        taskExecutor.execute(new Runnable() {
            public void run() {
                try {
                    joinPoint.proceed();
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            }
        });
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
