/**
 * DateRange.java
 *
 * 1.00 2001/6/29
 *
 */

package dods.clients.importwizard;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * This class creates a panel with date range
 * selection boxes.
 *
 * @version     1.00 29 Jun 2001
 * @author      Kashan A. Shaikh
 */
public class DateRange extends JPanel implements ActionListener{
    final static int NUM_MONTHS = 12;

    boolean yearly;
    boolean monthly;
    boolean multiYearMonthly;

    // date range storage
    private int lowYear,lowMonth,lowDay,highYear,highMonth,highDay;

    // Panels
    private JPanel lowYearPanel,lowMonthPanel,lowDayPanel,highYearPanel,highMonthPanel,highDayPanel;

    // Constructor for full Yearly Date Range
	public DateRange(int lyear, int lmonth, int lday, int hyear, int hmonth, int hday) {
		lowYear = lyear; lowMonth = lmonth; lowDay = lday;
		highYear = hyear; highMonth = hmonth; highDay = hday;
	
		yearly = true;
		multiYearMonthly = false;
		monthly = false;
		
		// format the panel
		setLayout(new FlowLayout());
		setBorder(BorderFactory.createCompoundBorder(
	            BorderFactory.createTitledBorder("Select Date Range"), 
	            BorderFactory.createEmptyBorder(10,10,10,10)));
	
		// create & add the selection boxes
		populateDateRange();
    }


    // Constructor for Muli-Year Monthly Date Range
         public DateRange(int lyear, int lmonth, int hyear, int hmonth) {
    		lowDay = 1; highDay = 1;
    		lowYear = lyear; lowMonth = lmonth;
    		highYear = hyear; highMonth = hmonth;
    		
    		yearly = false;
    		multiYearMonthly = true;
    		monthly = false;
    		
    		// format the panel
    		setLayout(new FlowLayout());
		setBorder(BorderFactory.createCompoundBorder(
	            BorderFactory.createTitledBorder("Select Date Range"),
	            BorderFactory.createEmptyBorder(10,10,10,10)));
	
		// create & add the selection boxes
		populateDateRange();
    	}
    	
    // Constructor for Monthly Date Range
         public DateRange(int lmonth, int hmonth) {
    		lowYear = 1; lowDay = 15; highYear = 1; highDay = 15;
    		lowMonth = lmonth;
    		highMonth = hmonth;
    		
    		yearly = false;
    		multiYearMonthly = false;
    		monthly = true;
    		
    		// format the panel
    		setLayout(new FlowLayout());
		setBorder(BorderFactory.createCompoundBorder(
	            BorderFactory.createTitledBorder("Select Date Range"),
	            BorderFactory.createEmptyBorder(10,10,10,10)));
	
		// create & add the selection boxes
		populateDateRange();
    	}



    // Create & add the selection boxes to the panel
    private void populateDateRange() {
		if ( yearly || multiYearMonthly ) {
			// create low year panel
			lowYearPanel = new JPanel();
			populateYearPanel(lowYearPanel,lowYear,highYear,lowYear,"lowYear");
			add(lowYearPanel);
		}

		// create low month panel
		lowMonthPanel = new JPanel();
		populateMonthPanel(lowMonthPanel,lowMonth,"lowMonth");
		add(lowMonthPanel);

		if (yearly) {
			// create low day panel
			lowDayPanel = new JPanel();
			populateDayPanel(lowDayPanel,lowYear,lowMonth,lowDay,"lowDay");
			add(lowDayPanel);
		}

		JPanel tpanel = new JPanel();
		tpanel.setLayout(new BoxLayout(tpanel,BoxLayout.Y_AXIS));
		JLabel tlabel = new JLabel(" ");
		tpanel.add(tlabel);
		tlabel = new JLabel("-");
		tpanel.add(tlabel);
		add(tpanel);
		
		if ( yearly || multiYearMonthly ) {
			// create high year panel
  			highYearPanel = new JPanel();
			populateYearPanel(highYearPanel,lowYear,highYear,highYear,"highYear");
			add(highYearPanel);
		}

		// create high month panel
		highMonthPanel = new JPanel();
		populateMonthPanel(highMonthPanel,highMonth,"highMonth");
		add(highMonthPanel);

		if (yearly) {
			// create high day panel
			highDayPanel = new JPanel();
			populateDayPanel(highDayPanel,highYear,highMonth,highDay,"highDay");
			add(highDayPanel);
		}
    }

    // Create Year Selection Box
    private void populateYearPanel(JPanel tpanel, int startYear, int endYear,
									int selectYear, String type) {
		JLabel tlabel = new JLabel("Year");
		tlabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		String[] years = new String[endYear - startYear + 1];
        	int selectedIndex = 0;
		int tmp = startYear;
		for (int i = 0; i <= (endYear - startYear); i++) {
	    	years[i] = Integer.toString(tmp);
	    	if (tmp == selectYear) {
				selectedIndex = i;
	    	}
	    	tmp++;
		}

		JComboBox tselect = new JComboBox(years);
		tselect.setSelectedIndex(selectedIndex);
		tselect.setMaximumSize(new Dimension(70,40));
		tselect.setAlignmentX(Component.CENTER_ALIGNMENT);

		// column layout
		tpanel.setLayout(new BoxLayout(tpanel,BoxLayout.Y_AXIS));

		tpanel.add(tlabel);
		tpanel.add(tselect);
	
		tselect.addActionListener(this); tselect.setActionCommand(type); }

    	// Create Month Selection Box
    	private void populateMonthPanel(JPanel tpanel, int selectMonth, String type) {
		JLabel tlabel = new JLabel("Month");
		tlabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	
		String[] months = new String[NUM_MONTHS];
		if (yearly) {
			for (int i = 0; i < NUM_MONTHS; i++) {
	   			months[i] = Integer.toString(i+1);
	   		}
		} else if ( multiYearMonthly || monthly ) {
			months[0] = "January";
			months[1] = "February";
			months[2] = "March";
			months[3] = "April";
			months[4] = "May";
			months[5] = "June";
			months[6] = "July";
			months[7] = "August";
			months[8] = "September";
			months[9] = "October";
			months[10] = "November";
			months[11] = "December";
			
		}
	
		JComboBox tselect = new JComboBox(months);
		tselect.setSelectedIndex(selectMonth-1);
		if (yearly) {
			tselect.setMaximumSize(new Dimension(50,40));
		} else if ( multiYearMonthly || monthly) {
			tselect.setMaximumSize(new Dimension(110,40));
		}
		tselect.setAlignmentX(Component.CENTER_ALIGNMENT);
		tselect.setMaximumRowCount(12);

		// column layout
		tpanel.setLayout(new BoxLayout(tpanel,BoxLayout.Y_AXIS));

		tpanel.add(tlabel);
		tpanel.add(tselect);

		tselect.addActionListener(this);
		tselect.setActionCommand(type);
    }

    // Create Day Selection Box
    private void populateDayPanel(JPanel tpanel,
				  				int year, int month, int selectDay, String type) {
		JLabel tlabel = new JLabel("Day");
		tlabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		int numdays = getDaysInMonth(year,month);
		String[] days = new String[numdays];
		for (int i = 0; i < numdays; i++) {
			days[i] = Integer.toString(i+1);
		}

		JComboBox tselect = new JComboBox(days);
		tselect.setSelectedIndex(selectDay-1);
		tselect.setMaximumSize(new Dimension(50,40));
		tselect.setAlignmentX(Component.CENTER_ALIGNMENT);

		// column layout
		tpanel.setLayout(new BoxLayout(tpanel,BoxLayout.Y_AXIS));

		tpanel.add(tlabel);
		tpanel.add(tselect);

		tselect.addActionListener(this);
		tselect.setActionCommand(type);
    }

    // Update the Day selection box
    private void updateDays(String type) {
		int numdays = 0;
		JComboBox tselect = null;
		if (type.equals("low")) {
			int tyear = 1;
			int tmonth = ((JComboBox) lowMonthPanel.getComponent(1)).getSelectedIndex() + 1;
			if (yearly || multiYearMonthly) {
				tyear = Integer.parseInt( ((JComboBox) lowYearPanel.getComponent(1)).getSelectedItem().toString() );
			}
			numdays = getDaysInMonth(tyear, tmonth);
			tselect = (JComboBox) lowDayPanel.getComponent(1);
		} else {
			int tyear = 1;
			int tmonth = ((JComboBox) highMonthPanel.getComponent(1)).getSelectedIndex() + 1;
			if (yearly || multiYearMonthly) {
				tyear = Integer.parseInt( ((JComboBox) highYearPanel.getComponent(1)).getSelectedItem().toString() );
			}
			numdays = getDaysInMonth(tyear, tmonth);
			tselect = (JComboBox) highDayPanel.getComponent(1);
		}
		
		for (int i = 28; i < numdays; i++) {
			if ( tselect.getItemAt(i) == null ) {
				tselect.addItem(Integer.toString(i+1));
			}
		}
		
		for (int i = numdays; i < 31; i++) {
			if ( tselect.getItemAt(numdays) != null ) {
				tselect.removeItemAt(numdays);
			}
		}
	}

    // Returns the number of days in the specified month
    private int getDaysInMonth(int year, int month) {
		int[] day = {31,0,31,30,31,30,31,31,30,31,30,31};
		if (month == 2) {
			if ( isLeapYear(year) == true ) { return 29; }
			return 28;
		}
		return day[month-1];
   	}

    // Determines if the year is a leap year
    private boolean isLeapYear(int year) {
		return (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) ? true : false;
    }

    
    // Implementation of ActionListener interface.
    public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand() == "lowYear") {
			// update the high year
			if (compareYears() > 0) {
					((JComboBox) highYearPanel.getComponent(1)).setSelectedIndex(
						((JComboBox) lowYearPanel.getComponent(1)).getSelectedIndex());
			}
			if (yearly) {
				// update low days
				updateDays("low");
			}
		} else if (event.getActionCommand() == "lowMonth") {
			// update the high month
			if ((compareMonths() > 0) && (compareYears() >= 0)) {
					((JComboBox) highMonthPanel.getComponent(1)).setSelectedIndex(
						((JComboBox) lowMonthPanel.getComponent(1)).getSelectedIndex());
			}
			if (yearly) {
				// update low days
				updateDays("low");
			}
		} else if (event.getActionCommand() == "lowDay") {
			// update the high day
			if ((compareDays() > 0) && (compareYears() >=0) && (compareMonths() >=0)) {
					((JComboBox) highDayPanel.getComponent(1)).setSelectedIndex(
						((JComboBox) lowDayPanel.getComponent(1)).getSelectedIndex());
			}
		} else if (event.getActionCommand() == "highYear") {
			if (yearly) {
				// update high days
				updateDays("high");
			}
		} else if (event.getActionCommand() == "highMonth") {
			if (yearly) {
				// update high days
				updateDays("high");
			}
		}
    }
	
	
	/** Compare the low and high years
	 *  Returns: -1 if low < high
	 *			  0 if low == high
	 *			 +1 if low > high
	 */
	private int compareYears() {
		if (yearly || multiYearMonthly) {
			if ( ((JComboBox) lowYearPanel.getComponent(1)).getSelectedIndex() <
					((JComboBox) highYearPanel.getComponent(1)).getSelectedIndex() ) {
				return -1;
			} else if ( ((JComboBox) lowYearPanel.getComponent(1)).getSelectedIndex() ==
					((JComboBox) highYearPanel.getComponent(1)).getSelectedIndex() ) {
				return 0;
			} else {
				return 1;
			}
		} else {
			return 0;
		}
	}
	
	/** Compare the low and high months
	 *  Returns: -1 if low < high
	 *			  0 if low == high
	 *			 +1 if low > high
	 */
	private int compareMonths() {
		if ( ((JComboBox) lowMonthPanel.getComponent(1)).getSelectedIndex() < 
					((JComboBox) highMonthPanel.getComponent(1)).getSelectedIndex() ) {
			return -1;
		} else if ( ((JComboBox) lowMonthPanel.getComponent(1)).getSelectedIndex() == 
					((JComboBox) highMonthPanel.getComponent(1)).getSelectedIndex() ) {
			return 0;
		} else {
			return 1;
		}
	}
	
	/** Compare the low and high days
	 *  Returns: -1 if low < high
	 *			  0 if low == high
	 *			 +1 if low > high
	 */
	private int compareDays() {
		if (yearly) {
			if ( ((JComboBox) lowDayPanel.getComponent(1)).getSelectedIndex() <
					((JComboBox) highDayPanel.getComponent(1)).getSelectedIndex() ) {
				return -1;
			} else if ( ((JComboBox) lowDayPanel.getComponent(1)).getSelectedIndex() ==
					((JComboBox) highDayPanel.getComponent(1)).getSelectedIndex() ) {
				return 0;
			} else {
				return 1;
			}
		} else {
			return 0;
		}
	}


    /** Access Methods */
    public int getLowYear() {
		if (yearly || multiYearMonthly) {
			return Integer.parseInt( ((JComboBox) lowYearPanel.getComponent(1)).getSelectedItem().toString() );
		} else if (monthly) {
		 	return lowYear;
		}
		return 0;
    }
    public int getLowMonth() {
		return ((JComboBox) lowMonthPanel.getComponent(1)).getSelectedIndex() + 1;
    }
    public int getLowDay() {
    		if (yearly) {
    			return ((JComboBox) lowDayPanel.getComponent(1)).getSelectedIndex() + 1;
    		} else if (multiYearMonthly || monthly) {
    			return lowDay;
    		}
    		return 0;
    }
    public int getHighYear() {
		if (yearly || multiYearMonthly) {
			return Integer.parseInt( ((JComboBox) highYearPanel.getComponent(1)).getSelectedItem().toString() );
		} else if (monthly) {
		 	return highYear;
		}
		return 0;
    }
    public int getHighMonth() {
		return ((JComboBox) highMonthPanel.getComponent(1)).getSelectedIndex() + 1;
    }
    public int getHighDay() {
    		if (yearly) {
    			return ((JComboBox) highDayPanel.getComponent(1)).getSelectedIndex() + 1;
    		} else if (multiYearMonthly || monthly) {
    			return highDay;
    		}
    		return 0;
    }
}
