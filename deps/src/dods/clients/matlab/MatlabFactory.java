package dods.clients.matlab;

import dods.dap.*;

/**
 * The factory to create matlab types
 *
 * @version $Revision: 1.1.1.1 $
 * @author jehamby
 * @see BaseTypeFactory
 */

public class MatlabFactory implements BaseTypeFactory {
  //..................................
  /** 
   * Construct a new DBoolean.
   * @return the new DBoolean
   */
//  public DBoolean newDBoolean() {
//    return new DBoolean();
//  }

  /**
   * Construct a new DBoolean with name n.
   * @param n the variable name
   * @return the new DBoolean
   */
//  public DBoolean newDBoolean(String n) {
//    return new DBoolean(n);
//  }

  //..................................
  /** 
   * Construct a new DByte.
   * @return the new DByte
   */
  public DByte newDByte() {
    return new DByte();
  }

  /**
   * Construct a new DByte with name n.
   * @param n the variable name
   * @return the new DByte
   */
  public DByte newDByte(String n) {
    return new DByte(n);
  }

  //..................................
  /** 
   * Construct a new DInt16.
   * @return the new DInt16
   */
  public DInt16 newDInt16() {
    return new DInt16();
  }

  /**
   * Construct a new DInt16 with name n.
   * @param n the variable name
   * @return the new DInt16
   */
  public DInt16 newDInt16(String n) {
    return new DInt16(n);
  }

  //..................................
  /** 
   * Construct a new DUInt16.
   * @return the new DUInt16
   */
  public DUInt16 newDUInt16() {
    return new DUInt16();
  }

  /**
   * Construct a new DUInt16 with name n.
   * @param n the variable name
   * @return the new DUInt16
   */
  public DUInt16 newDUInt16(String n) {
    return new DUInt16(n);
  }

  //..................................
  /** 
   * Construct a new DInt32.
   * @return the new DInt32
   */
  public DInt32 newDInt32() {
    return new DInt32();
  }

  /**
   * Construct a new DInt32 with name n.
   * @param n the variable name
   * @return the new DInt32
   */
  public DInt32 newDInt32(String n) {
    return new DInt32(n);
  }

  //..................................
  /** 
   * Construct a new DUInt32.
   * @return the new DUInt32
   */
  public DUInt32 newDUInt32() {
    return new DUInt32();
  }

  /**
   * Construct a new DUInt32 with name n.
   * @param n the variable name
   * @return the new DUInt32
   */
  public DUInt32 newDUInt32(String n) {
    return new DUInt32(n);
  }

  //..................................
  /** 
   * Construct a new DFloat32.
   * @return the new DFloat32
   */
  public DFloat32 newDFloat32() {
    return new DFloat32();
  }

  /**
   * Construct a new DFloat32 with name n.
   * @param n the variable name
   * @return the new DFloat32
   */
  public DFloat32 newDFloat32(String n) {
    return new DFloat32(n);
  }

  //..................................
  /** 
   * Construct a new DFloat64.
   * @return the new DFloat64
   */
  public DFloat64 newDFloat64() {
    return new DFloat64();
  }

  /**
   * Construct a new DFloat64 with name n.
   * @param n the variable name
   * @return the new DFloat64
   */
  public DFloat64 newDFloat64(String n) {
    return new DFloat64(n);
  }

  //..................................
  /** 
   * Construct a new DString.
   * @return the new DString
   */
  public DString newDString() {
    return new MatlabString();
  }

  /**
   * Construct a new DString with name n.
   * @param n the variable name
   * @return the new DString
   */
  public DString newDString(String n) {
    return new MatlabString(n);
  }

  //..................................
  /** 
   * Construct a new DURL.
   * @return the new DURL
   */
  public DURL newDURL() {
    return new MatlabURL();
  }

  /**
   * Construct a new DURL with name n.
   * @param n the variable name
   * @return the new DURL
   */
  public DURL newDURL(String n) {
    return new MatlabURL(n);
  }

  //..................................
  /** 
   * Construct a new DArray.
   * @return the new DArray
   */
  public DArray newDArray() {
    return new MatlabArray();
  }

  /**
   * Construct a new MatlabArray with name n.
   * @param n the variable name
   * @return the new MatlabArray
   */
  public DArray newDArray(String n) {
    return new MatlabArray(n);
  }

  //..................................
  /** 
   * Construct a new DList.
   * @return the new DList
   */
  public DList newDList() {
    return new MatlabList();
  }

  /**
   * Construct a new DList with name n.
   * @param n the variable name
   * @return the new DList
   */
  public DList newDList(String n) {
    return new MatlabList(n);
  }

  //..................................
  /** 
   * Construct a new DGrid.
   * @return the new DGrid
   */
  public DGrid newDGrid() {
    return new DGrid();
  }

  /**
   * Construct a new DGrid with name n.
   * @param n the variable name
   * @return the new DGrid
   */
  public DGrid newDGrid(String n) {
    return new DGrid(n);
  }

  //..................................
  /** 
   * Construct a new DStructure.
   * @return the new DStructure
   */
  public DStructure newDStructure() {
    return new DStructure();
  }

  /**
   * Construct a new DStructure with name n.
   * @param n the variable name
   * @return the new DStructure
   */
  public DStructure newDStructure(String n) {
    return new DStructure(n);
  }

  //..................................
  /** 
   * Construct a new MatlabSequence.
   * @return the new MatlabSequence
   */
  public DSequence newDSequence() {
    return new MatlabSequence();
  }

  /**
   * Construct a new DSequence with name n.
   * @param n the variable name
   * @return the new DSequence
   */
  public DSequence newDSequence(String n) {
    return new MatlabSequence(n);
  }

}
