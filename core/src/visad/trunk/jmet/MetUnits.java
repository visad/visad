package visad.jmet;

import visad.*;
import visad.data.netcdf.*;
import visad.data.netcdf.units.*;

public final class MetUnits {

  public static void make() throws Exception {
    UnitsDB du = DefaultUnitsDB.instance();
    Unit hpa = du.get("hPa");
    hpa = hpa.clone("hPa");
    du.putSymbol("HPA", hpa);
    du.putSymbol("hPa", hpa);
    du.putSymbol("MB", hpa);
    du.putSymbol("mb", hpa);

    Unit meter = du.get("m");
    meter = meter.clone("m");
    du.putSymbol("M", meter);
  }
}

