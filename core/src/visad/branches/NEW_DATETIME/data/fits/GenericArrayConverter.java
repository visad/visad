package visad.data.fits;

public abstract class GenericArrayConverter
{
  Object o;
  int[] lengths;

  public GenericArrayConverter(Class type, int[] lengths)
  {
    o = java.lang.reflect.Array.newInstance(type, lengths);

    this.lengths = lengths;
  }

  abstract void assign(Object obj, int index, double value);

  private Object getBottomArray(Object o, int[] which)
  {
    int d = which.length - 1;

    int i = 0;
    while (i < d) {
      o = java.lang.reflect.Array.get(o, which[i++]);
    }

    return o;
  }

  private Object getNextRMBottomArray(Object o, int[] coord)
  {
    int l = coord.length - 1;

    while (coord[l] >= lengths[l]) {
      coord[l] = 0;

      l--;
      if (l < 0) {
	return null;
      }

      coord[l]++;
    }

    return getBottomArray(o, coord);
  }

  private Object getNextCMBottomArray(Object o, int[] coord)
  {
    int l = 0;

    while (coord[l] >= lengths[l]) {
      coord[l] = 0;

      l++;
      if (l >= coord.length) {
	return null;
      }

      coord[l]++;
    }

    return getBottomArray(o, coord);
  }

  public Object getRowMajor(double[][] values)
  {
    int[] coord = new int[lengths.length];
    for (int i = 0; i < lengths.length; i++) {
      coord[i] = 0;
    }

    int lastCoord = lengths.length - 1;

    Object ra = getBottomArray(o, coord);
    for (int i = 0; i < values.length; i++) {
      for (int j = 0; j < values[i].length; j++) {
        assign(ra, coord[lastCoord]++, values[i][j]);

	if (coord[lastCoord] >= lengths[lastCoord]) {
	  ra = getNextRMBottomArray(o, coord);
	  if (ra == null) {
	    return o;
	  }
	}
      }
    }

    return o;
  }

  public Object getColumnMajor(double[][] values)
  {
    int[] coord = new int[lengths.length];
    for (int i = 0; i < lengths.length; i++) {
      coord[i] = 0;
    }

    int lastCoord = lengths.length - 1;

    Object ra;
    for (int i = 0; i < values.length; i++) {
      for (int j = 0; j < values[i].length; j++) {
        assign(getBottomArray(o, coord), coord[lastCoord], values[i][j]);
	coord[0]++;

	if (coord[0] >= lengths[0]) {
	  if (getNextCMBottomArray(o, coord) == null) {
	    return o;
	  }
	}
      }
    }

    return o;
  }
}
