//
// WeakMapValue.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

package visad.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * A weakly-referenced {@link java.util.Map} value.  The purpose of this class
 * is to aid the creation of memory-sensitive, canonical mappings.
 */
public final class WeakMapValue extends WeakReference {

  private final Object        key;

  /**
   * Constructs from a key and value.  The key and value are not copied.
   *
   * @param key                   The key for the map.
   * @param value                 The value for the map.
   * @param queue                 The queue to receive garbaged-collected
   *                              instances of this class.
   * @throws NullPointerException if the queue is <code>null</code>.
   */
  public WeakMapValue(Object key, Object value, ReferenceQueue queue) {
    super(value, queue);
    this.key = key;
  }

  /**
   * Returns the key associated with this value.  The returned object is not a
   * copy.
   *
   * @return                        The associated key.
   */
  public final Object getKey() {
    return key;
  }

  /**
   * Returns the value associated with this value.  The returned object is not
   * a copy.  It may be <code>null</code> if this reference object has been
   * cleared by the program or garbage collector.
   *
   * @return                        The associated value or <code>null</code>.
   */
  public final Object getValue() {
    return get();
  }

  /**
   * Indicates if this instance equals another object.  This method returns true
   * if and only if the other object is a WeakMapValue and the values of both
   * instances are either <code>null</code> or equal.
   *
   * @param obj                     The other object.
   * @return                        True if and only if this instance equals the
   *                                other object.
   */
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof WeakMapValue))
      return false;
    WeakMapValue that = (WeakMapValue)obj;
    Object thisVal = get();
    Object thatVal = that.get();
    return
      thisVal == null
        ? thatVal == null
        : thisVal.equals(thatVal);
  }

  /**
   * Returns the hash code of this instance.  This is the same as the hash code
   * of the associated value.
   *
   * @return                         The hash code of this instance.
   */
  public int hashCode() {
    Object val = get();
    return val == null ? 0 : val.hashCode();
  }
}
