package visad.data.netcdf.units;

import visad.Unit;

/** @deprecated Use <tt>visad.data.units.Parser</tt> instead */
public class Parser
  extends visad.data.units.Parser
{
  /** @deprecated Use <tt>visad.data.units.Parser.parse(spec)</tt> instead */
  public static synchronized Unit parse(String spec)
    throws ParseException, NoSuchUnitException
  {
    try {
      return visad.data.units.Parser.parse(spec);
    } catch (visad.data.units.NoSuchUnitException nsue) {
      throw new NoSuchUnitException(nsue.getMessage());
    } catch (visad.data.units.ParseException pe) {
      throw new ParseException(pe.getMessage());
    }
  }
}
