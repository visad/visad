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
    if (in.equalsIgnoreCase("mps")) out = "m/s";
    if (in.equalsIgnoreCase("mph")) out = "mi/h";
    if (in.equalsIgnoreCase("km")) out = "km";
    if (in.equalsIgnoreCase("cm")) out = "cm";
    if (in.equalsIgnoreCase("mi")) out = "mi";
    if (in.equalsIgnoreCase("nmi")) out = "nmi";
    if (in.equalsIgnoreCase("mm")) out = "mm";
    if (in.equalsIgnoreCase("yd")) out = "yd";
    if (in.equalsIgnoreCase("ft")) out = "ft";
    if (in.equalsIgnoreCase("f")) out = "degF";
    if (in.equalsIgnoreCase("c")) out = "defC";
    if (in.equalsIgnoreCase("k")) out = "K";
    if (in.equalsIgnoreCase("inhg")) out = "inhg";
    if (in.equalsIgnoreCase("kt")) out = "kt";
    if (in.equalsIgnoreCase("kts")) out = "kt";
    if (in.equalsIgnoreCase("g/kg")) out = "g/kg";

    return out;
  }
}

