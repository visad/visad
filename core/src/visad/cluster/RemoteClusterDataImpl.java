//
// RemoteClusterDataImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2021 Bill Hibbard, Curtis Rueden, Tom
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

/* Cluster Design Ideas

Everything is via RMI - no 'local' Impl

a Data object is partitioned if any Field in it has a
partitioned domain

interfaces:
  Thing
    Data
      RemoteData (extends Remote, Data, RemoteThing)
        RemoteClusterData
          RemoteClientData
            RemoteClientTuple (extends RemoteTupleIface)
            RemoteClientField (extends RemoteField)
            RemoteClientPartitionedField (extends RemoteField)
          RemoteNodeData
            RemoteNodeTuple (extends RemoteTupleIface)
            RemoteNodeField (extends RemoteField)
            RemoteNodePartitionedField (extends RemoteField)

classes:
  UnicastRemoteObject
    RemoteThingImpl
      RemoteDataImpl
        RemoteClusterDataImpl
          RemoteClientDataImpl
            RemoteClientTupleImpl
            RemoteClientFieldImpl
            RemoteClientPartitionedFieldImpl
          RemoteNodeDataImpl
            RemoteNodeTupleImpl
            RemoteNodeFieldImpl
            RemoteNodePartitionedFieldImpl


RemoteClientPartitionedFieldImpl.getDomainSet() return UnionSet
of getDomainSet() returns from each node

add TupleIface extends Data (Tuple implements TupleIface)
and RemoteTupleIface extends TupleIface

a non-partitioned Data object is local on the client
  that is, a DataImpl

a partitioned Data object is a RemoteClientDataImpl on the
cient connected to RemodeNodeDataImpl's on the nodes

NodeAgent, Serializable class sent from client to each node
gets a Thread on arrival at node, return value from send of
NodeAgent is RemoteAgentContact (and Impl)
values from NodeAgent back declared Serializable

  abstract class NodeAgent implements Serializable, Runnable
    void sendToClient(Serializable message)
      invokes RemoteClientAgent.sendToClient(message)
    RemoteAgentContactImpl getRemoteAgentContact()
  interface RemoteAgentContact extends Remote
  class RemoteAgentContactImpl implements RemoteAgentContact
  interface RemoteClientAgent extends Remote
    void sendToClient(Serializable message)
  abstract class RemoteClientAgentImpl implements RemoteClientAgent
  class DefaultNodeRendererAgent extends NodeAgent
    void run()

  interface RemoteNodeData
    RemoteAgentContact sendAgent(NodeAgent agent)

  NodeRendererJ3D(NodeAgent agent)
  NodeRendererJ3D.doTransform()
    invokes agent.sendToClient(VisADGroup branch)


see page 60 of Java Enterprise in a Nutshell
no easy way to load RMI classes - security issues


partitioned data on client has similar data trees on
client and nodes
  leaf node on client is:
    DataImpl
    Field with partitioned domain (RemoteClientPartitionedFieldImpl)
  non-leaf node on client is:
    Tuple (RemoteClientTupleImpl)
    Field with non-partitioned domain (RemoteClientFieldImpl)
  leaf tree-nodes on cluster-node is:
    Field with partitioned domain
      (RemoteNodePartitionedFieldImpl adapting FlatField)
  non-leaf tree-nodes on cluster-node is:
    Tuple (RemoteNodeTupleImpl)
    Field with non-partitioned domain (RemoteNodeFieldImpl)
    Field with partitioned domain
      (RemoteNodePartitionedFieldImpl adapting FieldImpl)

every object in data tree on client connects to objects
in data trees on nodes

may use DisplayImplJ3D on nodes for graphics, with api = TRANSFORM_ONLY
  and DisplayRenderer = NodeDisplayRendererJ3D (extends
  TransformOnlyDisplayRendererJ3D) doesn't render to screen
uses special DisplayImplJ3D constructor signature (conflict?)
  for cluster - modified version of collaborative Display

NodeRendererJ3D extends DefaultRendererJ3D, with
ShadowNode*TypeJ3D - addToGroup() etc to leave as Serializable
  note must replace 'Image image' in VisADAppearance
ClientRendererJ3D extends DefaultRendererJ3D, not even using
ShadowTypes, but assembling VisADSceneGraphs from nodes




may also need way for client to signal implicit resolution
reduction to nodes - custom DataRenderers with custon ShadowTypes
whose doTransforms resample down, then call super.doTransform()
with downsampled data


Control field in ScalarMap is marked transient and dglo9.txt
says it should be.  But can use the getSaveString() and
setSaveString() methods of Control to transmit Control states.


cluster design should include a native VisAD Data Model on
binary files, via serialization, for an implementation of
FileFlatField on nodes

also need to support FileFlatFields

*/

/*
  DisplayImpl.syncRemoteData()
    . . .
    if (!cluster) waitForTasks(); // WLH 11 April 2001

only needed for testing client and nodes in same JVM
BUT, dglo should make this waitForTasks() more precise
*/
/*
possible deadlock in ThreadPool, if all running ActionImpls
are waiting for other ActionImpls to run
*/

/* VisAD Data Model on various file formats

Data instance method for write
Data static method for read
a parameter to these methods is a file-format-specific
implementation of a FileIO interface, that is used for
low level I/O (should deal with missing data in
file-format-specific way)

other interfaces for constructing appropriate file-format-
specific structures for Tuple, Field, FlatField, Set, Real,
Text, RealTuple, CoordinateSystem, Unit, ErrorEstimate

get review from Steve on this

*/


package visad.cluster;

import visad.*;
import java.rmi.*;

/**
   RemoteClusterDataImpl is the super class for cluster
   client and node Data.<P>
*/
public abstract class RemoteClusterDataImpl extends RemoteDataImpl
       implements RemoteClusterData {

  /** Set that defines partition of Data across cluster;
      values in partitionSet's domain RealTupleType are
      partitioned according to:
         jvmTable[partitionSet.valueToIndex()] */
  private Set partitionSet = null;

  /** domain dimension of partitionSet */
  private int dimension = -1;

  /** lookup table for RemoteClusterData objects on nodes, last
      entry is on client (for non-distributed data) */
  private RemoteClusterData[] jvmTable = null;

  /** used for testing equality */
  private RemoteClusterData me = null;

  public RemoteClusterDataImpl() throws RemoteException {
    super(null); // RemoteDataImpl.AdaptedData =
                 // RemoteThingImpl.AdaptedThing = null
    // but adapt a ThingImpl and a RemoteThingImpl
    // used for over-riding RemoteThingImpl methods
    adaptedThingImpl = new ThingImpl();
    adaptedRemoteThingImpl = new RemoteThingImpl(adaptedThingImpl);
    me = this;
  }

  RemoteClusterData[] getTable() {
    return jvmTable;
  }

  /** return RemoteClusterData for JVM where data resides;
      may be RemoteClusterData for client for non-partitioned data;
      may be null for partitioned data outside partitoning */
  public RemoteClusterData getClusterData(RealTuple domain)
         throws RemoteException, VisADException {
    if (domain == null || partitionSet == null || jvmTable == null) {
      throw new ClusterException("null domain or setup not done");
    }
    if (dimension != domain.getDimension()) {
      // return client (last entry) for non-partitoned data
      return jvmTable[jvmTable.length - 1];
    }

    // "eval" partitionSet at domain
    // first extract values from domain
    double[][] vals = new double[dimension][1];
    for (int i=0; i<dimension; i++) {
      vals[i][0] = ((Real) domain.getComponent(i)).getValue();
    }
    // test whether domain and partitionSet CoordinateSystems match
    RealTupleType out = ((SetType) partitionSet.getType()).getDomain();
    CoordinateSystem coord_out = partitionSet.getCoordinateSystem();
    RealTupleType in = (RealTupleType) domain.getType();
    CoordinateSystem coord_in = domain.getCoordinateSystem();
    if (!CoordinateSystem.canConvert(out, coord_out, in, coord_in)) {
      // return client (last entry) for non-partitoned data
      return jvmTable[jvmTable.length - 1];
    }

    // if only one, then just return it
    if (partitionSet.getLength() == 1) {
      return jvmTable[0];
    }

    // transform coordinates and convert units
    vals = CoordinateSystem.transformCoordinates(
                     ((SetType) partitionSet.getType()).getDomain(),
                     partitionSet.getCoordinateSystem(),
                     partitionSet.getSetUnits(), null,
                     (RealTupleType) domain.getType(),
                     domain.getCoordinateSystem(),
                     domain.getTupleUnits(), null, vals);
    try {
      // convert transformed values to a partitionSet index
      int[] indices = partitionSet.doubleToIndex(vals);
      // return jvmTable entry
      return (indices[0] < 0) ? null : jvmTable[indices[0]];
    }
    catch (SetException e) {
      return null;
    }
  }

  public void setupClusterData(Set ps, RemoteClusterData[] table)
         throws RemoteException, VisADException {
/* WLH 4 Sept 2001
    if (ps == null || table == null) {
      throw new ClusterException("ps and table must be non-null");
    }
*/
    if (table == null) {
      throw new ClusterException("table must be non-null");
    }
    if (ps != null) {
      if ((ps.getLength() + 1) > table.length) {
        throw new ClusterException("table.length (" + table.length +") must " +
                                   " >= ps.length + 1 (" + (ps.getLength() + 1) +
                                   ")");
      }
      partitionSet = ps;
      dimension = ps.getDimension();
    }
    else {
      partitionSet = null;
      dimension = -1;
    }

    jvmTable = table;
  }

  public Set getPartitionSet() {
    return partitionSet;
  }

  public boolean clusterDataEquals(RemoteClusterData cd)
         throws RemoteException {
    return (cd == me); // seems to work - but does it really?
  }

  /** parent logic, looosely copied from DataImpl */
  private RemoteClusterDataImpl parent = null;
  public void setParent(RemoteClusterDataImpl p) {
    parent = p;
  }
  public void notifyReferences()
         throws VisADException, RemoteException {
    adaptedThingImpl.notifyReferences();
    // recursively propogate data change to parent
    if (parent != null) parent.notifyReferences();
  }

  /** over-ride methods of RemoteThingImpl, but note these
      are for notifyReferences(), which currently does nothing
      for RemoteThingImpl */
  private ThingImpl adaptedThingImpl = null;
  // adaptedRemoteThingImpl constructed from adaptedThingImpl
  private RemoteThingImpl adaptedRemoteThingImpl = null;
  public void addReference(ThingReference r) throws VisADException {
    adaptedRemoteThingImpl.addReference(r);
  }
  public void removeReference(ThingReference r) throws VisADException {
    adaptedRemoteThingImpl.removeReference(r);
  }

  public DataImpl local() throws VisADException, RemoteException {
    throw new ClusterException("no local() method for cluster data");
  }


/* MUST OVER-RIDE
methods that acccess AdaptedThing and AdaptedData

from RemoteThingImpl:
  public void addReference(ThingReference r) throws VisADException;
  public void removeReference(ThingReference r) throws VisADException;

from RemoteDataImpl:
  public DataImpl local() throws VisADException, RemoteException;
  public MathType getType() throws VisADException, RemoteException;
  public boolean isMissing() throws VisADException, RemoteException;
  public Data binary(Data data, int op, int sampling_mode, int error_mode)
              throws VisADException, RemoteException;
  public Data binary(Data data, int op, MathType new_type,
                     int sampling_mode, int error_mode )
              throws VisADException, RemoteException;
  public Data unary(int op, int sampling_mode, int error_mode)
              throws VisADException, RemoteException;
  public Data unary(int op, MathType new_type,
                    int sampling_mode, int error_mode)
              throws VisADException, RemoteException;
  public double[][] computeRanges(RealType[] reals)
         throws VisADException, RemoteException;
  public DataShadow computeRanges(ShadowType type, int n)
         throws VisADException, RemoteException;
  public DataShadow computeRanges(ShadowType type, DataShadow shadow)
         throws VisADException, RemoteException;
  public Data adjustSamplingError(Data error, int error_mode)
         throws VisADException, RemoteException;
  public String longString() throws VisADException, RemoteException;
  public String longString(String pre)
         throws VisADException, RemoteException;

END MUST OVER-RIDE */

  public static void main(String[] args)
         throws RemoteException, VisADException {
    Real r = new Real(0);
    RemoteClientTupleImpl cd = new RemoteClientTupleImpl(new Data[] {r});
    RemoteClientTupleImpl cd2 = new RemoteClientTupleImpl(new Data[] {r});
    System.out.println(cd.equals(cd)); // true
    System.out.println(cd.equals(cd2)); // false
    System.out.println(cd.clusterDataEquals(cd)); // true
    System.out.println(cd.clusterDataEquals(cd2)); // false
    System.exit(0);
  }

}

