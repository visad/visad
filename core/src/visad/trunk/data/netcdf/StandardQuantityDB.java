//
// StandardQuantityDB.java
//

/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: StandardQuantityDB.java,v 1.1 1998-06-22 18:32:03 visad Exp $
 */

package visad.data.netcdf;

import java.io.Serializable;
import visad.SI;
import visad.VisADException;
import visad.data.netcdf.units.NoSuchUnitException;
import visad.data.netcdf.units.ParseException;


/**
 * The following class implements a database of standard quantities.  It is
 * implemented as a singleton.  Instances of the class are immutable.
 *
 * @author Steven R. Emmerson
 */
public final class StandardQuantityDB
  extends	QuantityDB
  implements	Serializable
{
  /**
   * The singleton instance of this class.
   */
  private static /*final*/ StandardQuantityDB	db;


  /**
   * Default constructor.  Private to ensure use of the instance() method.
   *
   * @exception VisADException	Couldn't create necessary VisAD object.
   */
  private StandardQuantityDB()
    throws VisADException
  {
    super(/*otherDB=*/null);

    String	name;

    try {
      /*
       * From the SI class:
       */
      super.add(SI.ampere.quantityName(), "ampere");
      super.add(SI.candela.quantityName(), "candela");
      super.add(SI.kelvin.quantityName(), "kelvin");
      super.add(SI.kilogram.quantityName(), "kilogram");
      super.add(SI.meter.quantityName(), "meter");
      super.add(SI.second.quantityName(), "second");
      super.add(SI.mole.quantityName(), "mole");
      super.add(SI.radian.quantityName(), "radian");

      /*
       * Fundamental dimensions:
       */
      super.add("electric current", "ampere");
      super.add("luminous intensity", "candela");

      super.add("temperature", "kelvin");
      super.add("thermodynamic temperature", "kelvin");

      super.add("mass", "kilogram");
      super.add("length", "meter");
      super.add("time", "second");
      super.add("amount of substance", "mole");

      /*
       * Quasi-fundamental dimensions:
       */
      super.add("plane angle", "radian");
      super.add("angle", "radian");
      super.add("solid angle", "sr");

      /*
       * Derived dimensions.  The categories are somewhat arbitrary.
       */

      /*
       * Simple stuff:
       */
      super.add("volume", "m^3");
      super.add("volume fraction", "m^3/m^3");
      super.add("volume flow", "m^3/s");
      super.add("flow", "m^3/s");
      super.add("acceleration", "m/s^2");
      super.add("area", "m^2");
      super.add("frequency", "hz");
      super.add("wave number", "m^-1");
      super.add("speed", "m/s");
      super.add("velocity", "m/s");
      super.add("angular velocity", "radian/s");
      super.add("angular acceleration", "radian/s^2");

      /*
       * Mass:
       */
      super.add("mass per area", "kg/m^2");
      super.add("mass per length", "kg/m");
      super.add("mass fraction", "kg/kg");
      super.add("mass flux", "kg/s");
      super.add("mass flow", "kg/s");
      super.add("flow", "kg/s");
      super.add("mass density", "kg/m^3");
      super.add("density", "kg/m^3");
      super.add("specific volume", "m^3/kg");

      /*
       * Force:
       */
      super.add("force", "N");
      super.add("moment of force", "N m");
      super.add("surface tension", "N/m");
      super.add("force per length", "N/m");
      super.add("torque", "N m");
      super.add("torque per length", "N");
      super.add("pressure", "Pa");
      super.add("stress", "Pa");

      /*
       * Viscosity:
       */
      super.add("dynamic viscosity", "Pa s");
      super.add("kinematic viscosity", "m^2/s");

      /*
       * Energy:
       */
      super.add("energy", "J");
      super.add("work", "J");
      super.add("quantity of heat", "J");
      super.add("power", "W");
      super.add("energy per area time", "J/(m^2 s)");
      super.add("available energy", "J/kg");
      super.add("specific energy", "J/kg");
      super.add("available energy", "J/m^3");
      super.add("energy density", "J/m^3");

      /*
       * Heat and temperature:
       */
      super.add("thermal conductivity", "W/(m K)");
      super.add("thermal diffusivity", "m^2/s");
      super.add("thermal insulance", "(m^3 K)/W");
      super.add("thermal resistance", "K/W");
      super.add("thermal resistivity", "(m K)/W");
      super.add("coefficient of heat transfer", "W/(m^2 K)");
      super.add("density of heat", "J/m^2");
      super.add("density of heat flow rate", "W/m^2");
      super.add("heat flux density", "W/m^2");
      super.add("heat capacity", "J/K");
      super.add("entropy", "J/K");
      super.add("heat flow rate", "W");
      super.add("specific heat capcity", "J/(kg K)");
      super.add("specific heat", "J/(kg K)");
      super.add("specific entropy", "J/(kg K)");

      /**
       * Electricity and magnetism:
       */
      super.add("capacitance", "F");
      super.add("permittivity", "F/m");
      super.add("permeability", "H/m");
      super.add("electric charge", "C");
      super.add("electric charge density", "C/m^3");
      super.add("electric flux density", "C/m^2");
      super.add("electric resistance", "ohm");
      super.add("electric conductance", "ohm");
      super.add("electric potential difference", "V");
      super.add("electromotive force", "V");
      super.add("EMF", "V");
      super.add("current density", "A/m^2");
      super.add("inductance", "H");
      super.add("magnetic flux", "Wb");
      super.add("magnetic flux density", "T");
      super.add("magnetic field strength", "A/m");
      super.add("electric field strength", "V/m");

      /*
       * Photometry:
       */
      super.add("illuminance", "lx");
      super.add("irradiance", "W/m^2");
      super.add("radiance", "W/(m^2 sr)");
      super.add("luminance", "cd/m^2");
      super.add("luminous flux", "lm");
      super.add("radiant flux", "W");
      super.add("radiant intensity", "W/sr");

      /*
       * Amount of substance:
       */
      super.add("amount-of-substance fraction", "mol/mol");
      super.add("molar volume", "m^3/mol");
      super.add("molar mass", "kg/mol");
      super.add("amount-of-substance concentration", "mol/m^3");
      super.add("molality", "mol/kg");
      super.add("molar energy", "J/mol");
      super.add("molar entropy", "J/(mol K)");
      super.add("molar heat capacity", "J/(mol K)");

      /*
       * Flow permeability:
       */
      name = "permeability";
      super.add(name, "m^2");
      super.add(name, "kg/(Pa s m^2)");
      super.add(name, "kg/(Pa s m)");
      super.add(name, "W");

      /*
       * Radioactivity & radiation:
       */
      super.add("absorbed dose", "Gy");
      super.add("absorbed dose rate", "Gy/s");
      super.add("dose equivalent", "Sv");
      super.add("activity", "Bq");
      super.add("exposure", "C/kg");	// x & gamma rays

      /*
       * Fuel consumption:
       */
      name = "fuel consumption";
      super.add(name, "m^3/J");
      super.add(name, "m/m^3");
      super.add(name, "kg/J");

      /*
       * Geophysical sciences:
       */
      super.add("geodetic latitude", "degrees_north");
      super.add("geodetic longitude", "degrees_east");
      super.add("latitude", "degrees_north");
      super.add("longitude", "degrees_east");
      super.add("elevation", "m");
      super.add("altitude", "m");
      super.add("depth", "m");

    } catch (NoSuchUnitException e) {	// shouldn't happen
      throw new VisADException(e.getMessage());
    } catch (ParseException e) {	// shouldn't happen
      throw new VisADException(e.getMessage());
    }
  }


  /**
   * Add a quantity to the database given a name and a display unit
   * specification.
   *
   * @param name		The name of the quantity (e.g. "length").
   * @param unitSpec		The preferred display unit for the 
   *				quantity (e.g. "feet").
   * @exception UnsupportedOperationException
   *				Always thrown because a standard database 
   *				must be unmodifiable.
   */
  public void add(String name, String unitSpec)
  {
    throw new UnsupportedOperationException(
      "Standard Quantity database is unmodifiable");
  }


  /**
   * Return an instance of this database.
   *
   * @return			An instance of the database.
   * @exception VisADException	Couldn't create necessary VisAD object.
   */
  public static StandardQuantityDB instance()
    throws VisADException
  {
    if (db == null)
      db = new StandardQuantityDB();

    return db;
  }
}
