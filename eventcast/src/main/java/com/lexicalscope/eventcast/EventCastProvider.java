package com.lexicalscope.eventcast;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.google.inject.Provider;
import com.google.inject.TypeLiteral;

/*
 * Copyright 2011 Tim Wood
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

class EventCastProvider<T> implements Provider<T>, InvocationHandler {
    private final TypeLiteral<T> listenerType;
    private final EventCaster eventCaster;

    EventCastProvider(final TypeLiteral<T> listenerType, final EventCaster eventCaster) {
        this.listenerType = listenerType;
        this.eventCaster = eventCaster;
    }

    @SuppressWarnings("unchecked") public T get() {
        return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { listenerType.getRawType() }, this);
    }

    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        eventCaster.fire(listenerType, method, args);
        return null;
    }
}