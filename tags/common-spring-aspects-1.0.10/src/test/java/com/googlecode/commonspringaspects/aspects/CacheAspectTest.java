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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

public class CacheAspectTest {

    static final class SomeClass {
    }
    
    CachingAspect cachingAspect = new CachingAspect();

    IMocksControl control = EasyMock.createStrictControl();

    ProceedingJoinPoint pjpMock = control.createMock(ProceedingJoinPoint.class);

    Ehcache cacheMock = control.createMock(Ehcache.class);

    Signature signatureMock = control.createMock(Signature.class);

    SomeClass originalObject = new SomeClass();

    Capture<Element> capturedElement = new Capture<Element>();

    @Before
    public void setUp() throws Exception {
        cachingAspect.setCache(cacheMock);
    }

    @Test
    public void cacheObjectWithCacheEmpty() throws Throwable {
        expectSignatureMethods();
        expect(cacheMock.get("Class.SomeClass.arg1.2")).andReturn(null);
        expect(pjpMock.proceed()).andReturn(originalObject);
        cacheMock.put(capture(capturedElement));
        control.replay();
        Object objectFromCache = cachingAspect.cachedObject(pjpMock);
        control.verify();
        assertEquals(originalObject, objectFromCache);
        assertEquals("Class.SomeClass.arg1.2", capturedElement.getValue().getKey());
        assertEquals(originalObject, capturedElement.getValue().getObjectValue());
    }

    private void expectSignatureMethods() {
        expect(pjpMock.getTarget()).andReturn(SomeClass.class);
        expect(pjpMock.getSignature()).andReturn(signatureMock);
        expect(signatureMock.getName()).andReturn("SomeClass");
        expect(pjpMock.getArgs()).andReturn(new Object[] {"arg1", 2});
    }

    @Test
    public void cacheObjectWithCacheHit() throws Throwable {
        expectSignatureMethods();
        expect(cacheMock.get("Class.SomeClass.arg1.2")).andReturn(new Element("Class.SomeClass.arg1.2", originalObject));
        control.replay();
        Object objectFromCache = cachingAspect.cachedObject(pjpMock);
        control.verify();
        assertEquals(originalObject, objectFromCache);
    }

    @Test
    public void cacheObjectWithCachingDisabled() throws Throwable {
        cachingAspect.setEnabled(false);
        expect(pjpMock.proceed()).andReturn(originalObject);
        control.replay();
        Object objectFromCache = cachingAspect.cachedObject(pjpMock);
        control.verify();
        assertEquals(originalObject, objectFromCache);
    }

}
