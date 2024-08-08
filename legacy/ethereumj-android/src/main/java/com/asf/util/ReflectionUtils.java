package com.asf.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ReflectionUtils {
  private static final Map<Class, Collection<Field>> _reflectedFields =
      new ConcurrentHashMap<Class, Collection<Field>>();

  private ReflectionUtils() {
  }

  public static Collection<Field> getDeepDeclaredFields(Class c) {
    if (_reflectedFields.containsKey(c)) {
      return _reflectedFields.get(c);
    }
    Collection<Field> fields = new ArrayList<Field>();
    Class curr = c;

    while (curr != null) {
      try {
        Field[] local = curr.getDeclaredFields();

        for (Field field : local) {
          if (!field.isAccessible()) {
            try {
              field.setAccessible(true);
            } catch (Exception ignored) {
            }
          }

          int modifiers = field.getModifiers();
          if (!Modifier.isStatic(modifiers) && !field.getName()
              .startsWith("this$") && !Modifier.isTransient(modifiers)) {
            fields.add(field);
          }
        }
      } catch (ThreadDeath t) {
        throw t;
      } catch (Throwable ignored) {
      }

      curr = curr.getSuperclass();
    }
    _reflectedFields.put(c, fields);
    return fields;
  }
}
