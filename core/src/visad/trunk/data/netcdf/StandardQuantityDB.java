//
// StandardQuantityDB.java
//

/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: StandardQuantityDB.java,v 1.7 1999-01-07 17:01:29 steve Exp $
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
      add(RealType.Radius);
      add(RealType.XAxis);
      add(RealType.YAxis);
      add(RealType.ZAxis);
      add(RealType.Latitude);
      add(RealType.Longitude);
      add(RealType.Time);

      /*
       * From the SI class:
       */
      super.add(SI.ampere.quantityName(), "A");
      super.add(SI.candela.quantityName(), "cd");
      super.add(SI.kelvin.quantityName(), "K");
      super.add(SI.kilogram.quantityName(), "kg");
      super.add(SI.meter.quantityName(), "m");
      // super.add(SI.second.quantityName(), "s");	RealType.Time already
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
      super.add("VolumeFlow", "m3/s");
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
      quantity = new Quantity("SurfaceMassDensity", "kg/m2");
      super.add("SurfaceMassDensity", quantity);
      super.add("AreicMass", quantity);
      super.add("MassPerArea", quantity);
      quantity = new Quantity("LinearMassDensity", "kg/m2");
      super.add("LinearMassDensity", quantity);
      super.add("LineicMass", quantity);
      super.add("MassPerLength", "kg/m");
      super.add("MassFraction", "kg/kg");
      quantity = new Quantity("MassFlow", "kg/s");
      super.add("MassFlux", quantity);
      super.add("MassFlow", quantity);
      quantity = new Quantity("Density", "kg/m3");
      super.add("MassDensity", quantity);
      super.add("Density", quantity);
      super.add("VolumicMass", quantity);
      quantity = new Quantity("SpecificVolume", "m3/kg");
      super.add("SpecificVolume", quantity);
      super.add("MassicVolume", quantity);

      /*
       * Force:
       */
      super.add("Force", "N");
      super.add("MomentOfForce", "N.m");
      super.add("SurfaceTension", "N/m");
      quantity = new Quantity("LinearForceDensity", "N/m");
      super.add("LinearForceDensity", quantity);
      super.add("LineicForce", quantity);
      super.add("ForcePerLength", quantity);
      super.add("Torque", "N.m");
      quantity = new Quantity("LinearTorqueDensity", "N");
      super.add("LinearTorqueDensity", quantity);
      super.add("LineicTorque", quantity);
      super.add("TorquePerlength", quantity);
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
      quantity = new Quantity("SurfacePowerDensity", "J/(m2.s)");
      super.add("SurfacePowerDensity", quantity);
      super.add("AreicPower", quantity);
      super.add("EnergyPerAreaTime", quantity);
      super.add("SpecificAvailableEnergy", "J/kg");
      super.add("SpecificEnergy", "J/kg");
      quantity = new Quantity("AvailableEnergyDensity", "J/m3");
      super.add("AvailableEnergyDensity", quantity);
      super.add("VolumicAvailableEnergy", quantity);
      quantity = new Quantity("EnergyDensity", "J/m3");
      super.add("EnergyDensity", quantity);
      super.add("VolumicEnergy", quantity);

      /*
       * Heat and temperature:
       */
      super.add("ThermalConductivity", "W/(m.K)");
      super.add("ThermalDiffusivity", "m2/s");
      super.add("ThermalInsulance", "(m3.K)/W");
      super.add("ThermalResistance", "K/W");
      super.add("ThermalResistivity", "(m.K)/W");
      super.add("CoefficientOfHeatTransfer", "W/(m2.K)");
      quantity = new Quantity("SurfaceHeatDensity", "J/m2");
      super.add("SurfaceHeatDensity", quantity);
      super.add("AreicHeat", quantity);
      super.add("DensityOfHeat", quantity);
      quantity = new Quantity("SurfaceHeatFlowDensity", "W/m2");
      super.add("SurfaceHeatFlowDensity", quantity);
      super.add("SurfaceHeatFluxDensity", quantity);
      super.add("AreicHeatFlow", quantity);
      super.add("AreicHeatFlux", quantity);
      super.add("DensityOfHeatFlowRate", quantity);
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
      quantity = new Quantity("Permittivity", "F/m");
      super.add("Permittivity", quantity);
      super.add("MagneticPermittivity", quantity);
      super.add("MagneticPermeability", "H/m");
      super.add("ElectricCharge", "C");
      quantity = new Quantity("ElectricChargeDensity", "C/m3");
      super.add("ElectricChargeDensity", quantity);
      super.add("VolumicElectricCharge", quantity);
      quantity = new Quantity("ElectricFluxDensity", "C/m2");
      super.add("ElectricFluxDensity", quantity);
      super.add("SurfaceElectricChargeDensity", quantity);
      super.add("AreicElectricCharge", quantity);
      super.add("ElectricResistance", "Ohm");
      super.add("ElectricConductance", "S");
      quantity = new Quantity("EMF", "V");
      super.add("ElectricPotentialDifference", quantity);
      super.add("ElectromotiveForce", quantity);
      super.add("EMF", quantity);
      quantity = new Quantity("CurrentDensity", "A/m2");
      super.add("CurrentDensity", quantity);
      super.add("SurfaceCurrentDensity", quantity);
      super.add("AreicCurrent", quantity);
      super.add("Inductance", "H");
      super.add("MagneticFlux", "Wb");
      super.add("MagneticFluxDensity", "T");
      super.add("MagneticFieldStrength", "A/m");
      super.add("ElectricFieldStrength", "V/m");

      /*
       * Photometry:
       */
      super.add("Illuminance", "lx");
      super.add("Irradiance", "W/m2");
      super.add("RadiantEmittance", "W/m2");
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
      super.add("EquivalentPermeability", "m2");
      quantity = new Quantity("SurfaceFlowPermeabilityDensity", "kg/(Pa.s.m2)");
      super.add("SurfaceFlowPermeabilityDensity", quantity);
      super.add("AreicFlowPermeability", quantity);
      quantity = new Quantity("LinearFlowPermeabilityDensity", "kg/(Pa.s.m)");
      super.add("LinearFlowPermeabilityDensity", quantity);
      super.add("LineicFlowPermeability", quantity);

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
      super.add("VolumePerWorkFuelConsumption", "m3/J");
      quantity = new Quantity("DistancePerVolumeFuelConsumption", "m/m3");
      super.add("DistancePerVolumeFuelConsumption", quantity);
      super.add("DistanceDensityFuelConsumption", quantity);
      super.add("VolumicDistanceFuelConsumption", quantity);
      super.add("MassPerWorkFuelConsumption", "kg/J");

      /*
       * Geophysical sciences:
       */
      super.add("Direction", "deg");
      quantity = get("Latitude");		// from RealType.Latitude
      super.add("GeodeticLatitude", quantity);
      super.add("lat", quantity);
      quantity = get("Longitude");
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

    System.out.println("LaTiTuDe=<" + db.get("LaTiTuDe") + ">");
  }
}
