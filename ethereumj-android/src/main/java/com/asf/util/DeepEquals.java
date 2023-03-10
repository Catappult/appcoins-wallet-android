package com.asf.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;

public class DeepEquals {
  private static final Map<Class, Boolean> _customEquals = new ConcurrentHashMap<Class, Boolean>();
  private static final Map<Class, Boolean> _customHash = new ConcurrentHashMap<Class, Boolean>();

  public static boolean deepEquals(Object a, Object b) {
    Set<DualKey> visited = new HashSet<DualKey>();
    LinkedList<DualKey> stack = new LinkedList<DualKey>();
    stack.addFirst(new DualKey(a, b));

    while (!stack.isEmpty()) {
      DualKey dualKey = stack.removeFirst();
      visited.add(dualKey);

      if (dualKey._key1 == dualKey._key2) {
        continue;
      }

      if (dualKey._key1 == null || dualKey._key2 == null) {
        return false;
      }

      if (!dualKey._key1.getClass()
          .equals(dualKey._key2.getClass())) {
        return false;
      }

      if (dualKey._key1.getClass()
          .isArray()) {
        if (!compareArrays(dualKey._key1, dualKey._key2, stack, visited)) {
          return false;
        }
        continue;
      }

      if (dualKey._key1 instanceof SortedSet) {
        if (!compareOrderedCollection((Collection) dualKey._key1, (Collection) dualKey._key2, stack,
            visited)) {
          return false;
        }
        continue;
      }

      if (dualKey._key1 instanceof Set) {
        if (!compareUnorderedCollection((Collection) dualKey._key1, (Collection) dualKey._key2,
            stack, visited)) {
          return false;
        }
        continue;
      }

      if (dualKey._key1 instanceof Collection) {
        if (!compareOrderedCollection((Collection) dualKey._key1, (Collection) dualKey._key2, stack,
            visited)) {
          return false;
        }
        continue;
      }

      if (dualKey._key1 instanceof SortedMap) {
        if (!compareSortedMap((SortedMap) dualKey._key1, (SortedMap) dualKey._key2, stack,
            visited)) {
          return false;
        }
        continue;
      }

      if (dualKey._key1 instanceof Map) {
        if (!compareUnorderedMap((Map) dualKey._key1, (Map) dualKey._key2, stack, visited)) {
          return false;
        }
        continue;
      }

      if (hasCustomEquals(dualKey._key1.getClass())) {
        if (!dualKey._key1.equals(dualKey._key2)) {
          return false;
        }
        continue;
      }

      Collection<Field> fields = ReflectionUtils.getDeepDeclaredFields(dualKey._key1.getClass());

      for (Field field : fields) {
        try {
          DualKey dk = new DualKey(field.get(dualKey._key1), field.get(dualKey._key2));
          if (!visited.contains(dk)) {
            stack.addFirst(dk);
          }
        } catch (Exception ignored) {
        }
      }
    }

    return true;
  }

  private static boolean compareArrays(Object array1, Object array2, LinkedList stack,
      Set visited) {

    int len = Array.getLength(array1);
    if (len != Array.getLength(array2)) {
      return false;
    }

    for (int i = 0; i < len; i++) {
      DualKey dk = new DualKey(Array.get(array1, i), Array.get(array2, i));
      if (!visited.contains(dk)) {
        stack.addFirst(dk);
      }
    }
    return true;
  }

  private static boolean compareOrderedCollection(Collection col1, Collection col2,
      LinkedList stack, Set visited) {

    if (col1.size() != col2.size()) {
      return false;
    }

    Iterator i1 = col1.iterator();
    Iterator i2 = col2.iterator();

    while (i1.hasNext()) {
      DualKey dk = new DualKey(i1.next(), i2.next());
      if (!visited.contains(dk)) {
        stack.addFirst(dk);
      }
    }
    return true;
  }

  private static boolean compareUnorderedCollection(Collection col1, Collection col2,
      LinkedList stack, Set visited) {

    if (col1.size() != col2.size()) {
      return false;
    }

    Map<Integer, Object> fastLookup = new HashMap<Integer, Object>();
    for (Object o : col2) {
      fastLookup.put(deepHashCode(o), o);
    }

    for (Object o : col1) {
      Object other = fastLookup.get(deepHashCode(o));
      if (other == null) {
        return false;
      }

      DualKey dk = new DualKey(o, other);
      if (!visited.contains(dk)) {
        stack.addFirst(dk);
      }
    }
    return true;
  }

  private static boolean compareSortedMap(SortedMap map1, SortedMap map2, LinkedList stack,
      Set visited) {

    if (map1.size() != map2.size()) {
      return false;
    }

    Iterator i1 = map1.entrySet()
        .iterator();
    Iterator i2 = map2.entrySet()
        .iterator();

    while (i1.hasNext()) {
      Entry entry1 = (Entry) i1.next();
      Entry entry2 = (Entry) i2.next();

      DualKey dk = new DualKey(entry1.getKey(), entry2.getKey());
      if (!visited.contains(dk)) {
        stack.addFirst(dk);
      }

      dk = new DualKey(entry1.getValue(), entry2.getValue());
      if (!visited.contains(dk)) {
        stack.addFirst(dk);
      }
    }
    return true;
  }

  private static boolean compareUnorderedMap(Map map1, Map map2, LinkedList stack, Set visited) {

    if (map1.size() != map2.size()) {
      return false;
    }

    Map<Integer, Object> fastLookup = new HashMap<Integer, Object>();

    for (Entry entry : (Set<Entry>) map2.entrySet()) {
      fastLookup.put(deepHashCode(entry.getKey()), entry);
    }

    for (Entry entry : (Set<Entry>) map1.entrySet()) {
      Entry other = (Entry) fastLookup.get(deepHashCode(entry.getKey()));
      if (other == null) {
        return false;
      }

      DualKey dk = new DualKey(entry.getKey(), other.getKey());
      if (!visited.contains(dk)) {
        stack.addFirst(dk);
      }

      dk = new DualKey(entry.getValue(), other.getValue());
      if (!visited.contains(dk)) {
        stack.addFirst(dk);
      }
    }

    return true;
  }

  public static boolean hasCustomEquals(Class c) {
    Class origClass = c;
    if (_customEquals.containsKey(c)) {
      return _customEquals.get(c);
    }

    while (!Object.class.equals(c)) {
      try {
        c.getDeclaredMethod("equals", Object.class);
        _customEquals.put(origClass, true);
        return true;
      } catch (Exception ignored) {
      }
      c = c.getSuperclass();
    }
    _customEquals.put(origClass, false);
    return false;
  }

  public static int deepHashCode(Object obj) {
    Set<Object> visited = new HashSet<Object>();
    LinkedList<Object> stack = new LinkedList<Object>();
    stack.addFirst(obj);
    int hash = 0;

    while (!stack.isEmpty()) {
      obj = stack.removeFirst();
      if (obj == null || visited.contains(obj)) {
        continue;
      }

      visited.add(obj);

      if (obj.getClass()
          .isArray()) {
        int len = Array.getLength(obj);
        for (int i = 0; i < len; i++) {
          stack.addFirst(Array.get(obj, i));
        }
        continue;
      }

      if (obj instanceof Collection) {
        stack.addAll(0, (Collection) obj);
        continue;
      }

      if (obj instanceof Map) {
        stack.addAll(0, ((Map) obj).keySet());
        stack.addAll(0, ((Map) obj).values());
        continue;
      }

      if (hasCustomHashCode(obj.getClass())) {
        hash += obj.hashCode();
        continue;
      }

      Collection<Field> fields = ReflectionUtils.getDeepDeclaredFields(obj.getClass());
      for (Field field : fields) {
        try {
          stack.addFirst(field.get(obj));
        } catch (Exception ignored) {
        }
      }
    }
    return hash;
  }

  public static boolean hasCustomHashCode(Class c) {
    Class origClass = c;
    if (_customHash.containsKey(c)) {
      return _customHash.get(c);
    }

    while (!Object.class.equals(c)) {
      try {
        c.getDeclaredMethod("hashCode");
        _customHash.put(origClass, true);
        return true;
      } catch (Exception ignored) {
      }
      c = c.getSuperclass();
    }
    _customHash.put(origClass, false);
    return false;
  }

  private static class DualKey {
    private final Object _key1;
    private final Object _key2;

    private DualKey(Object k1, Object k2) {
      _key1 = k1;
      _key2 = k2;
    }

    public int hashCode() {
      int h1 = _key1 != null ? _key1.hashCode() : 0;
      int h2 = _key2 != null ? _key2.hashCode() : 0;
      return h1 + h2;
    }

    public boolean equals(Object other) {
      if (!(other instanceof DualKey)) {
        return false;
      }

      DualKey that = (DualKey) other;
      return _key1 == that._key1 && _key2 == that._key2;
    }
  }
}
