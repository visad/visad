//
// ThingReferenceImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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

import java.beans.VetoableChangeListener;
import java.rmi.*;
import java.util.*;
import java.util.Set;

/**
   ThingReferenceImpl is a local implementation of ThingReference.<P>

   ThingReferenceImpl is not Serializable and should not be copied
   between JVMs.<P>
*/
public class ThingReferenceImpl extends Object implements ThingReference {

  /**
   * The set of vetoable listeners.
   */
  private final Set	vetoableListeners;

  /**
   * The source JavaBean for SetThingEvent-s.
   */
  private final Object	sourceBean;

  /** name of scalar type */
  String Name;

  /** Thing object refered to (mutable);
      ThingReferenceImpl is not Serializable, but mark as transient anyway */
  private transient Thing thing;
  /** ref = this if thing is local
      ref = a RemoteThingReferenceImpl if thing is remote;
      ThingReferenceImpl is not Serializable, but mark as transient anyway */
  private transient ThingReference ref;

  /** Tick increments each time thing changes */
  private long Tick;

  /** Vector of ThingChangedLinks;
      ThingReferenceImpl is not Serializable, but mark as transient anyway */
  transient Vector ListenerVector = new Vector();

  public ThingReferenceImpl(String name) throws VisADException {
    this(name, (Object)null);
  }

  /**
   * Constructs from a name and a JavaBean source.
   *
   * @param name		The name for the VetoableThingReferenceImpl.
   * @param sourceBean		The JavaBean that will be the source of
   *				SetThingEvent-s.  May be <code>null</code>.
   * @throws VisADException	Couldn't perform necessary VisAD operation.
   */
  public ThingReferenceImpl(String name, Object sourceBean)
    throws VisADException
  {
    if (name == null) {
      throw new VisADException("ThingReference: name cannot be null");
    }
    Name = name;
    Tick = Long.MIN_VALUE + 1;
    this.sourceBean = sourceBean;
    vetoableListeners = new HashSet(sourceBean == null ? 0 : 1);
  }

  /**
   * Adds a vetoable listener for setThing() method invocations in the guise
   * of a regular VetoableChangeListener.  This method is provided as a
   * convenience.
   *
   * @param listener		The vetoable listener to be added.  If it is a
   *				VetoableThingReferenceListener, then it is 
   *				added; otherwise nothing happens.
   */
  public void
  addVetoableSetThingListener(VetoableChangeListener listener)
  {
    if (listener instanceof VetoableThingReferenceListener)
      addVetoableSetThingListener((VetoableThingReferenceListener)listener);
  }

  /**
   * Removes a vetoable listener for setThing() method invocations in the
   * guise of a regular VetoableChangeListener.  This method is provided as a
   * convenience.
   *
   * @param listener		The vetoable listener to be removed.  If it is a
   *				VetoableThingReferenceListener, then it is 
   *				removed; otherwise nothing happens.
   */
  public void
  removeVetoableSetThingListener(VetoableChangeListener listener)
  {
    if (listener instanceof VetoableThingReferenceListener)
      removeVetoableSetThingListener((VetoableThingReferenceListener)listener);
  }

  /**
   * Adds a vetoable listener for setThing() method invocations.
   *
   * @param listener		The vetoable listener to be added.
   */
  public synchronized void
  addVetoableSetThingListener(VetoableThingReferenceListener listener)
  {
      vetoableListeners.add((Object)listener);
  }

  /**
   * Removes a vetoable listener for setThing() method invocations.
   *
   * @param listener		The vetoable listener to be removed.
   */
  public synchronized void
  removeVetoableSetThingListener(VetoableThingReferenceListener listener)
  {
      vetoableListeners.remove((Object)listener);
  }

  public Thing getThing() {
    return thing;
  }

  /**
   * Sets the Thing referenced by this object if there are no objections.
   * This method validates the new Thing by notifying the set of registered
   * VetoableThingReferenceListener-s of the proposed change.  If any of them
   * objects, then the change is aborted and the Thing referenced by this object
   * is not changed.
   *
   * @param thing		The new Thing to be vetted by the registered
   *				VetoableThingReferenceListener-s and to become
   *				the Thing referenced by this object if there
   *				are no objections.  Must be a (local) ThingImpl.
   * @throws ReferenceException	<code>thing</code> is <code>null</code>.
   * @throws RemoteVisADException
   *				<code>thing</code> is a RemoteThing.
   * @throws SetThingVetoException
   *				A VetoableThingReferenceListener objected to the
   *				new Thing.  This object still references the
   *				old Thing.
   */
  public synchronized void setThing(Thing t)
         throws SetThingVetoException, VisADException, RemoteException {
/* WLH 9 July 98
    if (t == null) {
      throw new ReferenceException("ThingReferenceImpl: thing cannot be null");
    }
*/
    if (t instanceof RemoteThing) {
      throw new RemoteVisADException(
	"ThingReferenceImpl.setThing: cannot use " + "RemoteThing");
    }
    if (vetoableListeners.size() > 0)
    {
      SetThingEvent	event;
      try
      {
	event = new SetThingEvent(sourceBean, thing);
	for (Iterator iter = vetoableListeners.iterator(); iter.hasNext(); )
	  ((VetoableThingReferenceListener)iter.next()).vetoableSetThing(event);
      }
      catch (SetThingVetoException originalException)
      {
	/*
	 * Revert to original; notify vetoable listeners of reversion.
	 */
	try
	{
	  event = new SetThingEvent(sourceBean, getThing());
	  for (Iterator iter = vetoableListeners.iterator(); iter.hasNext(); )
	    ((VetoableThingReferenceListener)iter.next())
	      .vetoableSetThing(event);
	}
	catch (SetThingVetoException e)
	{}	// ignore any and all reversion objections
	throw originalException;	// inform the caller
      }
    }
    if (thing != null) thing.removeReference(ref);
    ref = this;
    thing = t;
    if (t != null) t.addReference(ref);
    incTick();
  }

  /** method for use by RemoteThingReferenceImpl that adapts this
      ThingReferenceImpl */
  synchronized void adaptedSetThing(RemoteThing t, RemoteThingReference r)
               throws VisADException, RemoteException {
    if (thing != null) thing.removeReference(ref);
    ref = r;
    thing = t;
    t.addReference(ref);
    incTick();
  }

  public long getTick() {
    return Tick;
  }

  /** synchronized because incTick, setThing, and adaptedSetThing
      share access to thing and ref */
  public synchronized long incTick()
         throws VisADException, RemoteException {
    Tick += 1;
    if (Tick == Long.MAX_VALUE) Tick = Long.MIN_VALUE + 1;
    if (ListenerVector != null) {
      synchronized (ListenerVector) {
        Enumeration listeners = ListenerVector.elements();
        while (listeners.hasMoreElements()) {
          ThingChangedLink listener =
            (ThingChangedLink) listeners.nextElement();
          ThingChangedEvent e =
            new ThingChangedEvent(listener.getId(), Tick);
          listener.queueThingChangedEvent(e);
        }
      }
    }
    return Tick;
  }

  public ThingChangedEvent acknowledgeThingChanged(Action a)
         throws VisADException {
    if (!(a instanceof ActionImpl)) {
      throw new RemoteVisADException("ThingReferenceImpl.acknowledgeThingChanged:" +
                                     " Action must be local");
    }
    if (ListenerVector == null) return null;
    ThingChangedLink listener = findThingChangedLink(a);
    return listener.acknowledgeThingChangedEvent();
  }

  public ThingChangedEvent adaptedAcknowledgeThingChanged(RemoteAction a)
         throws VisADException {
    if (ListenerVector == null) return null;
    ThingChangedLink listener = findThingChangedLink(a);
    return listener.acknowledgeThingChangedEvent();
  }

  /** find ThingChangedLink with action */
  public ThingChangedLink findThingChangedLink(Action a)
         throws VisADException {
    if (a == null) {
      throw new ReferenceException("ThingReferenceImpl.findThingChangedLink: " +
                                   "Action cannot be null");
    }
    if (ListenerVector == null) return null;
    synchronized (ListenerVector) {
      Enumeration listeners = ListenerVector.elements();
      while (listeners.hasMoreElements()) {
        ThingChangedLink listener =
          (ThingChangedLink) listeners.nextElement();
        if (a.equals(listener.getAction())) return listener;
      }
    }
    return null;
  }

  public String getName() {
    return Name;
  }

  /** addThingChangedListener and removeThingChangedListener provide
      ThingChangedEvent source semantics;
      Action must be local ActionImpl */
  public void addThingChangedListener(ThingChangedListener a, long id)
         throws VisADException {
    if (!(a instanceof ActionImpl)) {
      throw new RemoteVisADException("ThingReferenceImpl.addThingChanged" +
                                     "Listener: Action must be local");
    }
    synchronized (this) {
      if (ListenerVector == null) ListenerVector = new Vector();
    }
    synchronized(ListenerVector) {
      if (findThingChangedLink((ActionImpl) a) != null) {
        throw new ReferenceException("ThingReferenceImpl.addThingChangedListener:" +
                                     " link to Action already exists");
      }
      ListenerVector.addElement(new ThingChangedLink((ActionImpl) a, id));
    }
  }

  /** method for use by RemoteThingReferenceImpl that adapts this
      ThingReferenceImpl */
  void adaptedAddThingChangedListener(RemoteAction a, long id)
       throws VisADException {
    synchronized (this) {
      if (ListenerVector == null) ListenerVector = new Vector();
    }
    synchronized(ListenerVector) {
      if (findThingChangedLink(a) != null) {
        throw new ReferenceException("ThingReferenceImpl.addThingChangedListener:" +
                                     " link to Action already exists");
      }
      ListenerVector.addElement(new ThingChangedLink(a, id));
    }
  }

  /** ThingChangedListener must be local ActionImpl */
  public void removeThingChangedListener(ThingChangedListener a)
         throws VisADException {
    if (!(a instanceof ActionImpl)) {
      throw new RemoteVisADException("ThingReferenceImpl.removeThingChanged" +
                                     "Listener: Action must be local");
    }
    if (ListenerVector != null) {
      synchronized(ListenerVector) {
        ThingChangedLink listener = findThingChangedLink((ActionImpl) a);
        if (listener != null) {
          ListenerVector.removeElement(listener);
        }
      }
    }
  }

  /** method for use by RemoteThingReferenceImpl that adapts this
      ThingReferenceImpl */
  void adaptedRemoveThingChangedListener(RemoteAction a)
       throws VisADException {
    if (ListenerVector != null) {
      synchronized(ListenerVector) {
        ThingChangedLink listener = findThingChangedLink(a);
        if (listener != null) {
          ListenerVector.removeElement(listener);
        }
      }
    }
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof ThingReference)) return false;
    return obj == this;
  }

  public String toString() {
    return "ThingReference " + Name;
  }

}

