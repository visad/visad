// ScreenLockedDemo.java

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2021 Bill Hibbard, Curtis Rueden, Tom
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

// written by Jim Koutsovasilis

package visad.bom;

// Java
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.rmi.RemoteException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

// VisAD
import visad.ConstantMap;
import visad.DataReferenceImpl;
import visad.DelaunayCustom;
import visad.Display;
import visad.DisplayImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.GraphicsModeControl;
import visad.Gridded2DDoubleSet;
import visad.Irregular2DSet;
import visad.RealTupleType;
import visad.RealType;
import visad.ScalarMap;
import visad.UnionSet;
import visad.VisADException;
import visad.bom.ScreenLockedRendererJ3D;
import visad.java3d.DefaultRendererJ3D;
import visad.java3d.DisplayImplJ3D;
import visad.java3d.RendererJ3D;


/**
 * Demos the ScreenLockedRendererJ3D and its related problems.
 */
public final class ScreenLockedDemo
{

	private DisplayImplJ3D display;
	private ScalarMap xMap;
	private ScalarMap yMap;


	/**
	 * Constructor.
	 */
	public ScreenLockedDemo() throws VisADException, RemoteException
	{

		display = new DisplayImplJ3D("display");

		final GraphicsModeControl gmc =
			display.getGraphicsModeControl();
		gmc.setScaleEnable(false);
		gmc.setProjectionPolicy(DisplayImplJ3D.PARALLEL_PROJECTION);

		display.getDisplayRenderer().setBoxOn(false);

		final RealType x = RealType.getRealType("x");
		final RealType y = RealType.getRealType("y");

		xMap = new ScalarMap(x, Display.XAxis);
		yMap = new ScalarMap(y, Display.YAxis);

		display.addMap(xMap);
		display.addMap(yMap);

		addScreenLockedSquare(display);
		addCross(display);

		final JFrame frame = new JFrame("Screen Locked Demo");
		final JPanel panel = new JPanel(new BorderLayout());

		panel.add(display.getComponent(), BorderLayout.CENTER);

		final JPanel buttonsPanel = new JPanel();
		final ButtonListener buttonListener = new ButtonListener();

		JButton button = new JButton("Add Triangle");
		button.addActionListener(buttonListener);
		buttonsPanel.add(button);

		button = new JButton("Add Field");
		button.addActionListener(buttonListener);
		buttonsPanel.add(button);

		button = new JButton("Set Range");
		button.addActionListener(buttonListener);
		buttonsPanel.add(button);

		panel.add(buttonsPanel, BorderLayout.SOUTH);

		frame.getContentPane().add(panel);
		frame.setSize(640, 480);
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

	} // ScreenLockedDemo.ScreenLockedDemo()


	/**
	 * Adds a coloured square to the display, using a screen
	 * locked renderer.
	 */
	private static void addScreenLockedSquare(final DisplayImpl display)
		throws VisADException, RemoteException
	{

		final int numSamples = 4;
		final double[][]samples = new double[2][numSamples];
		samples[0][0] = -10;
		samples[1][0] = 10;
		samples[0][1] = 10;
		samples[1][1] = 10;
		samples[0][2] = 10;
		samples[1][2] = -10;
		samples[0][3] = -10;
		samples[1][3] = -10;

		final RealTupleType domainType = new RealTupleType(
			RealType.getRealType("a"), RealType.getRealType("b"));

		final Gridded2DDoubleSet set = new Gridded2DDoubleSet(
			domainType, samples, numSamples);
		final Irregular2DSet filledSet = DelaunayCustom.fill(set);

		final DataReferenceImpl dataRef =
			new DataReferenceImpl("square_data_ref");
		dataRef.setData(filledSet);

		final ScalarMap yMap = new ScalarMap(RealType.getRealType("a"),
			Display.YAxis);
		final ScalarMap xMap = new ScalarMap(RealType.getRealType("b"),
			Display.XAxis);
		display.addMap(yMap);
		display.addMap(xMap);

		yMap.setRange(-10, 10);
		xMap.setRange(-10, 10);

		final RendererJ3D renderer = new ScreenLockedRendererJ3D();
		final ConstantMap[] maps = new ConstantMap[] {
			new ConstantMap(1, Display.Red),
			new ConstantMap(0, Display.Green),
			new ConstantMap(0, Display.Blue),
			new ConstantMap(-0.03, Display.ZAxis)};
		display.addReferences(renderer, dataRef, maps);

	} // ScreenLockedDemo.addScreenLockedSquare()


	/**
	 * Adds a triangle to the display, using a default renderer.
	 */
	private static void addTriangle(final DisplayImpl display)
		throws VisADException, RemoteException
	{

		final int numSamples = 3;
		final double[][]samples = new double[2][numSamples];
		samples[0][0] = 12;
		samples[1][0] = -15;
		samples[0][1] = 17;
		samples[1][1] = -10;
		samples[0][2] = 22;
		samples[1][2] = -15;

		final RealTupleType type = new RealTupleType(
			RealType.getRealType("x"), RealType.getRealType("y"));

		final Gridded2DDoubleSet set = new Gridded2DDoubleSet(
			type, samples, numSamples);
		final Irregular2DSet filledSet = DelaunayCustom.fill(set);

		final DataReferenceImpl dataRef =
			new DataReferenceImpl("triangle_data_ref");
		dataRef.setData(filledSet);

		final RendererJ3D renderer = new DefaultRendererJ3D();
		final ConstantMap[] maps = new ConstantMap[] {
			new ConstantMap(0, Display.Red),
			new ConstantMap(0, Display.Green),
			new ConstantMap(1, Display.Blue),
			new ConstantMap(-0.02, Display.ZAxis)};
		display.addReferences(renderer, dataRef, maps);

	} // ScreenLockedDemo.addTriangle()


	/**
	 * Adds a cross to the dispaly, using a  default renderer.
	 */
	private static void addCross(final DisplayImpl display)
		throws VisADException, RemoteException
	{

		final int numSamples = 2;
		final double[][] samples = new double[2][numSamples];

		samples[0][0] = -12;
		samples[1][0] = 0;
		samples[0][1] = 12;
		samples[1][1] = 0;
		final RealTupleType type = new RealTupleType(
			RealType.getRealType("x"), RealType.getRealType("y"));
		Gridded2DDoubleSet horizontalSet = new Gridded2DDoubleSet(
			type, samples, numSamples);

		samples[0][0] = 0;
		samples[1][0] = 12;
		samples[0][1] = 0;
		samples[1][1] = -12;
		Gridded2DDoubleSet verticalSet = new Gridded2DDoubleSet(
			type, samples, numSamples);

		final UnionSet set = new UnionSet(
			new Gridded2DDoubleSet[]{
				horizontalSet, verticalSet});
		final DataReferenceImpl dataRef =
			new DataReferenceImpl("lines_data_ref");
		dataRef.setData(set);
		final ConstantMap[] maps = new ConstantMap[] {
			new ConstantMap(2, Display.LineWidth),
			new ConstantMap(-0.01, Display.ZAxis)};
		display.addReference(dataRef, maps);

	} // ScreenLockedDemo.addCross()


	/**
	 * Adds a field to the display.
	 */
	private static void addField(final DisplayImpl display)
		throws VisADException, RemoteException
	{

		final RealTupleType domainType = new RealTupleType(
			RealType.getRealType("x"), RealType.getRealType("y"));
		final RealType rangeType = RealType.getRealType("height");
		final FunctionType functionType =
			new FunctionType(domainType, rangeType);
		final FlatField field = FlatField.makeField1(
			functionType, 11, 18, 10, 11, 18, 10);
		final DataReferenceImpl dataRef =
			new DataReferenceImpl("field_data_ref");
		dataRef.setData(field);
		display.addMap(new ScalarMap(rangeType, Display.RGB));
		display.addReference(dataRef);

	} // ScreenLockedDemo.addField()


	/**
	 * Listens for button clicks.
	 */
	private class ButtonListener implements ActionListener
	{

		/**
		 * The user has clicked on a button.
		 */
		public void actionPerformed(ActionEvent event)
		{
			

			try {
				final String command = event.getActionCommand();

				if (command.equals("Add Triangle")) {
					addTriangle(display);
				} else if (command.equals("Add Field")) {
					addField(display);
				} else {
					xMap.setRange(-10, 10);
					yMap.setRange(-10, 10);
				}
			} catch (VisADException ex) {
				System.err.println(ex.getMessage());
				ex.printStackTrace();
			} catch (RemoteException ex) {
				System.err.println(ex.getMessage());
				ex.printStackTrace();
			}

		} // ButtonListener.actionPerformed()

	} // class ScreenLockedDemo.ButtonListener


	/**
	 * Used to run the program.
	 *
	 * Please read the class javadoc at the top of this file.
	 *
	 * @param args arguments are ignored.
	 */
	public static final void main(String[] args)
		throws VisADException, RemoteException
	{

		new ScreenLockedDemo();

	} // ScreenLockedDemo.main()

} // class ScreenLockedDemo

