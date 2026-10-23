package dev.daamky.core.event;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class EventBus {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private volatile Map<Class<? extends Event>, EventBus.EventData[]> listenerCache = Map.of();
   private final Map<Object, List<EventBus.EventData>> registeredObjects = new HashMap<>();
   private volatile boolean suspended;
// https://t.me/pasterplace

   public synchronized void register(Object listener) {
      if (!this.registeredObjects.containsKey(listener)) {
         ArrayList listenerData = new ArrayList();
         Lookup methodLookup = MethodHandles.lookup();

         for (Method handlerMethod : listener.getClass().getDeclaredMethods()) {
            if (!Modifier.isStatic(handlerMethod.getModifiers()) && handlerMethod.isAnnotationPresent(EventTarget.class) && handlerMethod.getParameterCount() == 1) {
               Class parameterType = handlerMethod.getParameterTypes()[0];
               if (Event.class.isAssignableFrom(parameterType)) {
                  Class eventType = parameterType;
                  EventTarget eventTarget = handlerMethod.getAnnotation(EventTarget.class);

                  try {
                     handlerMethod.setAccessible(true);
                     MethodHandle handler = methodLookup.unreflect(handlerMethod);
                     EventBus.EventData registeredListener = new EventBus.EventData(listener, handler, eventTarget.priority(), eventType);
                     listenerData.add(registeredListener);
                  } catch (IllegalAccessException accessException) {
                     accessException.printStackTrace();
                  }
               }
            }
         }

         if (!listenerData.isEmpty()) {
            this.registeredObjects.put(listener, listenerData);
            this.rebuildCache();
         }
      }
   }
// https://t.me/pasterplace

   public synchronized void unregister(Object listener) {
      List registeredListeners = this.registeredObjects.remove(listener);
      if (registeredListeners != null) {
         this.rebuildCache();
      }
   }
// https://t.me/pasterplace

   public void setSuspended(boolean suspended) {
      this.suspended = suspended;
   }
// https://t.me/pasterplace

   public <T extends Event> T post(T event) {
      if (this.suspended) {
         return (T)event;
      }

      EventBus.EventData[] listeners = this.listenerCache.get(event.getClass());
      if (listeners == null) {
         return (T)event;
      }
// https://t.me/pasterplace

      for (int listenerIndex = 0; listenerIndex < listeners.length; listenerIndex++) {
         try {
            listeners[listenerIndex].handle.invoke((Object)listeners[listenerIndex].target, (Event)event);
         } catch (Throwable listenerFailure) {
            listenerFailure.printStackTrace();
         }
      }

      return (T)event;
   }
// https://t.me/pasterplace

   private void rebuildCache() {
      Map<Class<? extends Event>, List<EventData>> listenersByEvent = new HashMap<>();
      for (List<EventData> registeredListeners : this.registeredObjects.values()) {
         for (EventData listener : registeredListeners) {
            listenersByEvent.computeIfAbsent(listener.eventClass, eventType -> new ArrayList<>()).add(listener);
         }
      }

      Map<Class<? extends Event>, EventData[]> rebuiltCache = new HashMap<>();
      for (Entry<Class<? extends Event>, List<EventData>> eventListenersEntry : listenersByEvent.entrySet()) {
         List<EventData> listeners = eventListenersEntry.getValue();
         listeners.sort(Comparator.comparingInt(listener -> listener.priority));
         rebuiltCache.put(eventListenersEntry.getKey(), listeners.toArray(new EventData[0]));
      }

      this.listenerCache = rebuiltCache;
   }
// https://t.me/pasterplace

   private static final class EventData {
      private final Object target;
      private final MethodHandle handle;
      private final byte priority;
      private final Class<? extends Event> eventClass;

      private EventData(Object target, MethodHandle handle, byte priority, Class<? extends Event> eventClass) {
         this.target = target;
         this.handle = handle;
         this.priority = priority;
         this.eventClass = eventClass;
      }
   }
}

