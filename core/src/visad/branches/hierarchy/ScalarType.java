//
// ScalarType.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad;

import java.io.InvalidObjectException;

import java.lang.ref.ReferenceQueue;

import java.util.HashMap;
import java.util.Map;

import visad.util.WeakMapValue;

/**
 * <p>ScalarType is the superclass of the VisAD hierarchy of scalar data types.
 * </p>
 *
 * <p>Every ScalarType may be the child of another, parent ScalarType of
 * which it is a subtype.  For example, a ScalarType named "AirTemperature"
 * would, presumably, have a ScalarType named "Temperature" as its parent.
 * Thus, every ScalarType is a node in a graph-theoretic tree that is rooted
 * at a most general ScalarType (one that has no parent ScalarType) and extends
 * towards more-and-more specific subtypes of the root ScalarType.
 * <dt>ScalarType tree</dt>
 *   <dd>A graph-theoretic tree of related ScalarType-s.</dd>
 * <dt>root ScalarType</dt>
 *   <dd>The ScalarType at the root of a ScalarType tree.</dd>
 * <dt>Level</dt>
 *   <dd>The distance from the root ScalarType in edges (root ScalarType-s are 
 *   at level 0).</dd>
 * </p>
 */
public abstract class ScalarType extends MathType implements Comparable {

  // name of scalar type - enforce uniqueness locally
  // but do not rely on it - names may be duplicated on remote systems
  String Name;

  /**
   * The parent ScalarType.  Will be <code>null</code> for root ScalarType-s.
   * @serial
   */
  private final ScalarType    parent;

  /**
   * The level of this instance.
   * @serial
   */
  private final transient int level;

  /**
   * Hashtable of scalar names used to make sure scalar names are unique
   * (within local VM).  Because the values in the hashtable are actually {@link
   * WeakMapValue}s, the existance of a {@link ScalarType} in the hashtable will
   * not prevent it from being garbage-collected when it is no longer strongly
   * referenced.
   */
  private static Map ScalarHash = new HashMap();

  // Aliases for scalar names
  private static Map Translations = new HashMap();
  private static Map ReverseTranslations = new HashMap();

  // Queue that receives garbage-collected ScalarType instances.
  private static final ReferenceQueue queue = new ReferenceQueue();

  /**
   * Constructs an instance with a specified name.
   *
   * @param name The name of this <CODE>ScalarType</CODE>
   *
   * @throws TypeException        if the name is invalid for a {@link 
   *                              ScalarType}.
   * @see #validateName(String)
   */
  public ScalarType(String name) throws TypeException {
      this(name, null);
  }

  // HIERARCHY:
  /*
   * Constructs from a name and a parent {@link ScalarType}.  If the parent
   * {@link ScalarType} is <code>null</code>, then the instance will be a root
   * ScalarType.
   *
   * @param name                  The name for the instance.
   * @param parent                The parent {@link ScalarType} or
   *                              <code>null</code>.
   * @throws TypeException        if the name is invalid for a {@link 
   *                              ScalarType}.
   * @see #validateName(String)
   */
  protected ScalarType(String name, ScalarType parent) throws TypeException {
    super();
    Name = name;
    this.parent = parent;
    this.level = parent == null ? 0 : parent.level + 1;
    synchronized(getClass()) {
	checkQueue();
	validateName(name, "name");
	ScalarHash.put(name, new WeakMapValue(Name, this, queue));
    }
  }

  /**
   * Trusted constructor used to create standard VisAD <CODE>RealType</CODE>s
   * without all the name-checking overhead.
   *
   * @param name Trusted name.
   * @param b Dummy value used to indicate that this is a trusted constructor.
   */
  ScalarType(String name, boolean b) {
    super(b);
    Name = name;
    parent = null;  // HIERARCHY
    level = 0;      // HIERARCHY
    synchronized(getClass()) {
	checkQueue();
	ScalarHash.put(name, new WeakMapValue(Name, this, queue));
    }
  }

  /**
   * Returns the parent {@link ScalarType} or <code>null</code> if this 
   * instance is a root ScalarType.
   *
   * @return                    The parent ScalarType or <code>null</code>.
   */
  public final ScalarType getParent() {
    return parent;
  }

  /**
   * Returns the root ScalarType of the ScalarType tree in which this instance 
   * is embedded.
   *
   * @return                    The root ScalarType of this instances's {@link
   *				ScalarType} tree.
   */
  public final ScalarType getRootScalarType() {
    ScalarType type;
    for (type = this; type.parent != null; type = type.parent) {}
    return type;
  }

  /**
   * Returns the ScalarType furthest from the root ScalarType that is in the
   * path from this instance to its root ScalarType and also in the path from
   * another instance to that instance's root ScalarType.  If no such ScalarType
   * exists, then <code>null</code> is returned.  Note that this instance or the
   * other instance may be returned.
   *
   * @param that                  The other instance or <code>null</code>.
   * @return                      The closest common ScalarType or <code>null
   *                              <code>.
   */
  public final ScalarType getCommonScalarType(ScalarType that) {
    ScalarType type;
    for (type = that; type != null && !isTypeOf(type); type = type.parent) {}
    return type;
  }

  /**
   * Indicates if this instance is a type of another instance.  This is true if
   * this instance is the other instance or if the other instance is an ancestor
   * of this instance.
   *
   * @param that                  The other instance or <code>null</code>.
   * @return			  true if and only if this instance is a type of
   *                              the other instance.
   */
  public final boolean isTypeOf(ScalarType that) {
    if (that == null)
      return false;
    for (ScalarType type = this; type != null; type = type.parent)
      if (type == that)
        return true;
    return false;
  }

  /**
   * Returns this instance's level.  The level of a ScalarType is the distance
   * from the ScalarType to the ScalarType's root ScalarType, measured in edges.
   * Root ScalarType-s are at level 0.
   *
   * @return                      The level of this instance.
   */
  public final int getLevel() {
    return level;
  }

  /**
   * Compares this object with another of the same type.  The comparison is
   * on the names.
   *
   * @param obj		The other object of the same type.
   * @return		A value less than zero, zero, or greater than zero
   *			depending on whether this object is considered less
   *			than, equal to, or greater than the other object,
   *			respectively.
   */
  public int compareTo(Object obj) {
    return getName().compareTo(((ScalarType)obj).getName());
  }

  /**
   * Indicates if this ScalarType is the same as another object.
   *
   * @param obj		The other object.
   * @return		<code>true</code> if and only if the other object is a
   *			ScalarType and compares equal to this ScalarType.
   */
  public boolean equals(Object obj) {
    return obj instanceof ScalarType && compareTo(obj) == 0;
  }

  /**
   * Obtains the hash code for this object.  If
   * <code>scalarType1.equals(scalarType2)</code>, then
   * <code>scalarType1.hashCode() == scalarType2.hashCode()</code>.
   *
   * @return		The hash code for this object.
   */
  public int hashCode() {
    return getName().hashCode();
  }

  /**
   * Change the primary name for this <CODE>ScalarType</CODE>.
   * The original name can still be used.<P>
   * If multiple aliases are created, the last one is dominant.<P>
   * This is handy for translating standard VisAD <CODE>RealType</CODE>
   * names to a language other than English.
   *
   * @param alias The new name.
   *
   * @exception TypeException If the new name is not valid.
   */
  public void alias(String alias)
    throws TypeException
  {
    synchronized(getClass()) {
      if (!Name.equals(Translations.get(alias))) {
	validateName(alias, "alias");
	Translations.put(alias, Name);
      }
      ReverseTranslations.put(Name, alias);
    }
  }

  /**
   * Returns this <CODE>ScalarType</CODE>'s name.
   *
   * @return The name of this <CODE>ScalarType</CODE>.
   */
  public String getName() {
    synchronized(getClass()) {
      String alias = (String )ReverseTranslations.get(Name);
      if (alias != null) {
	return alias;
      }
    }
    return Name;
  }

  public String getNameWithBlanks() {
    return getName().replace('_', ' ');
  }

  /**
   * Get the <CODE>ScalarType</CODE> which has the specified name.
   *
   * @param name Name of <CODE>ScalarType</CODE>.
   * @return Either the <CODE>ScalarType</CODE> if found,
   *          or <CODE>null</CODE>.
   */
  public static synchronized ScalarType getScalarTypeByName(String name) {
    if (name == null) {
      return null;
    }
    checkQueue();
    if (Translations.containsKey(name)) {
      name = (String )Translations.get(name);
    }
    ScalarType st;
    Object obj = ScalarHash.get(name);
    st =
      obj == null
	? null
	: (ScalarType)((WeakMapValue)obj).getValue();
    return st;
  }

  /**
   * Throws a {@link TypeException} if the name is invalid.  Valid names may not
   * contain the characters period, space, left or right parenthesis or already
   * be associated with a previously-created instance (either as a name or
   * alias).
   *
   * @param name           Name to check.
   * @param type           Type used in exception message (e.g. "name", 
   *                       "alias").
   * @throws TypeException if the name is <code>null</code> or, otherwise,
   *                       illegal.
   * @see #alias(String)
   */
  public static synchronized void validateName(String name, String type)
    throws TypeException
  {
    if (name == null) {
      throw new TypeException("ScalarType: " + type + " cannot be null");
    }
    if (name.indexOf(".") > -1 ||
        name.indexOf(" ") > -1 ||
        name.indexOf("(") > -1 ||
        name.indexOf(")") > -1) {
      throw new TypeException("ScalarType: " + type + " cannot contain " +
                              "space . ( or ) " + name);
    }
    if (getScalarTypeByName(name) != null) {
      throw new TypeException("ScalarType: " + type + " already used");
    }
    if (Translations.containsKey(name)) {
      throw new TypeException("ScalarType: " + type + " already used" +
			      " as an alias");
    }
  }

/*
  public static void dumpAliases()
  {
    java.util.Enumeration enum;

    boolean needHead = true;
    enum = Translations.keys();
    while (enum.hasMoreElements()) {
      Object key = enum.nextElement();
      if (needHead) {
        System.err.println("== Translation table");
        needHead = false;
      }
      System.err.println("   \"" + key + "\" => \"" +
                         Translations.get(key) + "\"");
    }

    boolean needMid = true;
    enum = ReverseTranslations.keys();
    while (enum.hasMoreElements()) {
      Object key = enum.nextElement();
      if (needMid) {
        if (needHead) {
          System.err.println("== Reverse Translation table");
          needHead = false;
        } else {
          System.err.println("-- Reverse Translation table");
        }
        needMid = false;
      }
      System.err.println("   \"" + key + "\" => \"" +
                         ReverseTranslations.get(key) + "\"");
    }
    if (!needHead) {
      System.err.println("==");
    }
  }
*/

  /**
   * <p>Returns the instance corresponding to this newly deserialized instance.
   * If a ScalarType with the same name as this instance already exists and
   * is compatible with this instance, then it is returned.  Otherwise, this
   * instance is returned. </p>
   *
   * <p>This method is protected so that it is always invoked during
   * deserialization and final to prevent subclasses from evading it.</p>
   *
   * @return                        the unique ScalarType object corresponding
   *                                to this object's name.
   * @throws InvalidObjectException if an incompatible ScalarType with the same
   *                                name as this instance already exists.
   */
  protected final Object readResolve()
    throws InvalidObjectException
  {
    ScalarType st;
    synchronized(getClass()) {
	st = getScalarTypeByName(Name);
	if (st == null) {
	  ScalarHash.put(Name, new WeakMapValue(Name, this, queue));
	  st = this;
	}
	else if (!equals(st)) {
	  throw new InvalidObjectException(
              "st=" + st + ", this=" + toString());
	}
    }
    return st;
  }

  /**
   * Checks the queue for garbage-collected instances and removes them from the
   * hash table and translation tables.
   */
  private static synchronized void checkQueue() {
    for (WeakMapValue ref; (ref = (WeakMapValue)queue.poll()) != null; ) {
      Object name = ref.getKey();
      ScalarHash.remove(name);
      Object alias = ReverseTranslations.remove(name);
      if (alias != null)
	Translations.remove(alias);
    }
  }
}

