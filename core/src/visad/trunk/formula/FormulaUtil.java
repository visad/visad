
//
// FormulaUtil.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad.formula;

import java.lang.reflect.*;
import java.util.StringTokenizer;
import visad.Thing;

/** Contains useful methods for advanced reflection.<P> */
public class FormulaUtil {

  /** convert an array of strings of the form
      &quot;package.Class.method(Class, Class, ...)&quot;
      to an array of Method objects */
  public static Method[] stringsToMethods(String[] strings) {
    int len = strings.length;
    Method[] methods = new Method[len];
    for (int j=0; j<len; j++) {
      // remove spaces
      StringTokenizer t = new StringTokenizer(strings[j], " ", false);
      String s = "";
      while (t.hasMoreTokens()) s = s + t.nextToken();

      // separate into two strings
      t = new StringTokenizer(s, "(", false);
      String pre = t.nextToken();
      String post = t.nextToken();

      // separate first string into class and method strings
      t = new StringTokenizer(pre, ".", false);
      String c = t.nextToken();
      int count = t.countTokens();
      for (int i=0; i<count-1; i++) c = c + "." + t.nextToken();
      String m = t.nextToken();

      // get argument array of strings
      t = new StringTokenizer(post, ",)", false);
      count = t.countTokens();
      String[] a;
      if (count == 0) a = null;
      else a = new String[count];
      int x = 0;
      while (t.hasMoreTokens()) a[x++] = t.nextToken();

      // convert result to Method object
      Class clas = null;
      try {
        clas = Class.forName(c);
      }
      catch (ClassNotFoundException exc) {
        if (FormulaVar.DEBUG) {
          System.out.println("ERROR: Class c does not exist!");
        }
        methods[j] = null;
        continue;
      }
      Class[] param;
      if (a == null) param = null;
      else param = new Class[a.length];
      for (int i=0; i<count; i++) {
        // hack to convert array arguments to correct form
        if (a[i].endsWith("[]")) {
          a[i] = "[L" + a[i].substring(0, a[i].length()-2);
          while (a[i].endsWith("[]")) {
            a[i] = "[" + a[i].substring(0, a[i].length()-2);
          }
          a[i] = a[i] + ";";
        }

        try {
          param[i] = Class.forName(a[i]);
        }
        catch (ClassNotFoundException exc) {
          if (FormulaVar.DEBUG) {
            System.out.println("ERROR: Class a[i] does not exist!");
          }
          methods[j] = null;
          continue;
        }
      }
      Method method = null;
      try {
        method = clas.getMethod(m, param);
      }
      catch (NoSuchMethodException exc) {
        if (FormulaVar.DEBUG) {
          System.out.println("ERROR: Method m does not exist!");
        }
        methods[j] = null;
        continue;
      }
      methods[j] = method;
    }
    return methods;
  }

  /** attempt to invoke a Method with the given Object arguments, performing
      static method auto-detection and automatic array compression */
  public static Thing invokeMethod(Method m, Object[] o)
                                   throws IllegalAccessException,
                                          IllegalArgumentException,
                                          InvocationTargetException {
    Object obj;
    Object[] args;
    Class[] c = m.getParameterTypes();
    int num = (o == null) ? 0 : o.length;
    int len = -1;
    int a = -1;
    if (c != null) {
      len = c.length;
      for (int i=0; i<len; i++) {
        if (c[i].isArray()) a = i;
      }
    }
    if (Modifier.isStatic(m.getModifiers())) {
      // static method
      obj = null;
      if (num > 0) {
        if (a < 0) {
          args = new Object[num];
          System.arraycopy(o, 0, args, 0, num);
        }
        else {
          // compress some of the arguments into array form
          args = new Object[len];
          if (a > 0) System.arraycopy(o, 0, args, 0, a);
          Object array = Array.newInstance(c[a].getComponentType(), num-len+1);
          System.arraycopy(o, a, array, 0, num-len+1);
          args[a] = array;
          if (a < len-1) System.arraycopy(o, num-len+a+1, args, a+1, len-a-1);
        }
      }
      else args = null;
    }
    else {
      // object method
      if (num > 0) obj = o[0];
      else obj = null;
      if (num > 1) {
        if (a < 0) {
          args = new Object[num-1];
          System.arraycopy(o, 1, args, 0, num-1);
        }
        else {
          // compress some of the arguments into array form
          args = new Object[len];
          if (a > 0) System.arraycopy(o, 1, args, 0, a);
          Object array = Array.newInstance(c[a].getComponentType(), num-len);
          System.arraycopy(o, a+1, array, 0, num-len);
          args[a+1] = array;
          if (a < len-1) System.arraycopy(o, num-len+a+1, args, a+1, len-a-1);
        }
      }
      else args = null;
    }
    Object ans = m.invoke(obj, args);
    return (ans instanceof Thing ? (Thing) ans : null);
  }

}

