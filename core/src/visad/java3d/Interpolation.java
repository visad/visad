package visad.java3d;

import Jama.Matrix;
import Jama.LUDecomposition;
import java.util.Arrays;

public class Interpolation {

      Jama.LUDecomposition solver;

      double[][] solution = null;

      double x0 = 0;
      double x1 = 0;
      double x2 = 0;

      float[] values0 = null;
      float[] values1 = null;
      float[] values2 = null;

      int numSpatialPts = 1;

      boolean doIntrp = true;
      
      boolean[] needed = null;
      boolean[] computed = null;
      
      int totalA = 0;
      int totalB = 0;

      public Interpolation() {
      }

      public Interpolation(boolean doIntrp, int numSpatialPts) {
         this.doIntrp = doIntrp;
         this.numSpatialPts = numSpatialPts;
         this.solution = new double[4][numSpatialPts];
         this.needed = new boolean[numSpatialPts];
         this.computed = new boolean[numSpatialPts];
         Arrays.fill(needed, false);
         Arrays.fill(computed, false);
      }

      void buildSolver() {
         double x0_p3 = x0*x0*x0;
         double x1_p3 = x1*x1*x1;
         double x2_p3 = x2*x2*x2;

         double x0_p2 = x0*x0;
         double x1_p2 = x1*x1;
         double x2_p2 = x2*x2;

         Matrix coeffs = new Matrix(new double[][]
              { {x0_p3, x0_p2, x0, 1},
                {x1_p3, x1_p2, x1, 1},
                {x2_p3, x2_p2, x2, 1},
                {3*x0_p2, 2*x0, 1, 0}}, 4, 4);

         solver = new Jama.LUDecomposition(coeffs);
      }

      public void interpolate(double xt, double[] interpValues) {
         for (int k=0; k<numSpatialPts; k++) {
            interpValues[k] = cubic_poly(xt, solution[0][k], solution[1][k], solution[2][k], solution[3][k]);
         }
      }

      public void interpolate(double xt, float[] interpValues) {
         if (!doIntrp) {
            if (xt == x0) {
               System.arraycopy(values0, 0, interpValues, 0, numSpatialPts);
            }
            else if (xt == x1) {
               System.arraycopy(values1, 0, interpValues, 0, numSpatialPts);
            }
            return;
         }
         for (int k=0; k<numSpatialPts; k++) {
            interpValues[k] = (float) cubic_poly(xt, solution[0][k], solution[1][k], solution[2][k], solution[3][k]);
         }
      }

      public void next(double x0, double x1, double x2, float[] values0, float[] values1, float[] values2) {
         this.x0 = x0;
         this.x1 = x1;
         this.x2 = x2;
         this.values0 = values0;
         this.values1 = values1;
         this.values2 = values2;
         Arrays.fill(computed, false);
         
         if (!doIntrp) {
           return;
         }
         
         buildSolver();
         totalA += numSpatialPts;
      }
      
      public void update(boolean[] needed) {
          java.util.Arrays.fill(this.needed, false);
          int cnt = 0;
          for (int k=0; k<numSpatialPts; k++) {
              if (needed[k]) {
                  if (!computed[k]) {
                      this.needed[k] = true;
                      cnt++;
                  }
              }
          }
          totalB += cnt;
          getSolution();
      }
      
      public void getSolution() {
         for (int k=0; k<numSpatialPts; k++) {
            if (!this.needed[k]) {
                continue;
            }
            double y0 = values0[k];
            double y1 = values1[k];
            double y2 = values2[k];

            // TODO: for now always initialize first derivative at first point with estimate from
            // the first two data pts instead of using derivative from cubic polynomial fit at the
            // last point.  This works pretty well, but can be improved. So set this to "true".
            if (true) {
               double D1 = (y1 - y0)/(x1 - x0);
               double[] sol = getSolution(y0, y1, y2, D1);
               solution[0][k] = sol[0];
               solution[1][k] = sol[1];
               solution[2][k] = sol[2];
               solution[3][k] = sol[3];
            }
            else {
               double D1 = cubic_poly_D1(x0, solution[0][k], solution[1][k], solution[2][k]);
               double[] sol = getSolution(y0, y1, y2, D1);
               solution[0][k] = sol[0];
               solution[1][k] = sol[1];
               solution[2][k] = sol[2];
               solution[3][k] = sol[3];
            }
            computed[k] = true;
         }
      }

      public double[] getSolution(double y0, double y1, double y2, double D1) {
        Matrix constants = new Matrix(new double[][]
             { {y0}, {y1}, {y2}, {D1} }, 4, 1);

        double[][] solution = (solver.solve(constants)).getArray();

        return new double[] {solution[0][0], solution[1][0], solution[2][0], solution[3][0]};
      }

      public static double cubic_poly_D1(double x, double a, double b, double c) {
         return 3*a*x*x + 2*b*x + c;
      }

      public static double cubic_poly(double x, double a, double b, double c, double d) {
         return a*x*x*x + b*x*x + c*x + d;
      }
  }
