/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: SoundingBean.java,v 1.1 1999-01-07 16:13:20 steve Exp $
 */

package visad.meteorology;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import visad.CommonUnit;
import visad.CoordinateSystem;
import visad.ErrorEstimate;
import visad.FlatField;
import visad.Gridded1DSet;
import visad.Real;
import visad.RealType;
import visad.SI;
import visad.SetType;
import visad.Unit;
import visad.UnitException;
import visad.VisADException;
import visad.data.netcdf.Plain;
import visad.data.netcdf.Quantity;
import visad.java3d.DisplayImplJ3D;


/**
 * VisAD display bean for a Skew T, Log P Diagram and wind hodograph.
 *
 * @author Steven R. Emmerson
 */
public class
SoundingBean
    extends	JFrame
    implements	Serializable
{
    /**
     * The Skew-T Chart.
     */
    private /*final*/ SkewTDisplay	skewT;

    /**
     * The wind hodograph.
     */
    private /*final*/ Hodograph3D	hodograph;

    /**
     * Whether 3-D is possible.
     */
    private boolean			canDo3D = true;

    private NamedUnit			pressureNamedUnit = 
	new NamedUnit("hPa", CommonUnits.MILLIBAR);
    private NamedUnit			temperatureNamedUnit =
	new NamedUnit("Cel", CommonUnits.CELSIUS);
    private NamedUnit			thetaNamedUnit =
	new NamedUnit("K", SI.kelvin);
    private NamedUnit			thetaESNamedUnit =
	new NamedUnit("K", SI.kelvin);
    private NamedUnit			eSatNamedUnit =
	new NamedUnit("hPa", CommonUnits.MILLIBAR);
    private NamedUnit			rSatNamedUnit =
	new NamedUnit("g/kg", CommonUnits.GRAMS_PER_KILOGRAM);
    private NamedUnit			speedNamedUnit =
	new NamedUnit("kt", CommonUnits.KNOT);
    private NamedUnit			directionNamedUnit =
	new NamedUnit("deg", CommonUnits.DEGREE);
    private NamedUnit			dimensionlessNamedUnit =
	new NamedUnit("", CommonUnit.dimensionless);

    private static final DecimalFormat	deciFormat = new DecimalFormat("0.0");
    private static final DecimalFormat	centiFormat =
	new DecimalFormat("0.00");
    private static final DecimalFormat	milliFormat = 
	new DecimalFormat("0.000");

    private Readout	pressureReadout = new Readout(
	"Pressure", pressureNamedUnit, deciFormat);
    private Readout	temperatureReadout = new Readout(
	"Temperature", temperatureNamedUnit, deciFormat);
    private Readout	thetaReadout = new Readout(
	"Potential Temperature", thetaNamedUnit, deciFormat);
    private Readout	thetaESReadout = new Readout(
	"Saturation Equivalent Potential Temperature",
	thetaESNamedUnit, deciFormat);
    private Readout	eSatReadout = new Readout(
	"Saturation Water Vapor Pressure", eSatNamedUnit, milliFormat);
    private Readout	rSatReadout = new Readout(
	"Saturation Mixing Ratio", rSatNamedUnit, milliFormat);
    private Readout	speedReadout = new Readout(
	"Wind Speed", speedNamedUnit, deciFormat);
    private Readout	directionReadout = new Readout(
	"Wind Direction", directionNamedUnit, deciFormat);
    private Readout	soundingTemperatureReadout = new Readout(
	"Temperature", temperatureNamedUnit, deciFormat);
    private Readout	soundingDewPointReadout = new Readout(
	"Dew Point", temperatureNamedUnit, deciFormat);
    private Readout	profileSpeedReadout = new Readout(
	"Wind Speed", speedNamedUnit, deciFormat);
    private Readout	profileDirectionReadout = new Readout(
	"Wind Direction", directionNamedUnit, deciFormat);
    private Readout	reynoldsNumberReadout = new Readout(
	"Reynolds Number", dimensionlessNamedUnit, centiFormat);


    /**
     * Constructs from nothing.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Java RMI failure.
     */
    public
    SoundingBean()
	throws RemoteException, VisADException
    {
	addWindowListener(
	    new WindowAdapter() {
		public void windowClosing(WindowEvent e) { System.exit(0); }
	    });

	/*
	 * Test for Java3D availability.
	 */
	try
	{
	    DisplayImplJ3D test = new DisplayImplJ3D("test");
	}
	catch (Exception err)
	{
	    canDo3D = false;
	}

	/*
	 * Set up the top-level container.
	 */
	Box	mainPane = new Box(BoxLayout.Y_AXIS);
	setContentPane(mainPane);

	/*
	 * Add the graphical displays.
	 */
	Component	component = createDisplays();
	component.validate();
	mainPane.add(component);

	/*
	 * Add the quantity readouts.
	 */
	component = createQuantities();
	component.validate();
	mainPane.add(component);

	/*
	 * Title and position the window, then make it appear.
	 */
	setTitle("VisAD Sounding");
	pack();
	Dimension	screenSize =
	    Toolkit.getDefaultToolkit().getScreenSize();
	Dimension	appSize = getPreferredSize();
	setLocation(screenSize.width/2 - appSize.width/2,
		    screenSize.height/2 - appSize.height/2);
	setVisible(true);
    }


    /**
     * Creates a component with the VisAD displays.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Java RMI failure.
     */
    protected Component
    createDisplays()
	throws VisADException, RemoteException
    {
	Box	container = new Box(BoxLayout.X_AXIS);

	/*
	 * Set up the Skew-T Chart.
	 */
	skewT = new SkewTDisplay();
	container.add(createDisplay("Skew-T Chart", skewT.getComponent())); 

	/*
	 * Set up the wind hodograph.
	 */
	hodograph = new Hodograph3D();
	container.add(createDisplay("3-D Wind Hodograph",
	    hodograph.getComponent()));

	return container;
    }


    /**
     * Creates a component with a single VisAD display.
     * @param title		The title for the component.
     * @param component		The VisAD component.
     */
    protected Component
    createDisplay(String title, Component component)
    {
	JPanel	container = new JPanel();
	container.setLayout(new GridLayout(1, 1));
	container.setBorder(
	    BorderFactory.createTitledBorder(
		BorderFactory.createEmptyBorder(),
		title,
		TitledBorder.CENTER,
		TitledBorder.TOP));
	container.add(component);
	return container;
    }


    /**
     * Provides support for value readouts.
     */
    protected static class
    Readout
    {
	private final String		name;
	private final NamedUnit		namedUnit;
	private final DecimalFormat	format;
	private AbstractTableModel	table;
	private int			row;
	private int			col;
	private Real			value = null;

	/**
	 * Constructs from a name, a named unit, and a printing format.
	 * @param name		The name.
	 * @param namedUnit	The named unit.
	 * @param format	The printing format.
	 */
	protected
	Readout(String name, NamedUnit namedUnit, DecimalFormat format)
	{
	    this.name = name;
	    this.namedUnit = namedUnit;
	    this.format = format;
	}

	/**
	 * Sets the AbstractTableModel that this readout belongs to.
	 * @param table		The AbstractTableModel that this readout
	 *			belongs to.
	 * @param row		The row in the table for the readout.
	 * @param col		The column in the table for the value.
	 */
	protected void
	setTableCell(AbstractTableModel table, int row, int col)
	{
	    this.table = table;
	    this.row = row;
	    this.col = col;
	}


	/**
	 * Returns the name of the quantity.
	 * @return		The name of the quantity.
	 */
	protected String
	getName()
	{
	    return name;
	}


	/**
	 * Returns the unit of the quantity.
	 * @return		The unit of the quantity.
	 */
	protected Unit
	getUnit()
	{
	    return namedUnit.getUnit();
	}


	/**
	 * Returns the unit string of the quantity.
	 * @return		The unit string of the quantity.
	 */
	protected String
	getUnitString()
	{
	    return namedUnit.getName();
	}


	/**
	 * Sets the value for the readout.
	 * @param value			The value for the readout.
	 */
	public void
	setValue(Real value)
	{
	    try
	    {
		table.setValueAt(
		    format.format(
			getUnit().toThis(value.getValue(), value.getUnit())),
		    row,
		    col);
		this.value = value;
	    }
	    catch (UnitException e)
	    {
		String	reason = e.getMessage();
		System.err.println("Couldn't set readout value" +
		    (reason == null ? "" : (": " + reason)));
	    }
	}

	/**
	 * Gets the readout value.
	 * @return		The readout value.
	 */
	public Real
	getValue()
	{
	    return value;
	}
    }


    /**
     * Provides support for a table of quantity readouts.
     */
    protected static class
    MyTableModel
	extends	DefaultTableModel
    {
	MyTableModel(Readout[] readouts)
	{
	    super(new String[] {"Name", "Value", "Unit"}, readouts.length);
	    for (int row = 0; row < readouts.length; ++row)
	    {
		Readout	readout = readouts[row];
		setValueAt(readout.getName(), row, 0);
		setValueAt(new Float(Float.NaN), row, 1);
		setValueAt(readout.getUnitString(), row, 2);
		readout.setTableCell(this, row, 1);
	    }
	}
	public Class
	getColumnClass(int col)
	{
	    return col == 1 ? Number.class : String.class;
	}
	public boolean
	isCellEditable(int row, int col)
	{
	    return false;
	}
    }


    /**
     * Provides support for a table within a titled border.
     */
    protected static class
    TitledTable
	extends	JPanel
    {
	private final static DefaultTableCellRenderer	valueRenderer =
	    new DefaultTableCellRenderer()
	    {
		private final NumberFormat	format = 
		    NumberFormat.getNumberInstance();
		protected void setValue(Object value)
		{
		    if (value != null)
		    {
			String	text;
			if (!(value instanceof Number))
			{
			    text = value.toString();
			}
			else
			{
			    double	val = ((Number)value).doubleValue();
			    text = format.format(val);
			}
			setText(text);
		    }
		}
	    };
	static
	{
	    valueRenderer.setHorizontalAlignment(JLabel.RIGHT);
	    valueRenderer.setText("123.456");
	    Dimension size = valueRenderer.getPreferredSize();
	    size.height = valueRenderer.getMinimumSize().height;
	    valueRenderer.setMinimumSize(size);
	}
	protected
	TitledTable(String title, Readout[] readouts)
	{
	    // setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	    setBorder(
		BorderFactory.createTitledBorder(
		    BorderFactory.createEmptyBorder(0, 10, 0, 0),
		    title,
		    TitledBorder.CENTER,
		    TitledBorder.TOP));
	    JTable	table = new JTable(new MyTableModel(readouts));
	    table.setColumnSelectionAllowed(false);
	    table.setRowSelectionAllowed(false);
	    table.setCellSelectionEnabled(false);
	    String	longestName = "";
	    String	longestUnitString = "";
	    for (int i = 0; i < readouts.length; ++i)
	    {
		String	name = readouts[i].getName();
		if (longestName.length() < name.length())
		    longestName = name;
		String	unitString = readouts[i].getUnitString();
		if (longestUnitString.length() < unitString.length())
		    longestUnitString = unitString;
	    }
	    DefaultTableCellRenderer	cellRenderer = 
		new DefaultTableCellRenderer();

	    cellRenderer.setText(longestName);
	    cellRenderer.validate();
	    int	width =  cellRenderer.getPreferredSize().width;
	    TableColumn	column = table.getColumn("Name");
	    column.setMinWidth(width);
	    column.setPreferredWidth(width);

	    cellRenderer.setText(longestUnitString);
	    cellRenderer.validate();
	    width =  cellRenderer.getPreferredSize().width;
	    column = table.getColumn("Unit");
	    column.setMinWidth(width);
	    column.setPreferredWidth(width);

	    column = table.getColumn("Value");
	    column.setMinWidth(valueRenderer.getPreferredSize().width);
	    column.setPreferredWidth(valueRenderer.getPreferredSize().width);
	    column.setCellRenderer(valueRenderer);

	    table.getColumnModel().setColumnMargin(0);

	    add(table);
	}
    }


    /**
     * Provides support for more than one table within a titled border.
     */
    protected static class
    TitledTables
	extends	JPanel
    {
	protected
	TitledTables(String title, String[] categories, Readout[][] readouts)
	{
	    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	    setAlignmentY(TOP_ALIGNMENT);
	    setBorder(
		BorderFactory.createTitledBorder(
		    BorderFactory.createEtchedBorder(),
		    title,
		    TitledBorder.LEFT,
		    TitledBorder.TOP));
	    for (int i = 0; i < categories.length; ++i)
		add(new TitledTable(categories[i], readouts[i]));
	    // add(Box.createVerticalGlue());	// causes bottom space when huge
	}
    }


    /**
     * Provides support for changing the values of common readouts.
     */
    protected class
    CursorPressureChangeListener
	implements	PropertyChangeListener
    {
	public void
	propertyChange(PropertyChangeEvent event)
	{
	    try
	    {
		Real		pressure = (Real)event.getNewValue();
		pressureReadout.setValue(pressure);
		soundingTemperatureReadout.setValue(
		    skewT.getSoundingTemperature(pressure));
		soundingDewPointReadout.setValue(
		    skewT.getSoundingDewPoint(pressure));
		profileSpeedReadout.setValue(
		    hodograph.getProfileSpeed(pressure));
		profileDirectionReadout.setValue(
		    hodograph.getProfileDirection(pressure));
	    }
	    catch (Exception e)
	    {}
	}
    }


    /**
     * Creates a component with the named quantities and their values.
     */
    protected Component
    createQuantities()
    {
	JPanel	container = new JPanel();

	skewT.addCursorPressureChangeListener(
	    new CursorPressureChangeListener()
	    {
		public void
		propertyChange(PropertyChangeEvent event)
		{
		    super.propertyChange(event);
		    temperatureReadout.setValue(skewT.getCursorTemperature());
		    thetaReadout.setValue(skewT.getCursorTheta());
		    thetaESReadout.setValue(skewT.getCursorThetaES());
		    try
		    {
			Real	temperature = temperatureReadout.getValue();
			eSatReadout.setValue(
			    new Real(
				CommonTypes.PRESSURE,
				ESat.eSat(
				    new double[] {temperature.getValue()},
				    temperature.getUnit(),
				    eSatReadout.getUnit())[0],
				eSatReadout.getUnit()));
		    }
		    catch (Exception e)	// can't happen
		    {}
		    rSatReadout.setValue(skewT.getCursorRSat());
		}
	    });
	hodograph.addCursorPressureChangeListener(
	    new CursorPressureChangeListener()
	    {
		public void
		propertyChange(PropertyChangeEvent event)
		{
		    super.propertyChange(event);
		    speedReadout.setValue(hodograph.getCursorSpeed());
		    directionReadout.setValue(hodograph.getCursorDirection());
		}
	    });

	container.add(
	    new TitledTables(
		"Sounding Data Values",
		new String[] {
		    "Thermodynamic Quantities",
		    "Kinematic Quantities",
		    "Hybrid Quantities"
		},
		new Readout[][] {
		    new Readout[] {
			soundingTemperatureReadout,
			soundingDewPointReadout
		    },
		    new Readout[] {
			profileSpeedReadout,
			profileDirectionReadout
		    },
		    new Readout[] {
			reynoldsNumberReadout,
		    }
		}
	    )
	);

	container.add(
	    new TitledTables(
		"Cursor Background Values",
		new String[] {
		    "Independent Variable",
		    "Thermodynamic Quantities",
		    "Kinematic Quantities"
		},
		new Readout[][] {
		    new Readout[] {
			pressureReadout,
		    },
		    new Readout[] {
			temperatureReadout,
			thetaReadout,
			thetaESReadout,
			eSatReadout,
			rSatReadout
		    },
		    new Readout[] {
			speedReadout,
			directionReadout
		    }
		}
	    )
	);

	container.setMaximumSize(container.getPreferredSize());

	return container;
    }


    /**
     * Sets the sounding property.
     *
     * @param sounding		The atmospheric sounding.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Java RMI failure.
     */
    public synchronized void
    setSounding(SoundingImpl sounding)
	throws RemoteException, VisADException
    {
	skewT.setSounding(sounding);
	hodograph.setSounding(sounding);
    }


    /**
     * Tests this class.
     */
    public static void
    main(String[] args)
	throws Exception
    {
	String		path = args.length > 0 ? args[0] : "sounding.nc";
	SoundingBean	soundingBean = new SoundingBean();
	Plain		plain = new Plain(MetQuantityDB.instance());

	FlatField	field = (FlatField)plain.open(path);
	SoundingImpl	sounding = new SoundingImpl(field, 0, 1, 3, 2);
	soundingBean.setSounding(sounding);
    }


    /**
     * Supports units with an associated, custom, string specification.
     */
    protected static class
    NamedUnit
    {
	/**
	 * The unit string.
	 */
	private final String	name;

	/**
	 * The unit.
	 */
	private final Unit	unit;


	/**
	 * Constructs from a name and a unit.
	 */
	public
	NamedUnit(String name, Unit unit)
	{
	    this.name = name;
	    this.unit = unit;
	}


	/**
	 * Gets the name portion of the named unit.
	 */
	public String
	getName()
	{
	    return name;
	}


	/**
	 * Gets the unit of the named unit.
	 */
	public Unit
	getUnit()
	{
	    return unit;
	}
    }
}
