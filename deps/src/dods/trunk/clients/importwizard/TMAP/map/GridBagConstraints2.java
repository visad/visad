/**
 * GridBagConstraints2.java -
 * Specializes GridBagConstraints by adding convenience methods
 * for setting fields. No fields are added to the class.
 * Does not include means to set ipadx and ipady since
 * these are rarely useful. (Although they can still be
 * assigned directly if necessary.)
 *
 *  This software was developed by the Thermal Modeling and Analysis
 *  Project(TMAP) of the National Oceanographic and Atmospheric
 *  Administration's (NOAA) Pacific Marine Environmental Lab(PMEL),
 *  hereafter referred to as NOAA/PMEL/TMAP.
 *
 *  Access and use of this software shall impose the following
 *  obligations and understandings on the user. The user is granted the
 *  right, without any fee or cost, to use, copy, modify, alter, enhance
 *  and distribute this software, and any derivative works thereof, and
 *  its supporting documentation for any purpose whatsoever, provided
 *  that this entire notice appears in all copies of the software,
 *  derivative works and supporting documentation.  Further, the user
 *  agrees to credit NOAA/PMEL/TMAP in any publications that result from
 *  the use of this software or in any product that includes this
 *  software. The names TMAP, NOAA and/or PMEL, however, may not be used
 *  in any advertising or publicity to endorse or promote any products
 *  or commercial entity unless specific written permission is obtained
 *  from NOAA/PMEL/TMAP. The user also understands that NOAA/PMEL/TMAP
 *  is not obligated to provide the user with any support, consulting,
 *  training or assistance of any kind with regard to the use, operation
 *  and performance of this software nor to provide the user with any
 *  updates, revisions, new versions or "bug fixes".
 *
 *  THIS SOFTWARE IS PROVIDED BY NOAA/PMEL/TMAP "AS IS" AND ANY EXPRESS
 *  OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL NOAA/PMEL/TMAP BE LIABLE FOR ANY SPECIAL,
 *  INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER
 *  RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF
 *  CONTRACT, NEGLIGENCE OR OTHER TORTUOUS ACTION, ARISING OUT OF OR IN
 *  CONNECTION WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 *
 */
package dods.clients.importwizard.TMAP.map;

import java.awt.GridBagConstraints;
import java.awt.Insets;

//---------------------------------------------------------------------------
public class GridBagConstraints2 extends GridBagConstraints 
{
	// set the 12 specified fields in this object
	public void set(int gridx, int gridy, int gridwidth, int gridheight,
		String anchorStr, String fillStr,
		int left, int top, int right, int bottom,
		double weightx, double weighty)
	{
		this.gridx = gridx;
		this.gridy = gridy;
		this.gridwidth = gridwidth;
		this.gridheight = gridheight;

		if (anchorStr.equalsIgnoreCase("n"))
			anchor = GridBagConstraints.NORTH;
		else if (anchorStr.equalsIgnoreCase("s"))
			anchor = GridBagConstraints.SOUTH;
		else if (anchorStr.equalsIgnoreCase("e"))
			anchor = GridBagConstraints.EAST;
		else if (anchorStr.equalsIgnoreCase("w"))
			anchor = GridBagConstraints.WEST;
		else if (anchorStr.equalsIgnoreCase("ne"))
			anchor = GridBagConstraints.NORTHEAST;
		else if (anchorStr.equalsIgnoreCase("nw"))
			anchor = GridBagConstraints.NORTHWEST;
		else if (anchorStr.equalsIgnoreCase("se"))
			anchor = GridBagConstraints.SOUTHEAST;
		else if (anchorStr.equalsIgnoreCase("sw"))
			anchor = GridBagConstraints.SOUTHWEST;
		else if (anchorStr.equalsIgnoreCase("c"))
			anchor = GridBagConstraints.CENTER;
		else
			throw new IllegalArgumentException(
			  "Illegal anchor '" + anchorStr + "'");

		if (fillStr.equalsIgnoreCase("horz"))
			fill = GridBagConstraints.HORIZONTAL;
		else if (fillStr.equalsIgnoreCase("vert"))
			fill = GridBagConstraints.VERTICAL;
		else if (fillStr.equalsIgnoreCase("both"))
			fill = GridBagConstraints.BOTH;
		else if (fillStr.equalsIgnoreCase("none"))
			fill = GridBagConstraints.NONE;
		else
			throw new IllegalArgumentException(
			  "Illegal fill '" + fillStr + "'");

		insets.left = left;
		insets.top = top;
		insets.right = right;
		insets.bottom = bottom;

		this.weightx = weightx;
		this.weighty = weighty;
	}

	// set 10 fields, and default weigthx and weighty
	public void set(int gridx, int gridy, int gridwidth, int gridheight,
		String anchorStr, String fillStr,
		int left, int top, int right, int bottom)
	{
		set(gridx, gridy, gridwidth, gridheight, anchorStr, fillStr,
	 		left, top, right, bottom, 0.0, 0.0);
	}

	// set 6 fields, and default insets, weigthx and weighty
	public void set(int gridx, int gridy, int gridwidth, int gridheight,
		String anchorStr, String fillStr)
	{
		set(gridx, gridy, gridwidth, gridheight, anchorStr, fillStr,
	 		0, 0, 0, 0, 0.0, 0.0);
	}

	// set 4 fields, and default anchor, fill, insets, weigthx and weighty
	public void set(int gridx, int gridy, int gridwidth, int gridheight)
	{
		set(gridx, gridy, gridwidth, gridheight, "c", "none",
	 		0, 0, 0, 0, 0.0, 0.0);
	}
}
//---------------------------------------------------------------------------
