package com.lexicalscope.eventcast;

import static com.google.common.collect.Multimaps.synchronizedSetMultimap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
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

class EventCaster {
    private final ThreadLocal<List<Event>> pending = new ThreadLocal<List<Event>>();
    private final SetMultimap<Object, Object> listeners = synchronizedSetMultimap(LinkedHashMultimap.create());

    void addListener(final TypeLiteral<?> interfaceType, final Object injectee) {
        listeners.put(interfaceType, injectee);
    }

    void fire(final TypeLiteral<?> listenerType, final Method method, final Object[] args) throws Throwable {
        final Event event = new Event(listenerType, method, args);

        if (pending.get() == null) {
            final List<Event> pendingEvents = new LinkedList<Event>();
            pendingEvents.add(event);

            pending.set(pendingEvents);
            try {
                while (!pendingEvents.isEmpty()) {
                    broadcastEvent(pendingEvents.remove(0));
                }
            } finally {
                pending.set(null);
            }
        } else {
            pending.get().add(event);
        }
    }
    private void broadcastEvent(final Event oldestEvent) throws IllegalAccessException, InvocationTargetException {
        for (final Object object : new ArrayList<Object>(listeners.get(oldestEvent.listenerType))) {
            oldestEvent.method.invoke(object, oldestEvent.args);
        }
    }
}