//
// StandardQuantityDB.java
//

/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: StandardQuantityDB.java,v 1.4 1998-12-16 16:08:24 steve Exp $
 */

package visad.data.netcdf;

import java.io.Serializable;
import visad.RealType;
import visad.SI;
import visad.VisADException;
import visad.data.netcdf.units.ParseException;


/**
 * The following class implements a database of standard quantities.  It is
 * implemented as a singleton.  Instances of the class are immutable.
 *
 * @author Steven R. Emmerson
 */
public final class
StandardQuantityDB
  extends	QuantityDBImpl
  implements	Serializable
{
  /**
   * The singleton instance of this class.
   */
  private static /*final*/ StandardQuantityDB	db;


  /**
   * Return an instance of this class.
   *
   * @throws VisADException	Couldn't create necessary VisAD object.
   */
  public static synchronized StandardQuantityDB
  instance()
    throws VisADException
  {
    if (db == null)
	db = new StandardQuantityDB();

    return db;
  }


  /**
   * Constucts from nothing.  Protected to ensure use of the instance()
   * method.
   *
   * @param otherDB		The quantity database for the get...()
   *				methods to search after this one if no
   *				entry found.  May be <code>null</code>.
   *
   * @exception VisADException	Couldn't create necessary VisAD object.
   */
  protected 
  StandardQuantityDB()
    throws VisADException
  {
    String	name;
    Quantity	quantity;

    try {
      /*
       * From the VisAD RealType class:
       */
      add(RealType.Generic);
      add(RealType.Latitude);
      add(RealType.Longitude);
      add(RealType.Radius);
      add(RealType.Time);
      add(RealType.XAxis);
      add(RealType.YAxis);
      add(RealType.ZAxis);

      /*
       * From the SI class:
       */
      super.add(SI.ampere.quantityName(), "A");
      super.add(SI.candela.quantityName(), "cd");
      super.add(SI.kelvin.quantityName(), "K");
      super.add(SI.kilogram.quantityName(), "kg");
      super.add(SI.meter.quantityName(), "m");
      super.add(SI.second.quantityName(), "s");
      super.add(SI.mole.quantityName(), "mol");
      super.add(SI.radian.quantityName(), "rad");

      /*
       * Quasi-fundamental dimensions:
       */
      super.add("SolidAngle", "sr");

      /*
       * Derived dimensions.  The categories are somewhat arbitrary.
       */

      /*
       * Simple stuff:
       */
      super.add("Volume", "m3");
      super.add("VolumeFraction", "m3/m3");
      quantity = new Quantity("Flow", "m3/s");
      super.add("VolumeFlow", quantity);
      super.add("Flow", quantity);
      super.add("Acceleration", "m/s2");
      super.add("Area", "m2");
      super.add("Frequency", "hz");
      super.add("WaveNumber", "m-1");
      super.add("Speed", "m/s");
      super.add("Velocity", "m/s");
      super.add("AngularVelocity", "rad/s");
      super.add("AngularAcceleration", "rad/s2");

      /*
       * Mass:
       */
      super.add("MassPerArea", "kg/m2");
      super.add("MassPerLength", "kg/m");
      super.add("MassFraction", "kg/kg");
      quantity = new Quantity("Flow", "kg/s");
      super.add("MassFlux", quantity);
      super.add("MassFlow", quantity);
      super.add("Flow", quantity);
      quantity = new Quantity("Density", "kg/m3");
      super.add("MassDensity", quantity);
      super.add("Density", quantity);
      super.add("SpecificVolume", "m3/kg");

      /*
       * Force:
       */
      super.add("Force", "N");
      super.add("MomentOfForce", "N.m");
      super.add("SurfaceTension", "N/m");
      super.add("ForcePerLength", "N/m");
      super.add("Torque", "N.m");
      super.add("TorquePerlength", "N");
      super.add("Pressure", "Pa");
      super.add("Stress", "Pa");

      /*
       * Viscosity:
       */
      super.add("DynamicViscosity", "Pa.s");
      super.add("KinematicViscosity", "m2/s");

      /*
       * Energy:
       */
      super.add("Energy", "J");
      super.add("Work", "J");
      super.add("QuantityOfHeat", "J");
      super.add("Power", "W");
      super.add("EnergyPerareaTime", "J/(m2.s)");
      super.add("AvailableEnergy", "J/kg");
      super.add("SpecificEnergy", "J/kg");
      super.add("AvailableEnergy", "J/m3");
      super.add("EnergyDensity", "J/m3");

      /*
       * Heat and temperature:
       */
      super.add("ThermalConductivity", "W/(m.K)");
      super.add("ThermalDiffusivity", "m2/s");
      super.add("ThermalInsulance", "(m3.K)/W");
      super.add("ThermalResistance", "K/W");
      super.add("ThermalResistivity", "(m.K)/W");
      super.add("CoefficientOfHeatTransfer", "W/(m2.K)");
      super.add("DensityOfHeat", "J/m2");
      super.add("DensityOfHeatFlowRate", "W/m2");
      super.add("HeatFluxDensity", "W/m2");
      super.add("HeatCapacity", "J/K");
      super.add("Entropy", "J/K");
      super.add("HeatFlowRate", "W");
      super.add("SpecificHeatCapcity", "J/(kg.K)");
      super.add("SpecificHeat", "J/(kg.K)");
      super.add("SpecificEntropy", "J/(kg.K)");

      /**
       * Electricity and magnetism:
       */
      super.add("Capacitance", "F");
      super.add("Permittivity", "F/m");
      super.add("Permeability", "H/m");
      super.add("ElectricCharge", "C");
      super.add("ElectricChargeDensity", "C/m3");
      super.add("ElectricFluxDensity", "C/m2");
      super.add("ElectricResistance", "ohm");
      super.add("ElectricConductance", "ohm");
      quantity = new Quantity("EMF", "V");
      super.add("ElectricPotentialDifference", quantity);
      super.add("ElectromotiveForce", quantity);
      super.add("EMF", quantity);
      super.add("CurrentDensity", "A/m2");
      super.add("Inductance", "H");
      super.add("MagneticFlux", "Wb");
      super.add("MagneticFlux density", "T");
      super.add("MagneticField strength", "A/m");
      super.add("ElectricField strength", "V/m");

      /*
       * Photometry:
       */
      super.add("Illuminance", "lx");
      super.add("Irradiance", "W/m2");
      super.add("Radiance", "W/(m2.sr)");
      super.add("Luminance", "cd/m2");
      super.add("LuminousFlux", "lm");
      super.add("RadiantFlux", "W");
      super.add("RadiantIntensity", "W/sr");

      /*
       * Amount of substance:
       */
      super.add("AmountOfSubstanceFraction", "mol/mol");
      super.add("MolarVolume", "m3/mol");
      super.add("MolarMass", "kg/mol");
      super.add("AmountOfSubstanceConcentration", "mol/m3");
      super.add("Molality", "mol/kg");
      super.add("MolarEnergy", "J/mol");
      super.add("MolarEntropy", "J/(mol K)");
      super.add("MolarHeatCapacity", "J/(mol K)");

      /*
       * Flow permeability:
       */
      name = "Permeability";
      super.add(name, "m2");
      super.add(name, "kg/(Pa.s.m2)");
      super.add(name, "kg/(Pa.s.m)");

      /*
       * Radioactivity & radiation:
       */
      super.add("AbsorbedDose", "Gy");
      super.add("AbsorbedDoseRate", "Gy/s");
      super.add("DoseEquivalent", "Sv");
      super.add("Activity", "Bq");
      super.add("Exposure", "C/kg");	// x & gamma rays

      /*
       * Fuel consumption:
       */
      name = "FuelConsumption";
      super.add(name, "m3/J");
      super.add(name, "m/m3");
      super.add(name, "kg/J");

      /*
       * Geophysical sciences:
       */
      quantity = get("Latitude", "deg");	// from RealType.Latitude
      super.add("GeodeticLatitude", quantity);
      super.add("lat", quantity);
      quantity = get("Longitude", "deg");
      super.add("GeodeticLongitude", quantity);	// from RealType.Longitude
      super.add("Longitude", quantity);
      super.add("lon", quantity);
      quantity = new Quantity("Altitude", "m");
      super.add("Elevation", quantity);
      super.add("Altitude", quantity);
      super.add("Depth", "m");

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
   * Add a quantity to the database given a VisAD RealType.
   *
   * @param realType		The VisAD RealType to be added.
   * @throws VisADException	Couldn't create necessary VisAD object.
   */
  protected void add(RealType realType)
    throws VisADException
  {
    super.add(realType.getName(), new Quantity(realType));
  }


  /**
   * Tests this class.
   */
  public static void
  main(String[] args)
    throws	Exception
  {
    StandardQuantityDB	db = StandardQuantityDB.instance();

    System.out.println("LaTiTuDe=<" + db.get("LaTiTuDe", SI.radian) + ">");
  }
}
