package visad.jmet;

import visad.*;
import visad.data.netcdf.*;
import visad.data.netcdf.units.*;

public class MetUnits {

  public MetUnits () throws Exception {
    UnitsDB du = DefaultUnitsDB.instance();
    Unit hpa = du.get("hPa");
    hpa = hpa.clone("hPa");
    du.putSymbol("HPA", hpa);
    du.putSymbol("hPa", hpa);
    du.putSymbol("MB", hpa);
    du.putSymbol("mb", hpa);
  }

  public String makeSymbol(String s) {
    String in = s.trim();
    String out = in;
    if (in.equalsIgnoreCase("m")) out = "m";
    if (in.equalsIgnoreCase("sec")) out = "s";
    if (in.equalsIgnoreCase("mps")) out = "m/s";
    if (in.equalsIgnoreCase("mph")) out = "mi/h";
    if (in.equalsIgnoreCase("km")) out = "km";
    if (in.equalsIgnoreCase("cm")) out = "cm";
    if (in.equalsIgnoreCase("mi")) out = "mi";
    if (in.equalsIgnoreCase("pa")) out = "Pa";
    if (in.equalsIgnoreCase("nmi")) out = "nmi";
    if (in.equalsIgnoreCase("mm")) out = "mm";
    if (in.equalsIgnoreCase("deg")) out = "deg";
    if (in.equalsIgnoreCase("yd")) out = "yd";
    if (in.equalsIgnoreCase("ft")) out = "ft";
    if (in.equalsIgnoreCase("f")) out = "degF";
    if (in.equalsIgnoreCase("c")) out = "defC";
    if (in.equalsIgnoreCase("k")) out = "K";
    if (in.equalsIgnoreCase("inhg")) out = "inhg";
    if (in.equalsIgnoreCase("kt")) out = "kt";
    if (in.equalsIgnoreCase("kts")) out = "kt";
    if (in.equalsIgnoreCase("g/kg")) out = "g/kg";

    // the following are decidedly McIDAS
    if (in.equalsIgnoreCase("paps")) out = "Pa/s";
    if (in.equalsIgnoreCase("mgps")) out = "km^2(kg/s)";
    if (in.equalsIgnoreCase("m2s2")) out = "m^2/s^2";
    if (in.equalsIgnoreCase("kpm")) out = "K/m";
    if (in.equalsIgnoreCase("m2ps")) out = "m^2/s";
    if (in.equalsIgnoreCase("gpkg")) out = "g/kg";
    if (in.equalsIgnoreCase("kgm2")) out = "kg/m^2";
    if (in.equalsIgnoreCase("kgm3")) out = "kg/m^3";
    if (in.equalsIgnoreCase("mmps")) out = "(kg/m^2)/s";
    if (in.equalsIgnoreCase("ps")) out = "s-1";
    if (in.equalsIgnoreCase("wpm2")) out = "W/m^2";
    if (in.equalsIgnoreCase("nm2")) out = "N/m^2";
    if (in.equalsIgnoreCase("kgps")) out = "kg/kg/s";
    if (in.equalsIgnoreCase("ps2")) out = "s^-2";
    if (in.equalsIgnoreCase("pspm")) out = "s^-1m^-1";
    if (in.equalsIgnoreCase("jpkg")) out = "J/kg";
    if (in.equalsIgnoreCase("kgkg")) out = "kg/kg";
    if (in.equalsIgnoreCase("psps")) out = "s^-1";

    return out;
  }
}

