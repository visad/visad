
//
// RemoteDisplayImpl.java
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

package visad;

import java.util.*;
import java.rmi.*;

/**
   RemoteDisplayImpl is the VisAD class for remote access to
   Display-s.<P>
*/
public class RemoteDisplayImpl extends RemoteActionImpl
       implements RemoteDisplay {
  // and RemoteActionImpl extends UnicastRemoteObject

  public RemoteDisplayImpl(DisplayImpl d) throws RemoteException {
    super(d);
  }

  /** link ref to this Display; this method may only be invoked
      after all links to ScalarMaps have been made */
  public void addReference(ThingReference ref)
         throws VisADException, RemoteException {
    if (!(ref instanceof DataReference)) {
      throw new ReferenceException("RemoteDisplayImpl.addReference: ref " +
                                   "must be DataReference");
    }
    addReference((DataReference) ref, null);
  }

  /** link ref to this Display; must be RemoteDataReference; this
      method may only be invoked after all links to ScalarMaps have
      been made; the ConstantMap array applies only to rendering ref */
  public void addReference(DataReference ref,
         ConstantMap[] constant_maps) throws VisADException, RemoteException {
    if (!(ref instanceof RemoteDataReference)) {
      throw new RemoteVisADException("RemoteDisplayImpl.addReference: requires " +
                                     "RemoteDataReference");
    }
    if (AdaptedAction == null) {
      throw new RemoteVisADException("RemoteDisplayImpl.addReference: " +
                                     "AdaptedAction is null");
    }
    ((DisplayImpl) AdaptedAction).adaptedAddReference(
                     (RemoteDataReference) ref, (RemoteDisplay) this,
                     constant_maps);
  }

  /** link ref to this Display using the non-default renderer;
      refs may be a mix of RemoteDataReference & DataReferenceImpl;
      cannot be called through RemoteDisplay interface, since
      renderer implements neither Remote nor Serializable;
      must be called locally;
      this method may only be invoked after all links to ScalarMaps
      have been made;
      this is a method of DisplayImpl and RemoteDisplayImpl rather
      than Display - see Section 6.1 of the Developer's Guide for
      more information */
  public void addReferences(DataRenderer renderer, DataReference ref)
         throws VisADException, RemoteException {
    addReferences(renderer, new DataReference[] {ref}, null);
  }
 
  /** link ref to this Display using the non-default renderer;
      refs may be a mix of RemoteDataReference & DataReferenceImpl;
      cannot be called through RemoteDisplay interface, since
      renderer implements neither Remote nor Serializable;
      must be called locally;
      this method may only be invoked after all links to ScalarMaps
      have been made;
      the maps array applies only to rendering ref;
      this is a method of DisplayImpl and RemoteDisplayImpl rather
      than Display - see Section 6.1 of the Developer's Guide for
      more information */
  public void addReferences(DataRenderer renderer, DataReference ref,
                            ConstantMap[] constant_maps)
         throws VisADException, RemoteException {
    addReferences(renderer, new DataReference[] {ref},
                  new ConstantMap[][] {constant_maps});
  }

  /** link refs to this Display using the non-default renderer;
      refs may be a mix of RemoteDataReference & DataReferenceImpl;
      cannot be called through RemoteDisplay interface, since
      renderer implements neither Remote nor Serializable;
      must be called locally;
      this method may only be invoked after all links to ScalarMaps
      have been made; this is a method of DisplayImpl and
      RemoteDisplayImpl rather than Display - see Section 6.1 of the
      Developer's Guide for more information */
  public void addReferences(DataRenderer renderer, DataReference[] refs)
         throws VisADException, RemoteException {
    addReferences(renderer, refs, null);
  }

  /** link refs to this Display using the non-default renderer;
      refs may be a mix of RemoteDataReference & DataReferenceImpl;
      cannot be called through RemoteDisplay interface, since
      renderer implements neither Remote nor Serializable;
      must be called locally;
      this method may only be invoked after all links to ScalarMaps
      have been made;
      the maps[i] array applies only to rendering refs[i];
      this is a method of DisplayImpl and RemoteDisplayImpl rather
      than Display - see Section 6.1 of the Developer's Guide for
      more information */
  public void addReferences(DataRenderer renderer, DataReference[] refs,
         ConstantMap[][] constant_maps) throws VisADException, RemoteException {
    if (AdaptedAction == null) {
      throw new RemoteVisADException("RemoteDisplayImpl.addReferences: " +
                                     "AdaptedAction is null");
    }
    ((DisplayImpl) AdaptedAction).adaptedAddReferences(renderer, refs,
                     (RemoteDisplay) this, constant_maps);
  }

  /** remove link to a DataReference;
      because DataReference array input to adaptedAddReferences may be a
      mix of local and remote, we tolerate either here */
  public void removeReference(ThingReference ref)
         throws VisADException, RemoteException {
    if (!(ref instanceof DataReference)) {
      throw new ReferenceException("RemoteDisplayImpl.addReference: ref " +
                                   "must be DataReference");
    }
    if (AdaptedAction == null) {
      throw new RemoteVisADException("RemoteDisplayImpl.removeReference: " +
                                     "AdaptedAction is null");
    }
    ((DisplayImpl) AdaptedAction).adaptedDisplayRemoveReference((DataReference) ref);
  }

  /** add a ScalarMap to this Display */
  public void addMap(ScalarMap map)
         throws VisADException, RemoteException {
    if (AdaptedAction == null) {
      throw new RemoteVisADException("RemoteDisplayImpl.addMap: " +
                                     "AdaptedAction is null");
    }
    ((DisplayImpl) AdaptedAction).addMap(map);
  }

  /** clear set of ScalarMap-s associated with this display */
  public void clearMaps() throws VisADException, RemoteException {
    if (AdaptedAction == null) {
      throw new RemoteVisADException("RemoteDisplayImpl.clearMaps: " +
                                     "AdaptedAction is null");
    }
    ((DisplayImpl) AdaptedAction).clearMaps();
  }

}

