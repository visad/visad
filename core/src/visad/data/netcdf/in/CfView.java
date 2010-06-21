/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: CfView.java,v 1.4 2002-10-21 20:07:45 donm Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.WeakHashMap;
import ucar.netcdf.Dimension;
import ucar.netcdf.Netcdf;
import ucar.netcdf.Variable;
import ucar.netcdf.VariableIterator;
import visad.CommonUnit;
import visad.data.netcdf.QuantityDB;
import visad.data.netcdf.QuantityDBImpl;
import visad.CoordinateSystem;
import visad.ErrorEstimate;
import visad.GriddedSet;
import visad.MathType;
import visad.OffsetUnit;
import visad.ProductSet;
import visad.RealTupleType;
import visad.RealType;
import visad.SampledSet;
import visad.SI;
import visad.TextType;
import visad.TypeException;
import visad.Unit;
import visad.VisADException;

/**
 * <p>
 * A view of a netCDF dataset according to the Climate and Forecast (CF)
 * conventions.
 * </p>
 *
 * <p>If this class can't be initialized, then an error message is printed to
 * {@link System#err} and the JVM is terminated.</p>
 *
 * @author Steven R. Emmerson
 * @version $Revision: 1.4 $ $Date: 2002-10-21 20:07:45 $
 * @see http://www.cgd.ucar.edu/cms/eaton/netcdf/CF-current.htm
 */
final class CfView
    extends     View
{
    /*
     * The following 6 fields are not "final" to accomodate a bug in JDK 1.2.
     */
    private SortedSet             auxCoordVars;
    private SortedSet             boundaryVars;
    private Map                   varToRealType;
    private Map                   dimsToDomain;
    private Map                   varToAuxCoordVars;
    private Map                   varToUnitString;

    /*
     * The following 5 fields are not "final" to accomodate a bug in JDK 1.2.
     */
    private static String[]       CF_CONVENTIONS_STRINGS;
    private static QuantityDBImpl cfQuantityDB;
    private static Variable[]     nilVarArray;
    private static Comparator     varComparator;

    static
    {
        CF_CONVENTIONS_STRINGS =
            new String[] {"CF-1.0", "COARDS/CF-1.0", "COARDS"};
        nilVarArray = new Variable[0];
        cfQuantityDB = new QuantityDBImpl((QuantityDB)null);
        varComparator =
            new Comparator()
            {
                public int compare(Object o1, Object o2)
                {
                    return
                        ((Variable)o1).getName().compareTo(
                        ((Variable)o2).getName());
                }
            };
        try
        {
            cfQuantityDB.add(
                new String[] {
                    "pressure","Pa",
                    "stress","Pa",
                    "mass","kg",
                    "area","m2",
                    "volume","m3",
                    "temperature","K",
                    "thickness","m",
                    "height","m",
                    "altitude","m",
                    "depth","m",
                    "mass_fraction","1",
                    "mass_mixing_ratio","1",
                    "volume_fraction","1",
                    "area_fraction","1",
                    "heat_flux_density","W m-2",
                    "heat_flux","W",
                    "power","W",
                    "mass_flux_density","kg m-2 s-1",
                    "mass_flux","kg s-1",
                    "volume_flux_density","m s-1",
                    "volume_flux","m3 s-1",
                    "energy","J",
                    "energy_content","J m-2",
                    "energy_density","J m-3",
                    "content","kg m-2",
                    "amount","kg m-2",
                    "speed","m s-1",
                    "velocity","m s-1",
                    "mass","kg",
                    "time","s",
                    "period","s",
                    "density","kg m-3",
                    "longitude","degrees_E",
                    "latitude","degrees_N",
                    "binary_mask","1",
                    "data_mask","1",
                    "frequency","s-1",
                    "frequency_of_occurrence","s-1",
                    "probability","1",
                    "sigma","1",
                    "hybrid_sigma_pressure","1",
                    "sigma_term_in_hybrid_sigma_pressure","1",
                    "pressure_fraction_term_in_hybrid_sigma_pressure","1",
                    "pressure_term_in_hybrid_sigma_pressure","Pa",
                    "hybrid_height","m",
                    "height_term_in_hybrid_height","1",
                    "altitude_term_in_hybrid_height","1",
                    "model_level_number","1",
                    "forecast_reference_time","s",
                    "forecast_period","s",
                    "specific_eddy_kinetic_energy","m2 s-2",
                    "sea_floor_depth","m",
                    "partial_pressure","Pa",
                    "surface_air_pressure","Pa",
                    "air_pressure","Pa",
                    "air_pressure_anomaly","Pa",
                    "rate_of_change_of_air_pressure","Pa s-1",
                    "air_density","kg m-3",
                    "sea_water_density","kg m-3",
                    "sea_water_potential_density","kg m-3",
                    "wind_speed","m s-1",
                    "eastward_wind","m s-1",
                    "northward_wind","m s-1",
                    "wind_direction","degree",
                    "grid_eastward_wind","m s-1",
                    "grid_northward_wind","m s-1",
                    "air_potential_temperature","K",
                    "soil_water_content","kg m-2",
                    "specific_humidity","1",
                    "mass_fraction_of_water_in_air","1",
                    "cloud_area_fraction","1",
                    "convective_cloud_area_fraction","1",
                    "low_cloud_area_fraction","1",
                    "medium_cloud_area_fraction","1",
                    "high_cloud_area_fraction","1",
                    "altitude_at_cloud_base","m",
                    "air_pressure_at_cloud_base","Pa",
                    "altitude_at_cloud_top","m",
                    "air_pressure_at_cloud_top","Pa",
                    "cloud_condensed_water_content","kg m-2",
                    "atmosphere_water_content","kg m-2",
                    "soil_temperature","K",
                    "canopy_water_amount","kg m-2",
                    "LWE_thickness_of_canopy_water_amount","m",
                    "surface_snow_amount","kg m-2",
                    "surface_snow_thickness","m",
                    "LWE_thickness_of_surface_snow_amount","m",
                    "surface_snow_area_fraction","1",
                    "surface_temperature","K",
                    "atmosphere_boundary_layer_thickness","m",
                    "surface_roughness_length","m",
                    "eastward_sea_water_velocity","m s-1",
                    "northward_sea_water_velocity","m s-1",
                    "sea_water_speed","m s-1",
                    "direction_of_sea_water_velocity","degree",
                    "land_binary_mask","1",
                    "sea_ice_area_fraction","1",
                    "sea_ice_thickness","m",
                    "sea_ice_amount","kg m-2",
                    "sea_ice_mass","kg",
                    "sea_ice_area","m2",
                    "sea_ice_extent","m2",
                    "sea_ice_volume","m3",
                    "sea_ice_freeboard","m",
                    "sea_ice_draft","m",
                    "surface_altitude","m",
                    "surface_temperature_anomaly","K",
                    "LWE_thickness_of_soil_water_content","m",
                    "soil_water_content_at_field_capacity","kg m-2",
                    "ratio_of_soil_water_content_to_soil_water_content_at_field_capacity","1",
                    "vegetation_area_fraction","1",
                    "root_depth","m",
                    "surface_albedo","1",
                    "surface_albedo_assuming_no_snow","1",
                    "surface_albedo_assuming_deep_snow","1",
                    "mass_fraction_of_O3_in_air","1",
                    "molar_fraction_of_O3_in_air","1",
                    "upward_wind","m s-1",
                    "upward_wind_expressed_as_rate_of_change_of_sigma","s-1",
                    "atmosphere_SO4_content","kg m-2",
                    "land_area_fraction","1",
                    "sea_area_fraction","1",
                    "land_ice_area_fraction","1",
                    "leaf_area_index","1",
                    "canopy_height","m",
                    "mass_fraction_of_unfrozen_water_in_soil_water","1",
                    "mass_fraction_of_frozen_water_in_soil_water","1",
                    "soil_frozen_water_content","kg m-2",
                    "soil_albedo","1",
                    "snow_soot_content","kg m-2",
                    "atmosphere_energy_content","J m-2",
                    "soil_carbon_content","kg m-2",
                    "snow_grain_size","m",
                    "snow_temperature","K",
                    "air_temperature","K",
                    "air_temperature_anomaly","K",
                    "TOA_downward_radiative_heat_flux_density","W m-2",
                    "surface_downward_shortwave_heat_flux_density","W m-2",
                    "downward_shortwave_heat_flux_density","W m-2",
                    "downward_longwave_heat_flux_density","W m-2",
                    "TOA_downward_shortwave_heat_flux_density","W m-2",
                    "TOA_incoming_shortwave_heat_flux_density","W m-2",
                    "TOA_outgoing_shortwave_heat_flux_density","W m-2",
                    "TOA_outgoing_shortwave_heat_flux_density_assuming_clear_sky","W m-2",
                    "surface_incident_shortwave_heat_flux_density_assuming_clear_sky","W m-2",
                    "surface_reflected_shortwave_heat_flux_density_assuming_clear_sky","W m-2",
                    "surface_reflected_shortwave_heat_flux_density","W m-2",
                    "large_scale_cloud_area_fraction","1",
                    "rate_of_change_of_air_temperature_due_to_shortwave_heating","K s-1",
                    "rate_of_change_of_air_temperature_due_to_shortwave_heating_assuming_clear_sky","K s-1",
                    "surface_incident_shortwave_heat_flux_density","W m-2",
                    "tropopause_downward_shortwave_heat_flux_density","W m-2",
                    "tropopause_upward_shortwave_heat_flux_density_from_below","W m-2",
                    "surface_downward_longwave_heat_flux_density","W m-2",
                    "surface_emitted_longwave_heat_flux_density","W m-2",
                    "surface_emitted_longwave_heat_flux_density_assuming_clear_sky","W m-2",
                    "TOA_upward_longwave_heat_flux_density","W m-2",
                    "TOA_downward_longwave_heat_flux_density","W m-2",
                    "TOA_upward_longwave_heat_flux_density_assuming_clear_sky","W m-2",
                    "surface_incident_longwave_heat_flux_density","W m-2",
                    "surface_incident_longwave_heat_flux_density_assuming_clear_sky","W m-2",
                    "rate_of_change_of_air_temperature_due_to_longwave_heating","K s-1",
                    "rate_of_change_of_air_temperature_due_to_longwave_heating_assuming_clear_sky","K s-1",
                    "tropopause_downward_longwave_heat_flux_density","W m-2",
                    "tropopause_downward_longwave_heat_flux_density_from_above","W m-2",
                    "downward_heat_flux_density_in_sea_ice","W m-2",
                    "downward_heat_flux_density_in_soil","W m-2",
                    "drag_coefficient","1",
                    "derivative_of_wind_speed_wrt_altitude_in_constant_flux_layer","s-1",
                    "downward_stress_in_constant_flux_layer","Pa",
                    "bulk_Richardson_number","1",
                    "upward_sensible_heat_flux_density_in_air","W m-2",
                    "downward_eastward_stress_in_air","Pa",
                    "downward_northward_stress_in_air","Pa",
                    "upward_water_vapour_mass_flux_density_in_air","kg m-2 s-1",
                    "wind_mixing_energy_flux_density_into_sea","W m-2",
                    "surface_upward_sensible_heat_flux_density","W m-2",
                    "surface_downward_sensible_heat_flux_density","W m-2",
                    "surface_upward_sensible_heat_flux_density_from_sea","W m-2",
                    "surface_upward_water_vapour_mass_flux_density","kg m-2 s-1",
                    "surface_upward_latent_heat_flux_density","W m-2",
                    "surface_downward_latent_heat_flux_density","W m-2",
                    "mass_fraction_of_cloud_ice_in_air","1",
                    "atmosphere_cloud_ice_content","kg m-2",
                    "mass_fraction_of_cloud_liquid_water_in_air","1",
                    "atmosphere_cloud_liquid_water_content","kg m-2",
                    "visibility","m",
                    "dew_point_temperature","K",
                    "freezing_temperature_of_sea_water","K",
                    "surface_snow_melt_amount","kg m-2",
                    "surface_snow_melt_heat_flux_density","W m-2",
                    "transpiration_amount","kg m-2",
                    "transpiration_mass_flux_density","kg m-2 s-1",
                    "gross_primary_productivity_of_carbon_amount","kg m-2 s-1",
                    "net_primary_productivity_of_carbon_amount","kg m-2 s-1",
                    "plant_respiration_mass_flux_density","kg m-2 s-1",
                    "large_scale_rainfall_amount","kg m-2",
                    "large_scale_snowfall_amount","kg m-2",
                    "large_scale_rainfall_mass_flux_density","kg m-2 s-1",
                    "large_scale_snowfall_mass_flux_density","kg m-2 s-1",
                    "relative_humidity","1",
                    "convective_rainfall_amount","kg m-2",
                    "convective_snowfall_amount","kg m-2",
                    "rate_of_change_of_specific_humidity_due_to_convection","s-1",
                    "convective_rainfall_mass_flux_density","kg m-2 s-1",
                    "convective_snowfall_mass_flux_density","kg m-2 s-1",
                    "air_pressure_at_convective_cloud_base","Pa",
                    "air_pressure_at_convective_cloud_top","Pa",
                    "mass_fraction_of_convective_condensed_water_in_air","1",
                    "rainfall_mass_flux_density","kg m-2 s-1",
                    "snowfall_mass_flux_density","kg m-2 s-1",
                    "precipitation_mass_flux_density","kg m-2 s-1",
                    "specific_potential_energy","J kg-1",
                    "specific_convectively_available_potential_energy","J kg-1",
                    "precipitation_amount","kg m-2",
                    "large_scale_precipitation_amount","kg m-2",
                    "convective_precipitation_amount","kg m-2",
                    "convective_precipitation_mass_flux_density","kg m-2 s-1",
                    "rate_of_change_of_wind_due_to_convention","m s-2",
                    "rate_of_change_of_specific_humidity_due_to_diabatic_processes","s-1",
                    "rate_of_change_of_air_temperature_due_to_diabatic_processes","s-1",
                    "rate_of_change_of_air_temperature_due_to_large_scale_precipitation","s-1",
                    "rate_of_change_of_air_temperature_due_to_moist_convection","s-1",
                    "rate_of_change_of_air_temperature_due_to_dry_convection","s-1",
                    "surface_eastward_gravity_wave_stress","Pa",
                    "surface_northward_gravity_wave_stress","Pa",
                    "rate_of_change_of_wind_due_to_gravity_wave_drag","m s-2",
                    "rate_of_change_of_eastward_wind_due_to_gravity_wave_drag","m s-2",
                    "rate_of_change_of_northward_wind_due_to_gravity_wave_drag","m s-2",
                    "surface_runoff_amount","kg m-2",
                    "subsurface_runoff_amount","kg m-2",
                    "surface_runoff_mass_flux_density","kg m-2 s-1",
                    "subsurface_runoff_mass_flux_density","kg m-2 s-1",
                    "runoff_mass_flux_density","kg m-2 s-1",
                    "wet_bulb_temperature","K",
                    "omega","Pa s-1",
                    "Ertel_potential_vorticity","K m2 kg-1 s-1",
                    "product_of_eastward_wind_and_northward_wind","m2 s-2",
                    "product_of_air_temperature_and_eastwind_wind","K m s-1",
                    "product_of_air_temperature_and_northward_wind","K m s-1",
                    "square_of_air_temperature","K2",
                    "square_of_eastward_wind","m2 s-2",
                    "square_of_northward_wind","m2 s-2",
                    "product_of_eastward_wind_and_omega","Pa m s-2",
                    "product_of_northward_wind_and_omega","Pa m s-2",
                    "product_of_eastward_wind_and_specific_humidity","m s-1",
                    "product_of_northward_wind_and_specific_humidity","m s-1",
                    "product_of_air_temperature_and_omega","K Pa s-1",
                    "atmosphere_kinetic_energy_content","J m-2",
                    "geopotential_height","m",
                    "geopotential_height_anomaly","m",
                    "product_of_eastward_wind_and_geopotential_height","m2 s-1",
                    "product_of_northward_wind_and_geopotential_height","m2 s-1",
                    "freezing_level_altitude","m",
                    "freezing_level_air_pressure","Pa",
                    "tropopause_air_pressure","Pa",
                    "tropopause_air_temperature","K",
                    "tropopause_altitude","m",
                    "sea_level_air_pressure","Pa",
                    "vegetation_carbon_content","kg m-2",
                    "litter_carbon_mass_flux_density","kg m-2 s-1",
                    "sea_water_temperature","K",
                    "sea_water_potential_temperature","K",
                    "sea_water_salinity","1",
                    "baroclinic_eastward_sea_water_velocity","m s-1",
                    "baroclinic_northward_sea_water_velocity","m s-1",
                    "ocean_barotropic_streamfunction","m3 s-1",
                    "rate_of_change_of_ocean_barotropic_streamfunction","m3 s-2",
                    "sea_surface_elevation","m",
                    "sea_surface_elevation_anomaly","m",
                    "barotropic_eastward_sea_water_velocity","m s-1",
                    "barotropic_northward_sea_water_velocity","m s-1",
                    "ocean_mixed_layer_thickness","m",
                    "eastward_stress_of_sea_ice_on_ocean","Pa",
                    "northward_stress_of_sea_ice_on_ocean","Pa",
                    "surface_snow_thickness_on_sea_ice","m",
                    "upward_sensible_heat_flux_density_in_sea_water_at_sea_ice_base","W m-2",
                    "sea_ice_speed","m s-1",
                    "sea_ice_eastward_velocity","m s-1",
                    "sea_ice_northward_velocity","m s-1",
                    "direction_of_sea_ice_velocity","degree",
                    "divergence_of_sea_ice_velocity","s-1",
                    "rate_of_change_of_sea_ice_thickness_due_to_thermodynamics","m s-1",
                    "surface_downward_eastward_stress","Pa",
                    "surface_downward_northward_stress","Pa",
                    "heat_flux_correction","W m-2",
                    "water_flux_correction","kg m-2 s-1",
                    "ocean_isopycnal_layer_thickness_diffusivity","m2 s-1",
                    "sea_water_upward_velocity","m s-1",
                    "northward_heat_flux_in_ocean","W",
                    "northward_salt_mass_flux_in_ocean","kg s-1",
                    "northward_fresh_water_mass_flux_in_ocean","kg s-1",
                    "significant_height_of_wind_waves_and_swell_waves","m",
                    "direction_of_wind_wave_velocity","degree",
                    "significant_height_of_wind_waves","m",
                    "wind_wave_period","s",
                    "direction_of_swell_wave_velocity","degree",
                    "significant_height_of_swell_waves","m",
                    "swell_wave_period","s"},
                new String[] {}
            );
        }
        catch (Exception e)
        {
            System.err.println(
                "ERROR: " +
                "Couldn't initialize class visad.data.netcdf.in.CfView: " + e);
            System.exit(1);
        }
    }

    /**
     * Constructs from a netCDF dataset and a quantity database.  The quantity
     * database will be supplemented with another database specific to this
     * view.
     *
     * @param netcdf                The netCDF dataset.
     * @param quantDb               The default quantity database.
     * @throws NullPointerException if the netCDF dataset argument is
     *                              <code>null</code>.
     * @throws IllegalArgumentException
     *                              if the netCDF dataset doesn't follow the
     *                              conventions of this view.
     */
    CfView(Netcdf netcdf, QuantityDB quantDb)
    {
        this(netcdf, quantDb, false);
    }

    /**
     * Constructs from a netCDF dataset and a quantity database.  The quantity
     * database will be supplemented with another database specific to this
     * view.
     *
     * @param netcdf                The netCDF dataset.
     * @param quantDb               The default quantity database.
     * @param charToText            Convert char variables to Text if true
     * @throws NullPointerException if the netCDF dataset argument is
     *                              <code>null</code>.
     * @throws IllegalArgumentException
     *                              if the netCDF dataset doesn't follow the
     *                              conventions of this view.
     */
    CfView(Netcdf netcdf, QuantityDB quantDb, boolean charToText)
    {
        super(netcdf, quantDb, charToText);
        /*
         * Check the "Conventions" global attribute.
         */
        {
            String conventions = getConventionsString(netcdf);
            if (conventions == null)
                throw new IllegalArgumentException(
                    "No \"Conventions\" attribute in netCDF dataset");
            int i;
            for (i = 0; i < CF_CONVENTIONS_STRINGS.length; i++)
                if (conventions.equals(CF_CONVENTIONS_STRINGS[i]))
                    break;
            if (i >= CF_CONVENTIONS_STRINGS.length)
                throw new IllegalArgumentException(
                    "Illegal \"Conventions\" attribute: \"" + conventions +
                    "\"");
        }
        /*
         * Allocate caches.
         */
        varToRealType = new WeakHashMap();
        varToUnitString = new WeakHashMap();
        dimsToDomain = new WeakHashMap();
        varToAuxCoordVars = new WeakHashMap();
        /*
         * Build a database of all netCDF variables that contain metadata
         * rather than data.
         */
        auxCoordVars = new TreeSet(varComparator);
        boundaryVars = new TreeSet();
        for (VariableIterator varIter = getNetcdf().iterator();
            varIter.hasNext(); )
        {
            Variable   var = varIter.next();
            Variable[] vars = getAuxCoordVars(var);
            for (int i = 0; i < vars.length; i++)
                auxCoordVars.add(vars[i]);
            Variable   boundVar = getBoundaryVar(var);
            if (boundVar != null)
                boundaryVars.add(boundVar);
        }
    }

    /**
     * Returns the "coordinates" attribute of a variable.  Returns 
     * <code>null</code> if the variable doesn't have such an attribute.
     *
     * @param var                   The variable.
     * @return                      The value of the attribute or 
     *                              <code>null</code>.
     * @throws NullPointerException if the variable is <code>null</code>.
     */
    private String getAuxCoordVarString(Variable var)
    {
        return getAttributeString(var, "coordinates");
    }

    /**
     * Indicates if a given variable is a CF auxilliary coordinate variable.
     *
     * @param var                    The variable.
     * @return                       <code>true</code> if and only if the
     *                               variable is an auxilliary coordinate 
     *                               variable.
     */
    private boolean isAuxCoordVar(Variable var)
    {
        return auxCoordVars.contains(var);
    }

    /**
     * Indicates if a given netCDF variable has any CF auxilliary
     * coordinate variables.
     *
     * @param var                  The variable.
     * @return                     True if and only if the variable has
     *                             any auxilliary coordinate variables.
     * @throw NullPointerException if the variable is <code>null</code>.
     */
    private boolean hasAuxCoordVars(Variable var)
    {
        return getAuxCoordVars(var).length > 0;
    }

    /**
     * <p>Returns the CF auxilliary coordinate variables of a given netCDF
     * variable in netCDF order (outermost dimension first).  The returned array
     * will be empty if the netCDF variable doesn't have any CF auxilliary
     * coordinate variables.</p>
     *
     * <p>This implementation uses {@link #getAuxCoordVarString(Variable)},
     * {@link isNumeric(String)}, and {@link #getVariable(String)}.
     *
     * @param var                  The variable.
     * @return                     The auxilliary coordinate variables of the
     *                             variable.  Will have zero length if the
     *                             variable doesn't have any.
     * @throw NullPointerException if the variable is <code>null</code>.
     */
    private Variable[] getAuxCoordVars(Variable var)
    {
        if (var == null)
            throw new NullPointerException();
        /*
         * This implementation caches results to improve performance.
         */
        synchronized(varToAuxCoordVars)
        {
            Variable[] vars = (Variable[])varToAuxCoordVars.get(var);
            if (vars == null)
            {
                String    attrStr = getAuxCoordVarString(var);
                if (attrStr == null)
                {
                    vars = nilVarArray;
                }
                else
                {
                    ArrayList list = new ArrayList(7);
                    for (StringTokenizer st = new StringTokenizer(attrStr);
                        st.hasMoreTokens(); )
                    {
                        String name = st.nextToken();
                        // ignore non-mumeric "list" variables
                        if (isNumeric(name))
                            list.add(getVariable(name));
                    }
                    vars = (Variable[])list.toArray(nilVarArray);
                }
                varToAuxCoordVars.put(var, vars);
            }
            return (Variable[])vars.clone();
        }
    }

    /**
     * Returns the CF boundary variable of a given netCDF variable or
     * <code>null</code> if the netCDF variable has no boundary variable.  If
     * the variable referenced by the input variable's <code>bounds</code>
     * attribute doesn't exist, then a warning message is printed to {@link
     * System#err} and <code>null</code> is returned.
     *
     * @param var                  The variable.
     * @return                     The associated CF boundary variable or
     *                             <code>null</code>.
     * @throw NullPointerException if the variable is <code>null</code>.
     */
    private Variable getBoundaryVar(Variable var)
    {
        if (var == null)
            throw new NullPointerException();
        String attrStr = getAttributeString(var, "bounds");
        if (attrStr == null)
            return null;
        Variable boundVar = getVariable(attrStr);
        if (boundVar == null)
            System.err.println(
                "WARNING: " +
                "The boundary variable of variable \"" + var.getName() +
                "\" doesn't exist");
        return boundVar;
    }

    /**
     * Indicates if a given variable is a CF boundary variable.
     *
     * @param var                    The variable.
     * @return                       <code>true</code> if and only if the
     *                               variable is a CF boundary variable.
     */
    private boolean isBoundaryVar(Variable var)
    {
        return boundaryVars.contains(var);
    }

    /**
     * Gets the standard name of a netCDF variable from the
     * <code>long_name</code> attribute.  If the attribute doesn't exist
     * then <code>null</code> is returned.  If the the attribute value isn't
     * a string, then and error message is printed and <code>null</code> is
     * returned.
     *
     * @param var               A netCDF variable.
     * @return                  The long name of <code>var</code> or 
     *                          <code>null</code>.
     */
    private String getStandardName(Variable var)
    {
        return getAttributeString(var, "standard_name");
    }

    /**
     * Returns the string value of the unit attribute of a netCDF variable.
     * Returns <code>null</code> if the unit attribute is missing or invalid.
     * Because the CF netCDF convention requires the use of the unit attribute,
     * this method prints a warning message to {@link System#err} if the
     * variable doesn't have a unit attribute.
     *
     * @param var               A netCDF variable.
     * @return                  The unit of the values of <code>var</code> or
     *                          <code>null</code>.
     */
    protected String getUnitString(Variable var)
    {
        /*
         * This method caches results to improve performance and to reduce the
         * number of warning messages.
         */
        String str;
        synchronized(varToUnitString)
        {
            // NB: A null entry may exist.
            str = (String)varToUnitString.get(var);
            if (!varToUnitString.containsKey(var))
            {
                str = super.getUnitString(var);  // doesn't print message
                if (str == null)
                    System.err.println(
                        "WARNING: " +
                        "Variable \"" + var.getName() +
                        "\" doesn't have a unit attribute.");
                varToUnitString.put(var, str);  // cache result
            }
        }
        return str;
    }

    /**
     * <p>Returns the unit of a netCDF variable according to the variable's unit
     * attribute.  Returns <code>null</code> if the unit attribute is missing or
     * invalid.</p>
     *
     * This implementation uses {@link #getUnitString(Variable)} and {@link
     * View#getUnitFromAttribute(Variable)}.
     *
     * @param var                   A netCDF variable.
     * @return                      The unit of <code>var</code> or
     *                              <code>null</code>.
     * @throws NullPointerException if the variable is <code>null</code>.
     */
    protected Unit getUnitFromAttribute(Variable var)
    {
        String unitStr = getUnitString(var);
        if (unitStr == null)
            return null;
        if (unitStr.equals("level") ||
            unitStr.equals("layer") ||
            unitStr.equals("sigma_level"))
        {
            return CommonUnit.dimensionless;
        }
        return super.getUnitFromAttribute(var);
    }

    /**
     * <p>Return the VisAD RealType of a netCDF variable.  If the variable is a
     * type of timestamp and references a non-supported calendar system, then a
     * warning message is printed to {@link System#err} and an attempt is made
     * to create a new {@link RealType} with a different name.</p>
     *
     * <p>This implementation uses {@link * View#getRealType(Variable)}.</p>
     *
     * @param var               The netCDF variable.
     * @return                  The VisAD RealType of <code>var</code>.
     * @throws TypeException    if a corresponding {@link RealType} needed
     *                          to be created but couldn't.
     */
    protected RealType getRealType(Variable var)
        throws TypeException
    {
        RealType type;
        /*
         * This method caches results to improve performance and to reduce
         * the number of warning messages.
         */
        synchronized(varToRealType)
        {
            type = (RealType)varToRealType.get(var);
            if (type == null)
            {
                type = getRealTypeFromStandardName(var);
                if (type != null)
                    varToRealType.put(var, type);  // cache result
                else
                    type = super.getRealType(var);
                Unit unit = type.getDefaultUnit();
                if (unit instanceof OffsetUnit &&
                    unit.getAbsoluteUnit().isConvertible(SI.second))
                {
                    String str = getAttributeString(var, "calendar");
                    if (str != null &&
                        !str.equals("gregorian") && !str.equals("standard"))
                    {
                        String  newName = newName(var);
                        System.err.println(
                            "WARNING: " +
                            "No support for \"" + str + 
                            "\" calendar of variable \"" + var + "\".  " +
                            "Attempting to create new quantity \"" + newName +
                            "\" with non-timescale unit.");
                        type =
                            RealType.getRealType(
                                newName, unit.getAbsoluteUnit());
                        varToRealType.put(var, type);  // cache result
                    }
                }
            }
        }
        return type;
    }

    /**
     * If the unit attribute of the variable is inconvertible with the unit of
     * the variables's standard quantity, then a warning message is printed
     * to {@link System#err} and an attempt is made to create a new {@link
     * RealType} with a different name.
     *
     * @return The corresponding {@link RealType} or <code>null</code>.
     */
    private RealType getRealTypeFromStandardName(Variable var)
    {
        RealType type;
        String   name = getStandardName(var);
        if (name == null)
        {
            type = null;
        }
        else
        {
            type = cfQuantityDB.get(name);
            if (type != null)
            {
                Unit unit = getUnitFromAttribute(var);
                if (unit != null &&
                    !Unit.canConvert(unit, type.getDefaultUnit()))
                {
                    String  newName = newName(var);
                    System.err.println(
                        "WARNING: " +
                        "The units attribute of variable " + var.getName() +
                        " is incompatible with the unit of the quantity" +
                        " referenced by the standard-name attribute.  " +
                        "Attempting to create new quantity \"" + newName +
                        "\".");
                    type = RealType.getRealType(newName, unit);
                }
            }
        }
        return type;
    }

    /**
     * Gets an iterator over the virtual VisAD data objects determined by
     * this view.
     *
     * @return                  An iterator for the virtual VisAD data objects
     *                          in the view.
     */
    public VirtualDataIterator getVirtualDataIterator()
    {
        return new DataIterator();
    }

    /**
     * <p>Indicates if a given variable should be ignored during iteration.</p>
     *
     * <p>This implementation returns the logical "or" of {@link
     * #isCoordinateVariable(Variable)}, {@link #isAuxCoordVar(Variable)}, and
     * {@link #isBoundaryVar(Variable)}.</p>
     *
     * @return                    <code>true</code> if and only if the variable
     *                            should be ignored.
     */
    protected boolean isIgnorable(Variable var)
    {
        return
            isCoordinateVariable(var) ||
            isAuxCoordVar(var) ||
            isBoundaryVar(var);
    }

    /**
     * Returns the domain of a netCDF variable.  This method supports CF
     * auxilliary coordinate variables.
     *
     * @param var               A netCDF variable.
     * @return                  The domain of the given variable.
     * @throws NullPointerException
     *                          if the variable is <code>null</code>.
     * @throws IllegalArgumentException
     *                          if the rank of the variable is zero.
     * @throws TypeException    if a {@link RealType} needed to be created but
     *                          couldn't.
     * @throws IOException      if a netCDF read-error occurs.
     */
    protected Domain getDomain(Variable var)
        throws TypeException, IOException
    {
        ArrayList   list = new ArrayList(7);
        ArrayList   vars = new ArrayList(7);
        Variable[]  auxVars = getAuxCoordVars(var);
        {
            Dimension[] dims = getDimensions(var);
            list = new ArrayList(dims.length + auxVars.length);
            for (int i = 0; i < dims.length; i++)
                list.add(new SimpleDimension(dims[i]));
        }
        for (int iaux = 0; iaux < auxVars.length; )
        {
            Variable    auxVar = auxVars[iaux];
            Dimension[] dims = getDimensions(auxVar);
            vars.add(auxVar);
            boolean     auxVarsHaveUnits = true;
            for (int j = iaux+1;
                j < auxVars.length &&
                    Arrays.equals(dims, getDimensions(auxVars[j]));
                j++)
            {
                vars.add(auxVars[j]);
                auxVarsHaveUnits &=
                    getRealType(auxVars[j]).getDefaultUnit() != null;
            }
            boolean dimsHaveUnits = true;
            for (int j = 0; j < dims.length; j++)
                dimsHaveUnits &= 
                    getRealType(dims[j]).getDefaultUnit() != null;
            /*
             * Ignore CF auxilliary coordinate variables if they don't
             * have units and the regular dimensions do because that
             * probably indicates that the auxilliary coordinate variables
             * are "alternative coordinates" and that the more important
             * coordinates are the regular dimensions.  Otherwise, favor
             * auxilliary coordinates variables.
             */
            if (!dimsHaveUnits || auxVarsHaveUnits)
            {
                int index = list.indexOf(new SimpleDimension(dims[0]));
                try
                {
                    for (int j = 0; j < dims.length; j++)
                        list.remove(
                            list.indexOf(new SimpleDimension(dims[j])));
                }
                catch (IndexOutOfBoundsException e)
                {
                    throw new IllegalArgumentException(
                        "Invalid dimensional structure: variable \"" +
                        var.getName() + "\"");
                }
                list.add(
                    index,
                    new AuxCoordVarsDimension(
                        (Variable[])vars.toArray(nilVarArray)));
            }
            iaux += vars.size();
            vars.clear();
        }
        /*
         * This implementation caches results to improve performance.
         */
        DimensionList domain;
        synchronized (dimsToDomain)
        {
            domain = (DimensionList)dimsToDomain.get(list);
            if (domain == null)
            {
                domain = new DimensionList(var, list);  // list not copied
                /*
                 * The clone() method is invoked to ensure that the
                 * DimensionList value in the WeakHashMap doesn't reference
                 * its key.
                 */
                dimsToDomain.put(list.clone(), domain);
            }
        }
        return domain;
    }

    /**
     * Iterates over the virtual VisAD data objects in a netCDF dataset
     * according to the CF conventions.
     */
    final class DataIterator
        extends VirtualDataIterator
    {
        /**
         * The netCDF variable iterator.
         */
        private final VariableIterator  varIter;

        /**
         * Constructs from nothing.
         *
         * @param view          A view of a netCDF dataset.
         */
        DataIterator()
        {
            super(CfView.this);
            varIter = CfView.this.getNetcdf().iterator();
        }

        /**
         * Returns a clone of the next virtual VisAD data object.
         *
         * <p>This implementation uses {@link #isCharToText()},
         * {@link #isNumeric(Variable)}, {@link #isIgnorable(Variable)}, 
         * and {@link #getData(Variable)}.</p>
         *
         * @return                      A clone of the next virtual VisAD data
         *                              object or <code> null</code> if there is
         *                              no more data.
         * @throws TypeException        if a {@link RealType} needed
         *                              to be created but couldn't.
         * @throws VisADException       Couldn't create necessary VisAD object.
         */
        protected VirtualData getData()
            throws TypeException, VisADException, IOException
        {
            while (varIter.hasNext())
            {
                Variable        var = varIter.next();
                // handle text only if charToText == true and rank <= 2
                if (!isNumeric(var) && (!isCharToText() || var.getRank() > 2))
                    continue;  // TODO: support arrays of text (Tuple?)
                if (isIgnorable(var))
                {
                    /*
                     * Ignore coordinate variables, auxilliary coordinate
                     * variables, and boundary variables.
                     */
                    continue;
                }
                VirtualScalar   scalar =
                    (isNumeric(var) == true)

                        ? (VirtualScalar) 
                            new VirtualReal(getRealType(var),
                                            var,
                                            getRangeSet(var),
                                            getUnitFromAttribute(var),
                                            getVetter(var))

                        : (VirtualScalar)
                            new VirtualText(getTextType(var), var);

                return
                    (var.getRank() == 0 || 
                     (!isNumeric(var) && var.getRank() == 1))
                        ? (VirtualData)scalar
                        : getDomain(var).getVirtualField(
                            new VirtualTuple(scalar));
            }
            return null;        // no more data
        }
    }

    /**
     * The CF domain of a netCDF variable.  A CF domain comprises a list of CF
     * dimensions.  A CF dimension is either a netCDF dimension or a list of
     * auxilliary coordinate variables of the same dimensionality (sequence of
     * netCDF dimensions).
     */
    private final class DimensionList
        extends Domain
    {
        /**
         * Outermost dimension first; at least one element.
         */
        private final ArrayList     list;
        private volatile int        hashCode;
        private volatile SampledSet domain;

        /**
         * @param var                       The netCDF variable.
         * @param list                      The list of CF dimensions of the
         *                                  variable.
         * @throws NullPointerException     if the variable or list is 
         *                                  <code>null</code>.
         * @throws IllegalArgumentException if the rank of the variable is 0 or
         *                                  if the variable has auxilliaray
         *                                  coordinate variables whose
         *                                  dimensional structure is invalid.
         * @throws TypeException            if a {@link RealType} needed to be
         *                                  created but couldn't.
         */
        DimensionList(Variable var, ArrayList list)
            throws TypeException
        {
            super(var);
            this.list = list;  // NOTE: not copied
        }

        /**
         * Returns a {@link VirtualField} corresponding to this domain and
         * a given range.
         *
         * @param range                 The range for the {@link VirtualField}.
         * @throws NullPointerException if the argument is <code>null</code>.
         * @throws IOException          if a read error occurs.
         * @throws VisADException       if a VisAD object can't be created.
         */
        protected VirtualField getVirtualField(VirtualTuple range)
            throws VisADException, IOException
        {
            VirtualField field;
            int          nCfDim = list.size();
            if (nCfDim == 1)
            {
                field = VirtualField.newVirtualField(getDomainSet(list), range);
            }
            else
            {
                CfDimension outerDim = (CfDimension)list.get(0);
                Unit[]      units = outerDim.getUnits();
                if (nCfDim == 2 && range.getType() instanceof TextType) { //char
                   field = 
                       VirtualField.newVirtualField(
                           getDomainSet(list.subList(0,1)), range);
                }
                else if (units.length > 1 || !CfView.this.isTime(units[0]))
                {
                    field =
                        VirtualField.newVirtualField(getDomainSet(list), range);
                }
                else
                {
                    field =
                        VirtualField.newVirtualField(
                            getDomainSet(list.subList(0, 1)),
                            new VirtualTuple(
                                VirtualField.newVirtualField(
                                    getDomainSet(list.subList(1, nCfDim)),
                                    range)));
                }
            }
            return field;
        }

        /**
         * @throws VisADException   if a VisAD object can't be created.
         */
        private SampledSet getDomainSet(List cfDims)
            throws VisADException, IOException
        {
            SampledSet[] sets = new SampledSet[cfDims.size()];
            for (int i = 0, j = sets.length; i < sets.length; i++)
                sets[i] = ((CfDimension)cfDims.get(--j)).getDomainSet();
                    // reverse order
            return
                sets.length == 1
                    ? sets[0]
                        /*
                         * WORKAROUND:  The product() method is invoked
                         * because the VisAD display subsystem has problems
                         * displaying ProductSet-s as of 2001-08-12.
                         */
                    : new ProductSet(sets).product();
        }

        public boolean equals(Object obj)
        {
            if (obj == this)
                return true;
            if (!(obj instanceof DimensionList))
                return false;
            return list.equals(((DimensionList)obj).list);
        }

        public int hashCode()
        {
            int hash = hashCode;
            if (hash == 0)
                hash = hashCode = list.hashCode();
            return hash;
        }
    }

    private abstract class CfDimension
    {
        /**
         * Units are in netCDF order (unit of outermost dimension first).
         */
        abstract Unit[] getUnits()
            throws TypeException;

        abstract SampledSet getDomainSet()
            throws VisADException, IOException;

        public abstract boolean equals(Object obj);

        public abstract int hashCode();
    }

    private final class SimpleDimension
        extends CfDimension
    {
        private final Dimension               dim;
        private transient volatile SampledSet domain;

        SimpleDimension(Dimension dim)
        {
            this.dim = dim;
        }

        /**
         * @throws TypeException if a {@link RealType} needed
         *                       to be created but couldn't.
         */
        Unit[] getUnits()
            throws TypeException
        {
            return new Unit[] {getRealType(dim).getDefaultUnit()};
        }

        /**
         * @throws VisADException   if a VisAD object can't be created.
         */
        SampledSet getDomainSet()
            throws VisADException, IOException
        {
            SampledSet set = domain;
            if (set == null)
                set = domain = CfView.this.getDomainSet(dim);
            return set;
        }

        public boolean equals(Object obj)
        {
            if (obj == this)
                return true;
            if (!(obj instanceof SimpleDimension))
                return false;
            return dim.equals(((SimpleDimension)obj).dim);
        }

        public int hashCode()
        {
            return dim.hashCode();
        }
    }

    private final class AuxCoordVarsDimension
        extends CfDimension
    {
        /*
         * Outermost dimension first; at least one element.
         */
        private final Variable[]              vars;
        private transient volatile int        hashCode;
        private transient volatile SampledSet domain;

        /**
         * WARNING: It is the responsibility of the client not to modify
         * the input array.
         *
         * @param auxCoordVars    Auxilliary coordinate variable in netCDF
         *                        order (outermost dimension first).
         */
        AuxCoordVarsDimension(Variable[] auxCoordVars)
        {
            if (auxCoordVars.length < 1)
                throw new IllegalArgumentException();
            vars = auxCoordVars;  // WARNING: not copied
        }

        /**
         * @throws TypeException if a {@link RealType} needed
         *                       to be created but couldn't.
         */
        Unit[] getUnits()
            throws TypeException
        {
            Unit[] units = new Unit[vars.length];
            for (int i = 0; i < units.length; i++)
                units[i] = getRealType(vars[i]).getDefaultUnit();
            return units;
        }

        /**
         * @throws TypeException if a {@link RealType} needed
         *                       to be created but couldn't.
         */
        SampledSet getDomainSet()
            throws IOException, TypeException, VisADException
        {
            SampledSet set = domain;
            if (set == null)
            {
                Variable var0 = vars[0];
                int      nDim = var0.getRank();
                int[]    lengths = var0.getLengths();
                // reverse order
                for (int i = 0, j = nDim; i < nDim/2; i++)
                {
                    int n = lengths[--j];
                    lengths[j] = lengths[i];
                    lengths[i] = n;
                }
                int        nVar = vars.length;
                RealType[] types = new RealType[nVar];
                Unit[]     units = new Unit[nVar];
                float[][]  values = new float[nVar][];
                for (int i = 0, j = nVar; i < nVar; i++)
                {
                    Variable var = vars[--j]; // reverse order
                    types[i] = getRealType(var);
                    values[i] = toFloat(var.toArray());
                    units[i] = getUnitFromAttribute(var);
                }
                set = domain =
                    GriddedSet.create(
                        nVar == 1
                            ? (MathType)types[0]
                            : new RealTupleType(types),
                        values,
                        lengths,
                        (CoordinateSystem)null,
                        units,
                        (ErrorEstimate[])null);
            }
            return set;
        }

        private float[] toFloat(Object obj)
        {
            if (obj instanceof byte[])
                return toFloat((byte[])obj);
            if (obj instanceof short[])
                return toFloat((short[])obj);
            if (obj instanceof int[])
                return toFloat((int[])obj);
            if (obj instanceof float[])
                return toFloat((float[])obj);
            return toFloat((double[])obj);
        }

        private float[] toFloat(byte[] values)
        {
            float[] vals = new float[values.length];
            for (int i = 0; i < vals.length; i++)
                vals[i] = values[i];
            return vals;
        }

        private float[] toFloat(short[] values)
        {
            float[] vals = new float[values.length];
            for (int i = 0; i < vals.length; i++)
                vals[i] = values[i];
            return vals;
        }

        private float[] toFloat(int[] values)
        {
            float[] vals = new float[values.length];
            for (int i = 0; i < vals.length; i++)
                vals[i] = values[i];
            return vals;
        }

        private float[] toFloat(float[] values)
        {
            return values;  // WARNING: Not copied
        }

        private float[] toFloat(double[] values)
        {
            float[] vals = new float[values.length];
            for (int i = 0; i < vals.length; i++)
                vals[i] = (float)values[i];
            return vals;
        }

        public boolean equals(Object obj)
        {
            if (obj == this)
                return true;
            if (!(obj instanceof AuxCoordVarsDimension))
                return false;
            AuxCoordVarsDimension that = (AuxCoordVarsDimension)obj;
            if (vars == that.vars)
                return true;
            if (vars.length != that.vars.length)
                return false;
            for (int i = 0; i < vars.length; i++)
                if (!vars[i].getName().equals(that.vars[i].getName()))
                    return false;
            return true;
        }

        public int hashCode()
        {
            int hash = hashCode;
            if (hash == 0)
            {
                hash = 0;
                for (int i = 0; i < vars.length; i++)
                    hash ^= vars[i].getName().hashCode();
                hashCode = hash;
            }
            return hash;
        }
    }
}
