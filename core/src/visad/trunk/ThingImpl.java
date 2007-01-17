//
// ThingImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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

import java.util.*;
import java.rmi.*;

/**
   ThingImpl is the abstract superclass of the VisAD objects that send
   ThingChangedEvents to Actions.<p>
*/
// public abstract class ThingImpl  WLH 8 July 2000
public class ThingImpl
       implements Thing, Cloneable, java.io.Serializable {

  class RemotePair {
    RemoteThingReference ref;
    RemoteThing data;

    RemotePair(RemoteThingReference r, RemoteThing d)
        throws ReferenceException {
      if (r == null) {
        throw new ReferenceException("Cannot create RemotePair " +
                                     "from null RemoteThingReference");
      }
      if (d == null) {
        throw new ReferenceException("Cannot create RemotePair " +
                                     "from null RemoteThing");
      }
      ref = r;
      data = d;
    }

    public boolean equals(Object pair) {

      // make sure we're comparing against another RemotePair
      if (!(pair instanceof RemotePair)) return false;

      // check null reference/data conditions
      RemotePair rp = (RemotePair )pair;

      // ref/data are non-null, use the equals() method
      return (ref.equals(((RemotePair) pair).ref) &&
              data.equals(((RemotePair) pair).data));
    }
  }

  /** vector of ThingReference that reference this Thing object */
    //To save on memory let's not create this automatically, rather
    //we create this on demand.
    //          private transient Vector references = new Vector();
    private transient Vector references;

  public ThingImpl() {
  }

  /**
   * Adds a listener for changes to this thing.  The listener will be notified
   * when this thing changes.
   *
   * @param r                     The listener for changes.
   * @throws RemoteVisADException if the listener isn't a {@link
   *                              ThingReferenceImpl}.
   * @throws VisADException       if a VisAD failure occurs.
   */
  public void addReference(ThingReference r) throws RemoteVisADException {
    if (!(r instanceof ThingReferenceImpl)) {
      throw new RemoteVisADException("ThingImpl.addReference: must use " +
                                     "ThingReferenceImpl");
    }
    synchronized (this) {
      if (references == null) references = new Vector();
    }
    references.addElement(r);
/* DEBUG
    System.out.println("ThingImpl " + " addReference " +
                       "(" + System.getProperty("os.name") + ")");
*/
  }

  /** method for use by RemoteThingImpl that adapts this ThingImpl */
  void adaptedAddReference(RemoteThingReference r, RemoteThing t)
        throws VisADException {
    RemotePair p;
    try {
      p = new RemotePair(r, t);
    } catch (ReferenceException e) {
      throw new ReferenceException("ThingImpl.adaptedAddReference: " +
                                   e.getMessage());
    }
    synchronized (this) {
      if (references == null) references = new Vector();
    }
    references.addElement(p);
/* DEBUG
    System.out.println("ThingImpl.adaptedAddReference " +
                       "(" + System.getProperty("os.name") + ")");
*/
  }

  /** remove a ThingReference to this ThingImpl;
      must be local ThingReferenceImpl;
      called by ThingReference.setThing;
      would like 'default' visibility here, but must be declared
      'public' because it is defined in the Thing interface */
  public void removeReference(ThingReference r)
         throws VisADException {
    if (references == null) {
      throw new ReferenceException("ThingImpl.removeReference: already clear");
    }
    if (!(r instanceof ThingReferenceImpl)) {
      throw new RemoteVisADException("ThingImpl.removeReference: must use " +
                                     "RemoteThing for RemoteThingReference");
    }
    if (!references.removeElement(r)) {
      throw new ReferenceException("ThingImpl.removeReference: not found");
    }
  }

  /** method for use by RemoteThingImpl that adapts this ThingImpl */
  void adaptedRemoveReference(RemoteThingReference r, RemoteThing t)
       throws VisADException {
    if (references == null) {
      throw new ReferenceException("ThingImpl.removeReference: already clear");
    }

    RemotePair p;
    try {
      p = new RemotePair(r, t);
    } catch (ReferenceException e) {
      throw new ReferenceException("ThingImpl.adaptedRemoveReference: " +
                                   e.getMessage());
    }

    if (!references.removeElement(p)) {
      throw new ReferenceException("ThingImpl.adaptedRemoveReference: " +
                                   " not found");
    }
  }

  /** notify local ThingReferenceImpl-s that this ThingImpl has changed;
      incTick in RemoteThingImpl for RemoteThingReferenceImpl-s;
      would like 'default' visibility here, but must be declared
      'public' because it is defined in the Thing interface */
  public void notifyReferences()
         throws VisADException, RemoteException {
    if (references != null) {
      // lock references for iterating through it
      synchronized (references) {
        Enumeration refs = references.elements();
        while (refs.hasMoreElements()) {
          Object r = refs.nextElement();
          if (r instanceof ThingReferenceImpl) {
            // notify local ThingReferenceImpl
            ((ThingReferenceImpl) r).incTick();
          }
          else { // r instanceof RemotePair
            // RemoteThingReference, so only incTick in
            // local RemoteThingImpl
            RemoteThing d = ((RemotePair) r).data;
            d.incTick();
          }
        }
      }
    }
  }

  /**
   * <p>Clones this instance.  Information on the set of listeners to changes in
   * this instance is not cloned, so -- following the general contract of the
   * <code>clone() </code> method -- subclasses should not test for equality of
   * the set of listeners in any <code>equals(Object)</code> method.</p>
   *
   * <p>This implementation never throws {@link CloneNotSupportException}.</p>
   *
   * @return                            A clone of this instance.
   * @throws CloneNotSupportedException if cloning isn't supported.
   */
  public Object clone() throws CloneNotSupportedException {
    ThingImpl clone;
    
    try {
      clone = (ThingImpl)super.clone();
    }
    catch (CloneNotSupportedException ex) {
      throw new Error("Assertion failure");  // can't happen
    }

    clone.references = new Vector();

    return clone;
  }

}

