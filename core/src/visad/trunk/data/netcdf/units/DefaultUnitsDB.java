package visad.data.netcdf.units;

import java.util.Enumeration;

import visad.Unit;
import visad.BaseUnit;

/** @deprecated Use <tt>visad.data.units.DefaultUnitsDB</tt> instead */
public class DefaultUnitsDB
  implements UnitsDB, visad.data.units.UnitsDB
{
  private visad.data.units.DefaultUnitsDB dfltDB;

  /** @deprecated Use <tt>visad.data.units.DefaultUnitsDB(db)</tt> instead */
  public DefaultUnitsDB(visad.data.units.UnitsDB db)
  {
    dfltDB = (visad.data.units.DefaultUnitsDB )db;
  }

  /** @deprecated Use <tt>visad.data.units.DefaultUnitsDB.instance()</tt> instead */
  public static UnitsDB instance()
    throws visad.UnitException
  {
    return new DefaultUnitsDB(visad.data.units.DefaultUnitsDB.instance());
  }

  /** @deprecated Use <tt>visad.data.units.DefaultUnitsDB.get(name)</tt> instead */
  public Unit get(String name) { return dfltDB.get(name); }
  /** @deprecated Use <tt>visad.data.units.DefaultUnitsDB.getNameEnumeration()</tt> instead */
  public Enumeration getNameEnumeration() { return dfltDB.getNameEnumeration(); }
  /** @deprecated Use <tt>visad.data.units.DefaultUnitsDB.getSymbolEnumeration()</tt> instead */
  public Enumeration getSymbolEnumeration() { return dfltDB.getSymbolEnumeration(); }
  /** @deprecated Use <tt>visad.data.units.DefaultUnitsDB.getUnitEnumeration()</tt> instead */
  public Enumeration getUnitEnumeration() { return dfltDB.getUnitEnumeration(); }
  /** @deprecated Use <tt>visad.data.units.DefaultUnitsDB.list()</tt> instead */
  public void list() { dfltDB.list(); }
  /** @deprecated Use <tt>visad.data.units.DefaultUnitsDB.put(bu)</tt> instead */
  public void put(BaseUnit bu) { dfltDB.put(bu); }
  /** @deprecated Use <tt>visad.data.units.DefaultUnitsDB.putName(name, u)</tt> instead */
  public void putName(String name, Unit u) { dfltDB.putName(name, u); }
  /** @deprecated Use <tt>visad.data.units.DefaultUnitsDB.putSymbol(name, u)</tt> instead */
  public void putSymbol(String name, Unit u) { dfltDB.putSymbol(name, u); }
}
