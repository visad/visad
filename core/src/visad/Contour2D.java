//
// Contour2D.java
//

/*
 VisAD system for interactive analysis and visualization of numerical
 data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
 Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
 Tommy Jasmin.

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Library General Public
 License as published by the Free Software Foundation; either
 version 2 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Library General Public License for more details.

 You should have received a copy of the GNU Library General Public
 License along with this library; if not, write to the Free
 Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 MA 02111-1307, USA
 */

package visad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import visad.util.Trace;
import visad.util.HersheyFont;
import java.awt.Font;
import java.text.DecimalFormat;

/**
 * Contour2D is a class equipped with a 2-D contouring function.
 * <P>
 */

public class Contour2D {

	/**           */
	protected Contour2D con;

	/**           */
	protected int whichlabels = 0;

	/**           */
	protected boolean showgrid;

	/**           */
	protected int rows, cols, scale;

	/**           */
	protected int[] num1, num2, num3, num4;

	/**           */
	protected float[][] vx1, vy1;

	/**           */
	public static final int DIFFICULTY_THRESHOLD = 600000;

	/**           */
	public static final float DIMENSION_THRESHOLD = 5.0E5f;

	public static final byte SIDE_LEFT = 3;
	public static final byte SIDE_RIGHT = 1;
	public static final byte SIDE_TOP = 2;
	public static final byte SIDE_BOTTOM = 0;
	public static final byte SIDE_NONE = -1;
	public static final byte CLOCKWISE = -1;
	public static final byte CNTRCLOCKWISE = 1;

	/**
	 * Compute contour lines for a 2-D array. If the interval is negative, then
	 * contour lines less than base will be drawn as dashed lines. The contour
	 * lines will be computed for all V such that:
	 * 
	 * <pre>
	 * lowlimit &lt;= V &lt;= highlimit
	 * </pre>
	 * 
	 * and
	 * 
	 * <pre>
	 * V = base + n*interval  for some integer n
	 * </pre>
	 * 
	 * Note that the input array, g, should be in column-major (FORTRAN) order.
	 * 
	 * @param g
	 *            the 2-D array to contour.
	 * @param nr
	 *            size of 2-D array in rows
	 * @param nc
	 *            size of 2-D array in columns.
	 * @param interval
	 *            contour interval
	 * @param lowlimit
	 *            the lower limit on values to contour.
	 * @param highlimit
	 *            the upper limit on values to contour.
	 * @param base
	 *            base value to start contouring at.
	 * @param vx1
	 *            array to put contour line vertices (x value)
	 * @param vy1
	 *            array to put contour line vertices (y value)
	 * @param numv1
	 *            pointer to int to return number of vertices in vx1,vy1
	 * @param auxValues
	 *            colors corresponding to grid points
	 * @param swap
	 * @param fill
	 *            true if filling contours
	 * @param grd_normals
	 * @param interval_colors
	 * @param lbl_vv
	 *            values for label line segments
	 * @param lbl_cc
	 *            label color triples
	 * @param lbl_loc
	 *            center points for label locations
	 * @param scale_ratio
	 * @param label_size
	 * @param labelColor
	 * @param spatial_set
	 * 
	 * @throws VisADException
	 */
	
	public static void contour(float g[], int nr, int nc, float interval,
			float lowlimit, float highlimit, float base, float vx1[][],
			float vy1[][], int[] numv1, byte[][] auxValues,
			boolean[] swap, boolean fill, float[][][] grd_normals,
			byte[][] interval_colors, float[][][][] lbl_vv,
			byte[][][][] lbl_cc, float[][][] lbl_loc, double[] scale, double scale_ratio,
			int label_freq, int label_line_skip,
			double label_size, boolean labelAlign, byte[] labelColor,
			Object labelFont, boolean sphericalDisplayCS,
			Gridded3DSet spatial_set) throws VisADException {
		boolean[] dashes = { false };
		float[] intervals = intervalToLevels(interval, lowlimit, highlimit,
				base, dashes);
		boolean dash = dashes[0];

		contour(g, nr, nc, intervals, lowlimit, highlimit, base, dash,
				auxValues, swap, fill, grd_normals, interval_colors, scale,
				scale_ratio, label_freq, label_line_skip,
				label_size, labelAlign, labelColor, labelFont,
				sphericalDisplayCS, spatial_set);
	}

	/**
	 * Returns an array of contour values and an indication on whether to use
	 * dashed lines below the base value.
	 * 
	 * @param interval
	 *            The contouring interval. Must be non-zero. If the interval is
	 *            negative, then contour lines less than the base will be drawn
	 *            as dashed lines. Must not be NaN.
	 * @param low
	 *            The minimum contour value. The returned array will not contain
	 *            a value below this. Must not be NaN.
	 * @param high
	 *            The maximum contour value. The returned array will not contain
	 *            a value above this. Must not be NaN.
	 * @param ba
	 *            The base contour value. The returned values will be integer
	 *            multiples of the interval away from this this value. Must not
	 *            be NaN. dash Whether or not contour lines less than the base
	 *            should be drawn as dashed lines. This is a computed and
	 *            returned value.
	 * @param dash
	 * 
	 * @return Levels array
	 * @throws VisADException
	 *             The contour interval is zero or too small.
	 */
	
	public static float[] intervalToLevels(float interval, float low,
			float high, float ba, boolean[] dash) throws VisADException {
		float[] levs = null;

		if (interval == 0.0) {
			throw new VisADException("Contour interval cannot be zero");
		}

		dash[0] = false;
		if (interval < 0) {
			dash[0] = true;
			interval = -interval;
		}

		// compute list of contours
		// compute nlo and nhi, for low and high contour values in the box
		long nlo = Math.round((Math.ceil((low - ba) / Math.abs(interval))));
		long nhi = Math.round((Math.floor((high - ba) / Math.abs(interval))));

		// how many contour lines are needed.
		int numc = (int) (nhi - nlo) + 1;
		if (numc < 1)
			return levs;
		if (numc > 4000) {
			throw new VisADException("Contour interval " + interval
					+ " too small for range " + low + "," + high);
		}

		try {
			levs = new float[numc];
		} catch (OutOfMemoryError e) {
			throw new VisADException("Contour interval too small");
		}

		for (int i = 0; i < numc; i++) {
			levs[i] = ba + (nlo + i) * interval;
		}

		return levs;
	}

	/**           */
	public static int vertexCnt = 0;

	/**           */
	public static boolean TRUEVALUE = true;

	public static ContourOutput contour(float g[], int nr, int nc,
			float[] values, float lowlimit, float highlimit, float base,
			boolean dash, byte[][] auxValues, boolean[] swap, boolean fill,
			float[][][] grd_normals, byte[][] interval_colors, double[] scale,
			double scale_ratio, int label_freq, int label_line_skip,
			double label_size, boolean labelAlign,
			byte[] labelColor, Object labelFont, boolean sphericalDisplayCS,
			Gridded3DSet spatial_set) throws VisADException {

		dash = fill ? false : dash;
		int ir, ic;
		int nrm, ncm;
		int numc, il;
		int lr, lc, lc2, lrr, lr2, lcc;
		float xd, yd, xx, yy;
		float xdd, ydd;
		float gg;

		// these are just estimates
		// int est = 2 * Length; WLH 14 April 2000
		double dest = Math.sqrt((double) spatial_set.getLength());
		int est = (int) (dest * Math.sqrt(dest));
		if (est < 1000)
			est = 1000;
		int maxsize = (2 * 2 * est) + est;

		// setup colors arrays
		int interval_length = (interval_colors.length > 0) ? interval_colors[0].length : 0;

		// setup display coordinate arrays
		float[] vx = new float[maxsize];
		float[] vy = new float[maxsize];

		int numv;

		// JDM:Find the max and min values of the data
		float maxValue = Float.NEGATIVE_INFINITY;
		float minValue = Float.POSITIVE_INFINITY;
		for (int i = 0; i < g.length; i++) {
			if (g[i] > maxValue)
				maxValue = g[i];
			if (g[i] < minValue)
				minValue = g[i];
		}

		/* DRM 1999-05-18, CTR 29 Jul 1999: values could be null */
		float[] myvals = null;
		boolean debug = false;
		if (values != null && (minValue < maxValue) /* grid was not all missing */) {
			myvals = (float[]) values.clone();

			// Sort the values. Get the original indidices
			int[] indices = QuickSort.sort(myvals);

			// JDM: Now, change the order of the colors to reflect the new sort
			// order
			// BMF 2009-03-17: don't assume there are interval colors
			byte[][] tmpColors = new byte[interval_colors.length][interval_length];
			for (int colorIdx = 0; colorIdx < interval_length; colorIdx++) {
				for (int rgbIdx = 0; rgbIdx < interval_colors.length; rgbIdx++) {
					tmpColors[rgbIdx][indices[colorIdx]] = interval_colors[rgbIdx][colorIdx];
				}
			}
			interval_colors = tmpColors;

			// JDM: Clip the myvals array to only use values within the range of
			// the
			// data
			int minIdx = 0;
			int maxIdx = myvals.length - 1;

			for (int i = 0; i < myvals.length; i++) {
				if (minValue <= myvals[i]) {
					break;
				}
				minIdx = i;
			}
			for (int i = myvals.length - 1; i >= 0; i--) {
				if (maxValue >= myvals[i]) {
					break;
				}
				maxIdx = i;
			}
			// debug =true;
			int newSize = maxIdx - minIdx + 1;
			int newIdx = 0;
			if (debug) {
				System.err.println("min: " + minValue + " max:" + maxValue
						+ " idx:" + minIdx + "-" + maxIdx + " new size:"
						+ newSize);
				System.err.print("original values: ");
				for (int i = 0; i < myvals.length; i++) {
					System.err.print(myvals[i] + ",");
				}
				System.err.println("");
			}

			float[] tmpValues = new float[newSize];
			tmpColors = new byte[interval_colors.length][newSize];
			for (int i = minIdx; i <= maxIdx; i++) {
				for (int rgbIdx = 0; rgbIdx < interval_colors.length; rgbIdx++) {
					tmpColors[rgbIdx][newIdx] = interval_colors[rgbIdx][i];
				}
				tmpValues[newIdx] = myvals[i];
				newIdx++;
			}

			if (debug) {
				System.err.print("new values: ");
				for (int i = 0; i < tmpValues.length; i++) {
					System.err.print(tmpValues[i] + ",");
				}
				System.err.println("");
			}

			myvals = tmpValues;
			interval_colors = tmpColors;
		}

		int low;
		int hi;

		int t;

		byte[][] auxLevels = null;
		int naux = (auxValues != null) ? auxValues.length : 0;
		byte[] auxa = null;
		byte[] auxb = null;
		byte[] auxc = null;
		byte[] auxd = null;
		if (naux > 0) {
			for (int i = 0; i < naux; i++) {
				if (auxValues[i].length != g.length) {
					throw new SetException("Contour2D.contour: "
							+ "auxValues lengths don't match");
				}
			}
			auxa = new byte[naux];
			auxb = new byte[naux];
			auxc = new byte[naux];
			auxd = new byte[naux];
			auxLevels = new byte[naux][maxsize];
		}

		if (values == null)
			return null; // WLH 24 Aug 99

		// JDM: if we have no values then return
		if (myvals == null || myvals.length == 0)
			return null;

		// flags for each level indicating dashed rendering
		boolean[] dashFlags = new boolean[myvals.length];

		int numLevels = myvals.length;
		float minLevelValue = myvals[0];
		float maxLevelValue = myvals[numLevels - 1];

		/*
		 * DRM: 1999-05-19 - Not needed since dash is a boolean // check for bad
		 * contour interval if (interval==0.0) { throw new
		 * DisplayException("Contour2D.contour: interval cannot be 0"); } if
		 * (!dash) { // draw negative contour lines as dashed lines interval =
		 * -interval; idash = 1; } else { idash = 0; }
		 */

		nrm = nr - 1;
		ncm = nc - 1;

		xdd = ((nr - 1) - 0.0f) / (nr - 1.0f); // = 1.0
		ydd = ((nc - 1) - 0.0f) / (nc - 1.0f); // = 1.0

		/**
		 * -TDR xd = xdd - 0.0001f; yd = ydd - 0.0001f; gap too big *
		 */
		xd = xdd - 0.00002f;
		yd = ydd - 0.00002f;

		/*
		 * set up mark array mark= 0 if avail for label center, 2 if in label,
		 * and 1 if not available and not in label
		 * 
		 * lr and lc give label size in grid boxes lrr and lcc give unavailable
		 * radius
		 */
		if (swap[0]) {
			lr = 1 + (nr - 2) / 10;
			lc = 1 + (nc - 2) / 50;
		} else {
			lr = 1 + (nr - 2) / 50;
			lc = 1 + (nc - 2) / 10;
		}
		lc2 = lc / 2;
		lr2 = lr / 2;
		lrr = 1 + (nr - 2) / 8;
		lcc = 1 + (nc - 2) / 8;

		// allocate mark array
		char[] mark = new char[nr * nc];

		// set top and bottom rows to 1
		float max_g = -Float.MAX_VALUE;
		float min_g = Float.MAX_VALUE;
		for (ic = 0; ic < nc; ic++) {
			for (ir = 0; ir < lr; ir++) {
				mark[(ic) * nr + (ir)] = 1;
				mark[(ic) * nr + (nr - ir - 2)] = 1;
				float val = g[(ic) * nr + (ir)];
				if (val > max_g)
					max_g = val;
				if (val < min_g)
					min_g = val;
			}
		}

		// set left and right columns to 1
		for (ir = 0; ir < nr; ir++) {
			for (ic = 0; ic < lc; ic++) {
				mark[(ic) * nr + (ir)] = 1;
				mark[(nc - ic - 2) * nr + (ir)] = 1;
			}
		}
		numv = 0;

		// - color fill arrays
		byte[][] color_bin = null;
		byte[][][] o_flags = null;
		short[][] n_lines = null;
		short[][] ctrLow = null;

		if (fill) {
			color_bin = interval_colors;
			o_flags = new byte[nrm][ncm][];
			n_lines = new short[nrm][ncm];
			ctrLow = new short[nrm][ncm];
		}

		ContourStripSet ctrSet = new ContourStripSet(myvals, swap,
				scale_ratio, label_freq, label_line_skip, label_size, nr, nc, spatial_set);

		visad.util.Trace.call1("Contour2d.loop", " nrm=" + nrm + " ncm=" + ncm
				+ " naux=" + naux + " myvals.length=" + myvals.length);

		// compute contours
		for (ic = 0; ic < ncm; ic++) {
			int ic_plus1 = ic + 1;
			yy = ydd * ic + 0.0f; // = ic
			for (ir = 0; ir < nrm; ir++) {
				int ir_plus1 = ir + 1;
				xx = xdd * ir + 0.0f; // = ir

				int ic_times_nr = ic * nr;
				int ic_plus1_times_nr = ic_plus1 * nr;

				float ga, gb, gc, gd;
				float gAvg, gMin, gMax;
				float tmp1, tmp2;

				// WLH 21 April 2000
				// if (numv+8 >= maxsize || nump+4 >= 2*maxsize) {
				if (numv + 8 >= maxsize) {
					// allocate more space
					maxsize = 2 * maxsize;
					/*
					 * WLH 21 April 2000 int[] tt = ipnt; ipnt = new int[2
					 * maxsize]; System.arraycopy(tt, 0, ipnt, 0, nump);
					 */
					float[] tx = vx;
					float[] ty = vy;
					vx = new float[maxsize];
					vy = new float[maxsize];
					System.arraycopy(tx, 0, vx, 0, numv);
					System.arraycopy(ty, 0, vy, 0, numv);
					tx = null;
					ty = null;
					if (naux > 0) {
						byte[][] ta = auxLevels;
						auxLevels = new byte[naux][maxsize];
						for (int i = 0; i < naux; i++) {
							System.arraycopy(ta[i], 0, auxLevels[i], 0, numv);
						}
						ta = null;
					}
				}

				// save index of first vertex in this grid box
				// JDM: ipnt[nump++] = numv;

				/*
				 * ga = ( g[ (ic) nr + (ir) ] ); gb = ( g[ (ic) nr + (ir+1) ] );
				 * gc = ( g[ (ic+1) nr + (ir) ] ); gd = ( g[ (ic+1) nr + (ir+1)
				 * ] ); boolean miss = false; if (ga != ga || gb != gb || gc !=
				 * gc || gd != gd) { miss = true; System.out.println("ic, ir = "
				 * + ic + "  " + ir + " gabcd = " + ga + " " + gb + " " + gc +
				 * " " + gd); }
				 */

				/*
				 * if (ga != ga || gb != gb || gc != gc || gd != gd) { if
				 * (!anymissing) { anymissing = true;
				 * System.out.println("missing"); } } else { if (!anynotmissing)
				 * { anynotmissing = true; System.out.println("notmissing"); } }
				 */
				// get 4 corner values, skip box if any are missing
				//
				// [c, (x,y+ydd)]-------[d, (x+xdd,y+ydd)]
				// | |
				// | |
				// | |
				// [a, (x,y)]------------[b, (x+xdd,y)]
				//
				// ------------------------------
				ga = g[ic_times_nr + ir];
       			if (Float.isNaN(ga))
         			continue;
				gb = g[ic_times_nr + ir_plus1];
       			if (Float.isNaN(gb))
         			continue;
				gc = g[ic_plus1_times_nr + ir];
       			if (Float.isNaN(gc))
         			continue;
				gd = g[ic_plus1_times_nr + ir_plus1];
       			if (Float.isNaN(gd))
         			continue;

				/*
				 * DRM move outside the loop byte[] auxa = null; byte[] auxb =
				 * null; byte[] auxc = null; byte[] auxd = null; if (naux > 0) {
				 * auxa = new byte[naux]; auxb = new byte[naux]; auxc = new
				 * byte[naux]; auxd = new byte[naux];
				 */
				if (naux > 0) {
					for (int i = 0; i < naux; i++) {
						byte[] auxValues_i = auxValues[i];
						auxa[i] = auxValues_i[ic_times_nr + ir];
						auxb[i] = auxValues_i[ic_times_nr + ir_plus1];
						auxc[i] = auxValues_i[ic_plus1_times_nr + ir];
						auxd[i] = auxValues_i[ic_plus1_times_nr + ir_plus1];
					}
				}

				// find average, min, and max of 4 corner values
				gAvg = (ga + gb + gc + gd) / 4.0f;

				// gMin = MIN4(ga,gb,gc,gd);
				tmp1 = ((ga) < (gb) ? (ga) : (gb));
				tmp2 = ((gc) < (gd) ? (gc) : (gd));
				gMin = ((tmp1) < (tmp2) ? (tmp1) : (tmp2));

				// gMax = MAX4(ga,gb,gc,gd);
				tmp1 = ((ga) > (gb) ? (ga) : (gb));
				tmp2 = ((gc) > (gd) ? (gc) : (gd));
				gMax = ((tmp1) > (tmp2) ? (tmp1) : (tmp2));

				/*
				 * remove for new signature, replace with code below // compute
				 * clow and chi, low and high contour values in the box tmp1 =
				 * (gMin-base) / interval; clow = base + interval (( (tmp1) >= 0
				 * ? (int) ((tmp1) + 0.5) : (int) ((tmp1)-0.5) )-1); while
				 * (clow<gMin) { clow += interval; }
				 * 
				 * tmp1 = (gMax-base) / interval; chi = base + interval ((
				 * (tmp1) >= 0 ? (int) ((tmp1) + 0.5) : (int) ((tmp1)-0.5) )+1);
				 * while (chi>gMax) { chi -= interval; }
				 * 
				 * // how many contour lines in the box: tmp1 = (chi-clow) /
				 * interval; numc = 1+( (tmp1) >= 0 ? (int) ((tmp1) + 0.5) :
				 * (int) ((tmp1)-0.5) );
				 * 
				 * // gg is current contour line value gg = clow;
				 */

				low = 0;
				hi = numLevels - 1;
				if (gMax < minLevelValue || gMin > maxLevelValue) {
					// no contours
					numc = 1;
				} else {
					// some inside the box
					// JDM: Instead of iterating through the whole list just do
					// a
					// binarySearch
					/*
					 * for (int i = 0; i < myvals.length; i++) { if (i == 0 &&
					 * myvals[i] >= gn) { low = i; } else if (myvals[i] >= gn &&
					 * myvals[i-1] < gn) { low = i; } if (i == 0 && myvals[i] >=
					 * gx) { hi = i; } else if (myvals[i] >= gx && myvals[i-1] <
					 * gx) { hi = i; } }
					 */
					hi = java.util.Arrays.binarySearch(myvals, gMax);
					if (hi < 0)
						hi = (-hi) - 1;
					if (hi >= myvals.length)
						hi = myvals.length - 1;
					low = java.util.Arrays.binarySearch(myvals, gMin);
					if (low < 0)
						low = (-low) - 1;

					numc = hi - low + 1;
				}

				// gg = myvals[low];
				/*
				 * if (!any && numc > 0) { System.out.println("gMin = " + gMin +
				 * " gMax = " + gMax + " gAvg = " + gAvg);
				 * System.out.println("numc = " + numc + " clow = " +
				 * myvals[low] + " chi = " + myvals[hi]); any = true; }
				 */
				if (fill) {
					o_flags[ir][ic] = new byte[2 * numc]; // - case flags
					n_lines[ir][ic] = 0; // - number of contour line segments
					ctrLow[ir][ic] = (short) hi;
				}

				for (il = 0; il < numc; il++) {
					if ((low + il) >= myvals.length) {
						System.err.println("bad range: myvals.length=" + myvals
								+ " il=" + il + " low=" + low + " high=" + hi);
					}
					gg = myvals[low + il];

					// WLH 21 April 2000
					// if (numv+8 >= maxsize || nump+4 >= 2*maxsize) {
					if (numv + 8 >= maxsize) {
						// allocate more space
						maxsize = 2 * maxsize;
						/*
						 * WLH 21 April 2000 int[] tt = ipnt; ipnt = new int[2
						 * maxsize]; System.arraycopy(tt, 0, ipnt, 0, nump);
						 */
						float[] tx = vx;
						float[] ty = vy;
						vx = new float[maxsize];
						vy = new float[maxsize];
						System.arraycopy(tx, 0, vx, 0, numv);
						System.arraycopy(ty, 0, vy, 0, numv);
						tx = null;
						ty = null;
						if (naux > 0) {
							byte[][] ta = auxLevels;
							auxLevels = new byte[naux][maxsize];
							for (int i = 0; i < naux; i++) {
								System.arraycopy(ta[i], 0, auxLevels[i], 0,
										numv);
							}
							ta = null;
						}
					}

					// make sure gg is within contouring limits
					if (gg < gMin)
						continue;
					if (gg > gMax)
						break;
					if (gg < lowlimit)
						continue;
					if (gg > highlimit)
						break;

					// compute orientation of lines inside box
					int ii = 0;
					if (gg > ga)
						ii = 1;
					if (gg > gb)
						ii += 2;
					if (gg > gc)
						ii += 4;
					if (gg > gd)
						ii += 8;
					if (ii > 7)
						ii = 15 - ii;
					if (ii <= 0)
						continue;

					if (fill) {
						if ((low + il) < ctrLow[ir][ic])
							ctrLow[ir][ic] = (short) (low + il);
					}

					// DO LABEL HERE
					if ((mark[(ic) * nr + (ir)]) == 0) {
						int kc, kr, mc, mr, jc, jr;

						// Insert a label

						// BOX TO AVOID
						kc = ic - lc2 - lcc;
						kr = ir - lr2 - lrr;
						mc = kc + 2 * lcc + lc - 1;
						mr = kr + 2 * lrr + lr - 1;
						// OK here
						for (jc = kc; jc <= mc; jc++) {
							if (jc >= 0 && jc < nc) {
								for (jr = kr; jr <= mr; jr++) {
									if (jr >= 0 && jr < nr) {
										if ((mark[(jc) * nr + (jr)]) != 2) {
											mark[(jc) * nr + (jr)] = 1;
										}
									}
								}
							}
						}

						// BOX TO HOLD LABEL
						kc = ic - lc2;
						kr = ir - lr2;
						mc = kc + lc - 1;
						mr = kr + lr - 1;
						for (jc = kc; jc <= mc; jc++) {
							if (jc >= 0 && jc < nc) {
								for (jr = kr; jr <= mr; jr++) {
									if (jr >= 0 && jr < nr) {
										mark[(jc) * nr + (jr)] = 2;
									}
								}
							}
						}
					}

					float gba, gca, gdb, gdc;
					switch (ii) {
					case 1:
						gba = gb - ga;
						gca = gc - ga;

						if (naux > 0) {
							float ratioba = (gg - ga) / gba;
							float ratioca = (gg - ga) / gca;
							for (int i = 0; i < naux; i++) {
								t = (int) ((1.0f - ratioba)
										* ((auxa[i] < 0) ? ((float) auxa[i]) + 256.0f
												: ((float) auxa[i])) + ratioba
										* ((auxb[i] < 0) ? ((float) auxb[i]) + 256.0f
												: ((float) auxb[i])));
								auxLevels[i][numv] = (byte) ((t < 0) ? 0
										: ((t > 255) ? -1 : ((t < 128) ? t
												: t - 256)));
								t = (int) ((1.0f - ratioca)
										* ((auxa[i] < 0) ? ((float) auxa[i]) + 256.0f
												: ((float) auxa[i])) + ratioca
										* ((auxc[i] < 0) ? ((float) auxc[i]) + 256.0f
												: ((float) auxc[i])));
								auxLevels[i][numv + 1] = (byte) ((t < 0) ? 0
										: ((t > 255) ? -1 : ((t < 128) ? t
												: t - 256)));
								/*
								 * MEM_WLH auxLevels[i][numv] = auxa[i] +
								 * (auxb[i]-auxa[i]) ratioba;
								 * auxLevels[i][numv+1] = auxa[i] +
								 * (auxc[i]-auxa[i]) ratioca;
								 */
							}
						}

						if (((gba) < 0 ? -(gba) : (gba)) < 0.0000001) {
							vx[numv] = xx;
						} else {
							vx[numv] = xx + xd * (gg - ga) / gba;
						}
						vy[numv] = yy;
						numv++;
						if (((gca) < 0 ? -(gca) : (gca)) < 0.0000001) {
							vy[numv] = yy;
						} else {
							vy[numv] = yy + yd * (gg - ga) / gca;
						}
						vx[numv] = xx;
						numv++;
						if (fill) {
							o_flags[ir][ic][n_lines[ir][ic]] = (byte) ii;
							n_lines[ir][ic]++;
						}
						if (vx[numv - 2] == vx[numv - 1]
								|| vy[numv - 2] == vy[numv - 1]) {
							vx[numv - 2] += 0.00001f;
							vy[numv - 1] += 0.00001f;
						}
						break;

					case 2:
						gba = gb - ga;
						gdb = gd - gb;

						if (naux > 0) {
							float ratioba = (gg - ga) / gba;
							float ratiodb = (gg - gb) / gdb;
							for (int i = 0; i < naux; i++) {
								t = (int) ((1.0f - ratioba)
										* ((auxa[i] < 0) ? ((float) auxa[i]) + 256.0f
												: ((float) auxa[i])) + ratioba
										* ((auxb[i] < 0) ? ((float) auxb[i]) + 256.0f
												: ((float) auxb[i])));
								auxLevels[i][numv] = (byte) ((t < 0) ? 0
										: ((t > 255) ? -1 : ((t < 128) ? t
												: t - 256)));
								t = (int) ((1.0f - ratiodb)
										* ((auxb[i] < 0) ? ((float) auxb[i]) + 256.0f
												: ((float) auxb[i])) + ratiodb
										* ((auxd[i] < 0) ? ((float) auxd[i]) + 256.0f
												: ((float) auxd[i])));
								auxLevels[i][numv + 1] = (byte) ((t < 0) ? 0
										: ((t > 255) ? -1 : ((t < 128) ? t
												: t - 256)));
								/*
								 * MEM_WLH auxLevels[i][numv] = auxa[i] +
								 * (auxb[i]-auxa[i]) ratioba;
								 * auxLevels[i][numv+1] = auxb[i] +
								 * (auxd[i]-auxb[i]) ratiodb;
								 */
							}
						}

						if (((gba) < 0 ? -(gba) : (gba)) < 0.0000001)
							vx[numv] = xx;
						else
							vx[numv] = xx + xd * (gg - ga) / gba;
						vy[numv] = yy;
						numv++;
						if (((gdb) < 0 ? -(gdb) : (gdb)) < 0.0000001)
							vy[numv] = yy;
						else
							vy[numv] = yy + yd * (gg - gb) / gdb;
						vx[numv] = xx + xd;
						numv++;
						if (fill) {
							o_flags[ir][ic][n_lines[ir][ic]] = (byte) ii;
							n_lines[ir][ic]++;
						}
						if (vx[numv - 2] == vx[numv - 1]
								|| vy[numv - 2] == vy[numv - 1]) {
							vx[numv - 2] -= 0.00001f;
							vy[numv - 1] += 0.00001f;
						}
						break;

					case 3:
						gca = gc - ga;
						gdb = gd - gb;

						if (naux > 0) {
							float ratioca = (gg - ga) / gca;
							float ratiodb = (gg - gb) / gdb;
							for (int i = 0; i < naux; i++) {
								t = (int) ((1.0f - ratioca)
										* ((auxa[i] < 0) ? ((float) auxa[i]) + 256.0f
												: ((float) auxa[i])) + ratioca
										* ((auxc[i] < 0) ? ((float) auxc[i]) + 256.0f
												: ((float) auxc[i])));
								auxLevels[i][numv] = (byte) ((t < 0) ? 0
										: ((t > 255) ? -1 : ((t < 128) ? t
												: t - 256)));
								t = (int) ((1.0f - ratiodb)
										* ((auxb[i] < 0) ? ((float) auxb[i]) + 256.0f
												: ((float) auxb[i])) + ratiodb
										* ((auxd[i] < 0) ? ((float) auxd[i]) + 256.0f
												: ((float) auxd[i])));
								auxLevels[i][numv + 1] = (byte) ((t < 0) ? 0
										: ((t > 255) ? -1 : ((t < 128) ? t
												: t - 256)));
								/*
								 * MEM_WLH auxLevels[i][numv] = auxa[i] +
								 * (auxc[i]-auxa[i]) ratioca;
								 * auxLevels[i][numv+1] = auxb[i] +
								 * (auxd[i]-auxb[i]) ratiodb;
								 */
							}
						}

						if (((gca) < 0 ? -(gca) : (gca)) < 0.0000001)
							vy[numv] = yy;
						else
							vy[numv] = yy + yd * (gg - ga) / gca;
						vx[numv] = xx;
						numv++;
						if (((gdb) < 0 ? -(gdb) : (gdb)) < 0.0000001)
							vy[numv] = yy;
						else
							vy[numv] = yy + yd * (gg - gb) / gdb;
						vx[numv] = xx + xd;
						numv++;
						if (fill) {
							o_flags[ir][ic][n_lines[ir][ic]] = (byte) ii;
							n_lines[ir][ic]++;
						}
						break;

					case 4:
						gca = gc - ga;
						gdc = gd - gc;

						if (naux > 0) {
							float ratioca = (gg - ga) / gca;
							float ratiodc = (gg - gc) / gdc;
							for (int i = 0; i < naux; i++) {
								t = (int) ((1.0f - ratioca)
										* ((auxa[i] < 0) ? ((float) auxa[i]) + 256.0f
												: ((float) auxa[i])) + ratioca
										* ((auxc[i] < 0) ? ((float) auxc[i]) + 256.0f
												: ((float) auxc[i])));
								auxLevels[i][numv] = (byte) ((t < 0) ? 0
										: ((t > 255) ? -1 : ((t < 128) ? t
												: t - 256)));
								t = (int) ((1.0f - ratiodc)
										* ((auxc[i] < 0) ? ((float) auxc[i]) + 256.0f
												: ((float) auxc[i])) + ratiodc
										* ((auxd[i] < 0) ? ((float) auxd[i]) + 256.0f
												: ((float) auxd[i])));
								auxLevels[i][numv + 1] = (byte) ((t < 0) ? 0
										: ((t > 255) ? -1 : ((t < 128) ? t
												: t - 256)));
								/*
								 * MEM_WLH auxLevels[i][numv] = auxa[i] +
								 * (auxc[i]-auxa[i]) ratioca;
								 * auxLevels[i][numv+1] = auxc[i] +
								 * (auxd[i]-auxc[i]) ratiodc;
								 */
							}
						}

						if (((gca) < 0 ? -(gca) : (gca)) < 0.0000001)
							vy[numv] = yy;
						else
							vy[numv] = yy + yd * (gg - ga) / gca;
						vx[numv] = xx;
						numv++;
						if (((gdc) < 0 ? -(gdc) : (gdc)) < 0.0000001)
							vx[numv] = xx;
						else
							vx[numv] = xx + xd * (gg - gc) / gdc;
						vy[numv] = yy + yd;
						numv++;
						if (fill) {
							o_flags[ir][ic][n_lines[ir][ic]] = (byte) ii;
							n_lines[ir][ic]++;
						}
						if (vx[numv - 2] == vx[numv - 1]
								|| vy[numv - 2] == vy[numv - 1]) {
							vx[numv - 1] += 0.00001f;
							vy[numv - 2] -= 0.00001f;
						}
						break;

					case 5:
						gba = gb - ga;
						gdc = gd - gc;

						if (naux > 0) {
							float ratioba = (gg - ga) / gba;
							float ratiodc = (gg - gc) / gdc;
							for (int i = 0; i < naux; i++) {
								t = (int) ((1.0f - ratioba)
										* ((auxa[i] < 0) ? ((float) auxa[i]) + 256.0f
												: ((float) auxa[i])) + ratioba
										* ((auxb[i] < 0) ? ((float) auxb[i]) + 256.0f
												: ((float) auxb[i])));
								auxLevels[i][numv] = (byte) ((t < 0) ? 0
										: ((t > 255) ? -1 : ((t < 128) ? t
												: t - 256)));
								t = (int) ((1.0f - ratiodc)
										* ((auxc[i] < 0) ? ((float) auxc[i]) + 256.0f
												: ((float) auxc[i])) + ratiodc
										* ((auxd[i] < 0) ? ((float) auxd[i]) + 256.0f
												: ((float) auxd[i])));
								auxLevels[i][numv + 1] = (byte) ((t < 0) ? 0
										: ((t > 255) ? -1 : ((t < 128) ? t
												: t - 256)));
								/*
								 * MEM_WLH auxLevels[i][numv] = auxa[i] +
								 * (auxb[i]-auxa[i]) ratioba;
								 * auxLevels[i][numv+1] = auxc[i] +
								 * (auxd[i]-auxc[i]) ratiodc;
								 */
							}
						}

						if (((gba) < 0 ? -(gba) : (gba)) < 0.0000001)
							vx[numv] = xx;
						else
							vx[numv] = xx + xd * (gg - ga) / gba;
						vy[numv] = yy;
						numv++;
						if (((gdc) < 0 ? -(gdc) : (gdc)) < 0.0000001)
							vx[numv] = xx;
						else
							vx[numv] = xx + xd * (gg - gc) / gdc;
						vy[numv] = yy + yd;
						numv++;
						if (fill) {
							o_flags[ir][ic][n_lines[ir][ic]] = (byte) ii;
							n_lines[ir][ic]++;
						}
						break;

					case 6:
						gba = gb - ga;
						gdc = gd - gc;
						gca = gc - ga;
						gdb = gd - gb;

						if (naux > 0) {
							float ratioba = (gg - ga) / gba;
							float ratiodc = (gg - gc) / gdc;
							float ratioca = (gg - ga) / gca;
							float ratiodb = (gg - gb) / gdb;
							for (int i = 0; i < naux; i++) {
								t = (int) ((1.0f - ratioba)
										* ((auxa[i] < 0) ? ((float) auxa[i]) + 256.0f
												: ((float) auxa[i])) + ratioba
										* ((auxb[i] < 0) ? ((float) auxb[i]) + 256.0f
												: ((float) auxb[i])));
								auxLevels[i][numv] = (byte) ((t < 0) ? 0
										: ((t > 255) ? -1 : ((t < 128) ? t
												: t - 256)));
								/*
								 * MEM_WLH auxLevels[i][numv] = auxa[i] +
								 * (auxb[i]-auxa[i]) ratioba;
								 */
								if ((gg > gAvg) ^ (ga < gb)) {
									t = (int) ((1.0f - ratioca)
											* ((auxa[i] < 0) ? ((float) auxa[i]) + 256.0f
													: ((float) auxa[i])) + ratioca
											* ((auxc[i] < 0) ? ((float) auxc[i]) + 256.0f
													: ((float) auxc[i])));
									auxLevels[i][numv + 1] = (byte) ((t < 0) ? 0
											: ((t > 255) ? -1 : ((t < 128) ? t
													: t - 256)));
									t = (int) ((1.0f - ratiodb)
											* ((auxb[i] < 0) ? ((float) auxb[i]) + 256.0f
													: ((float) auxb[i])) + ratiodb
											* ((auxd[i] < 0) ? ((float) auxd[i]) + 256.0f
													: ((float) auxd[i])));
									auxLevels[i][numv + 2] = (byte) ((t < 0) ? 0
											: ((t > 255) ? -1 : ((t < 128) ? t
													: t - 256)));
									/*
									 * MEM_WLH auxLevels[i][numv+1] = auxa[i] +
									 * (auxc[i]-auxa[i]) ratioca;
									 * auxLevels[i][numv+2] = auxb[i] +
									 * (auxd[i]-auxb[i]) ratiodb;
									 */
								} else {
									t = (int) ((1.0f - ratiodb)
											* ((auxb[i] < 0) ? ((float) auxb[i]) + 256.0f
													: ((float) auxb[i])) + ratiodb
											* ((auxd[i] < 0) ? ((float) auxd[i]) + 256.0f
													: ((float) auxd[i])));
									auxLevels[i][numv + 1] = (byte) ((t < 0) ? 0
											: ((t > 255) ? -1 : ((t < 128) ? t
													: t - 256)));
									t = (int) ((1.0f - ratioca)
											* ((auxa[i] < 0) ? ((float) auxa[i]) + 256.0f
													: ((float) auxa[i])) + ratioca
											* ((auxc[i] < 0) ? ((float) auxc[i]) + 256.0f
													: ((float) auxc[i])));
									auxLevels[i][numv + 2] = (byte) ((t < 0) ? 0
											: ((t > 255) ? -1 : ((t < 128) ? t
													: t - 256)));
									/*
									 * MEM_WLH auxLevels[i][numv+1] = auxb[i] +
									 * (auxd[i]-auxb[i]) ratiodb;
									 * auxLevels[i][numv+2] = auxa[i] +
									 * (auxc[i]-auxa[i]) ratioca;
									 */
								}
								t = (int) ((1.0f - ratiodc)
										* ((auxc[i] < 0) ? ((float) auxc[i]) + 256.0f
												: ((float) auxc[i])) + ratiodc
										* ((auxd[i] < 0) ? ((float) auxd[i]) + 256.0f
												: ((float) auxd[i])));
								auxLevels[i][numv + 3] = (byte) ((t < 0) ? 0
										: ((t > 255) ? -1 : ((t < 128) ? t
												: t - 256)));
								/*
								 * MEM_WLH auxLevels[i][numv+3] = auxc[i] +
								 * (auxd[i]-auxc[i]) ratiodc;
								 */
							}
						}

						if (((gba) < 0 ? -(gba) : (gba)) < 0.0000001)
							vx[numv] = xx;
						else
							vx[numv] = xx + xd * (gg - ga) / gba;
						vy[numv] = yy;
						numv++;
						// here's a brain teaser
						if ((gg > gAvg) ^ (ga < gb)) { // (XOR)
							if (((gca) < 0 ? -(gca) : (gca)) < 0.0000001)
								vy[numv] = yy;
							else
								vy[numv] = yy + yd * (gg - ga) / gca;
							vx[numv] = xx;
							numv++;
							if (fill) {
								o_flags[ir][ic][n_lines[ir][ic]] = (byte) 1
										+ (byte) 32;
								n_lines[ir][ic]++;
							}
							if (((gdb) < 0 ? -(gdb) : (gdb)) < 0.0000001)
								vy[numv] = yy;
							else
								vy[numv] = yy + yd * (gg - gb) / gdb;
							vx[numv] = xx + xd;
							if (fill) {
								o_flags[ir][ic][n_lines[ir][ic]] = (byte) 7
										+ (byte) 32;
								n_lines[ir][ic]++;
							}
							numv++;
						} else {
							if (((gdb) < 0 ? -(gdb) : (gdb)) < 0.0000001)
								vy[numv] = yy;
							else
								vy[numv] = yy + yd * (gg - gb) / gdb;
							vx[numv] = xx + xd;
							numv++;
							if (fill) {
								o_flags[ir][ic][n_lines[ir][ic]] = (byte) 2
										+ (byte) 32;
								n_lines[ir][ic]++;
							}
							if (((gca) < 0 ? -(gca) : (gca)) < 0.0000001)
								vy[numv] = yy;
							else
								vy[numv] = yy + yd * (gg - ga) / gca;
							vx[numv] = xx;
							numv++;
							if (fill) {
								o_flags[ir][ic][n_lines[ir][ic]] = (byte) 4
										+ (byte) 32;
								n_lines[ir][ic]++;
							}
						}
						if (((gdc) < 0 ? -(gdc) : (gdc)) < 0.0000001)
							vx[numv] = xx;
						else
							vx[numv] = xx + xd * (gg - gc) / gdc;
						vy[numv] = yy + yd;
						numv++;
						break;

					case 7:
						gdb = gd - gb;
						gdc = gd - gc;

						if (naux > 0) {
							float ratiodb = (gg - gb) / gdb;
							float ratiodc = (gg - gc) / gdc;
							for (int i = 0; i < naux; i++) {
								t = (int) ((1.0f - ratiodb)
										* ((auxb[i] < 0) ? ((float) auxb[i]) + 256.0f
												: ((float) auxb[i])) + ratiodb
										* ((auxd[i] < 0) ? ((float) auxd[i]) + 256.0f
												: ((float) auxd[i])));
								auxLevels[i][numv] = (byte) ((t < 0) ? 0
										: ((t > 255) ? -1 : ((t < 128) ? t
												: t - 256)));
								t = (int) ((1.0f - ratiodc)
										* ((auxc[i] < 0) ? ((float) auxc[i]) + 256.0f
												: ((float) auxc[i])) + ratiodc
										* ((auxd[i] < 0) ? ((float) auxd[i]) + 256.0f
												: ((float) auxd[i])));
								auxLevels[i][numv + 1] = (byte) ((t < 0) ? 0
										: ((t > 255) ? -1 : ((t < 128) ? t
												: t - 256)));
								/*
								 * MEM_WLH auxLevels[i][numv] = auxb[i] +
								 * (auxb[i]-auxb[i]) ratiodb;
								 * auxLevels[i][numv+1] = auxc[i] +
								 * (auxd[i]-auxc[i]) ratiodc;
								 */
							}
						}

						if (((gdb) < 0 ? -(gdb) : (gdb)) < 0.0000001)
							vy[numv] = yy;
						else
							vy[numv] = yy + yd * (gg - gb) / gdb;
						vx[numv] = xx + xd;
						numv++;
						if (((gdc) < 0 ? -(gdc) : (gdc)) < 0.0000001)
							vx[numv] = xx;
						else
							vx[numv] = xx + xd * (gg - gc) / gdc;
						vy[numv] = yy + yd;
						numv++;
						if (fill) {
							o_flags[ir][ic][n_lines[ir][ic]] = (byte) ii;
							n_lines[ir][ic]++;
						}
						if (vx[numv - 2] == vx[numv - 1]
								|| vy[numv - 2] == vy[numv - 1]) {
							vx[numv - 1] -= 0.00001f;
							vy[numv - 2] -= 0.00001f;
						}
						break;
					} // switch

					// If contour level is negative, make dashed line
					if (gg < base && dash) { /* DRM: 1999-05-19 */
						dashFlags[low + il] = true;
					}
					/*
					 * if ((20.0 <= vy[numv-2] && vy[numv-2] < 22.0) || (20.0 <=
					 * vy[numv-1] && vy[numv-1] < 22.0)) {
					 * System.out.println("vy = " + vy[numv-1] + " " +
					 * vy[numv-2] + " ic, ir = " + ic + " " + ir); }
					 */

					if (ii == 6) { // - add last two pairs
                                                ctrSet.add(vx, vy, numv - 4, numv - 3, low + il);
                                                ctrSet.add(vx, vy, numv - 2, numv - 1, low + il);
					} else {
                                                ctrSet.add(vx, vy, numv - 2, numv - 1, low + il);
					}

				} // for il -- NOTE: gg incremented in for statement
			} // for ic
		} // for ir

		// System.err.println ("ii:" + ii1 + " " +ii2 + " " +ii3 + " " +ii4 +
		// " "
		// +ii5 + " " +ii6);
		visad.util.Trace.call2("Contour2d.loop");

		/** ------------------- Color Fill ------------------------- */
		TriangleStripBuilder triStripBldr = null;

		if (fill) {
			triStripBldr = new TriangleStripBuilder(ncm, nrm, color_bin.length);
			fillGridBox(g, n_lines, vx, vy, xd, xdd, yd, ydd, nr, nrm, nc, ncm,
					ctrLow, o_flags, myvals, color_bin, grd_normals,
					triStripBldr);
			// BMF 2006-10-04 do not return, ie. draw labels on filled contours
			// for now, just return because we don't need to do labels
			// return;
		}

		// ---TDR, build Contour Strips

		Trace.call1("Contour2d.getLineColorArrays");
		ctrSet.getLineColorArrays(vx, vy, auxLevels, labelColor, labelFont,
				labelAlign, sphericalDisplayCS, dashFlags);
		Trace.call2("Contour2d.getLineColorArrays");

		return new ContourOutput(ctrSet, triStripBldr);
	}

	/**
	 * 
	 * @param g
	 * @param n_lines
	 * @param vx
	 * @param vy
	 * @param xd
	 * @param xdd
	 * @param yd
	 * @param ydd
	 * @param nr
	 * @param nrm
	 * @param nc
	 * @param ncm
	 * @param ctrLow
	 * @param o_flags
	 * @param values
	 * @param color_bin
	 * @param grd_normals
	 * @param triStripBldr
	 */
	
	private static void fillGridBox(float[] g, short[][] n_lines, float[] vx,
			float[] vy, float xd, float xdd, float yd, float ydd, int nr,
			int nrm, int nc, int ncm, short[][] ctrLow, byte[][][] o_flags,
			float[] values, byte[][] color_bin, float[][][] grd_normals,
			TriangleStripBuilder triStripBldr) {
		float xx, yy;
		int[] numv = new int[1];
		numv[0] = 0;

		for (int ic = 0; ic < ncm; ic++) {
			yy = ydd * ic + 0.0f;
			for (int ir = 0; ir < nrm; ir++) {
				triStripBldr.setGridBox(ic, ir);
				float ga, gb, gc, gd;
				xx = xdd * ir + 0.0f;

				// get 4 corner values, skip box if any are missing
				ga = (g[(ic) * nr + (ir)]);
       			// test for missing
       			if (Float.isNaN(ga))
         			continue;
				gb = (g[(ic) * nr + (ir + 1)]);
       			// test for missing
       			if (Float.isNaN(gb))
         			continue;
				gc = (g[(ic + 1) * nr + (ir)]);
       			// test for missing
       			if (Float.isNaN(gc))
         			continue;
				gd = (g[(ic + 1) * nr + (ir + 1)]);
       			// test for missing
       			if (Float.isNaN(gd ))
         			continue;

				numv[0] += n_lines[ir][ic] * 2;

				fillGridBox(new float[] { ga, gb, gc, gd }, n_lines[ir][ic],
						vx, vy, xx, yy, xd, yd, ic, ir, ctrLow[ir][ic],
						numv[0], o_flags[ir][ic], values, color_bin,
						grd_normals, triStripBldr);
			}
		}
	}

	/**
	 * 
	 * @param corners
	 * @param numc
	 * @param vx
	 * @param vy
	 * @param xx
	 * @param yy
	 * @param xd
	 * @param yd
	 * @param nc
	 * @param nr
	 * @param ctrLow
	 * @param numv
	 * @param o_flags
	 * @param values
	 * @param color_bin
	 * @param grd_normals
	 * @param triStripBldr
	 */
	
	private static void fillGridBox(float[] corners, int numc, float[] vx,
			float[] vy, float xx, float yy, float xd, float yd, int nc, int nr,
			short ctrLow, int numv, byte[] o_flags, float[] values,
			byte[][] color_bin, float[][][] grd_normals,
			TriangleStripBuilder triStripBldr) {

		int il = 0;
		int color_length = color_bin.length;
		float[] vv1 = new float[2];
		float[] vv2 = new float[2];
		float[] vv1_last = new float[2];
		float[] vv2_last = new float[2];
		float[][] vv = new float[2][2];
		float[][] vv_last = new float[2][2];

		int dir = 1;
		int start = numv - 2;
		int o_start = numc - 1;
		int o_idx = 0;
		byte o_flag = o_flags[o_idx];
		int[] closed = { 0 };
		boolean up;
		boolean right;

		int v_idx = start + dir * il * 2;

		int cc_start = (dir > 0) ? (ctrLow - 1) : (ctrLow + (numc - 1));

		// -- color level at corners
		// ------------------------------
		byte[][] crnr_color = new byte[4][color_length];
		int[] crnrLevelIdx = new int[4];
		boolean[] crnr_out = new boolean[] { true, true, true, true };
		boolean all_out = true;
		for (int tt = 0; tt < corners.length; tt++) {
			int cc = 0;
			int kk = 0;
			for (kk = 0; kk < (values.length - 1); kk++) {
				if ((corners[tt] >= values[kk])
						&& (corners[tt] < values[kk + 1])) {
					cc = kk;
					all_out = false;
					crnr_out[tt] = false;
				}
			}
			for (int ii = 0; ii < color_length; ii++) {
				crnr_color[tt][ii] = color_bin[ii][cc];
			}
			crnrLevelIdx[tt] = cc;
		}

		dir = 1;
		start = numv - numc * 2;
		o_start = 0;
		v_idx = start + dir * il * 2;
		up = false;
		right = false;
		float[] x_avg = new float[2];
		float[] y_avg = new float[2];

		if (numc > 1) { // -- first/next ctr line midpoints
			int idx = v_idx;
			x_avg[0] = (vx[idx] + vx[idx + 1]) / 2;
			y_avg[0] = (vy[idx] + vy[idx + 1]) / 2;
			idx = v_idx + 2;
			x_avg[1] = (vx[idx] + vx[idx + 1]) / 2;
			y_avg[1] = (vy[idx] + vy[idx + 1]) / 2;
			if ((x_avg[1] - x_avg[0]) > 0)
				up = true;
			if ((y_avg[1] - y_avg[0]) > 0)
				right = true;
		} else if (numc == 1) { // - default values for logic below
			x_avg[0] = 0f;
			y_avg[0] = 0f;
			x_avg[1] = 1f;
			y_avg[1] = 1f;
		} else if (numc == 0) // - empty grid box (no contour lines)
		{
			if (all_out)
				return;

			float[][] tri = new float[2][4];
			float[][] normals = new float[3][4];
			byte[] color = new byte[color_length];
			for (int ii = 0; ii < color_length; ii++) {
				color[ii] = crnr_color[0][ii];
			}

			normals[0][0] = grd_normals[nc][nr][0];
			normals[1][0] = grd_normals[nc][nr][1];
			normals[2][0] = grd_normals[nc][nr][2];
			tri[0][0] = xx;
			tri[1][0] = yy;

			normals[0][1] = grd_normals[nc + 1][nr][0];
			normals[1][1] = grd_normals[nc + 1][nr][1];
			normals[2][1] = grd_normals[nc + 1][nr][2];
			tri[0][1] = xx;
			tri[1][1] = yy + yd;

			normals[0][2] = grd_normals[nc][nr + 1][0];
			normals[1][2] = grd_normals[nc][nr + 1][1];
			normals[2][2] = grd_normals[nc][nr + 1][2];
			tri[0][2] = xx + xd;
			tri[1][2] = yy;

			normals[0][3] = grd_normals[nc + 1][nr + 1][0];
			normals[1][3] = grd_normals[nc + 1][nr + 1][1];
			normals[2][3] = grd_normals[nc + 1][nr + 1][2];
			tri[0][3] = xx + xd;
			tri[1][3] = yy + yd;

			byte first_tri_orient = CLOCKWISE;
			byte last_tri_orient = CNTRCLOCKWISE;
			byte first_strp_side = SIDE_LEFT;
			byte last_strp_side = SIDE_RIGHT;

			triStripBldr.addVerticies(crnrLevelIdx[0], tri, normals, color,
					first_strp_side, first_tri_orient, last_strp_side,
					last_tri_orient);

			return;
		} // -- end no contour lines

		// If any case 6 (saddle point), handle all contour lines with special
		// logic.
		for (int iii = 0; iii < o_flags.length; iii++) {
			if (o_flags[iii] > 32) {
				fillCaseSix(xx, yy, xd, yd, v_idx, dir, o_flags, ctrLow, vx,
						vy, nc, nr, crnr_color, crnrLevelIdx, crnr_out,
						color_bin, color_length, grd_normals, closed,
						triStripBldr);
				return;
			}
		}

		// -- start making triangles for color fill
		// ---------------------------------------------

		if (o_flag == 1 || o_flag == 4 || o_flag == 2 || o_flag == 7) {
			boolean opp = false;
			float dy = 0;
			float dx = 0;
			float dist_0 = 0;
			float dist_1 = 0;

			/**
			 * compare midpoints distances for first/next contour lines
			 * -------------------------------------------------
			 */
			if (o_flag == 1) {
				dy = (y_avg[1] - (yy));
				dx = (x_avg[1] - (xx));
				dist_1 = dy * dy + dx * dx;
				dy = (y_avg[0] - (yy));
				dx = (x_avg[0] - (xx));
				dist_0 = dy * dy + dx * dx;
			}
			if (o_flag == 2) {
				dy = (y_avg[1] - (yy));
				dx = (x_avg[1] - (xx + xd));
				dist_1 = dy * dy + dx * dx;
				dy = (y_avg[0] - (yy));
				dx = (x_avg[0] - (xx + xd));
				dist_0 = dy * dy + dx * dx;
			}
			if (o_flag == 4) {
				dy = (y_avg[1] - (yy + yd));
				dx = (x_avg[1] - (xx));
				dist_1 = dy * dy + dx * dx;
				dy = (y_avg[0] - (yy + yd));
				dx = (x_avg[0] - (xx));
				dist_0 = dy * dy + dx * dx;
			}
			if (o_flag == 7) {
				dy = (y_avg[1] - (yy + yd));
				dx = (x_avg[1] - (xx + xd));
				dist_1 = dy * dy + dx * dx;
				dy = (y_avg[0] - (yy + yd));
				dx = (x_avg[0] - (xx + xd));
				dist_0 = dy * dy + dx * dx;
			}
			if (dist_1 < dist_0)
				opp = true;
			if (opp) {
				fillToOppCorner(xx, yy, xd, yd, v_idx, o_flag, dir, vx, vy, nc,
						nr, crnr_color, crnrLevelIdx, crnr_out, grd_normals,
						closed, triStripBldr);
			} else {
				fillToNearCorner(xx, yy, xd, yd, v_idx, o_flag, dir, vx, vy,
						nc, nr, crnr_color, crnrLevelIdx, crnr_out,
						grd_normals, closed, triStripBldr);
			}
		} else if (o_flags[o_idx] == 3) {
			int flag = 1;
			if (right)
				flag = -1;
			fillToSide(xx, yy, xd, yd, v_idx, o_flag, flag, dir, vx, vy, nc,
					nr, crnr_color, crnrLevelIdx, crnr_out, grd_normals,
					closed, triStripBldr);
		} else if (o_flags[o_idx] == 5) {
			int flag = 1;
			if (!up)
				flag = -1;
			fillToSide(xx, yy, xd, yd, v_idx, o_flag, flag, dir, vx, vy, nc,
					nr, crnr_color, crnrLevelIdx, crnr_out, grd_normals,
					closed, triStripBldr);
		}

		byte last_o = o_flags[o_idx];

		// - move to next contour line
		// --------------------------------
		il++;
		for (il = 1; il < numc; il++) { // - iterate over contour line box
										// intersection segments
			v_idx = start + dir * il * 2;
			o_idx = o_start + dir * il;
			int v_idx_last = v_idx - 2 * dir;
			int cc = cc_start + dir * il;

			if (o_flags[o_idx] != last_o) // - contour line case change
			{
				// - box side of line segment verticies, 0->v_idx,
				// 1->v_idx->v_idx+1
				byte[] side_s = new byte[2];

				// - box side of last line segment verticies, 0->v_idx_last,
				// 1->v_idx->v_idx_last+1
				byte[] last_side_s = new byte[2];

				boolean flip;

				getBoxSide(vx, vy, xx, xd, yy, yd, v_idx, dir, o_flags[o_idx],
						side_s);
				getBoxSide(vx, vy, xx, xd, yy, yd, v_idx_last, dir, last_o,
						last_side_s);

				// -1: no line segment points lie on the same box side
				// 0:
				// 1:
				int same_side_idx = -1;

				if (side_s[0] == last_side_s[0]) {
					flip = false;
					same_side_idx = 0;
				} else if (side_s[0] == last_side_s[1]) {
					flip = true;
					same_side_idx = 1;
				} else if (side_s[1] == last_side_s[0]) {
					flip = true;
					same_side_idx = 0;
				} else if (side_s[1] == last_side_s[1]) {
					flip = false;
					same_side_idx = 1;
				} else {
					if (((side_s[0] + last_side_s[0]) & 1) == 1) { // flip so
																	// 0idx and
																	// 1idx on
																	// opposite
																	// sides
						flip = false;
					} else {
						flip = true;
					}
				}

				// - flip only (v_idx, v_idx+1) line segment, not 'last', ie.
				// (v_idx_last, v_idx_last+1)
				if (!flip) {
					vv1[0] = vx[v_idx];
					vv1[1] = vy[v_idx];
					vv2[0] = vx[v_idx + dir];
					vv2[1] = vy[v_idx + dir];

					vv[0][0] = vx[v_idx];
					vv[1][0] = vy[v_idx];
					vv[0][1] = vx[v_idx + dir];
					vv[1][1] = vy[v_idx + dir];
				} else { // do the flip
					vv1[0] = vx[v_idx + dir];
					vv1[1] = vy[v_idx + dir];
					vv2[0] = vx[v_idx];
					vv2[1] = vy[v_idx];

					vv[0][0] = vx[v_idx + dir];
					vv[1][0] = vy[v_idx + dir];
					vv[0][1] = vx[v_idx];
					vv[1][1] = vy[v_idx];
					// - reflect that the flipped occurred.
					byte tmp = side_s[0];
					side_s[0] = side_s[1];
					side_s[1] = tmp;
				}
				vv1_last[0] = vx[v_idx_last];
				vv1_last[1] = vy[v_idx_last];
				vv2_last[0] = vx[v_idx_last + dir];
				vv2_last[1] = vy[v_idx_last + dir];

				vv_last[0][0] = vx[v_idx_last];
				vv_last[1][0] = vy[v_idx_last];
				vv_last[0][1] = vx[v_idx_last + dir];
				vv_last[1][1] = vy[v_idx_last + dir];

				fillCaseChange(xx, yy, xd, yd, nc, nr, vv1, vv2, vv1_last,
						vv2_last, color_bin, cc, color_length, grd_normals,
						closed, same_side_idx, side_s, last_side_s,
						triStripBldr);
			} else {
				vv1[0] = vx[v_idx];
				vv1[1] = vy[v_idx];
				vv2[0] = vx[v_idx + dir];
				vv2[1] = vy[v_idx + dir];
				vv1_last[0] = vx[v_idx_last];
				vv1_last[1] = vy[v_idx_last];
				vv2_last[0] = vx[v_idx_last + dir];
				vv2_last[1] = vy[v_idx_last + dir];

				fillToLast(xx, yy, xd, yd, nc, nr, vv1, vv2, vv1_last,
						vv2_last, o_flags[o_idx], color_bin, cc, color_length,
						grd_normals, -1, triStripBldr);
			}
			last_o = o_flags[o_idx];
		} // ---- contour lines loop

		/*- last or first/last contour line
		------------------------------------*/
		int flag_set = 0;
		if ((last_o == 1) || (last_o == 2) || (last_o == 4) || (last_o == 7)) {
			if (last_o == 1)
				flag_set = (closed[0] & 1);
			if (last_o == 2)
				flag_set = (closed[0] & 2);
			if (last_o == 4)
				flag_set = (closed[0] & 4);
			if (last_o == 7)
				flag_set = (closed[0] & 8);

			if (flag_set > 0) {
				fillToOppCorner(xx, yy, xd, yd, v_idx, last_o, dir, vx, vy, nc,
						nr, crnr_color, crnrLevelIdx, crnr_out, grd_normals,
						closed, triStripBldr);
			} else {
				fillToNearCorner(xx, yy, xd, yd, v_idx, last_o, dir, vx, vy,
						nc, nr, crnr_color, crnrLevelIdx, crnr_out,
						grd_normals, closed, triStripBldr);
			}
		} else if (last_o == 3) {
			int flag = -1;
			if (closed[0] == 3)
				flag = 1;
			fillToSide(xx, yy, xd, yd, v_idx, last_o, flag, dir, vx, vy, nc,
					nr, crnr_color, crnrLevelIdx, crnr_out, grd_normals,
					closed, triStripBldr);
		} else if (last_o == 5) {
			int flag = 1;
			if (closed[0] == 5)
				flag = -1;
			fillToSide(xx, yy, xd, yd, v_idx, last_o, flag, dir, vx, vy, nc,
					nr, crnr_color, crnrLevelIdx, crnr_out, grd_normals,
					closed, triStripBldr);
		}

	} // --- end fillGridBox

	/**
	 * 
	 * @param vx
	 * @param vy
	 * @param xx
	 * @param xd
	 * @param yy
	 * @param yd
	 * @param v_idx
	 * @param dir
	 * @param o_flag
	 * @param side
	 */
	
	private static void getBoxSide(float[] vx, float[] vy, float xx, float xd,
			float yy, float yd, int v_idx, int dir, byte o_flag, byte[] side) {
		/*
		 * if (vy[v_idx] == yy) side[0] = 0; // a-b if (vy[v_idx] == (yy + yd))
		 * side[0] = 2; // c-d if (vx[v_idx] == xx) side[0] = 3; // a-c if
		 * (vx[v_idx] == (xx + xd)) side[0] = 1; // b-d
		 */

		for (int kk = 0; kk < 2; kk++) {
			int ii = v_idx + kk * dir;
			switch (o_flag) {
			case 1:
				side[kk] = 3;
				if (vy[ii] == yy)
					side[kk] = 0;
				break;
			case 2:
				side[kk] = 1;
				if (vy[ii] == yy)
					side[kk] = 0;
				break;
			case 4:
				side[kk] = 3;
				if (vy[ii] == (yy + yd))
					side[kk] = 2;
				break;
			case 7:
				side[kk] = 1;
				if (vy[ii] == (yy + yd))
					side[kk] = 2;
				break;
			case 3:
				side[kk] = 1;
				if (vx[ii] == xx)
					side[kk] = 3;
				break;
			case 5:
				side[kk] = 0;
				if (vy[ii] == (yy + yd))
					side[kk] = 2;
				break;
			}
		}
		// - check for degenerate corner case, ie both intersection points on a
		// corner
		switch (o_flag) {
		case 1:
			if (side[0] == side[1]) {
				side[0] = 0;
				side[1] = 3;
			}
			break;
		case 2:
			if (side[0] == side[1]) {
				side[0] = 0;
				side[1] = 1;
			}
			break;
		case 4:
			if (side[0] == side[1]) {
				side[0] = 3;
				side[1] = 2;
			}
			break;
		case 7:
			if (side[0] == side[1]) {
				side[0] = 1;
				side[1] = 2;
			}
			break;
		}
	}

	/**
	 * 
	 * @param vx
	 * @param vy
	 * @param xx
	 * @param yy
	 * @param nc
	 * @param nr
	 * @param xd
	 * @param yd
	 * @param grd_normals
	 * @param n_idx
	 * @param tri_normals
	 */

	private static void interpNormals(float[] vx, float[] vy, float xx,
			float yy, int nc, int nr, float xd, float yd,
			float[][][] grd_normals, float[][] tri_normals) {

		int n_verts = vx.length;
		float[][] tmp = new float[3][1];

		for (int k = 0; k < n_verts; k++) {
			interpNormals(vx[k], vy[k], xx, yy, nc, nr, xd, yd, grd_normals,
					tmp);
			tri_normals[0][k] = tmp[0][0];
			tri_normals[1][k] = tmp[1][0];
			tri_normals[2][k] = tmp[2][0];
		}
	}

	private static void interpNormals(float vx, float vy, float xx, float yy,
			int nc, int nr, float xd, float yd, float[][][] grd_normals,
			float[][] tri_normals) {

		int[] n_idx = new int[] { 0 };
		float[] temp = new float[3];

		interpNormals(vx, vy, xx, yy, nc, nr, xd, yd, grd_normals, n_idx, temp);
		tri_normals[0][0] = temp[0];
		tri_normals[1][0] = temp[1];
		tri_normals[2][0] = temp[2];
	}

	private static void interpNormals(float vx, float vy, float xx, float yy,
			int nc, int nr, float xd, float yd, float[][][] grd_normals,
			int[] n_idx, float[] tri_normals) {
		int side = -1;
		float[] nn = new float[3];

		if (vy == yy)
			side = 0; // a-b
		if (vy == (yy + yd))
			side = 2; // c-d
		if (vx == xx)
			side = 3; // a-c
		if (vx == (xx + xd))
			side = 1; // b-d

		float dx = vx - xx;
		float dy = vy - yy;

		switch (side) {
		case 0:
			nn[0] = ((grd_normals[nc][nr + 1][0] - grd_normals[nc][nr][0]) / xd)
					* dx + grd_normals[nc][nr][0];
			nn[1] = ((grd_normals[nc][nr + 1][1] - grd_normals[nc][nr][1]) / xd)
					* dx + grd_normals[nc][nr][1];
			nn[2] = ((grd_normals[nc][nr + 1][2] - grd_normals[nc][nr][2]) / xd)
					* dx + grd_normals[nc][nr][2];
			break;
		case 3:
			nn[0] = ((grd_normals[nc + 1][nr][0] - grd_normals[nc][nr][0]) / yd)
					* dy + grd_normals[nc][nr][0];
			nn[1] = ((grd_normals[nc + 1][nr][1] - grd_normals[nc][nr][1]) / yd)
					* dy + grd_normals[nc][nr][1];
			nn[2] = ((grd_normals[nc + 1][nr][2] - grd_normals[nc][nr][2]) / yd)
					* dy + grd_normals[nc][nr][2];
			break;
		case 1:
			nn[0] = ((grd_normals[nc + 1][nr + 1][0] - grd_normals[nc][nr + 1][0]) / yd)
					* dy + grd_normals[nc][nr + 1][0];
			nn[1] = ((grd_normals[nc + 1][nr + 1][1] - grd_normals[nc][nr + 1][1]) / yd)
					* dy + grd_normals[nc][nr + 1][1];
			nn[2] = ((grd_normals[nc + 1][nr + 1][2] - grd_normals[nc][nr + 1][2]) / yd)
					* dy + grd_normals[nc][nr + 1][2];
			break;
		case 2:
			nn[0] = ((grd_normals[nc + 1][nr + 1][0] - grd_normals[nc + 1][nr][0]) / xd)
					* dx + grd_normals[nc + 1][nr][0];
			nn[1] = ((grd_normals[nc + 1][nr + 1][1] - grd_normals[nc + 1][nr][1]) / xd)
					* dx + grd_normals[nc + 1][nr][1];
			nn[2] = ((grd_normals[nc + 1][nr + 1][2] - grd_normals[nc + 1][nr][2]) / xd)
					* dx + grd_normals[nc + 1][nr][2];
			break;
		default:
			System.out.println("interpNormals, bad side: " + side);
		}
		// - re-normalize
		float mag = (float) Math.sqrt(nn[0] * nn[0] + nn[1] * nn[1] + nn[2]
				* nn[2]);
		nn[0] /= mag;
		nn[1] /= mag;
		nn[2] /= mag;
		tri_normals[n_idx[0]++] = nn[0];
		tri_normals[n_idx[0]++] = nn[1];
		tri_normals[n_idx[0]++] = nn[2];
	}

	/**
	 * 
	 * @param xx
	 * @param yy
	 * @param xd
	 * @param yd
	 * @param nc
	 * @param nr
	 * @param vv1
	 * @param vv2
	 * @param vv1_last
	 * @param vv2_last
	 * @param kase
	 * @param color_bin
	 * @param cc
	 * @param color_length
	 * @param grd_normals
	 * @param same_side_idx
	 * @param triStripBldr
	 */
	
	private static void fillToLast(float xx, float yy, float xd, float yd,
			int nc, int nr, float[] vv1, float[] vv2, float[] vv1_last,
			float[] vv2_last, byte kase, byte[][] color_bin, int cc,
			int color_length, float[][][] grd_normals, int same_side_idx,
			TriangleStripBuilder triStripBldr) {
		float[][] tri = new float[2][4];
		float[][] normals = new float[3][4];
		byte[] side_s = new byte[2];
		byte[] last_side_s = new byte[2];
		byte[] strp_sides = new byte[4];
		int startIdx = 0;

		float[] vx = new float[2];
		float[] vy = new float[2];

		vx[0] = vv1[0];
		vx[1] = vv2[0];
		vy[0] = vv1[1];
		vy[1] = vv2[1];
		getBoxSide(vx, vy, xx, xd, yy, yd, 0, 1, kase, side_s);

		vx[0] = vv1_last[0];
		vx[1] = vv2_last[0];
		vy[0] = vv1_last[1];
		vy[1] = vv2_last[1];
		getBoxSide(vx, vy, xx, xd, yy, yd, 0, 1, kase, last_side_s);

		fillToLast(xx, yy, xd, yd, nc, nr, vv1, vv2, vv1_last, vv2_last,
				color_bin, cc, color_length, grd_normals, same_side_idx,
				side_s, last_side_s, strp_sides, tri, normals, startIdx,
				triStripBldr);

	}

	private static void fillToLast(float xx, float yy, float xd, float yd,
			int nc, int nr, float[] vv1, float[] vv2, float[] vv1_last,
			float[] vv2_last, byte[][] color_bin, int cc, int color_length,
			float[][][] grd_normals, int same_side_idx, byte[] side_s,
			byte[] last_side_s, byte[] strp_sides, float[][] tri,
			float[][] normals, int startIdx, TriangleStripBuilder triStripBldr) {
		float x0, y0, x1, y1, x2, y2, x3, y3;

		float[][] tmp = new float[3][1];
		byte[] color = new byte[color_length];

		for (int ii = 0; ii < color_length; ii++) {
			color[ii] = color_bin[ii][cc];
		}

		if (same_side_idx <= 0) {
			x0 = vv1[0];
			y0 = vv1[1];
			x1 = vv1_last[0];
			y1 = vv1_last[1];
			x2 = vv2[0];
			y2 = vv2[1];
			x3 = vv2_last[0];
			y3 = vv2_last[1];
			strp_sides[0] = side_s[0];
			strp_sides[1] = last_side_s[0];
			strp_sides[2] = side_s[1];
			strp_sides[3] = last_side_s[1];
		} else {
			x0 = vv2[0];
			y0 = vv2[1];
			x1 = vv2_last[0];
			y1 = vv2_last[1];
			x2 = vv1[0];
			y2 = vv1[1];
			x3 = vv1_last[0];
			y3 = vv1_last[1];
			strp_sides[0] = side_s[1];
			strp_sides[1] = last_side_s[1];
			strp_sides[2] = side_s[0];
			strp_sides[3] = last_side_s[0];
		}

		int idx = startIdx + 0;
		tri[0][idx] = x0;
		tri[1][idx] = y0;
		interpNormals(tri[0][idx], tri[1][idx], xx, yy, nc, nr, xd, yd,
				grd_normals, tmp);
		normals[0][idx] = tmp[0][0];
		normals[1][idx] = tmp[1][0];
		normals[2][idx] = tmp[2][0];

		idx = startIdx + 1;
		tri[0][idx] = x1;
		tri[1][idx] = y1;
		interpNormals(tri[0][idx], tri[1][idx], xx, yy, nc, nr, xd, yd,
				grd_normals, tmp);
		normals[0][idx] = tmp[0][0];
		normals[1][idx] = tmp[1][0];
		normals[2][idx] = tmp[2][0];

		idx = startIdx + 2;
		tri[0][idx] = x2;
		tri[1][idx] = y2;
		interpNormals(tri[0][idx], tri[1][idx], xx, yy, nc, nr, xd, yd,
				grd_normals, tmp);
		normals[0][idx] = tmp[0][0];
		normals[1][idx] = tmp[1][0];
		normals[2][idx] = tmp[2][0];

		idx = startIdx + 3;
		tri[0][idx] = x3;
		tri[1][idx] = y3;
		interpNormals(tri[0][idx], tri[1][idx], xx, yy, nc, nr, xd, yd,
				grd_normals, tmp);
		normals[0][idx] = tmp[0][0];
		normals[1][idx] = tmp[1][0];
		normals[2][idx] = tmp[2][0];

		if (triStripBldr != null) {
			byte first_tri_orient = 0;
			byte last_tri_orient = 0;
			byte kase = strp_sides[0];

			byte first_strp_side = strp_sides[0];
			byte last_strp_side = strp_sides[3];

			switch (kase) {
			case 1:
				if ((tri[0][1] - tri[0][0]) >= 0) {
					first_tri_orient = 1;
					last_tri_orient = -1;
				} else {
					first_tri_orient = -1;
					last_tri_orient = 1;
				}
				break;
			case 2:
				if ((tri[0][1] - tri[0][0]) >= 0) {
					first_tri_orient = 1;
					last_tri_orient = -1;
				} else {
					first_tri_orient = -1;
					last_tri_orient = 1;
				}
				break;
			case 4:
				if ((tri[1][1] - tri[1][0]) >= 0) {
					first_tri_orient = -1;
					last_tri_orient = 1;
				} else {
					first_tri_orient = 1;
					last_tri_orient = -1;
				}
				break;
			case 7:
				if ((tri[1][1] - tri[1][0]) >= 0) {
					first_tri_orient = 1;
					last_tri_orient = -1;
				} else {
					first_tri_orient = -1;
					last_tri_orient = 1;
				}
				break;
			case 3:
				if ((tri[1][1] - tri[1][0]) >= 0) {
					first_tri_orient = -1;
					last_tri_orient = 1;
				} else {
					first_tri_orient = 1;
					last_tri_orient = -1;
				}
				break;
			case 5:
				if ((tri[0][1] - tri[0][0]) >= 0) {
					first_tri_orient = 1;
					last_tri_orient = -1;
				} else {
					first_tri_orient = -1;
					last_tri_orient = 1;
				}
				break;
			}

			triStripBldr.addVerticies(cc, tri, normals, color, first_strp_side,
					first_tri_orient, last_strp_side, last_tri_orient);
		}
	}

	private static void fillCaseChange(float xx, float yy, float xd, float yd,
			int nc, int nr, float[] vv1, float[] vv2, float[] vv1_last,
			float[] vv2_last, byte[][] color_bin, int cc, int color_length,
			float[][][] grd_normals, int[] closed, int same_side_idx,
			byte[] side_s, byte[] last_side_s, TriangleStripBuilder triStripBldr) {
		float[][] tri = null;
		float[][] normals = null;
		byte first_tri_orient = 0;
		byte last_tri_orient = 0;
		byte first_strp_side = SIDE_NONE;
		byte last_strp_side = SIDE_NONE;
		byte[] strp_sides = new byte[4];

		byte[] color = new byte[color_length];
		for (int k = 0; k < color_length; k++)
			color[k] = color_bin[k][cc];

		/*
		 * no line segments points are on the same box side. Close off (2)
		 * opposite corners
		 */
		if (same_side_idx == -1) {
			tri = new float[2][6];
			normals = new float[3][6];

			// - use box side of first point in both contour segments.
			byte side = side_s[0];
			byte last_s = last_side_s[0];
			byte[] cornersToAdd = new byte[2];

			fillToLast(xx, yy, xd, yd, nc, nr, vv1, vv2, vv1_last, vv2_last,
					color_bin, cc, color_length, grd_normals, same_side_idx,
					side_s, last_side_s, strp_sides, tri, normals, 1, null);

			if ((side == 0 && last_s == 1) || (side == 3 && last_s == 2)
					|| (side == 2 && last_s == 3) || (side == 1 && last_s == 0)) {
				if (strp_sides[0] == 0 && strp_sides[1] == 1) {
					cornersToAdd[0] = 1;
					cornersToAdd[1] = 2;
					first_tri_orient = -1;
					last_tri_orient = 1;
				}

				if (strp_sides[0] == 1 && strp_sides[1] == 0) {
					cornersToAdd[0] = 1;
					cornersToAdd[1] = 2;
					first_tri_orient = 1;
					last_tri_orient = -1;
				}

				if (strp_sides[0] == 3 && strp_sides[1] == 2) {
					cornersToAdd[0] = 2;
					cornersToAdd[1] = 1;
					first_tri_orient = 1;
					last_tri_orient = -1;
				}

				if (strp_sides[0] == 2 && strp_sides[1] == 3) {
					cornersToAdd[0] = 2;
					cornersToAdd[1] = 1;
					first_tri_orient = -1;
					last_tri_orient = 1;
				}
			}

			if ((side == 0 && last_s == 3) || (side == 1 && last_s == 2)
					|| (side == 3 && last_s == 0) || (side == 2 && last_s == 1)) {
				if (strp_sides[0] == 0 && strp_sides[1] == 3) {
					cornersToAdd[0] = 0;
					cornersToAdd[1] = 3;
					first_tri_orient = 1;
					last_tri_orient = -1;
				}

				if (strp_sides[0] == 3 && strp_sides[1] == 0) {
					cornersToAdd[0] = 0;
					cornersToAdd[1] = 3;
					first_tri_orient = -1;
					last_tri_orient = 1;
				}

				if (strp_sides[0] == 1 && strp_sides[1] == 2) {
					cornersToAdd[0] = 3;
					cornersToAdd[1] = 0;
					first_tri_orient = -1;
					last_tri_orient = 1;
				}

				if (strp_sides[0] == 2 && strp_sides[1] == 1) {
					cornersToAdd[0] = 3;
					cornersToAdd[1] = 0;
					first_tri_orient = 1;
					last_tri_orient = -1;
				}
			}

			first_strp_side = strp_sides[0];
			last_strp_side = strp_sides[3];
			addCorner(xx, yy, xd, yd, nc, nr, cornersToAdd[0], grd_normals,
					closed, 0, tri, normals);
			addCorner(xx, yy, xd, yd, nc, nr, cornersToAdd[1], grd_normals,
					closed, 5, tri, normals);

			triStripBldr.addVerticies(cc, tri, normals, color, first_strp_side,
					first_tri_orient, last_strp_side, last_tri_orient);
		} else { // - 2 line segment end points have the same box side
			byte side, last_s;
			int indx = (same_side_idx == 0) ? 1 : 0;
			side = side_s[indx];
			last_s = last_side_s[indx];

			byte kase = 0;

			if ((side == 0 && last_s == 3) || (side == 3 && last_s == 0))
				kase = 1;
			if ((side == 0 && last_s == 1) || (side == 1 && last_s == 0))
				kase = 2;
			if ((side == 2 && last_s == 3) || (side == 3 && last_s == 2))
				kase = 4;
			if ((side == 2 && last_s == 1) || (side == 1 && last_s == 2))
				kase = 7;
			if ((side == 1 && last_s == 3) || (side == 3 && last_s == 1))
				kase = 3;
			if ((side == 2 && last_s == 0) || (side == 0 && last_s == 2))
				kase = 5;

			if (kase == 1 || kase == 2 || kase == 4 || kase == 7) { // close off
																	// (1)
																	// corner
				byte cornerId = 127;
				tri = new float[2][5];
				normals = new float[3][5];

				fillToLast(xx, yy, xd, yd, nc, nr, vv1, vv2, vv1_last,
						vv2_last, color_bin, cc, color_length, grd_normals,
						same_side_idx, side_s, last_side_s, strp_sides, tri,
						normals, 0, null);

				if (kase == 1) {
					cornerId = 0;
					last_strp_side = strp_sides[3];
					first_strp_side = strp_sides[0];
					if (last_strp_side == 3) {
						last_tri_orient = 1;
						first_tri_orient = 1;
					} else if (last_strp_side == 0) {
						last_tri_orient = -1;
						first_tri_orient = -1;
					}
				}
				if (kase == 2) {
					cornerId = 1;
					last_strp_side = strp_sides[3];
					first_strp_side = strp_sides[0];
					if (last_strp_side == 1) {
						last_tri_orient = -1;
						first_tri_orient = -1;
					} else if (last_strp_side == 0) {
						last_tri_orient = 1;
						first_tri_orient = 1;
					}
				}
				if (kase == 4) {
					cornerId = 2;
					last_strp_side = strp_sides[3];
					first_strp_side = strp_sides[0];
					if (last_strp_side == 2) {
						last_tri_orient = 1;
						first_tri_orient = 1;
					} else if (last_strp_side == 3) {
						last_tri_orient = -1;
						first_tri_orient = -1;
					}
				}
				if (kase == 7) {
					cornerId = 3;
					last_strp_side = strp_sides[3];
					first_strp_side = strp_sides[0];
					if (last_strp_side == 2) {
						last_tri_orient = -1;
						first_tri_orient = -1;
					} else if (last_strp_side == 1) {
						last_tri_orient = 1;
						first_tri_orient = 1;
					}
				}
				addCorner(xx, yy, xd, yd, nc, nr, cornerId, grd_normals,
						closed, 4, tri, normals);
			} else if (kase == 5) { // close off (2) adjacent corners
				byte[] oppCorners = new byte[] { 127, 127 };
				byte same_side = side_s[same_side_idx];
				tri = new float[2][6];
				normals = new float[3][6];
				fillToLast(xx, yy, xd, yd, nc, nr, vv1, vv2, vv1_last,
						vv2_last, color_bin, cc, color_length, grd_normals,
						same_side_idx, side_s, last_side_s, strp_sides, tri,
						normals, 0, null);

				if (same_side == 1) {
					if (strp_sides[3] == 2) {
						oppCorners = new byte[] { 0, 2 };
						first_tri_orient = 1;
						last_tri_orient = -1;
						first_strp_side = 1;
						last_strp_side = 3;
					} else if (strp_sides[3] == 0) {
						oppCorners = new byte[] { 2, 0 };
						first_tri_orient = -1;
						last_tri_orient = 1;
						first_strp_side = 1;
						last_strp_side = 3;
					}
				}
				if (same_side == 3) {
					if (strp_sides[3] == 2) {
						oppCorners = new byte[] { 1, 3 };
						first_tri_orient = -1;
						last_tri_orient = 1;
						first_strp_side = 3;
						last_strp_side = 1;
					} else if (strp_sides[3] == 0) {
						oppCorners = new byte[] { 3, 1 };
						first_tri_orient = 1;
						last_tri_orient = -1;
						first_strp_side = 3;
						last_strp_side = 1;
					}
				}
				addCorner(xx, yy, xd, yd, nc, nr, oppCorners[0], grd_normals,
						closed, 4, tri, normals);
				addCorner(xx, yy, xd, yd, nc, nr, oppCorners[1], grd_normals,
						closed, 5, tri, normals);
			} else if (kase == 3) { // - close off (2) adjacent corners
				byte[] oppCorners = new byte[] { 127, 127 };
				byte same_side = side_s[same_side_idx];
				tri = new float[2][6];
				normals = new float[3][6];
				fillToLast(xx, yy, xd, yd, nc, nr, vv1, vv2, vv1_last,
						vv2_last, color_bin, cc, color_length, grd_normals,
						same_side_idx, side_s, last_side_s, strp_sides, tri,
						normals, 0, null);

				if (same_side == 0) {
					if (strp_sides[3] == 3) {
						oppCorners = new byte[] { 3, 2 };
						first_tri_orient = -1;
						last_tri_orient = 1;
						first_strp_side = 0;
						last_strp_side = 2;
					} else if (strp_sides[3] == 1) {
						oppCorners = new byte[] { 2, 3 };
						first_tri_orient = 1;
						last_tri_orient = -1;
						first_strp_side = 0;
						last_strp_side = 2;
					}
				}
				if (same_side == 2) {
					if (strp_sides[3] == 3) {
						oppCorners = new byte[] { 1, 0 };
						first_tri_orient = 1;
						last_tri_orient = -1;
						first_strp_side = 2;
						last_strp_side = 0;
					} else if (strp_sides[3] == 1) {
						oppCorners = new byte[] { 0, 1 };
						first_tri_orient = -1;
						last_tri_orient = 1;
						first_strp_side = 2;
						last_strp_side = 0;
					}
				}
				addCorner(xx, yy, xd, yd, nc, nr, oppCorners[0], grd_normals,
						closed, 4, tri, normals);
				addCorner(xx, yy, xd, yd, nc, nr, oppCorners[1], grd_normals,
						closed, 5, tri, normals);
			}

			triStripBldr.addVerticies(cc, tri, normals, color, first_strp_side,
					first_tri_orient, last_strp_side, last_tri_orient);
		}
	}

	/**
	 *  
	 * @param xx
	 * @param yy
	 * @param xd
	 * @param yd
	 * @param v_idx
	 * @param dir
	 * @param o_flags
	 * @param ctrLow
	 * @param vx
	 * @param vy
	 * @param nc
	 * @param nr
	 * @param crnr_color
	 * @param crnrLevelIdx
	 * @param crnr_out
	 * @param color_bin
	 * @param color_length
	 * @param grd_normals
	 * @param closed
	 * @param triStripBldr
	 */
	
	private static void fillCaseSix(float xx, float yy, float xd, float yd,
			int v_idx, int dir, byte[] o_flags, short ctrLow, float[] vx,
			float[] vy, int nc, int nr, byte[][] crnr_color,
			int[] crnrLevelIdx, boolean[] crnr_out, byte[][] color_bin,
			int color_length, float[][][] grd_normals, int[] closed,
			TriangleStripBuilder triStripBldr) {

		float[][] tri = null;

		int n1 = 0; // - number of case1 line segments
		int n2 = 0; // - case2
		int n4 = 0; // - case4
		int n7 = 0; // - case7

		for (int kk = 0; kk < o_flags.length; kk++) {
			if ((o_flags[kk] - 32) == 1 || o_flags[kk] == 1)
				n1++;
			if ((o_flags[kk] - 32) == 2 || o_flags[kk] == 2)
				n2++;
			if ((o_flags[kk] - 32) == 4 || o_flags[kk] == 4)
				n4++;
			if ((o_flags[kk] - 32) == 7 || o_flags[kk] == 7)
				n7++;
		}
		// - all case coords in separate arrays
		float[][] vv1 = new float[2][n1 * 2]; // - all case1 coords in one array
		float[][] vv2 = new float[2][n2 * 2]; // 2
		float[][] vv4 = new float[2][n4 * 2]; // 4
		float[][] vv7 = new float[2][n7 * 2]; // 7
		int[] clr_idx1 = new int[n1];
		int[] clr_idx2 = new int[n2];
		int[] clr_idx4 = new int[n4];
		int[] clr_idx7 = new int[n7];
		float[] vvv1 = new float[2];
		float[] vvv2 = new float[2];
		float[] vvv1_last = new float[2];
		float[] vvv2_last = new float[2];

		n1 = 0;
		n2 = 0;
		n4 = 0;
		n7 = 0;
		int ii = v_idx;
		int cc = ctrLow - 1;
		int cnt = 0;
		int[] cases = { 1, 2, 7, 4 }; // - corner cases, counter-clockwise
										// around box

		for (int kk = 0; kk < o_flags.length; kk++) {
			if (o_flags[kk] > 32)
				cnt++;

			if ((o_flags[kk] - 32) == 1 || o_flags[kk] == 1) {
				clr_idx1[n1] = cc;
				vv1[0][2 * n1] = vx[ii];
				vv1[1][2 * n1] = vy[ii];
				vv1[0][2 * n1 + 1] = vx[ii + 1];
				vv1[1][2 * n1 + 1] = vy[ii + 1];
				n1++;
			} else if ((o_flags[kk] - 32) == 2 || o_flags[kk] == 2) {
				clr_idx2[n2] = cc;
				vv2[0][2 * n2] = vx[ii];
				vv2[1][2 * n2] = vy[ii];
				vv2[0][2 * n2 + 1] = vx[ii + 1];
				vv2[1][2 * n2 + 1] = vy[ii + 1];
				n2++;
			} else if ((o_flags[kk] - 32) == 4 || o_flags[kk] == 4) {
				clr_idx4[n4] = cc;
				vv4[0][2 * n4] = vx[ii];
				vv4[1][2 * n4] = vy[ii];
				vv4[0][2 * n4 + 1] = vx[ii + 1];
				vv4[1][2 * n4 + 1] = vy[ii + 1];
				n4++;
			} else if ((o_flags[kk] - 32) == 7 || o_flags[kk] == 7) {
				clr_idx7[n7] = cc;
				vv7[0][2 * n7] = vx[ii];
				vv7[1][2 * n7] = vy[ii];
				vv7[0][2 * n7 + 1] = vx[ii + 1];
				vv7[1][2 * n7 + 1] = vy[ii + 1];
				n7++;
			}
			if (o_flags[kk] < 32) {
				cc += 1;
			} else if (cnt == 2) {
				cnt = 0;
				cc++;
			}
			ii += 2;
		}

		int[] clr_idx = null;
		float[] vvx = null;
		float[] vvy = null;
		float[] x_avg = new float[2];
		float[] y_avg = new float[2];
		float dist_0 = 0;
		float dist_1 = 0;
		float xxx = 0;
		float yyy = 0;
		float dx = 0;
		float dy = 0;
		int nn = 0;
		int pt = 0;
		int n_pt = 0;
		int s_idx = 0;
		int ns_idx = 0;
		byte[] tmp = null;
		byte[] cntr_color = null;
		int cntr_clr = Integer.MIN_VALUE;

		float[][] edge_points = new float[2][8];
		boolean[] edge_point_a_corner = { false, false, false, false, false,
				false, false, false };
		boolean[] edge_point_out = { false, false, false, false, false, false,
				false, false };
		boolean this_crnr_out = false;
		int n_crnr_out = 0;

		int[] which_corner = new int[2]; // - use case values
		int num_corners = 0;

		/*
		 * Letters are the grid corners consistent with previous definition.
		 * Numbers are indexes into the edge_points array. Note, if there is no
		 * corner line segement, the adjacent indexes both refer to the corner:
		 * 4,3 -> D, 6,5 -> C, 0,7 -> A, 1,2 -> B, otherwise they are the
		 * contour line segment intersection points.
		 * 
		 * C------(5)-------(4)------D | | | | | | (6) (3) | | | | | | (7) (2) |
		 * | | | | | A------(0)-------(1)------B
		 */

		/*
		 * Iterate through cases, they're all corners, and create the fill
		 * geometry. When finished the final edge points are used below to fill
		 * the remainder of the grid box.
		 */
		for (int kk = 0; kk < cases.length; kk++) {
			switch (cases[kk]) {
			case 1:
				nn = n1;
				clr_idx = clr_idx1;
				vvx = vv1[0];
				vvy = vv1[1];
				xxx = xx;
				yyy = yy;
				pt = 0;
				n_pt = 7;
				s_idx = 0;
				ns_idx = 1;
				tmp = crnr_color[0];
				this_crnr_out = crnr_out[0];
				break;
			case 2:
				nn = n2;
				clr_idx = clr_idx2;
				vvx = vv2[0];
				vvy = vv2[1];
				xxx = xx + xd;
				yyy = yy;
				pt = 1;
				n_pt = 2;
				s_idx = 0;
				ns_idx = 1;
				tmp = crnr_color[1];
				this_crnr_out = crnr_out[1];
				break;
			case 4:
				nn = n4;
				clr_idx = clr_idx4;
				vvx = vv4[0];
				vvy = vv4[1];
				xxx = xx;
				yyy = yy + yd;
				pt = 5;
				n_pt = 6;
				s_idx = 1;
				ns_idx = 0;
				tmp = crnr_color[2];
				this_crnr_out = crnr_out[2];
				break;
			case 7:
				nn = n7;
				clr_idx = clr_idx7;
				vvx = vv7[0];
				vvy = vv7[1];
				xxx = xx + xd;
				yyy = yy + yd;
				pt = 3;
				n_pt = 4;
				s_idx = 0;
				ns_idx = 1;
				tmp = crnr_color[3];
				this_crnr_out = crnr_out[3];
				break;
			}

			if (nn == 0) {
				edge_points[0][pt] = xxx;
				edge_points[1][pt] = yyy;
				edge_points[0][n_pt] = xxx;
				edge_points[1][n_pt] = yyy;
				cntr_color = tmp;
				edge_point_a_corner[pt] = true;
				edge_point_a_corner[n_pt] = true;
				edge_point_out[pt] = this_crnr_out;
				edge_point_out[n_pt] = this_crnr_out;
				which_corner[num_corners] = cases[kk];
				num_corners++;
				if (this_crnr_out)
					n_crnr_out++;
			} else if (nn == 1) {
				fillToNearCorner(xx, yy, xd, yd, 0, (byte) cases[kk], dir, vvx,
						vvy, nc, nr, crnr_color, crnrLevelIdx, crnr_out,
						grd_normals, closed, triStripBldr);
				edge_points[0][pt] = vvx[s_idx];
				edge_points[1][pt] = vvy[s_idx];
				edge_points[0][n_pt] = vvx[ns_idx];
				edge_points[1][n_pt] = vvy[ns_idx];
				if (clr_idx[0] > cntr_clr)
					cntr_clr = clr_idx[0];
			} else {
				int il = 0;
				int idx = 0;
				x_avg[0] = (vvx[idx] + vvx[idx + 1]) / 2;
				y_avg[0] = (vvy[idx] + vvy[idx + 1]) / 2;
				idx = idx + 2;
				x_avg[1] = (vvx[idx] + vvx[idx + 1]) / 2;
				y_avg[1] = (vvy[idx] + vvy[idx + 1]) / 2;

				dy = (y_avg[1] - (yyy));
				dx = (x_avg[1] - (xxx));
				dist_1 = dy * dy + dx * dx;
				dy = (y_avg[0] - (yyy));
				dx = (x_avg[0] - (xxx));
				dist_0 = dy * dy + dx * dx;

				boolean cornerFirst = false;
				if (dist_1 > dist_0)
					cornerFirst = true;

				if (cornerFirst) {
					fillToNearCorner(xx, yy, xd, yd, 0, (byte) cases[kk], dir,
							vvx, vvy, nc, nr, crnr_color, crnrLevelIdx,
							crnr_out, grd_normals, closed, triStripBldr);
				} else {
					edge_points[0][pt] = vvx[s_idx];
					edge_points[1][pt] = vvy[s_idx];
					edge_points[0][n_pt] = vvx[ns_idx];
					edge_points[1][n_pt] = vvy[ns_idx];
					if (clr_idx[0] > cntr_clr)
						cntr_clr = clr_idx[0];
				}
				for (il = 1; il < nn; il++) {
					idx = dir * il * 2;
					int idx_last = idx - 2 * dir;

					vvv1[0] = vvx[idx];
					vvv1[1] = vvy[idx];
					vvv2[0] = vvx[idx + dir];
					vvv2[1] = vvy[idx + dir];
					vvv1_last[0] = vvx[idx_last];
					vvv1_last[1] = vvy[idx_last];
					vvv2_last[0] = vvx[idx_last + dir];
					vvv2_last[1] = vvy[idx_last + dir];

					fillToLast(xx, yy, xd, yd, nc, nr, vvv1, vvv2, vvv1_last,
							vvv2_last, (byte) cases[kk], color_bin,
							clr_idx[il], color_length, grd_normals, -1,
							triStripBldr);

					if (!cornerFirst && il == (nn - 1)) {
						fillToNearCorner(xx, yy, xd, yd, idx, (byte) cases[kk],
								dir, vvx, vvy, nc, nr, crnr_color,
								crnrLevelIdx, crnr_out, grd_normals, closed,
								triStripBldr);
					}
					if (cornerFirst && il == (nn - 1)) {
						edge_points[0][pt] = vvx[idx + s_idx];
						edge_points[1][pt] = vvy[idx + s_idx];
						edge_points[0][n_pt] = vvx[idx + ns_idx];
						edge_points[1][n_pt] = vvy[idx + ns_idx];
						if (clr_idx[il] > cntr_clr)
							cntr_clr = clr_idx[il];
					}
				}
			}
		}

		/*
		 * Now use edge_points to create the triangle strip to fill the
		 * remainder of the grid box, ie. the center region.
		 */

		if (n_crnr_out == 2) { // - don't fill center region
			return;
		}

		if (cntr_color == null) { // - All corners were closed off
			cntr_color = new byte[color_length];
			for (int c = 0; c < color_length; c++) {
				cntr_color[c] = color_bin[c][cntr_clr];
			}
		}

		byte first_tri_orient = 0;
		byte last_tri_orient = 0;
		byte first_strp_side = SIDE_NONE;
		byte last_strp_side = SIDE_NONE;

		float[][] normals = null;
		if (num_corners == 0) {
			tri = new float[2][8];
			normals = new float[3][8];
			tri[0][0] = edge_points[0][7];
			tri[1][0] = edge_points[1][7];
			tri[0][1] = edge_points[0][6];
			tri[1][1] = edge_points[1][6];
			tri[0][2] = edge_points[0][0];
			tri[1][2] = edge_points[1][0];
			tri[0][3] = edge_points[0][5];
			tri[1][3] = edge_points[1][5];
			tri[0][4] = edge_points[0][1];
			tri[1][4] = edge_points[1][1];
			tri[0][5] = edge_points[0][4];
			tri[1][5] = edge_points[1][4];
			tri[0][6] = edge_points[0][2];
			tri[1][6] = edge_points[1][2];
			tri[0][7] = edge_points[0][3];
			tri[1][7] = edge_points[1][3];

			first_strp_side = 3;
			last_strp_side = 1;
			first_tri_orient = -1;
			last_tri_orient = 1;

			interpNormals(tri[0], tri[1], xx, yy, nc, nr, xd, yd, grd_normals,
					normals);
			triStripBldr.addVerticies(cntr_clr, tri, normals, cntr_color,
					first_strp_side, first_tri_orient, last_strp_side,
					last_tri_orient);
		} else if (num_corners == 1) {
			tri = new float[2][7];
			normals = new float[3][7];
			if (which_corner[0] == 1) {
				tri[0][0] = edge_points[0][6];
				tri[1][0] = edge_points[1][6];
				tri[0][1] = edge_points[0][0];
				tri[1][1] = edge_points[1][0];
				tri[0][2] = edge_points[0][5];
				tri[1][2] = edge_points[1][5];
				tri[0][3] = edge_points[0][1];
				tri[1][3] = edge_points[1][1];
				tri[0][4] = edge_points[0][4];
				tri[1][4] = edge_points[1][4];
				tri[0][5] = edge_points[0][2];
				tri[1][5] = edge_points[1][2];
				tri[0][6] = edge_points[0][3];
				tri[1][6] = edge_points[1][3];
				first_strp_side = 3;
				last_strp_side = 1;
				first_tri_orient = 1;
				last_tri_orient = 1;
			} else if (which_corner[0] == 2) {
				tri[0][0] = edge_points[0][6];
				tri[1][0] = edge_points[1][6];
				tri[0][1] = edge_points[0][7];
				tri[1][1] = edge_points[1][7];
				tri[0][2] = edge_points[0][5];
				tri[1][2] = edge_points[1][5];
				tri[0][3] = edge_points[0][0];
				tri[1][3] = edge_points[1][0];
				tri[0][4] = edge_points[0][4];
				tri[1][4] = edge_points[1][4];
				tri[0][5] = edge_points[0][1];
				tri[1][5] = edge_points[1][1];
				tri[0][6] = edge_points[0][3];
				tri[1][6] = edge_points[1][3];
				first_strp_side = 3;
				last_strp_side = 1;
				first_tri_orient = 1;
				last_tri_orient = 1;
			} else if (which_corner[0] == 7) {
				tri[0][0] = edge_points[0][7];
				tri[1][0] = edge_points[1][7];
				tri[0][1] = edge_points[0][6];
				tri[1][1] = edge_points[1][6];
				tri[0][2] = edge_points[0][0];
				tri[1][2] = edge_points[1][0];
				tri[0][3] = edge_points[0][5];
				tri[1][3] = edge_points[1][5];
				tri[0][4] = edge_points[0][1];
				tri[1][4] = edge_points[1][1];
				tri[0][5] = edge_points[0][4];
				tri[1][5] = edge_points[1][4];
				tri[0][6] = edge_points[0][2];
				tri[1][6] = edge_points[1][2];
				first_strp_side = 3;
				last_strp_side = 1;
				first_tri_orient = -1;
				last_tri_orient = -1;
			} else if (which_corner[0] == 4) {
				tri[0][0] = edge_points[0][7];
				tri[1][0] = edge_points[1][7];
				tri[0][1] = edge_points[0][6];
				tri[1][1] = edge_points[1][6];
				tri[0][2] = edge_points[0][0];
				tri[1][2] = edge_points[1][0];
				tri[0][3] = edge_points[0][4];
				tri[1][3] = edge_points[1][4];
				tri[0][4] = edge_points[0][1];
				tri[1][4] = edge_points[1][1];
				tri[0][5] = edge_points[0][3];
				tri[1][5] = edge_points[1][3];
				tri[0][6] = edge_points[0][2];
				tri[1][6] = edge_points[1][2];
				first_strp_side = 3;
				last_strp_side = 1;
				first_tri_orient = -1;
				last_tri_orient = -1;
			}

			interpNormals(tri[0], tri[1], xx, yy, nc, nr, xd, yd, grd_normals,
					normals);
			triStripBldr.addVerticies(cntr_clr, tri, normals, cntr_color,
					first_strp_side, first_tri_orient, last_strp_side,
					last_tri_orient);
		} else if (num_corners == 2) {
			tri = new float[2][6];
			normals = new float[3][6];
			int flag = ((which_corner[0] == 1 && which_corner[1] == 7) || (which_corner[0] == 7 && which_corner[1] == 1)) ? 1
					: 4;
			if (flag == 4) {
				tri[0][0] = edge_points[0][6];
				tri[1][0] = edge_points[1][6];
				tri[0][1] = edge_points[0][7];
				tri[1][1] = edge_points[1][7];
				tri[0][2] = edge_points[0][4];
				tri[1][2] = edge_points[1][4];
				tri[0][3] = edge_points[0][0];
				tri[1][3] = edge_points[1][0];
				tri[0][4] = edge_points[0][3];
				tri[1][4] = edge_points[1][3];
				tri[0][5] = edge_points[0][1];
				tri[1][5] = edge_points[1][1];
				first_strp_side = 3;
				last_strp_side = 1;
				first_tri_orient = 1;
				last_tri_orient = -1;
			} else if (flag == 1) {
				tri[0][0] = edge_points[0][6];
				tri[1][0] = edge_points[1][6];
				tri[0][1] = edge_points[0][7];
				tri[1][1] = edge_points[1][7];
				tri[0][2] = edge_points[0][5];
				tri[1][2] = edge_points[1][5];
				tri[0][3] = edge_points[0][1];
				tri[1][3] = edge_points[1][1];
				tri[0][4] = edge_points[0][3];
				tri[1][4] = edge_points[1][3];
				tri[0][5] = edge_points[0][2];
				tri[1][5] = edge_points[1][2];
				first_strp_side = 3;
				last_strp_side = 1;
				first_tri_orient = 1;
				last_tri_orient = -1;
			}

			interpNormals(tri[0], tri[1], xx, yy, nc, nr, xd, yd, grd_normals,
					normals);
			triStripBldr.addVerticies(cntr_clr, tri, normals, cntr_color,
					first_strp_side, first_tri_orient, last_strp_side,
					last_tri_orient);
		}
	}

	/**
	 * 
	 * @param xx
	 * @param yy
	 * @param xd
	 * @param yd
	 * @param v_idx
	 * @param o_flag
	 * @param cnt
	 * @param dir
	 * @param vx
	 * @param vy
	 * @param nc
	 * @param nr
	 * @param crnr_color
	 * @param crnrLevelIdx
	 * @param crnr_out
	 * @param grd_normals
	 * @param closed
	 * @param triStripBldr
	 */

	private static void fillToNearCorner(float xx, float yy, float xd,
			float yd, int v_idx, byte o_flag, int dir, float[] vx, float[] vy,
			int nc, int nr, byte[][] crnr_color, int[] crnrLevelIdx,
			boolean[] crnr_out, float[][][] grd_normals, int[] closed,
			TriangleStripBuilder triStripBldr) {
		float cx = 0;
		float cy = 0;
		int cc = 0;

		int color_length = crnr_color[0].length;

		int vidx_0 = 0; // first segment index maps to this in outgoing strip
						// array
		int vidx_1 = 0; // next "                                       "
		int crn = 0; // corner index "                             "
		float[][] normals = new float[3][3];
		float[][] tri = new float[2][3];
		float[][] tmp = new float[3][1];
		byte[] color = new byte[color_length];

		byte first_tri_orient = 0;
		byte last_tri_orient = 0;
		byte first_strp_side = SIDE_NONE;
		byte last_strp_side = SIDE_NONE;

		switch (o_flag) {
		case 1:
			cc = 0;
			closed[0] = closed[0] | 1;
			if (crnr_out[cc])
				return;
			cx = xx;
			cy = yy;
			vidx_0 = 2;
			vidx_1 = 1;
			crn = 0;
			normals[0][crn] = grd_normals[nc][nr][0];
			normals[1][crn] = grd_normals[nc][nr][1];
			normals[2][crn] = grd_normals[nc][nr][2];
			first_tri_orient = -1;
			last_tri_orient = -1;
			first_strp_side = 3;
			last_strp_side = 0;
			break;
		case 4:
			cc = 2;
			closed[0] = closed[0] | 4;
			if (crnr_out[cc])
				return;
			cx = xx;
			cy = yy + yd;
			vidx_0 = 0;
			vidx_1 = 2;
			crn = 1;
			normals[0][crn] = grd_normals[nc + 1][nr][0];
			normals[1][crn] = grd_normals[nc + 1][nr][1];
			normals[2][crn] = grd_normals[nc + 1][nr][2];
			first_tri_orient = -1;
			last_tri_orient = -1;
			first_strp_side = 3;
			last_strp_side = 2;
			break;
		case 2:
			cc = 1;
			closed[0] = closed[0] | 2;
			if (crnr_out[cc])
				return;
			cx = xx + xd;
			cy = yy;
			vidx_0 = 0;
			vidx_1 = 2;
			crn = 1;
			normals[0][crn] = grd_normals[nc][nr + 1][0];
			normals[1][crn] = grd_normals[nc][nr + 1][1];
			normals[2][crn] = grd_normals[nc][nr + 1][2];
			first_tri_orient = 1;
			last_tri_orient = 1;
			first_strp_side = 0;
			last_strp_side = 1;
			break;
		case 7:
			cc = 3;
			closed[0] = closed[0] | 8;
			if (crnr_out[cc])
				return;
			cx = xx + xd;
			cy = yy + yd;
			vidx_0 = 2;
			vidx_1 = 0;
			crn = 1;
			normals[0][crn] = grd_normals[nc + 1][nr + 1][0];
			normals[1][crn] = grd_normals[nc + 1][nr + 1][1];
			normals[2][crn] = grd_normals[nc + 1][nr + 1][2];
			first_tri_orient = -1;
			last_tri_orient = -1;
			first_strp_side = 2;
			last_strp_side = 1;
			break;
		}

		for (int ii = 0; ii < color_length; ii++) {
			color[ii] = crnr_color[cc][ii];
		}
		int levIdx = crnrLevelIdx[cc];

		tri[0][crn] = cx;
		tri[1][crn] = cy;

		tri[0][vidx_0] = vx[v_idx];
		tri[1][vidx_0] = vy[v_idx];
		interpNormals(tri[0][vidx_0], tri[1][vidx_0], xx, yy, nc, nr, xd, yd,
				grd_normals, tmp);
		normals[0][vidx_0] = tmp[0][0];
		normals[1][vidx_0] = tmp[1][0];
		normals[2][vidx_0] = tmp[2][0];

		tri[0][vidx_1] = vx[v_idx + dir];
		tri[1][vidx_1] = vy[v_idx + dir];
		interpNormals(tri[0][vidx_1], tri[1][vidx_1], xx, yy, nc, nr, xd, yd,
				grd_normals, tmp);
		normals[0][vidx_1] = tmp[0][0];
		normals[1][vidx_1] = tmp[1][0];
		normals[2][vidx_1] = tmp[2][0];

		triStripBldr.addVerticies(levIdx, tri, normals, color, first_strp_side,
				first_tri_orient, last_strp_side, last_tri_orient);
	}

	/**
	 * 
	 * @param xx
	 * @param yy
	 * @param xd
	 * @param yd
	 * @param v_idx
	 * @param o_flag
	 * @param dir
	 * @param vx
	 * @param vy
	 * @param nc
	 * @param nr
	 * @param crnr_color
	 * @param crnrLevelIdx
	 * @param crnr_out
	 * @param grd_normals
	 * @param closed
	 * @param triStripBldr
	 */
	
	private static void fillToOppCorner(float xx, float yy, float xd, float yd,
			int v_idx, byte o_flag, int dir, float[] vx, float[] vy, int nc,
			int nr, byte[][] crnr_color, int[] crnrLevelIdx,
			boolean[] crnr_out, float[][][] grd_normals, int[] closed,
			TriangleStripBuilder triStripBldr) {

		float cx1 = 0;
		float cx2 = 0;
		float cx3 = 0;
		float cy1 = 0;
		float cy2 = 0;
		float cy3 = 0;
		int cc = 0;
		int[][] grd = new int[3][2];
		int color_length = crnr_color[0].length;

		float[][] tri = new float[2][5];
		float[][] normals = new float[3][5];
		float[][] tmp = new float[3][1];
		byte[] color = new byte[color_length];

		int vidx_0 = 0; // first segment index maps to this in outgoing strip
						// array
		int vidx_1 = 0; // next "                                       "
		int crn_1 = 0; // corner's index into outgoing strip array
		int crn_2 = 0; // "                "
		int crn_3 = 0; // "              "

		byte first_tri_orient = 0;
		byte last_tri_orient = 0;
		byte first_strp_side = SIDE_NONE;
		byte last_strp_side = SIDE_NONE;

		switch (o_flag) {
		case 1:
			closed[0] = closed[0] | 14;
			if (crnr_out[1] || crnr_out[2] || crnr_out[3])
				return;
			cx1 = xx + xd;
			cy1 = yy;
			cx2 = xx + xd;
			cy2 = yy + yd;
			cx3 = xx;
			cy3 = yy + yd;
			cc = 3;
			grd[0][0] = 1;
			grd[0][1] = 0;
			grd[1][0] = 1;
			grd[1][1] = 1;
			grd[2][0] = 0;
			grd[2][1] = 1;
			vidx_0 = 2;
			vidx_1 = 0;
			crn_1 = 4;
			crn_2 = 3;
			crn_3 = 1;
			first_tri_orient = -1;
			last_tri_orient = -1;
			first_strp_side = 3;
			last_strp_side = 1;
			break;
		case 2:
			closed[0] = closed[0] | 13;
			if (crnr_out[0] || crnr_out[2] || crnr_out[3])
				return;
			cx1 = xx;
			cy1 = yy;
			cx2 = xx;
			cy2 = yy + yd;
			cx3 = xx + xd;
			cy3 = yy + yd;
			cc = 2;
			grd[0][0] = 0;
			grd[0][1] = 0;
			grd[1][0] = 0;
			grd[1][1] = 1;
			grd[2][0] = 1;
			grd[2][1] = 1;
			vidx_0 = 2;
			vidx_1 = 4;
			crn_1 = 0;
			crn_2 = 1;
			crn_3 = 3;
			first_tri_orient = -1;
			last_tri_orient = -1;
			first_strp_side = 3;
			last_strp_side = 1;
			break;
		case 4:
			closed[0] = closed[0] | 11;
			if (crnr_out[0] || crnr_out[1] || crnr_out[3])
				return;
			cx1 = xx;
			cy1 = yy;
			cx2 = xx + xd;
			cy2 = yy;
			cx3 = xx + xd;
			cy3 = yy + yd;
			cc = 1;
			grd[0][0] = 0;
			grd[0][1] = 0;
			grd[1][0] = 1;
			grd[1][1] = 0;
			grd[2][0] = 1;
			grd[2][1] = 1;
			vidx_0 = 1;
			vidx_1 = 3;
			crn_1 = 0;
			crn_2 = 2;
			crn_3 = 4;
			first_tri_orient = -1;
			last_tri_orient = -1;
			first_strp_side = 3;
			last_strp_side = 2;
			break;
		case 7:
			closed[0] = closed[0] | 7;
			if (crnr_out[0] || crnr_out[1] || crnr_out[2])
				return;
			cx1 = xx + xd;
			cy1 = yy;
			cx2 = xx;
			cy2 = yy;
			cx3 = xx;
			cy3 = yy + yd;
			cc = 0;
			grd[0][0] = 1;
			grd[0][1] = 0;
			grd[1][0] = 0;
			grd[1][1] = 0;
			grd[2][0] = 0;
			grd[2][1] = 1;
			vidx_0 = 4;
			vidx_1 = 3;
			crn_1 = 2;
			crn_2 = 0;
			crn_3 = 1;
			first_tri_orient = -1;
			last_tri_orient = -1;
			first_strp_side = 3;
			last_strp_side = 1;
			break;
		}

		for (int ii = 0; ii < color_length; ii++) {
			color[ii] = crnr_color[cc][ii];
		}
		int levIdx = crnrLevelIdx[cc];

		tri[0][crn_1] = cx1;
		tri[1][crn_1] = cy1;
		normals[0][crn_1] = grd_normals[nc + grd[0][1]][nr + grd[0][0]][0];
		normals[1][crn_1] = grd_normals[nc + grd[0][1]][nr + grd[0][0]][1];
		normals[2][crn_1] = grd_normals[nc + grd[0][1]][nr + grd[0][0]][2];

		tri[0][crn_2] = cx2;
		tri[1][crn_2] = cy2;
		normals[0][crn_2] = grd_normals[nc + grd[1][1]][nr + grd[1][0]][0];
		normals[1][crn_2] = grd_normals[nc + grd[1][1]][nr + grd[1][0]][1];
		normals[2][crn_2] = grd_normals[nc + grd[1][1]][nr + grd[1][0]][2];

		tri[0][crn_3] = cx3;
		tri[1][crn_3] = cy3;
		normals[0][crn_3] = grd_normals[nc + grd[2][1]][nr + grd[2][0]][0];
		normals[1][crn_3] = grd_normals[nc + grd[2][1]][nr + grd[2][0]][1];
		normals[2][crn_3] = grd_normals[nc + grd[2][1]][nr + grd[2][0]][2];

		tri[0][vidx_0] = vx[v_idx];
		tri[1][vidx_0] = vy[v_idx];
		interpNormals(tri[0][vidx_0], tri[1][vidx_0], xx, yy, nc, nr, xd, yd,
				grd_normals, tmp);
		normals[0][vidx_0] = tmp[0][0];
		normals[1][vidx_0] = tmp[1][0];
		normals[2][vidx_0] = tmp[2][0];

		tri[0][vidx_1] = vx[v_idx + dir];
		tri[1][vidx_1] = vy[v_idx + dir];
		interpNormals(tri[0][vidx_1], tri[1][vidx_1], xx, yy, nc, nr, xd, yd,
				grd_normals, tmp);
		normals[0][vidx_1] = tmp[0][0];
		normals[1][vidx_1] = tmp[1][0];
		normals[2][vidx_1] = tmp[2][0];

		triStripBldr.addVerticies(levIdx, tri, normals, color, first_strp_side,
				first_tri_orient, last_strp_side, last_tri_orient);
	}

	/**
	 * 
	 * @param xx
	 * @param yy
	 * @param xd
	 * @param yd
	 * @param v_idx
	 * @param o_flag
	 * @param flag
	 * @param cnt
	 * @param dir
	 * @param vx
	 * @param vy
	 * @param nc
	 * @param nr
	 * @param crnr_color
	 * @param crnrLevelIdx
	 * @param crnr_out
	 * @param grd_normals
	 * @param closed
	 * @param triStripBldr
	 */

	private static void fillToSide(float xx, float yy, float xd, float yd,
			int v_idx, byte o_flag, int flag, int dir, float[] vx, float[] vy,
			int nc, int nr, byte[][] crnr_color, int[] crnrLevelIdx,
			boolean[] crnr_out, float[][][] grd_normals, int[] closed,
			TriangleStripBuilder triStripBldr) {

		float[][] tri = new float[2][4];
		float[][] normals = new float[3][4];

		fillToSide(xx, yy, xd, yd, v_idx, o_flag, flag, dir, vx, vy, nc, nr,
				crnr_color, crnrLevelIdx, crnr_out, grd_normals, closed, tri,
				normals, triStripBldr);

	}

	private static void fillToSide(float xx, float yy, float xd, float yd,
			int v_idx, byte o_flag, int flag, int dir, float[] vx, float[] vy,
			int nc, int nr, byte[][] crnr_color, int[] crnrLevelIdx,
			boolean[] crnr_out, float[][][] grd_normals, int[] closed,
			float[][] strpverts, float[][] strpnrmls,
			TriangleStripBuilder triStripBldr) {

		float cx1 = 0;
		float cy1 = 0;
		float cx2 = 0;
		float cy2 = 0;
		int cc = 0;
		int[][] grd = new int[2][2];
		int color_length = crnr_color[0].length;

		float[][] tri = new float[2][4];
		float[][] normals = new float[3][4];
		float[][] tmp = new float[3][1];
		byte[] color = new byte[color_length];
		int vidx_0 = 0;
		int vidx_1 = 0;
		int crn_1 = 0;
		int crn_2 = 0;

		byte first_tri_orient = 0;
		byte last_tri_orient = 0;
		byte first_strp_side = SIDE_NONE;
		byte last_strp_side = SIDE_NONE;

		switch (o_flag) {
		case 3:
			switch (flag) {
			case 1:
				closed[0] = closed[0] | 12;
				if (crnr_out[2] || crnr_out[3])
					return;
				cx1 = xx;
				cy1 = yy + yd;
				cx2 = xx + xd;
				cy2 = yy + yd;
				cc = 3;
				grd[0][0] = 0;
				grd[0][1] = 1;
				grd[1][0] = 1;
				grd[1][1] = 1;
				crn_1 = 1;
				crn_2 = 3;
				vidx_0 = 0;
				vidx_1 = 2;
				first_tri_orient = -1;
				last_tri_orient = 1;
				first_strp_side = 3;
				last_strp_side = 1;
				break;
			case -1:
				closed[0] = closed[0] | 3;
				if (crnr_out[0] || crnr_out[1])
					return;
				cx1 = xx;
				cy1 = yy;
				cx2 = xx + xd;
				cy2 = yy;
				cc = 0;
				grd[0][0] = 0;
				grd[0][1] = 0;
				grd[1][0] = 1;
				grd[1][1] = 0;
				crn_1 = 0;
				crn_2 = 2;
				vidx_0 = 1;
				vidx_1 = 3;
				first_tri_orient = -1;
				last_tri_orient = 1;
				first_strp_side = 3;
				last_strp_side = 1;
				break;
			}
			break;

		case 5:
			switch (flag) {
			case 1:
				closed[0] = closed[0] | 5;
				if (crnr_out[0] || crnr_out[2])
					return;
				cx1 = xx;
				cy1 = yy;
				cx2 = xx;
				cy2 = yy + yd;
				cc = 0;
				grd[0][0] = 0;
				grd[0][1] = 0;
				grd[1][0] = 0;
				grd[1][1] = 1;
				crn_1 = 0;
				crn_2 = 1;
				vidx_0 = 2;
				vidx_1 = 3;
				first_tri_orient = -1;
				last_tri_orient = 1;
				first_strp_side = 3;
				last_strp_side = -1;
				break;
			case -1:
				closed[0] = closed[0] | 10;
				if (crnr_out[1] || crnr_out[3])
					return;
				cx1 = xx + xd;
				cy1 = yy;
				cx2 = xx + xd;
				cy2 = yy + yd;
				grd[0][0] = 1;
				grd[0][1] = 0;
				grd[1][0] = 1;
				grd[1][1] = 1;
				cc = 3;
				crn_1 = 2;
				crn_2 = 3;
				vidx_0 = 0;
				vidx_1 = 1;
				first_tri_orient = -1;
				last_tri_orient = 1;
				first_strp_side = -1;
				last_strp_side = 1;
				break;
			}
			break;
		}

		for (int ii = 0; ii < color_length; ii++) {
			color[ii] = crnr_color[cc][ii];
		}
		int levIdx = crnrLevelIdx[cc];

		tri[0][crn_1] = cx1;
		tri[1][crn_1] = cy1;
		int i = grd[0][0];
		int j = grd[0][1];
		normals[0][crn_1] = grd_normals[nc + j][nr + i][0];
		normals[1][crn_1] = grd_normals[nc + j][nr + i][1];
		normals[2][crn_1] = grd_normals[nc + j][nr + i][2];

		tri[0][crn_2] = cx2;
		tri[1][crn_2] = cy2;
		i = grd[1][0];
		j = grd[1][1];
		normals[0][crn_2] = grd_normals[nc + j][nr + i][0];
		normals[1][crn_2] = grd_normals[nc + j][nr + i][1];
		normals[2][crn_2] = grd_normals[nc + j][nr + i][2];

		tri[0][vidx_0] = vx[v_idx];
		tri[1][vidx_0] = vy[v_idx];
		interpNormals(tri[0][vidx_0], tri[1][vidx_0], xx, yy, nc, nr, xd, yd,
				grd_normals, tmp);
		normals[0][vidx_0] = tmp[0][0];
		normals[1][vidx_0] = tmp[1][0];
		normals[2][vidx_0] = tmp[2][0];

		tri[0][vidx_1] = vx[v_idx + dir];
		tri[1][vidx_1] = vy[v_idx + dir];
		interpNormals(tri[0][vidx_1], tri[1][vidx_1], xx, yy, nc, nr, xd, yd,
				grd_normals, tmp);
		normals[0][vidx_1] = tmp[0][0];
		normals[1][vidx_1] = tmp[1][0];
		normals[2][vidx_1] = tmp[2][0];

		if (triStripBldr != null) {
			triStripBldr.addVerticies(levIdx, tri, normals, color,
					first_strp_side, first_tri_orient, last_strp_side,
					last_tri_orient);
		}
	}

	private static void addCorner(float xx, float yy, float xd, float yd,
			int nc, int nr, byte cornerID, float[][][] grd_normals,
			int[] closed, int strpIdx, float[][] strpverts, float[][] strpnrmls) {

		float cx = 0;
		float cy = 0;
		int i = 0; // int offsets for corners
		int j = 0;

		if (cornerID == 0) {
			closed[0] = closed[0] | 1;
			cx = xx;
			cy = yy;
			i = 0;
			j = 0;
		} else if (cornerID == 1) {
			closed[0] = closed[0] | 2;
			cx = xx + xd;
			cy = yy;
			i = 1;
			j = 0;
		} else if (cornerID == 2) {
			closed[0] = closed[0] | 4;
			cx = xx;
			cy = yy + yd;
			i = 0;
			j = 1;
		} else if (cornerID == 3) {
			closed[0] = closed[0] | 8;
			cx = xx + xd;
			cy = yy + yd;
			i = 1;
			j = 1;
		}

		strpverts[0][strpIdx] = cx;
		strpverts[1][strpIdx] = cy;
		strpnrmls[0][strpIdx] = grd_normals[nc + j][nr + i][0];
		strpnrmls[1][strpIdx] = grd_normals[nc + j][nr + i][1];
		strpnrmls[2][strpIdx] = grd_normals[nc + j][nr + i][2];
	}

	public static int[] getTriOrientation(float[][] verts) {
		/* note: doesn't deal with cross-product == 0 */
		int len = verts[0].length;
		float xa = verts[0][1] - verts[0][0];
		float ya = verts[1][1] - verts[1][0];
		float xb = verts[0][2] - verts[0][0];
		float yb = verts[1][2] - verts[1][0];

		float first = xa * yb - xb * ya;

		xa = verts[0][len - 2] - verts[0][len - 3];
		ya = verts[1][len - 2] - verts[1][len - 3];
		xb = verts[0][len - 1] - verts[0][len - 3];
		yb = verts[1][len - 1] - verts[1][len - 3];

		float last = xa * yb - xb * ya;

		int firstOrient = (first < 0) ? CLOCKWISE : CNTRCLOCKWISE;
		int lastOrient = (last < 0) ? CLOCKWISE : CNTRCLOCKWISE;

		return new int[] { firstOrient, lastOrient };
	}

	static final class ContourOutput {

		public final ContourStripSet stripSet;
		public final TriangleStripBuilder triStripBldr;

		ContourOutput(ContourStripSet set, TriangleStripBuilder tsb) {
			stripSet = set;
			triStripBldr = tsb;
		}

		boolean isLineStyled(int lvl) {
			return stripSet.isLevelStyled(lvl);
		}

		List<float[][][]> getLineStripCoordinates(int lvl) {
			return stripSet.getLineStripCoordinates(lvl);
		}

		List<byte[][][]> getLineStripColors(int lvl) {
			return stripSet.getLineStripColors(lvl);
		}

		int getIntervalCount() {
			return stripSet.vecArray.length;
		}

		List<ContourStrip> getStrips(int lvl) {
			return stripSet.vecArray[lvl];
		}

		int[] getLabelIndexes(int lvlIdx) {
			return stripSet.labelIndexes[lvlIdx];
		}

		float[] getLevels() {
			return stripSet.levels;
		}
	}
} // end class

/**
 * Class ContourQuadSet
 * 
 */

class ContourQuadSet {

	/**           */
	int nx = 1;

	/**           */
	int ny = 1;

	/**           */
	int npx;

	/**           */
	int npy;

	/**           */
	int nc;

	/**           */
	int nr;

	/**           */
	int lev_idx;

	/**           */
	int numv = 0;

	/**           */
	ContourStripSet css = null;

	/**           */
	ContourQuad[][] qarray = null;

	/**           */
	int snumv = 0;

	/**           */
	public Map<CachedArrayDimension, CachedArray> subGridMap = new HashMap<CachedArrayDimension, CachedArray>();

	/**           */
	public Map<CachedArrayDimension, CachedArray> subGrid2Map = new HashMap<CachedArrayDimension, CachedArray>();

	/**           */
	public Map<CachedArrayDimension, CachedArray> markGridMap = new HashMap<CachedArrayDimension, CachedArray>();

	/**           */
	public Map<CachedArrayDimension, CachedArray> markGrid2Map = new HashMap<CachedArrayDimension, CachedArray>();

	/**
	 * 
	 * @param nr
	 * @param nc
	 * @param lev_idx
	 * @param css
	 */
	
	ContourQuadSet(int nr, int nc, int lev_idx, ContourStripSet css) {
		this.nc = nc;
		this.nr = nr;
		this.lev_idx = lev_idx;
		this.css = css;

		npy = (int) (nr / ny);
		npx = (int) (nc / nx);

		qarray = new ContourQuad[ny][nx];
		// JDM: Only make the ContourQuad objects when we need them
		/*
		 * for (int j=0; j<ny; j++) { for (int i=0; i<nx; i++) { qarray[j][i] =
		 * makeContourQuad(j,i); } }
		 */
	}

	/**
	 * 
	 * @param j
	 * @param i
	 * 
	 * @return
	 */
	
	private ContourQuad makeContourQuad(int j, int i) {
		int lenx = npx;
		int leny = npy;
		if (j == ny - 1)
			leny = nr - (ny - 1) * npy;
		if (i == nx - 1)
			lenx = nc - (nx - 1) * npx;
		return new ContourQuad(this, j * npy, i * npx, leny, lenx);
	}

	/**
	 * 
	 * @param idx0
	 * @param ir
	 * @param ic
	 */
	
	public void add(int idx0, int ir, int ic) {
		int ix = (int) (ic / npx);
		int iy = (int) (ir / npy);
		if (ix >= nx)
			ix = nx - 1;
		if (iy >= ny)
			iy = ny - 1;
		if (qarray[iy][ix] == null) {
			qarray[iy][ix] = makeContourQuad(iy, ix);
		}
		qarray[iy][ix].add(idx0, ir, ic);
	}

	/**
	 * 
	 * @param vx
	 * @param vy
	 */
	
	public void get(float[] vx, float[] vy) {
		numv = 0;
		snumv = 0;
		for (int j = 0; j < ny; j++) {
			for (int i = 0; i < nx; i++) {
				if (qarray[j][i] == null)
					continue;
				ContourStrip[] c_strps = qarray[j][i].getContourStrips(vx, vy);
				numv += qarray[j][i].numv;
				snumv += qarray[j][i].stripCnt;
				if (c_strps != null) {
					css.vecArray[lev_idx].add(c_strps[0]);
				}
			}
		}
	}

	/**
	 * 
	 * @param vx
	 * @param vy
	 * @param auxLevels
	 * @param vx1
	 * @param vy1
	 * @param vz1
	 * @param colors
	 * @param spatial_set
	 * 
	 * @throws VisADException
	 */
	
	public void getArrays(float[] vx, float[] vy, byte[][] auxLevels,
			float[][] vx1, float[][] vy1, float[][] vz1, byte[][] colors,
			Gridded3DSet spatial_set) throws VisADException {

		float[][] arrays = new float[2][2 * numv];
		int clr_dim = 3;
		if (auxLevels != null) {
			clr_dim = auxLevels.length;
			colors[0] = new byte[2 * numv];
			colors[1] = new byte[2 * numv];
			colors[2] = new byte[2 * numv];
			colors[3] = new byte[2 * numv];
		}

		int cnt = 0;
		for (int j = 0; j < ny; j++) {
			for (int i = 0; i < nx; i++) {
				if (qarray[j][i] == null)
					continue;
				for (int k = 0; k < qarray[j][i].numv; k++) {
					int vidx = qarray[j][i].vert_indices[k];
					if (vidx >= 0) {
						arrays[0][cnt] = vx[vidx];
						arrays[1][cnt] = vy[vidx];
						if (auxLevels != null) {
							colors[0][cnt] = auxLevels[0][vidx];
							colors[1][cnt] = auxLevels[1][vidx];
							colors[2][cnt] = auxLevels[2][vidx];
							if (clr_dim == 4)
								colors[3][cnt] = auxLevels[3][vidx];
						}
						cnt++;
						arrays[0][cnt] = vx[vidx + 1];
						arrays[1][cnt] = vy[vidx + 1];
						if (auxLevels != null) {
							colors[0][cnt] = auxLevels[0][vidx + 1];
							colors[1][cnt] = auxLevels[1][vidx + 1];
							colors[2][cnt] = auxLevels[2][vidx + 1];
							if (clr_dim == 4)
								colors[3][cnt] = auxLevels[3][vidx];
						}
						cnt++;
					}
				}
			}
		}

		float[][] tmp = new float[2][cnt];
		System.arraycopy(arrays[0], 0, tmp[0], 0, cnt);
		System.arraycopy(arrays[1], 0, tmp[1], 0, cnt);

		float[][] tmp3D = spatial_set.gridToValue(tmp);
		vx1[0] = tmp3D[0];
		vy1[0] = tmp3D[1];
		vz1[0] = tmp3D[2];
		arrays = null;
		colors = null;
	}
}

/**
 * Class ContourQuad
 * 
 */

class ContourQuad {

	/**           */
	int[] vert_indices;

	/**           */
	int[] vert_indices_save = null;

	/**           */
	int[] grid_indices;

	/**           */
	int maxnumv;

	/**           */
	int numv;

	/**           */
	ContourQuadSet qs;

	/**           */
	int nc;

	/**           */
	int nr;

	/**           */
	int strty;

	/**           */
	int strtx;

	/**           */
	int leny;

	/**           */
	int lenx;

	/**           */
	int lev_idx;

	/**           */
	int[][] sub_grid = null;

	/**           */
	int[][] sub_grid_2 = null;

	/**           */
	int[][] mark_grid = null;

	/**           */
	int[][] mark_grid_2 = null;

	/**           */
	ContourStripSet css = null;

	/**           */
	int[] stripVert_indices;

	/**           */
	int stripCnt = 0;

	/**
	 * 
	 * @param qs
	 * @param strty
	 * @param strtx
	 * @param leny
	 * @param lenx
	 */
	
	ContourQuad(ContourQuadSet qs, int strty, int strtx, int leny, int lenx) {
		maxnumv = 100;
		numv = 0;
		vert_indices = new int[maxnumv]; // - indices into vx,vy
		grid_indices = new int[maxnumv]; // - location on the grid
		this.qs = qs;
		this.nc = qs.nc;
		this.nr = qs.nr;
		this.strty = strty;
		this.strtx = strtx;
		this.leny = leny;
		this.lenx = lenx;
		css = qs.css;
		lev_idx = qs.lev_idx;
	}

	/**
	 * 
	 * @param idx0
	 * @param gy
	 * @param gx
	 */
	
	public void add(int idx0, int gy, int gx) {
		if (numv < maxnumv - 2) {
			vert_indices[numv] = idx0;
			grid_indices[numv] = gy * nc + gx;
			numv++;
		} else {
			maxnumv += 50;
			int[] tmpA = vert_indices;
			int[] tmpB = grid_indices;
			vert_indices = new int[maxnumv];
			grid_indices = new int[maxnumv];
			System.arraycopy(tmpA, 0, vert_indices, 0, numv);
			System.arraycopy(tmpB, 0, grid_indices, 0, numv);
			tmpA = null;
			tmpB = null;
			vert_indices[numv] = idx0;
			grid_indices[numv] = gy * nc + gx;
			numv++;
		}
	}

	/**
	 * 
	 * @param leny
	 * @param lenx
	 * 
	 * @return
	 */
	
	public int[][][] getWorkArrays(int leny, int lenx) {
		Object key;

		java.util.Set<CachedArrayDimension> keySet = qs.subGridMap.keySet();

		key = null;
		for (CachedArrayDimension obj : keySet) {
			if (obj.equals(new CachedArrayDimension(leny, lenx))) {
				key = obj;
				break;
			}
		}

		int[][] subgrid = null;
		int[][] subgrid2 = null;
		int[][] markgrid = null;
		int[][] markgrid2 = null;

		if (key != null) {
			subgrid = ((CachedArray) qs.subGridMap.get(key)).getArray();
			subgrid2 = ((CachedArray) qs.subGrid2Map.get(key)).getArray();
			markgrid = ((CachedArray) qs.markGridMap.get(key)).getArray();
			markgrid2 = ((CachedArray) qs.markGrid2Map.get(key)).getArray();
		} else {
			subgrid = new int[leny][lenx];
			subgrid2 = new int[leny][lenx];
			markgrid = new int[leny][lenx];
			markgrid2 = new int[leny][lenx];
			CachedArrayDimension newKey = new CachedArrayDimension(leny, lenx);
			qs.subGridMap.put(newKey, new CachedArray(subgrid));
			qs.subGrid2Map.put(newKey, new CachedArray(subgrid2));
			qs.markGridMap.put(newKey, new CachedArray(markgrid));
			qs.markGrid2Map.put(newKey, new CachedArray(markgrid2));
		}
		return new int[][][] { subgrid, subgrid2, markgrid, markgrid2 };
	}

	/**
	 *
	 */
	
	public void get() {
		int ix_i = -1;
		int iy_i = -1;
		int[][][] workarrays = getWorkArrays(leny, lenx);
		sub_grid = workarrays[0];
		sub_grid_2 = workarrays[1];
		mark_grid = workarrays[2];
		mark_grid_2 = workarrays[3];

		for (int j = 0; j < leny; j++) {
			for (int i = 0; i < lenx; i++) {
				sub_grid[j][i] = 0;
				sub_grid_2[j][i] = 0;
			}
		}

		if (vert_indices_save == null) {
			vert_indices_save = new int[vert_indices.length];
			System.arraycopy(vert_indices, 0, vert_indices_save, 0,
					vert_indices.length);
		} else {
			System.arraycopy(vert_indices_save, 0, vert_indices, 0,
					vert_indices.length);
		}

		for (int ii = 0; ii < numv; ii++) {
			int kk = grid_indices[ii];
			int iy = (int) kk / nc;
			int ix = kk - iy * nc;
			sub_grid[iy - strty][ix - strtx] = vert_indices[ii];
			mark_grid[iy - strty][ix - strtx] = ii;
			if (ix_i == ix && iy_i == iy) {
				sub_grid_2[iy - strty][ix - strtx] = vert_indices[ii];
				mark_grid_2[iy - strty][ix - strtx] = ii;
			}
			ix_i = ix;
			iy_i = iy;
		}
	}

	/**
	 *
	 */
	
	public void reset() {
		for (int j = 0; j < leny; j++) {
			for (int i = 0; i < lenx; i++) {
				if (sub_grid[j][i] < 0) {
					sub_grid[j][i] *= -1;
				}
			}
		}
	}

	/**
	 * 
	 * @param vx
	 * @param vy
	 * 
	 * @return
	 */
	
	public ContourStrip[] getContourStrips(float[] vx, float[] vy) {

		get();
		stripVert_indices = new int[200];
		int n_segs = 0;
		int[][] udrl = { { 1, -1, 0, 0 }, { 0, 0, 1, -1 } }; // -up/down,
																// right/left

		// - find starting point
		int[] start = getStartPoint();
		if (start == null) {
			return null;
		}
		int iy = start[0];
		int ix = start[1];

		int idx0 = sub_grid[iy][ix];
		int idx1 = idx0 + 1;

		ContourStrip c_strp = new ContourStrip(lev_idx, idx0, idx1, css);

		int idxA = idx0;
		int idxB = idx0;
		int idxA_2;
		int idxB_2;

		int ix_t, iy_t;
		int ix_a = ix;
		int iy_a = iy;
		int ix_b = ix;
		int iy_b = iy;

		int cnt = 0;
		stripVert_indices[cnt++] = idx0;
		sub_grid[iy][ix] *= -1;

		int test_cnt = 0;
		while ((n_segs < 100) && (test_cnt < 300)) {
			test_cnt++;

			// - A
			for (int k = 0; k < 4; k++) {
				ix_t = ix_a + udrl[0][k];
				iy_t = iy_a + udrl[1][k];
				if ((iy_t >= 0 && iy_t < leny) && (ix_t >= 0 && ix_t < lenx)) {

					idxA = sub_grid[iy_t][ix_t];
					idxA_2 = sub_grid_2[iy_t][ix_t];

					if (idxA > 0) {
						if (c_strp.addPair(vx, vy, idxA, idxA + 1)) {
							sub_grid[iy_t][ix_t] *= -1;
							stripVert_indices[cnt++] = idxA;
							ix_a = ix_t;
							iy_a = iy_t;
							vert_indices[mark_grid[iy_t][ix_t]] = -1;
							n_segs++;
							break;
						}
					}
					if (idxA_2 > 0) {
						if (c_strp.addPair(vx, vy, idxA_2, idxA_2 + 1)) {
							sub_grid_2[iy_t][ix_t] *= -1;
							stripVert_indices[cnt++] = idxA_2;
							ix_a = ix_t;
							iy_a = iy_t;
							vert_indices[mark_grid_2[iy_t][ix_t]] = -1;
							n_segs++;
							break;
						}
					}
				}
			}

			// - B
			for (int k = 0; k < 4; k++) {
				ix_t = ix_b + udrl[0][k];
				iy_t = iy_b + udrl[1][k];
				if ((iy_t >= 0 && iy_t < leny) && (ix_t >= 0 && ix_t < lenx)) {

					idxB = sub_grid[iy_t][ix_t];
					idxB_2 = sub_grid_2[iy_t][ix_t];

					if (idxB > 0) {
						if (c_strp.addPair(vx, vy, idxB, idxB + 1)) {
							sub_grid[iy_t][ix_t] *= -1;
							stripVert_indices[cnt++] = idxB;
							ix_b = ix_t;
							iy_b = iy_t;
							vert_indices[mark_grid[iy_t][ix_t]] = -1;
							n_segs++;
							break;
						}
					}
					if (idxB_2 > 0) {
						if (c_strp.addPair(vx, vy, idxB_2, idxB_2 + 1)) {
							sub_grid_2[iy_t][ix_t] *= -1;
							stripVert_indices[cnt++] = idxB_2;
							ix_b = ix_t;
							iy_b = iy_t;
							vert_indices[mark_grid_2[iy_t][ix_t]] = -1;
							n_segs++;
							break;
						}
					}
				}
			}
		}
		stripCnt = cnt;

		sub_grid = null;
		sub_grid_2 = null;
		return new ContourStrip[] { c_strp };

	}

	/**
	 * getStartPoint
	 * 
	 * @return Starting point as two-element int array
	 */
	
	public int[] getStartPoint() {
		int n_trys = 20;
		float nn = (float) lenx * leny;
		java.util.Random rnd = new java.util.Random();

		for (int tt = 0; tt < n_trys; tt++) {
			int kk = (int) (nn * rnd.nextFloat());
			int j = (int) kk / lenx;
			int i = kk - j * lenx;
			if (sub_grid[j][i] != 0) {
				return new int[] { j, i };
			}
		}
		return null;
	}
}

/**
 * Class CachedArray
 * 
 */

class CachedArray {

	/**           */
	int[][] array;

	/**
	 * 
	 * 
	 * @param array
	 */
	public CachedArray(int[][] array) {
		this.array = array;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	int[][] getArray() {
		return array;
	}
}

/**
 * Class CachedArrayDimension
 * 
 */

class CachedArrayDimension {

	/**           */
	int lenx;

	/**           */
	int leny;

	/**
	 * 
	 * 
	 * @param leny
	 * @param lenx
	 */
	CachedArrayDimension(int leny, int lenx) {
		this.leny = leny;
		this.lenx = lenx;
	}

	/**
	 * 
	 * 
	 * @param obj
	 * 
	 * @return
	 */
	public boolean equals(CachedArrayDimension obj) {
		return (lenx == obj.lenx && leny == obj.leny);
	}
}

/**
 * ContourStripSet is used internally by Contour2D
 */

class ContourStripSet {

	// value for dash algm.

	/**           */
	static final int DEFAULT_DASH_VALUE = 2;

	/**           */
	static final int DISABLE_DASH_VALUE = -1;

	/**           */
	float[] levels;

	int[][] labelIndexes;
	
	int labelFreq = ContourControl.LABEL_FREQ_LO;

	/**           */
	int n_levs;

	/**           */
	int nr;

	/**           */
	int nc;

	/**           */
	Gridded3DSet spatial_set;

	/** Contour strips by level. */
	List<ContourStrip>[] vecArray;

	/**           */
	List<ContourStrip> vec;

        /** Closed strips by level. */
	List<ContourStrip>[] closedStripArray;

        List<ContourStrip> closedStripList;

	/**           */
	boolean[] swap;

	/**           */
	ContourQuadSet[] qSet;

	/** Grid X coordinates. */
	private float[] gridX;
	/** Grid Y coordinates. */
	private float[] gridY;
	/** Colors corresponding to grid values. */
	private byte[][] gridColors;

	ArrayList<ContourLabelGeometry> labels = new ArrayList<ContourLabelGeometry>();
	ArrayList<VisADLineStripArray> fillLines = new ArrayList<VisADLineStripArray>();
	ArrayList<VisADLineStripArray> fillLinesStyled = new ArrayList<VisADLineStripArray>();
	ArrayList<VisADLineStripArray> cntrLines = new ArrayList<VisADLineStripArray>();
	ArrayList<VisADLineStripArray> cntrLinesStyled = new ArrayList<VisADLineStripArray>();

	double labelScale;

	public int labelLineSkip = ContourControl.EVERY_NTH_DEFAULT;

	/**
	 * 
	 * @param levels
	 * @param swap
	 * @param scale_ratio
	 * @param label_size
	 * @param nr
	 * @param nc
	 * @param spatial_set
	 * @param contourDifficulty
	 * 
	 * @throws VisADException
	 */

	ContourStripSet(float[] levels, boolean[] swap, double scale_ratio,
			int label_freq, int label_line_skip, double label_size, int nr, int nc,
			Gridded3DSet spatial_set) throws VisADException {

		this.levels = levels;
		n_levs = levels.length;
		labelIndexes = new int[n_levs][];
		
		labelFreq = label_freq;
		labelLineSkip = label_line_skip;
		
		vecArray = new List[n_levs];
                closedStripArray = new List[n_levs];
		labelScale = ((0.062 * (1.0 / scale_ratio)) * label_size);
		this.nr = nr;
		this.nc = nc;
		this.swap = swap;
		this.spatial_set = spatial_set;

		for (int kk = 0; kk < n_levs; kk++) {
			vecArray[kk] = new ArrayList<ContourStrip>();
			closedStripArray[kk] = new ArrayList<ContourStrip>();
		}

		qSet = new ContourQuadSet[n_levs];
		for (int kk = 0; kk < n_levs; kk++) {
			qSet[kk] = new ContourQuadSet(nr, nc, kk, this);
		}
	}

	/**
	 * Set the grid coordinates used to contruct <code>ContourStrip</code>s
	 * contained in this set.
	 * 
	 * @param gx
	 * @param gy
	 */
	
	void setGridValues(float[] gx, float[] gy) {
		gridX = gx;
		gridY = gy;
	}

	void setGridColors(byte[][] colors) {
		gridColors = colors;
	}

	/**
	 * 
	 * @param vx
	 * @param vy
	 * @param idx0
	 * @param idx1
	 * @param lev_idx
	 * @param ir
	 * @param ic
	 */
	
	void add(float[] vx, float[] vy, int idx0, int idx1, int lev_idx, int ir,
			int ic) {
		qSet[lev_idx].add(idx0, ir, ic);
	}

	/**
	 * 
	 * @param vx
	 * @param vy
	 * @param idx0
	 * @param idx1
	 * @param level
	 */
	
	void add(float[] vx, float[] vy, int idx0, int idx1, float level) {
		int lev_idx = 0;
		for (int kk = 0; kk < n_levs; kk++) {
			if (level == levels[kk])
				lev_idx = kk;
		}
		add(vx, vy, idx0, idx1, lev_idx);
	}

	/**
	 * 
	 * @param vx
	 * @param vy
	 * @param idx0
	 * @param idx1
	 * @param lev_idx
	 */
	
	void add(float[] vx, float[] vy, int idx0, int idx1, int lev_idx) {
                float delx = vx[idx1] - vx[idx0];
                float dely = vy[idx1] - vy[idx0];
                // skip really small segments
                if ((delx <= 0.0001 && delx >= -0.0001) && (dely <= 0.0001 && dely >= -0.0001)) {
                    return;
                }

		vec = vecArray[lev_idx];
                closedStripList = closedStripArray[lev_idx];
		int n_strip = vec.size();

		if (n_strip == 0) {
			ContourStrip c_strp = new ContourStrip(lev_idx, idx0, idx1, this);
			vec.add(c_strp);
		} else {
			int[] found_array = new int[3];
			int found = 0;
			for (int kk = 0; kk < n_strip; kk++) {
				ContourStrip c_strp = vec.get(kk);
				if (c_strp.addPair(vx, vy, idx0, idx1)) {
					found_array[found] = kk;
					found++;
                                        if (c_strp.closed) { // take off main list, add to closed (done) list.
                                           vec.remove(c_strp);
                                           closedStripList.add(c_strp);
                                           break;
                                        }
					// exit loop if we hit threshold value
					if (found == 3) break;
				}
			}
			if (found == 3) {
				ContourStrip c_strp = new ContourStrip(lev_idx, idx0,
						idx1, this);
				vec.add(c_strp);

			} else if (found == 2) {
				ContourStrip c_strpA = vec.get(found_array[0]);
				ContourStrip c_strpB = vec.get(found_array[1]);
				c_strpA.merge(c_strpB);
				vec.remove(found_array[1]);

			} else if (found == 0) {
				ContourStrip c_strp = new ContourStrip(lev_idx, idx0,
						idx1, this);
				vec.add(c_strp);
			}
		}
	}

	/**
         * Iterates over list of ContourStrips for each contour level index.
         *
	 * 
	 * @param vx
	 *            Grid coordinate values.
	 * @param vy
	 *            Grid coordinate values.
	 * @param colors
	 *            Colors for grid coordinate values.
	 * @param labelColor
	 *            Color for labels if filling.
	 * @param lev_idx
	 *            Index of the level to process.
	 * @param out_vv
	 *            Output line display coords for basic lines.
	 * @param out_bb
	 *            Output colors for basic lines.
	 * @param out_vvL
	 *            Output line display coords for labels.
	 * @param out_bbL
	 *            Output colors for label lines.
	 * @param out_loc
	 *            Output location coords for labels.
	 * @param dashed
	 *            Flags indicating which levels to dash.
	 * @throws VisADException
	 */
	
	void getLineColorArraysAtCntrLevel(float[] vx, float[] vy, byte[][] colors,
			byte[] labelColor, Object labelFont, boolean labelAlign,
			boolean sphericalDisplayCS, int lev_idx, boolean[] dashed)
			throws VisADException {

                // open strips (must end on grid boundary)
		int n_strips = vecArray[lev_idx].size();
		for (int kk = 0; kk < n_strips; kk++) {
			ContourStrip cs = vecArray[lev_idx].get(kk);
			cs.isDashed = dashed[lev_idx];
			cs.getLabeledLineColorArray(vx, vy, colors, labelColor, labelFont,
					labelAlign, sphericalDisplayCS);
		}

                // closed strips
                n_strips = closedStripArray[lev_idx].size();
                for (int kk = 0; kk < n_strips; kk++) {
                        ContourStrip cs = closedStripArray[lev_idx].get(kk);
                        cs.isDashed = dashed[lev_idx];
                        cs.getLabeledLineColorArray(vx, vy, colors, labelColor, labelFont,
                                        labelAlign, sphericalDisplayCS);
                }


	}

	/**
         * Called just after the grid walking is complete.
         *
	 * @param vx
	 * @param vy
	 * @param colors
	 *            shared colors
	 * @param labelColor
	 *            RGB label color byte array
	 * @param out_vv
	 *            output vector verticie array {{ X }, { Y }}
	 * @param out_bb
	 * @param out_vvL
	 * @param out_bbL
	 * @param out_loc
	 * @param dashFlags
	 * @param contourDifficulty
	 * @throws VisADException
	 */
	
	void getLineColorArrays(float[] vx, float[] vy, byte[][] colors,
			byte[] labelColor, Object labelFont, boolean labelAlign,
			boolean sphericalDisplayCS, boolean[] dashFlags)
			throws VisADException {
              
                /* Don't use the tiling logic for now.
		makeContourStrips(vx, vy);
                */

		// set the line and color arrays for each level
		for (int kk = 0; kk < n_levs; kk++) {
			getLineColorArraysAtCntrLevel(vx, vy, colors, labelColor, labelFont,
					labelAlign, sphericalDisplayCS, kk, dashFlags);
		}

	}

	/**
	 * 
	 * @param vx
	 * @param vy
	 */
	
	void makeContourStrips(float[] vx, float[] vy) {
		for (int kk = 0; kk < n_levs; kk++) {
			int nx = qSet[kk].nx;
			int ny = qSet[kk].ny;
			for (int j = 0; j < ny; j++) {
				for (int i = 0; i < nx; i++) {
					if (qSet[kk].qarray[j][i] == null)
						continue;
					int[] vert_indices = qSet[kk].qarray[j][i].vert_indices;
					int len = qSet[kk].qarray[j][i].numv;
					for (int q = 0; q < len; q++) {
						int idx = vert_indices[q];
						add(vx, vy, idx, idx + 1, kk);
					}
				}
			}
		}
	}

	/**
	 * Get grid coordinates representing the data at the level specified.
	 * 
	 * @param lvlIdx
	 *            The level for which to generate an array.
	 * @return An list of in line strip format, an emtpy list if none.
	 * @see {@link VisADLineStripArray}
	 */
	
	List<float[][][]> getLineStripCoordinates(int lvlIdx) {
		if (lvlIdx > vecArray.length - 1) {
			return new ArrayList<float[][][]>(0);
		}
		List<ContourStrip> strips = vecArray[lvlIdx];
		List<float[][][]> stripValues = new ArrayList<float[][][]>();
		for (ContourStrip strip : strips) {
			stripValues.add(strip.getLineStripArrays(gridX, gridY));
		}
		return stripValues;
	}

	/**
	 * Get colors corresponding to the grid coordinates for a level.
	 * 
	 * @param lvlIdx
	 *            The level for which to get colors.
	 * @return A list of arrays for the strips that make up the level, an empty
	 *         list if none.
	 */
	
	List<byte[][][]> getLineStripColors(int lvlIdx) {
		if (lvlIdx > vecArray.length - 1) {
			return new ArrayList<byte[][][]>(0);
		}
		List<ContourStrip> strips = vecArray[lvlIdx];
		List<byte[][][]> stripColors = new ArrayList<byte[][][]>();
		for (ContourStrip strip : strips) {
			;
			stripColors.add(strip.getColorStripArrays(gridColors));
		}
		return stripColors;
	}

	/**
	 * Are we using line style for a level.
	 * 
	 * @param lvl
	 *            The index of the the level.
	 * @return True if the first strip is using line style, false otherwise.
	 *         There is an assumption that if the first level is styled they all
	 *         are.
	 */
	
	boolean isLevelStyled(int lvl) {
		if (vecArray.length > lvl + 1 && vecArray[lvl] != null) {
			if (vecArray[lvl].size() > 0) {
				return vecArray[lvl].get(0).isDashed;
			}
		}
		return false;
	}

	boolean isLabeled(int lvl) {
		return vecArray[lvl].get(0).isLabeled();
	}

} //--------  ContourStripSet -------------------

/**
 * ContourStrip is used internally by Contour2D to track the indexes associated
 * with a strip. Indexes are in line strip format and not line array format.
 */

class ContourStrip {

  /** Default label Font */
	private static final HersheyFont TIMESR_FONT = new HersheyFont("timesr");

  /** Minimum number of points for which to perform label algm */
	static final int LBL_ALGM_THRESHHOLD = 20;

	/**
	 * Array of indexes to values in the grid coordinate arrays that make up
	 * this strip.
	 */
	IndexPairList idxs = new IndexPairList();

	/** Index to the level for this strip in the intervals array. */
	int lev_idx;

	private boolean isLabeled = false;
	/** First index which starts the break for the label. */
	private int start_break;
	/** Last index which ends the break for the label. */
	private int stop_break;
	/** Number of indexes that make up the break for the label. */
	private int n_skip;

	/** Number of labels on this strip. */
	int numLabels;
	
	/** Label every Nth line */
	int numSkipLines;

	boolean isDashed = false;

	/**           */
	PlotDigits plot;

	/**           */
	ContourStripSet css;

    boolean closed = false;

	/**
	 * 
	 * @param lev_idx
	 * @param idx0
	 * @param idx1
	 * @param plot
	 * @param css
	 */
	
	ContourStrip(int lev_idx, int idx0, int idx1,
			ContourStripSet css) {
		this.lev_idx = lev_idx;

		idxs.addFirst(idx0, idx1);

		this.css = css;
		numLabels = css.labelFreq;
		numSkipLines = css.labelLineSkip;
	}

	/**
	 * 
	 * @param vx
	 * @param vy
	 * @param idx0
	 * @param idx1
	 * 
	 * @return
	 */
	
	boolean addPair(float[] vx, float[] vy, int idx0, int idx1) {

		// test for closed strip, bail out early if found
                if (closed) return false;
 
                float delta = 0.001f;
		   
		float vx0 = vx[idx0];
		float vy0 = vy[idx0];
		float vx1 = vx[idx1];
		float vy1 = vy[idx1];

		float vx_s = vx[idxs.first.idx0];
		float vy_s = vy[idxs.first.idx0];

                float delx = vx0 - vx_s;
                float dely = vy0 - vy_s;
                if ((delx > -delta && delx < delta) && (dely > -delta && dely < delta)) {
			idxs.addFirst(idx1, idx0);
                        setIsClosed(vx, vy);
			return true;
		}
                delx = vx1 - vx_s;
                dely = vy1 - vy_s;
                if ((delx > -delta && delx < delta) && (dely > -delta && dely < delta)) {
			idxs.addFirst(idx0, idx1);
                        setIsClosed(vx, vy);
			return true;
		}

		vx_s = vx[idxs.last.idx1];
		vy_s = vy[idxs.last.idx1];
                delx = vx0 - vx_s;
                dely = vy0 - vy_s;
                if ((delx > -delta && delx < delta) && (dely > -delta && dely < delta)) {
			idxs.addLast(idx0, idx1);
                        setIsClosed(vx, vy);
			return true;
		}
                delx = vx1 - vx_s;
                dely = vy1 - vy_s;
                if ((delx > -delta && delx < delta) && (dely > -delta && dely < delta)) {
			idxs.addLast(idx1, idx0);
                        setIsClosed(vx, vy);
			return true;
		}

		return false;
	}

        /** Check if endpoints are equal and set the closed flag.
         */
        void setIsClosed(float[] vx, float[] vy) {
           float delta = 0.001f;
           float delx = vx[idxs.first.idx0] - vx[idxs.last.idx1];
           float dely = vy[idxs.first.idx0] - vy[idxs.last.idx1];
           closed = ((idxs.numIndices > 2) && (delx > -delta && delx < delta) && (dely > -delta && dely < delta));
        }


	/**
	 * 
	 * @param vx
	 * @param vy
	 * @param colors
	 * @param labelColor
	 * @param out_vv
	 * @param out_colors
	 * @param out_vvL
	 * @param out_colorsL
	 * @param lbl_loc
	 * @throws VisADException
	 */
	
	void getLabeledLineColorArray(float[] vx, float[] vy, byte[][] colors,
			byte[] labelColor, Object labelFont, boolean labelAlign,
			boolean sphericalDisplayCS) throws VisADException {

                boolean hasColors = (colors != null);

		int linArrLen = idxs.getNumIndices();
           
		// break up each line into chunks according to label frequency
		// Below heuristic can be tweaked if desired.  Just provides a
		// label freq 1 to 9 mapping to point count for repeating the label

                int labelRepeat = linArrLen;
		switch (numLabels) {
			case 1:
				labelRepeat = linArrLen;
				break;
			case 3:
				labelRepeat = 200;
				break;
			case 5:
				labelRepeat = 150;
				break;
			case 7:
				labelRepeat = 100;
				break;
			case 9:
				labelRepeat = 50;
				break;
			default: 
				labelRepeat = linArrLen;
				break;
		}
		int labelCount = linArrLen / labelRepeat;
		int labelRemain = linArrLen % labelRepeat;

                if (labelRemain <= 4 && labelRemain > 0 && labelCount > 0) {
                   labelCount -= 1;
                   labelRemain += labelRepeat;
                }

		int labelsDone = 0;

		for (int i = 0; i < labelCount; i++) {

                        int start = i*labelRepeat/2;
                        int stop = start + labelRepeat/2 - 1;

                        float[][] vvTmp = getLineArray(vx, vy, start, stop);
                        byte[][] bbTmp = null;
                        if (hasColors) {
                           bbTmp = getColorArray(colors, start, stop);
                        }

			processLineArrays(vvTmp, bbTmp, labelColor, labelFont, labelAlign,
					sphericalDisplayCS);
			labelsDone++;
		}
		
		if (labelRemain > 0) {

                        int start = labelsDone*labelRepeat/2;
                        int stop = start + labelRemain/2 - 1;

                        float[][] vvTmp = getLineArray(vx, vy, start, stop);
                        byte[][] bbTmp = null;
                        if (hasColors) {
                           bbTmp = getColorArray(colors, start, stop);
                        }

			processLineArrays(vvTmp, bbTmp, labelColor, labelFont, labelAlign,
					sphericalDisplayCS);
		}
	}

	/**
	 * Common line array code
	 * 
	 * @param vv_grid grid coordinates..
	 * 
	 * @param bb grid color values.
	 * 
	 * @param labelColor RGB label color byte array
	 * 
	 * @param out_vv
	 * 
	 * @param out_colors
	 * 
	 * @param out_vvL
	 * 
	 * @param out_colorsL
	 * 
	 * @param lbl_loc
	 */
	
	private void processLineArrays(float[][] vv_grid, byte[][] bb,
			byte[] labelColor, Object labelFont, boolean labelAlign,
			boolean sphericalDisplayCS) throws VisADException {

		float[][] vv = css.spatial_set.gridToValue(vv_grid);

		int clr_dim = 0;
		if (bb != null)
			clr_dim = bb.length;

		int totalPts = vv[0].length / 2;
		int loc = 0;
		int pos = 0;

		VisADGeometryArray label = null;

		DecimalFormat numFormat = new DecimalFormat();
		numFormat.setMaximumFractionDigits(1);
		numFormat.setGroupingUsed(false);
		String numStr = numFormat.format((double) css.levels[lev_idx]);

		float lbl_half = 0.1f;
		isLabeled = false;
		boolean labelThisLine = false;

		// label every Nth line, user can adjust this
		if ((lev_idx % css.labelLineSkip) == 0) {
			labelThisLine = true;
		}
                
		if ((totalPts > LBL_ALGM_THRESHHOLD) && (labelThisLine)) {
			isLabeled = true;
			loc = (vv[0].length) / 2; // - start at half-way pt.
			int n_pairs_b = 1;
			int n_pairs_f = 1;
			boolean found = false;
			float ctr_dist;
			pos = loc;

			// - get a unit vector parallel to the contour line at the label
			// - position.
			float del_x;
			float del_y;
			float del_z;
			del_z = vv[2][pos + 1] - vv[2][pos - 1];
			del_y = vv[1][pos + 1] - vv[1][pos - 1];
			del_x = vv[0][pos + 1] - vv[0][pos - 1];
			float mag = (float) Math.sqrt(del_y * del_y + del_x * del_x + del_z
					* del_z);
			float[] ctr_u = new float[] { del_x / mag, del_y / mag, del_z / mag };

			if (ctr_u[0] < 0) {
				ctr_u[0] = -ctr_u[0];
				ctr_u[1] = -ctr_u[1];
				ctr_u[2] = -ctr_u[2];
			}

			float ctr_u_dot_lbl = ctr_u[0] * 1f + ctr_u[1] * 0f + ctr_u[2] * 0f;

			if (labelFont instanceof Font) {
				label = PlotText.render_font(numStr, (Font) labelFont,
						new double[] { vv[0][loc], vv[1][loc], vv[2][loc] },
						new double[] { 1.0, 0.0, 0.0 }, new double[] { 0.0,
								1.0, 0.0 }, TextControl.Justification.CENTER,
						TextControl.Justification.CENTER, 0.0, css.labelScale,
						null);
			} else if (labelFont instanceof HersheyFont) {
				label = PlotText.render_font(numStr, (HersheyFont) labelFont,
						new double[] { vv[0][loc], vv[1][loc], vv[2][loc] },
						new double[] { 1.0, 0.0, 0.0 }, new double[] { 0.0,
								1.0, 0.0 }, TextControl.Justification.CENTER,
						TextControl.Justification.CENTER, 0.0, css.labelScale,
						null);
			} else if (labelFont == null) {
				label = PlotText.render_font(numStr, TIMESR_FONT,
						new double[] { vv[0][loc], vv[1][loc], vv[2][loc] },
						new double[] { 1.0, 0.0, 0.0 }, new double[] { 0.0,
								1.0, 0.0 }, TextControl.Justification.CENTER,
						TextControl.Justification.CENTER, 0.0, css.labelScale,
						null);
			} else {
				label = PlotText.render_font(numStr, TIMESR_FONT,
						new double[] { vv[0][loc], vv[1][loc], vv[2][loc] },
						new double[] { 1.0, 0.0, 0.0 }, new double[] { 0.0,
								1.0, 0.0 }, TextControl.Justification.CENTER,
						TextControl.Justification.CENTER, 0.0, css.labelScale,
						null);
			}

			float x_min = Float.MAX_VALUE;
			float x_max = -Float.MAX_VALUE;
			float y_min = Float.MAX_VALUE;
			float y_max = -Float.MAX_VALUE;
			float z_min = Float.MAX_VALUE;
			float z_max = -Float.MAX_VALUE;

			for (int k = 0; k < label.vertexCount; k++) {
				int i = 3 * k;
				float x = label.coordinates[i];
				float y = label.coordinates[i + 1];
				float z = label.coordinates[i + 2];
				if (x > x_max)
					x_max = x;
				if (y > y_max)
					y_max = y;
				if (z > z_max)
					z_max = z;
				if (x < x_min)
					x_min = x;
				if (y < y_min)
					y_min = y;
				if (z < z_min)
					z_min = z;
			}

			if (labelAlign) {
				lbl_half = (x_max - x_min) / 2;
			} else {
				if (ctr_u_dot_lbl > 0.5) {
					lbl_half = (x_max - x_min) / 2;
				} else {
					lbl_half = (y_max - y_min);
				}
			}

			lbl_half += lbl_half * 0.08;

			// - compute distance between label location (loc) and points
			// - on each side, when greater than lbl_half - stop. This
			// - assumes that around the label the contour line is fairly
			// - linear, seems good approx almost all of time.

			while (!found) { // - go backwards, ie decreasing index val
				pos -= 2;
				if (pos < 0 || pos > (vv[0].length - 1))
					return;
				float dx = vv[0][pos] - vv[0][loc];
				float dy = vv[1][pos] - vv[1][loc];
				float dz = vv[2][pos] - vv[2][loc];
				ctr_dist = (float) Math.sqrt((double) (dx * dx + dy * dy + dz
						* dz));
				if (ctr_dist > (float) Math.abs((double) lbl_half)) {
					found = true;
				} else {
					n_pairs_b++;
				}
			}

			pos = loc;
			found = false;
			while (!found) { // - go fowards, ie increasing index val
				pos += 2;
				if (pos < 0 || pos > (vv[0].length - 1))
					return;
				float dx = vv[0][pos] - vv[0][loc];
				float dy = vv[1][pos] - vv[1][loc];
				float dz = vv[2][pos] - vv[2][loc];
				ctr_dist = (float) Math.sqrt((double) (dx * dx + dy * dy + dz
						* dz));
				if (ctr_dist > (float) Math.abs((double) lbl_half)) {
					found = true;
				} else {
					n_pairs_f++;
				}
			}

			// - total number of points skipped (removed)
			n_skip = (n_pairs_b + n_pairs_f) * 2;
			// always start_break on even (1st in pair), stop_break on odd index
			// (2nd in pair)
			if ((loc & 1) == 1) { // - odd
				start_break = loc - (1 + (n_pairs_b - 1) * 2);
				stop_break = loc + (2 + (n_pairs_f - 1) * 2);
			} else { // -even
				start_break = loc - (2 + (n_pairs_b - 1) * 2);
				stop_break = loc + (1 + (n_pairs_f - 1) * 2);
			}

		}

		boolean doLabel = false;
		// - check if label blocks out too may points
		if (start_break >= 4 && stop_break <= totalPts * 2 - 3)
			doLabel = true;

		if (doLabel && isLabeled) {

			
			
			/*-------LABEL START --------------------*/
			float[] ctr_u = null;
			float[] norm_x_ctr = null;

			// - get a unit vector perpendicular to display coordinate grid
			// - at this label location (loc)
			float[][] norm = null;
			Gridded3DSet cg3d = (Gridded3DSet) css.spatial_set;

			norm = cg3d.getNormals(new float[][] { { vv_grid[0][loc] },
					{ vv_grid[1][loc] } });

			/*
			 * test if (norm[2][0] < 0) { norm[0][0] = -norm[0][0]; norm[1][0] =
			 * -norm[1][0]; norm[2][0] = -norm[2][0]; }
			 */

			float[] labelBase = null;
			float[] labelUp = null;

			if (labelAlign) { // - align labels with contours
				// - get a unit vector parallel to the contour line at the label
				// - position.
				float del_x;
				float del_y;
				float del_z;

				del_z = vv[2][stop_break] - vv[2][start_break];
				del_y = vv[1][stop_break] - vv[1][start_break];
				del_x = vv[0][stop_break] - vv[0][start_break];

				float mag = (float) Math.sqrt(del_y * del_y + del_x * del_x
						+ del_z * del_z);

				ctr_u = new float[] { del_x / mag, del_y / mag, del_z / mag };

				if (ctr_u[0] < 0) {
					ctr_u[0] = -ctr_u[0];
					ctr_u[1] = -ctr_u[1];
					ctr_u[2] = -ctr_u[2];
				}

				if (sphericalDisplayCS) {
					float[] newNorm = SphericalCoordinateSystem
							.getNormal(new float[] { vv[0][pos], vv[1][pos],
									vv[2][pos] });

					norm[0][0] = newNorm[0];
					norm[1][0] = newNorm[1];
					norm[2][0] = newNorm[2];

					float[] unitI = SphericalCoordinateSystem
							.getUnitI(new float[] { vv[0][pos], vv[1][pos],
									vv[2][pos] });

					float ctr_u_dot_unitI = ctr_u[0] * unitI[0] + ctr_u[1]
							* unitI[1] + ctr_u[2] * unitI[2];
					if (ctr_u_dot_unitI < 0) {
						ctr_u[0] = -ctr_u[0];
						ctr_u[1] = -ctr_u[1];
						ctr_u[2] = -ctr_u[2];
					}
				}

				// - get a vector perpendicular to contour line, and in the
				// local
				// - tangent plane. norm_x_ctr: cross-product of local norm and
				// - unit vector parallel to contour line at label location.
				norm_x_ctr = new float[] {
						norm[1][0] * ctr_u[2] - norm[2][0] * ctr_u[1],
						-(norm[0][0] * ctr_u[2] - norm[2][0] * ctr_u[0]),
						norm[0][0] * ctr_u[1] - norm[1][0] * ctr_u[0] };

				mag = (float) Math.sqrt(norm_x_ctr[0] * norm_x_ctr[0]
						+ norm_x_ctr[1] * norm_x_ctr[1] + norm_x_ctr[2]
						* norm_x_ctr[2]);

				// - normalize vector
				norm_x_ctr[0] = norm_x_ctr[0] / mag;
				norm_x_ctr[1] = norm_x_ctr[1] / mag;
				norm_x_ctr[2] = norm_x_ctr[2] / mag;

				if (!sphericalDisplayCS) {
					if (Math.abs((double) norm[2][0]) <= 0.00001) {
						if (norm_x_ctr[2] < 0) {
							norm_x_ctr[0] = -norm_x_ctr[0];
							norm_x_ctr[1] = -norm_x_ctr[1];
							norm_x_ctr[2] = -norm_x_ctr[2];
						}
					} else {
						if (norm_x_ctr[1] < 0) {
							norm_x_ctr[0] = -norm_x_ctr[0];
							norm_x_ctr[1] = -norm_x_ctr[1];
							norm_x_ctr[2] = -norm_x_ctr[2];
						}
					}
				}

				labelBase = ctr_u;
				labelUp = norm_x_ctr;
			} else {
				float a = norm[0][0];
				float b = norm[1][0];
				float c = norm[2][0];

				float[] unitI = null;

				if (sphericalDisplayCS) {
					float[] newNorm = SphericalCoordinateSystem
							.getNormal(new float[] { vv[0][pos], vv[1][pos],
									vv[2][pos] });

					unitI = SphericalCoordinateSystem.getUnitI(new float[] {
							vv[0][pos], vv[1][pos], vv[2][pos] });

					a = newNorm[0];
					b = newNorm[1];
					c = newNorm[2];
				}

				if (visad.util.Util.isApproximatelyEqual(a, 0)
						&& visad.util.Util.isApproximatelyEqual(b, 0)) {
					labelBase = new float[] { 1, 0, 0 };
					labelUp = new float[] { 0, 1, 0 };
				} else {
					float D = -(a * vv[0][loc] + b * vv[1][loc] + c
							* vv[2][loc]);
					float K = -D - c * vv[2][loc];

					float xLine = vv[0][loc] + 0.5f;
					float yLine = (K - a * xLine) / b;

					float delX = xLine - vv[0][loc];
					float delY = yLine - vv[1][loc];

					float mag = (float) Math.sqrt(delX * delX + delY * delY);

					float[] uLine = null;

					if (sphericalDisplayCS) {
						uLine = new float[] { unitI[0], unitI[1], unitI[2] };
					} else {
						uLine = new float[] { delX / mag, delY / mag, 0 };
					}

					float[] norm_x_uLine = new float[] {
							(b * uLine[2] - c * uLine[1]),
							-(a * uLine[2] - c * uLine[0]),
							(a * uLine[1] - b * uLine[0]) };

					if (norm_x_uLine[2] < 0) {
						norm_x_uLine[2] = 1f;
						norm_x_uLine[1] = -norm_x_uLine[1];
						norm_x_uLine[0] = -norm_x_uLine[0];
					}

					labelBase = uLine;
					labelUp = norm_x_uLine;

				}
			}

			// -- translate to label plot location --------------

			if (labelFont instanceof Font) {
				label = PlotText
						.render_font(numStr, (Font) labelFont, new double[] {
								vv[0][loc], vv[1][loc], vv[2][loc] },
								new double[] { labelBase[0], labelBase[1],
										labelBase[2] }, new double[] {
										labelUp[0], labelUp[1], labelUp[2] },
								TextControl.Justification.CENTER,
								TextControl.Justification.CENTER, 0.0,
								css.labelScale, null);
			} else if (labelFont instanceof HersheyFont) {
				label = PlotText
						.render_font(numStr, (HersheyFont) labelFont,
								new double[] { vv[0][loc], vv[1][loc],
										vv[2][loc] }, new double[] {
										labelBase[0], labelBase[1],
										labelBase[2] }, new double[] {
										labelUp[0], labelUp[1], labelUp[2] },
								TextControl.Justification.CENTER,
								TextControl.Justification.CENTER, 0.0,
								css.labelScale, null);
			} else if (labelFont == null) {
				label = PlotText
						.render_font(numStr, TIMESR_FONT,
								new double[] { vv[0][loc], vv[1][loc],
										vv[2][loc] }, new double[] {
										labelBase[0], labelBase[1],
										labelBase[2] }, new double[] {
										labelUp[0], labelUp[1], labelUp[2] },
								TextControl.Justification.CENTER,
								TextControl.Justification.CENTER, 0.0,
								css.labelScale, null);
			} else {
				label = PlotText
						.render_font(numStr, TIMESR_FONT,
								new double[] { vv[0][loc], vv[1][loc],
										vv[2][loc] }, new double[] {
										labelBase[0], labelBase[1],
										labelBase[2] }, new double[] {
										labelUp[0], labelUp[1], labelUp[2] },
								TextControl.Justification.CENTER,
								TextControl.Justification.CENTER, 0.0,
								css.labelScale, null);
			}

			// no lighting/shading for labels
			label.normals = null;

			// set the color arrays for label, can be null
			byte[] lblClr = null;
			if (labelColor != null) {
				int clrDim = labelColor.length;
				lblClr = new byte[clrDim * label.vertexCount];
				for (int kk = 0; kk < label.vertexCount; kk++) {
					lblClr[kk * clrDim] = labelColor[0];
					lblClr[kk * clrDim + 1] = labelColor[1];
					lblClr[kk * clrDim + 2] = labelColor[2];
					if (clrDim == 4)
						lblClr[kk * clrDim + 3] = labelColor[3];
				}
			} else if (bb != null) {
				lblClr = new byte[clr_dim * label.vertexCount];
				for (int kk = 0; kk < label.vertexCount; kk++) {
					lblClr[kk * clr_dim] = bb[0][loc];
					lblClr[kk * clr_dim + 1] = bb[1][loc];
					lblClr[kk * clr_dim + 2] = bb[2][loc];
					if (clr_dim == 4)
						lblClr[kk * clr_dim + 3] = bb[3][loc];
				}
			}
			label.colors = lblClr;

			VisADLineArray labelAnchor = new VisADLineArray();

			SampledSet.setGeometryArray(labelAnchor, new float[][] {
					{ vv[0][loc] }, { vv[1][loc] }, { vv[2][loc] } }, clr_dim,
					null);

			/*-------- LABEL DONE -------------------*/

			// - this sections creates the contour gap for the label

			int s_pos = 0;
			int d_pos = 0;
			int cnt = start_break;

			// - make indexed
			float[] lineCoords = new float[3 * ((start_break / 2 + 1)
					+ (((totalPts * 2 - start_break) - n_skip) / 2) + 1)];
			byte[] lineColors = new byte[clr_dim
					* ((start_break / 2 + 1)
							+ (((totalPts * 2 - start_break) - n_skip) / 2) + 1)];
			float[] fillLineCoords = new float[3 * (n_skip / 2 + 1)];
			byte[] fillLineColors = new byte[clr_dim * (n_skip / 2 + 1)];

			int lineClrCnt = 0;
			int lineCnt = 0;
			lineCoords[lineCnt++] = vv[0][0];
			lineCoords[lineCnt++] = vv[1][0];
			lineCoords[lineCnt++] = vv[2][0];
			for (int cc = 0; cc < clr_dim; cc++) {
				lineColors[lineClrCnt++] = bb[cc][0];
			}

			for (int t = 1; t < cnt; t += 2) {
				lineCoords[lineCnt++] = vv[0][t];
				lineCoords[lineCnt++] = vv[1][t];
				lineCoords[lineCnt++] = vv[2][t];
				for (int c = 0; c < clr_dim; c++) {
					lineColors[lineClrCnt++] = bb[c][t];
				}
			}

			// label fill line
			s_pos = start_break;
			d_pos = 0;
			cnt = n_skip;

			fillLineCoords[0] = vv[0][s_pos];
			fillLineCoords[1] = vv[1][s_pos];
			fillLineCoords[2] = vv[2][s_pos];
			for (int cc = 0; cc < clr_dim; cc++) {
				fillLineColors[cc] = bb[cc][s_pos];
			}
			int kk = 3;
			int nn = clr_dim;
			for (int t = 1; t < n_skip; t += 2) {
				fillLineCoords[kk++] = vv[0][s_pos + t];
				fillLineCoords[kk++] = vv[1][s_pos + t];
				fillLineCoords[kk++] = vv[2][s_pos + t];
				for (int cc = 0; cc < clr_dim; cc++) {
					fillLineColors[nn++] = bb[cc][s_pos + t];
				}
			}

			VisADLineStripArray fillLineArray = new VisADLineStripArray();
			fillLineArray.stripVertexCounts = new int[] { (n_skip / 2) + 1 };
			fillLineArray.vertexCount = (n_skip / 2) + 1;
			fillLineArray.coordinates = fillLineCoords;
			if (fillLineColors.length > 0)
				fillLineArray.colors = fillLineColors;
			if (isDashed) {
				css.fillLinesStyled.add(fillLineArray);
			} else {
				css.fillLines.add(fillLineArray);
			}

			// -- end label fill line;

			s_pos = stop_break + 1;
			d_pos = start_break;
			cnt = vv[0].length - s_pos;

			// - make indexed
			lineCoords[lineCnt++] = vv[0][s_pos];
			lineCoords[lineCnt++] = vv[1][s_pos];
			lineCoords[lineCnt++] = vv[2][s_pos];
			for (int cc = 0; cc < clr_dim; cc++) {
				lineColors[lineClrCnt++] = bb[cc][s_pos];
			}

			for (int t = 1; t < ((totalPts * 2 - start_break) - n_skip); t += 2) {
				lineCoords[lineCnt++] = vv[0][s_pos + t];
				lineCoords[lineCnt++] = vv[1][s_pos + t];
				lineCoords[lineCnt++] = vv[2][s_pos + t];
				for (int c = 0; c < clr_dim; c++) {
					lineColors[lineClrCnt++] = bb[c][s_pos + t];
				}
			}

			VisADLineStripArray lineArray = new VisADLineStripArray();
			lineArray.stripVertexCounts = new int[] { (start_break / 2) + 1,
					(((totalPts * 2 - start_break) - n_skip) / 2) + 1 };
			lineArray.vertexCount = lineArray.stripVertexCounts[0]
					+ lineArray.stripVertexCounts[1];
			lineArray.coordinates = lineCoords;
			if (lineColors.length > 0) {
				lineArray.colors = lineColors;
			}
			if (isDashed) {
				css.cntrLinesStyled.add(lineArray);
			} else {
				css.cntrLines.add(lineArray);
			}

			// --- end label gap code

			// --- expanding/contracting left-right segments

			// - left
			s_pos = start_break;
			d_pos = 0;
			cnt = 2;

			// - unit left
			float dx = vv[0][loc] - vv[0][s_pos];
			float dy = vv[1][loc] - vv[1][s_pos];
			float dz = vv[2][loc] - vv[2][s_pos];
			float dd = (float) Math
					.sqrt((double) (dx * dx + dy * dy + dz * dz));
			dx = dx / dd;
			dy = dy / dd;
			dz = dz / dd;
			float mm = dd - (float) Math.abs((double) lbl_half);
			dx *= mm;
			dy *= mm;
			dz *= mm;
			byte[][] segColors = new byte[clr_dim][2];
			if (bb != null) {
				for (int cc = 0; cc < clr_dim; cc++) {
					System.arraycopy(bb[cc], s_pos, segColors[cc], d_pos, cnt);
				}
			}

			VisADLineArray expSegLeft = new VisADLineArray();
			VisADLineArray segLeftAnchor = new VisADLineArray();
			SampledSet.setGeometryArray(expSegLeft, new float[][] {
					{ vv[0][s_pos], vv[0][s_pos] + dx },
					{ vv[1][s_pos], vv[1][s_pos] + dy },
					{ vv[2][s_pos], vv[2][s_pos] + dz } }, clr_dim, segColors);
			SampledSet.setGeometryArray(segLeftAnchor, new float[][] {
					{ vv[0][s_pos] }, { vv[1][s_pos] }, { vv[2][s_pos] } },
					clr_dim, null);

			float[] segLeftScaleInfo = new float[] { lbl_half, dd };

			// - right
			s_pos = stop_break - 1;
			d_pos = 0;
			cnt = 2;

			// - unit right
			dx = vv[0][loc] - vv[0][stop_break];
			dy = vv[1][loc] - vv[1][stop_break];
			dz = vv[2][loc] - vv[2][stop_break];
			dd = (float) Math.sqrt((double) (dx * dx + dy * dy + dz * dz));
			dx = dx / dd;
			dy = dy / dd;
			dz = dz / dd;
			mm = dd - (float) Math.abs((double) lbl_half);
			dx *= mm;
			dy *= mm;
			dz *= mm;
			segColors = new byte[clr_dim][2];
			if (bb != null) {
				for (int cc = 0; cc < clr_dim; cc++) {
					System.arraycopy(bb[cc], s_pos, segColors[cc], d_pos, cnt);
				}
			}

			VisADLineArray expSegRight = new VisADLineArray();
			SampledSet.setGeometryArray(expSegRight, new float[][] {
					{ vv[0][stop_break], vv[0][stop_break] + dx },
					{ vv[1][stop_break], vv[1][stop_break] + dy },
					{ vv[2][stop_break], vv[2][stop_break] + dz } }, clr_dim,
					segColors);
			VisADLineArray segRightAnchor = new VisADLineArray();
			SampledSet.setGeometryArray(segRightAnchor, new float[][] {
					{ vv[0][stop_break] }, { vv[1][stop_break] },
					{ vv[2][stop_break] } }, clr_dim, null);

			float[] segRightScaleInfo = new float[] { lbl_half, dd };

			// ----- end expanding/contracting line segments

			ContourLabelGeometry ctrLabel = new ContourLabelGeometry(label,
					labelAnchor, expSegLeft, segLeftAnchor, segLeftScaleInfo,
					expSegRight, segRightAnchor, segRightScaleInfo);
			ctrLabel.isStyled = isDashed;
			css.labels.add(ctrLabel);
		} else { // no label
			float[] lineCoords = new float[3 * (totalPts + 1)];
			byte[] lineColors = new byte[clr_dim * (totalPts + 1)];

			int lineClrCnt = 0;
			int lineCnt = 0;
			lineCoords[lineCnt++] = vv[0][0];
			lineCoords[lineCnt++] = vv[1][0];
			lineCoords[lineCnt++] = vv[2][0];
			for (int cc = 0; cc < clr_dim; cc++) {
				lineColors[lineClrCnt++] = bb[cc][0];
			}

			for (int t = 1; t < totalPts * 2; t += 2) {
				lineCoords[lineCnt++] = vv[0][t];
				lineCoords[lineCnt++] = vv[1][t];
				lineCoords[lineCnt++] = vv[2][t];
				for (int c = 0; c < clr_dim; c++) {
					lineColors[lineClrCnt++] = bb[c][t];
				}
			}

			VisADLineStripArray lineArray = new VisADLineStripArray();
			lineArray.stripVertexCounts = new int[] { (totalPts) + 1 };
			lineArray.vertexCount = lineArray.stripVertexCounts[0];
			lineArray.coordinates = lineCoords;
			if (lineColors.length > 0) {
				lineArray.colors = lineColors;
			}
			if (totalPts >= 2) {
				if (isDashed) {
					css.cntrLinesStyled.add(lineArray);
				} else {
					css.cntrLines.add(lineArray);
				}
			}
		}
	}

	/**
	 * Get a line array using this instances cached indexes.
	 * 
	 * @param vx
	 *            X values to apply cached indexes to.
	 * @param vy
	 *            Y values to apply cached indexes to.
	 * @see {@link VisADLineArray}
	 * @return
	 */
	
	float[][] getLineArray(float[] vx, float[] vy) {
		if (vx == null || vy == null) {
			return null;
		}
		int[] idx_array = idxs.toArray();

		float[] vvx = new float[idx_array.length];
		float[] vvy = new float[vvx.length];

		for (int ii = 0; ii < idx_array.length; ii++) {
			vvx[ii] = vx[idx_array[ii]];
			vvy[ii] = vy[idx_array[ii]];
		}
		return new float[][] { vvx, vvy };
	}

        /**
         * Get a line array using this instances cached indexes from start to stop (0-based, inclusive).
         * 
         * @param vx
         *            X values to apply cached indexes to.
         * @param vy
         *            Y values to apply cached indexes to.
         * @param start
         *            start pair index.
         * @param stop
         *            stop pair index.
         * @see {@link VisADLineArray}
         *
         * @return
         */
        float[][] getLineArray(float[] vx, float[] vy, int start, int stop) {
                if (vx == null || vy == null) {
                        return null;
                }
                int[] idx_array = idxs.toArray(start, stop);

                float[] vvx = new float[idx_array.length];
                float[] vvy = new float[vvx.length];

                for (int ii = 0; ii < idx_array.length; ii++) {
                        vvx[ii] = vx[idx_array[ii]];
                        vvy[ii] = vy[idx_array[ii]];
                }
                return new float[][] { vvx, vvy };
        }


	/**
	 * Get line strip arrays for this strip.
	 * 
	 * @param vx
	 *            X grid coords to apply cached indexes to.
	 * @param vy
	 *            Y grid coords to apply cached indexes to.
	 * @return If this strip is has a label the first dim will be 2 arrays, one
	 *         for before the label and one for after. Otherwise the first
	 *         dimension will be a single array for the entire strip.
	 */
	
	float[][][] getLineStripArrays(float[] vx, float[] vy) {

		int[] idx_array = idxs.toArray();
		int count = idx_array.length;

		int lenBefore = start_break / 2 + 1;
		int lenAfter = (count - stop_break + 1) / 2;

		// if we're labeling and there are enough points before
		// and after to make at least 1 line
		if (isLabeled && (lenBefore >= 2 && lenAfter >= 2)) {
			float[][] vvBefore = new float[2][lenBefore];

			// the first point in the array
			vvBefore[0][0] = vx[idx_array[0]];
			vvBefore[1][0] = vy[idx_array[0]];

			// every other point up to the start of the break
			int kk = 1;
			for (int ii = 1; ii < vvBefore[0].length; kk += 2, ii++) {
				vvBefore[0][ii] = vx[idx_array[kk]];
				vvBefore[1][ii] = vy[idx_array[kk]];
			}

			// skip to stop_break
			kk += n_skip - 1;

			float[][] vvAfter = new float[2][lenAfter];
			vvAfter[0][0] = vx[idx_array[kk]];
			vvAfter[1][0] = vy[idx_array[kk]];

			// every other point to the end
			kk++;
			for (int ii = 1; ii < vvAfter[0].length; kk += 2, ii++) {
				vvAfter[0][ii] = vx[idx_array[kk]];
				vvAfter[1][ii] = vy[idx_array[kk]];
			}

			// return new float[][][]{vvAfter};
			return new float[][][] { vvBefore, vvAfter };
		}

		// no label
		float[][] vv = null;
		if (count == 2) { // can't have less than 2 verticies
			vv = new float[2][count];
		} else {
			vv = new float[2][count / 2 + 1];
		}

		vv[0][0] = vx[idx_array[0]];
		vv[1][0] = vy[idx_array[0]];

		for (int kk = 1, ii = 1; ii < vv[0].length; kk += 2, ii++) {
			vv[0][ii] = vx[idx_array[kk]];
			vv[1][ii] = vy[idx_array[kk]];
		}

		return new float[][][] { vv };
	}

	/**
	 * Get the array of colors corresponding to cached indexes.
	 * 
	 * @param colors
	 *            Line array formatted colors where the first dimension is the
	 *            color dimension and the second the color values.
	 * @see {@link VisADLineArray}
	 * @return Array of colors in line array format.
	 */
	
	byte[][] getColorArray(byte[][] colors) {
		if (colors == null)
			return null;
		int clr_dim = colors.length;
		int[] idx_array = idxs.toArray();
		byte[][] new_colors = new byte[clr_dim][idx_array.length];

		for (int ii = 0; ii < idx_array.length; ii++) {
			for (int cc = 0; cc < clr_dim; cc++) {
				new_colors[cc][ii] = colors[cc][idx_array[ii]];
			}
		}
		return new_colors;
	}

        /**
         * Get the array of colors corresponding to cached indexes from start pair to stop pair (0-based, inclusive).
         * 
         * @param colors
         *            Line array formatted colors where the first dimension is the
         *            color dimension and the second the color values.
         * @see {@link VisADLineArray}
         * @return Array of colors in line array format.
         */
        byte[][] getColorArray(byte[][] colors, int start, int stop) {
                if (colors == null)
                        return null;
                int clr_dim = colors.length;
                int[] idx_array = idxs.toArray(start, stop);
                byte[][] new_colors = new byte[clr_dim][idx_array.length];

                for (int ii = 0; ii < idx_array.length; ii++) {
                        for (int cc = 0; cc < clr_dim; cc++) {
                                new_colors[cc][ii] = colors[cc][idx_array[ii]];
                        }
                }
                return new_colors;
        }


	/**
	 * Get the array of colors corresponding to cached indexes.
	 * 
	 * @param colors
	 *            Line array formatted colors where the first dimension is the
	 *            color dimension and the second the color values.
	 * @see {@link VisADStripLineArray}
	 * @return Array of colors in line strip array format.
	 */
	
	byte[][][] getColorStripArrays(byte[][] colors) {

		int clrDim = colors.length;
		int[] idx_array = idxs.toArray();
		int count = idx_array.length;

		int lenBefore = start_break / 2 + 1;
		int lenAfter = (count - stop_break + 1) / 2;

		if (isLabeled && (lenBefore >= 2 && lenAfter >= 2)) {
			byte[][] colorsBefore = new byte[clrDim][start_break / 2 + 1];

			// first point
			for (int cc = 0; cc < clrDim; cc++) {
				colorsBefore[cc][0] = colors[cc][idx_array[0]];
			}

			// every other redundant point
			int kk = 1;
			for (int ii = 1; ii < colorsBefore[0].length; kk += 2, ii++) {
				for (int cc = 0; cc < clrDim; cc++) {
					colorsBefore[cc][ii] = colors[cc][idx_array[kk]];
				}
			}

			kk += n_skip - 1;

			byte[][] colorsAfter = new byte[clrDim][(count - stop_break + 1) / 2];
			for (int cc = 0; cc < clrDim; cc++) {
				colorsAfter[cc][0] = colors[cc][idx_array[kk]];
			}

			// every other redundant point
			kk++;
			for (int ii = 1; ii < colorsAfter[0].length; kk += 2, ii++) {
				for (int cc = 0; cc < clrDim; cc++) {
					colorsAfter[cc][ii] = colors[cc][idx_array[kk]];
				}
			}

			// return new byte[][][]{colorsAfter};
			return new byte[][][] { colorsBefore, colorsAfter };
		}

		// no label
		byte[][] bb = null;
		if (count == 2) {
			bb = new byte[clrDim][count];
		} else {
			bb = new byte[clrDim][count / 2 + 1];
		}

		for (int cc = 0; cc < clrDim; cc++) {
			bb[cc][0] = colors[cc][idx_array[0]];
		}

		for (int ii = 1, kk = 1; kk < idx_array.length; kk += 2, ii++) {
			for (int cc = 0; cc < clrDim; cc++) {
				bb[cc][ii] = colors[cc][idx_array[kk]];
			}
		}

		return new byte[][][] { bb };
	}

	boolean isLabeled() {
		return isLabeled;
	}

	/**
	 * 
	 * @param c_strp
	 * 
	 * @return
	 */
	
	void merge(ContourStrip that) {
		if (this.lev_idx != that.lev_idx) {
			System.out.println("Contour2D.ContourStrip.merge: !BIG ATTENTION!");
		}

		int[] thisLo = new int[2];
		int[] thisHi = new int[2];
		int[] thatLo = new int[2];
		int[] thatHi = new int[2];

		thisLo[0] = this.idxs.first.idx0;
		thisLo[1] = this.idxs.first.idx1;
		thisHi[0] = this.idxs.last.idx1;
		thisHi[1] = this.idxs.last.idx0;

		thatLo[0] = that.idxs.first.idx0;
		thatLo[1] = that.idxs.first.idx1;
		thatHi[0] = that.idxs.last.idx1;
		thatHi[1] = that.idxs.last.idx0;

		/*
		 * THAT THIS H----------------------L L---------------------H
		 */
		if (((thisLo[0] == thatLo[0]) || (thisLo[0] == thatLo[1]))
				|| ((thisLo[1] == thatLo[0]) || (thisLo[1] == thatLo[1]))) {

			IndexPairList.Node n = that.idxs.first.next; // skip redundant point
															// idxs
			while (n != null) {
				this.idxs.addFirst(n.idx1, n.idx0);
				n = n.next;
			}

			/*
			 * THAT THIS L----------------------H L---------------------H
			 */
		} else if (((thisLo[0] == thatHi[0]) || (thisLo[0] == thatHi[1]))
				|| ((thisLo[1] == thatHi[0]) || (thisLo[1] == thatHi[1]))) {

			this.idxs.first.prev = that.idxs.last.prev; // skip redundant point
														// idxs
			this.idxs.first.prev.next = this.idxs.first;
			this.idxs.first = that.idxs.first;
			this.idxs.numIndices = this.idxs.numIndices + that.idxs.numIndices
					- 2;

			/*
			 * THIS THAT L----------------------H H---------------------L
			 */
		} else if (((thisHi[0] == thatHi[0]) || (thisHi[0] == thatHi[1]))
				|| ((thisHi[1] == thatHi[0]) || (thisHi[1] == thatHi[1]))) {

			IndexPairList.Node n = that.idxs.last.prev; // skip redundant point
														// idxs
			while (n != null) {
				this.idxs.addLast(n.idx1, n.idx0);
				n = n.prev;
			}

			/*
			 * THIS THAT L----------------------H L---------------------H
			 */
		} else if (((thisHi[0] == thatLo[0]) || (thisHi[0] == thatLo[1]))
				|| ((thisHi[1] == thatLo[0]) || (thisHi[1] == thatLo[1]))) {

			this.idxs.last.next = that.idxs.first.next; // skip redundant point
														// idxs
			this.idxs.last.next.prev = this.idxs.last;
			this.idxs.last = that.idxs.last;
			this.idxs.numIndices = this.idxs.numIndices + that.idxs.numIndices
					- 2;

		}
	}

	/**
	 * 
	 * @return
	 */
	
	public String toString() {
		return "<" + this.getClass().getName() + "(" + idxs.first.idx0 + ","
				+ idxs.first.idx1 + "), (" + idxs.last.idx0 + ","
				+ idxs.first.idx1 + ")>";
	}

} //---------  ContourStrip ------------------/ 

/**
 * A double ended list for pairs of integers implemented as a doubly linked
 * list.
 */

class IndexPairList {

	/**
	 * Node object of a pair of indices.
	 */
	static final class Node {
		Node prev;
		Node next;
		final int idx0;
		final int idx1;

		Node(int idx0, int idx1) {
			this.idx0 = idx0;
			this.idx1 = idx1;
		}
	}

	/**
	 * Total number of indices which will always be the number of nodes divided
	 * by 2.
	 */
	int numIndices = 0;
	/** Last pair node in list. */
	Node last;
	/** First pair node in list. */
	Node first;

	/**
	 * Create a node for the pair of indices and add to the beginning of this
	 * list.
	 * 
	 * @param i0
	 * @param i1
	 */
	
	void addFirst(int i0, int i1) {
		addFirst(new Node(i0, i1));
	}

	/**
	 * Add a node the the beginning of this list.
	 * 
	 * @param n
	 */
	
	private void addFirst(Node n) {
		n.next = null;
		n.prev = null;
		if (numIndices == 0) {
			first = n;
			last = n;
		} else {
			first.prev = n;
			n.next = first;
			first = n;
		}
		numIndices += 2;
	}

	/**
	 * Create a node for the pair of indices and add to the end of this list.
	 * 
	 * @param i0
	 * @param i1
	 */
	
	void addLast(int i0, int i1) {
		addLast(new Node(i0, i1));
	}

	/**
	 * Add the Node to the end of this list.
	 * 
	 * @param n
	 */
	
	private void addLast(Node n) {
		n.next = null;
		n.prev = null;
		if (numIndices == 0) {
			last = n;
			first = n;
		} else {
			last.next = n;
			n.prev = last;
			last = n;
		}
		numIndices += 2;
	}

	/**
	 * Clear the list.
	 * <p>
	 * NOTE: We do not need to null out all the node objects because the garbage
	 * collector is <u>supposed</u> to collect even cyclic references.
	 */
	
	void clear() {
		first = null;
		last = null;
		numIndices = 0;
	}

        int getNumIndices() {
              return numIndices;
        }

	/**
	 * Return array of this lists indices. Each nodes idx0 precedes it's idx1
	 * with a total array length of <code>numIndices</code>.
	 * 
	 * @return
	 */
	
	int[] toArray() {
		int[] idxs = new int[numIndices];
		int idx = 0;
		Node n = first;
		while (n != null) {
			idxs[idx++] = n.idx0;
			idxs[idx++] = n.idx1;
			n = n.next;
		}
		return idxs;
	}

        /**
         * Return array of this list's indices from start pair to stop pair index (0-based and inclusive). 
         *
         * @param start index of first pair.
         * @param stop index of last pair.
         * 
         * @return array of length num of pairs (inclusive) times two.
         */
        int[] toArray(int start, int stop) {
                int[] idxs = new int[(stop - start + 1)*2];
                int pairIdx = 0;
                Node n = first;
                int cnt = 0;
                while (pairIdx <= stop && n != null) {
                     if (pairIdx >= start) {
                        idxs[cnt++] = n.idx0;
                        idxs[cnt++] = n.idx1;
                     }
                     n = n.next;
                     pairIdx += 1;
                }
                return idxs;
        }
}


