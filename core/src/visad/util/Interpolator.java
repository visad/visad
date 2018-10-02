package visad.util;

public interface Interpolator {

   void interpolate(double xt, float[] interpValues);

   void next(double x0, double x1, double x2, float[] values0, float[] values1, float[] values2);

   void update(boolean[] needed);
   
}
