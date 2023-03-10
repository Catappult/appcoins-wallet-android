package com.asf.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ReflectionUtils {
  private static final Map<Class, Collection<Field>> _reflectedFields =
      new ConcurrentHashMap<Class, Collection<Field>>();

  private ReflectionUtils() {
  }

  public static Annotation getMethodAnnotation(Method method, Class annoClass) {
    Annotation a = method.getAnnotation(annoClass);
    if (a != null) {
      return a;
    }

    Class[] interfaces = method.getDeclaringClass()
        .getInterfaces();
    if (interfaces != null) {
      for (Class interFace : interfaces) {
        Method m = getMethod(interFace, method.getName(), method.getParameterTypes());
        a = m.getAnnotation(annoClass);
        if (a != null) {
          return a;
        }
      }
    }
    return null;
  }

  public static Method getMethod(Class c, String method, Class... types) {
    try {
      return c.getMethod(method, types);
    } catch (Exception nse) {
      return null;
    }
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

  public static Map<String, Field> getDeepDeclaredFieldMap(Class c) {
    Map<String, Field> fieldMap = new HashMap<String, Field>();
    Collection<Field> fields = getDeepDeclaredFields(c);
    for (Field field : fields) {
      String fieldName = field.getName();
      if (fieldMap.containsKey(fieldName)) {
        fieldMap.put(field.getDeclaringClass()
            .getName() + '.' + fieldName, field);
      } else {
        fieldMap.put(fieldName, field);
      }
    }

    return fieldMap;
  }
}
