package visad.data.fits;

import visad.FlatField;
import visad.GriddedSet;
import visad.Set;
import visad.VisADException;

public class ConvertDoubleArray
	extends ConvertArray
{
  private double[][] values;

  public ConvertDoubleArray(FlatField fld)
	throws VisADException
  {
    Set set = fld.getDomainSet();
    if (!(set instanceof GriddedSet)) {
      throw new VisADException("Cannot convert non-GriddedSet FlatField");
    }

    lengths = ((GriddedSet )set).getLengths();
    values = fld.getValues();
  }

  public ConvertDoubleArray(int[] lengths, double[][] values)
	throws VisADException
  {
    this.lengths = lengths;
    this.values = values;
  }

  int analyzeArray()
  {
    double max = values[0][0];
    double min = values[0][0];

    boolean integral = true;
    for (int i = 0; i < values.length; i++) {
      for (int j = 0; j < values[i].length; j++) {
	double v = values[i][j];
	if (v > max) {
	  max = v;
	}
	if (v < min) {
	  min = v;
	}
	if (v >= Long.MIN_VALUE && v <= Long.MAX_VALUE) {
	  if (v % 1 > 0) {
	    integral = false;
	  }
	} else {
	  integral = false;
	}
      }
    }

    return getArrayType(min, max, integral);
  }
}
