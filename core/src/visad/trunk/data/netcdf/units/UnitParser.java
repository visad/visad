package visad.data.netcdf.units;

/** @deprecated Use <tt>visad.data.units.UnitParser</tt> instead */
public class UnitParser
  extends visad.data.units.UnitParser
{
  /** @deprecated Use <tt>visad.data.units.UnitParser(stream)</tt> instead */
  public UnitParser(java.io.InputStream stream)
  {
    super(stream);
  }

  /** @deprecated Use <tt>visad.data.units.UnitParser.encodeTimestamp()</tt> instead */
  public static double encodeTimestamp(int year, int month, int day, int hour,
                                       int minute, float second, int zone)
  {
    return visad.data.units.UnitParser.encodeTimestamp(year, month, day, hour,
                                                       minute, second, zone);
  }
}
