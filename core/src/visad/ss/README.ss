                    VisAD SpreadSheet User Interface README file
                                  24 January 2001
 
                                 Table of Contents

1. The visad.ss Package
  1.1 Description
  1.2 Compiling and Running
  1.3 Source Files
    1.3.1 BasicSSCell
    1.3.2 FancySSCell
    1.3.3 MappingDialog
    1.3.4 SpreadSheet
    1.3.5 SSCellChangeEvent
    1.3.6 SSCellData
    1.3.7 SSCellImpl
    1.3.8 SSCellListener
    1.3.9 SSLayout
2. Features of the SpreadSheet User Interface
  2.1 Basic Commands
  2.2 Menu Commands
    2.2.1 File Menu
    2.2.2 Edit Menu
    2.2.3 Setup Menu
    2.2.4 Cell Menu
    2.2.5 Layout Menu
    2.2.6 Options Menu
  2.3 Toolbars
    2.3.1 Main Toolbar
    2.3.2 Formula Toolbar
      2.3.2.1 Description and Usage
      2.3.2.2 How To Enter Formulas
      2.3.2.3 Formula Syntax
      2.3.2.4 Linking to External Java Code
      2.3.2.5 Examples of Valid Formulas
  2.4 Remote Collaboration
    2.4.1 Creating a SpreadSheet RMI server
    2.4.2 Sharing individual SpreadSheet cells
    2.4.3 Cloning entire SpreadSheets
    2.4.4 Creating a SpreadSheet slave
  2.5 Undocumented Features
3. Known Bugs


1. The visad.ss Package

1.1 Description

This README file explains what the visad.ss package is, what it does,
and how to use it.

The visad.ss package is a "generic" spreadsheet user interface for VisAD.
It is intended to be powerful and flexible, and it can be used to visualize
many types of data, without any programming.  It supports many features of a
traditional spreadsheet, such as formulas.  The package also provides a class
structure such that developers can easily create their own user interfaces
using SpreadSheet cells from the visad.ss package.

Each VisAD SpreadSheet cell can display an arbitrary number of VisAD Data
objects, mapped to the display in any way you choose.  Data can be imported
from a file or URL, an RMI address for a server SpreadSheet running on
another machine, or computed from a formula, similar to a traditional
spreadsheet application.

For up-to-date information about the VisAD SpreadSheet, see the VisAD
SpreadSheet web page at http://www.ssec.wisc.edu/~curtis/ss.html

For up-to-date information about VisAD in general, see the VisAD web page
at http://www.ssec.wisc.edu/~billh/visad.html

1.2 Compiling and Running

To compile the package, type the following from the visad/ss directory:

    javac -J-mx32m *.java

To run the SpreadSheet user interface, type:

    java -mx64m visad.ss.SpreadSheet

You can optionally specify the number of spreadsheet cells with:

    java -mx64m visad.ss.SpreadSheet (cols) (rows)

where (cols) is the number of columns, and (rows) is the number of rows.
The default is four cells (two columns, two rows).  Note that rows and
columns can be added or deleted at run time using the commands from the
Layout menu.

The SpreadSheet user interface requires a lot of memory (at least 32 MB),
especially if you want to work with large data sets.  If you receive an
OutOfMemoryError, you should increase the amount of memory allocated to
the program (increase the # in "-mx#m").

To load a spreadsheet file or data file into the SpreadSheet automatically
upon launch, use:

    java -mx64m visad.ss.SpreadSheet -file (filename)

where (filename) is the name of the spreadsheet file or data file to be loaded.

Other useful command line parameters include:
  -debug   Runs the SpreadSheet in "debug mode" (all errors will print stack
           traces to the console, and some additional warnings will also be
           outputted where appropriate).
  -no3d    Disables Java3D.  The SpreadSheet will run as though Java3D is not
           present on the system, allowing only "2-D (Java2D)" displays.
  -gui     Causes a dialog box to pop up, allowing you to configure the
           SpreadSheet.  Options include setting up a SpreadSheet server (clone
           or slave), specifying numbers of rows and columns, and toggling
           debug mode or Java3D support.  This flag is useful, for example, if
           you are running Windows and want to create a shortcut to the
           SpreadSheet, but still wish to have the full power of the command
           line arguments.

1.3 Source Files

The following source files are part of the visad.ss package:
    - BasicSSCell.java
    - FancySSCell.java
    - MappingDialog.java
    - SpreadSheet.java
    - SSCellChangeEvent.java
    - SSCellData.java
    - SSCellImpl.java
    - SSCellListener.java
    - SSLayout.java

The following included GIF files are needed by the package:
    - 2d.gif
    - 3d.gif
    - add.gif
    - copy.gif
    - cut.gif
    - del.gif
    - display.gif
    - j2d.gif
    - mappings.gif
    - open.gif
    - paste.gif
    - reset.gif
    - save.gif
    - show.gif
    - tile.gif

1.3.1 BasicSSCell

This class can be instantiated and added to a JFC user interface.  It
represents a single spreadsheet cell with some basic capabilities.  It is
designed to be "quiet" (i.e., it throws exceptions rather than displaying
errors in error message dialog boxes).

1.3.2 FancySSCell

This class is an extension of BasicSSCell that can be instantiated and
added to a JFC user interface to provide all of the capabilities of a
BasicSSCell, plus some additional, "fancy" capabilities.  It is designed to
be "loud" (i.e., it displays errors in error message dialog boxes rather
than throwing exceptions).

1.3.3 MappingDialog

This class is a dialog box allowing the user to specify ScalarMaps for
the current data sets.

1.3.4 SpreadSheet

This is the main SpreadSheet user interface class.  It manages
multiple FancySSCells.

1.3.5 SSCellChangeEvent

An event signifying a data, display or dimension change in an SSCell.

1.3.6 SSCellData

This class encapsulates a VisAD Data object and important associated
information, such as the data's variable name (e.g., A1d1).

1.3.7 SSCellImpl

Each VisAD Data object present in an SSCell is monitored by an instance of this
class, which takes care of updating the SSCell display and notifying remote
cells of the data changes that occur.

1.3.8 SSCellListener

An interface for classes that wish to be informed when an SSCell changes.

1.3.9 SSLayout

This is the layout manager for the spreadsheet cells and their labels.

2. Features of the SpreadSheet User Interface

2.1 Basic Commands

The spreadsheet cell with the yellow border is the current, highlighted
cell.  Any operation you perform (such as importing a data set), will affect
the highlighted cell.  Cells that are not highlighted will be colored according
to the following scheme:
  - Gray - no data
  - Red - a formula
  - Blue - an RMI address
  - Green - a filename or URL
  - Rainbow - multiple data objects

In addition, the following border colors are supported, although they will not
appear during normal SpreadSheet operation:
  - Purple - data from an unknown source
  - Yellow - data from a remote source
  - Cyan - data that was set directly

To change which cell is highlighted, click inside the desired cell with a mouse
button.  You can also resize the spreadsheet cells, to allow some cells to be
larger than others, by dragging the yellow blocks between cell labels.

2.2 Menu Commands

2.2.1 File Menu

Here are the commands from the File menu:

Import data - Brings up a dialog box that allows the user to select a file for
the SpreadSheet to import to the current cell.  Currently, VisAD supports the
following file types:
    GIF, JPEG, PNG, netCDF, HDF-5, HDF-EOS, FITS,
    Vis5D, McIDAS area, and serialized data.
-------------------------------------------------------------------------------
Note: You must have the HDF-EOS and HDF-5 file adapter native C code compiled
      in order to import data sets of those types.  See the SpreadSheet web
      page for information on how to compile this native code.
-------------------------------------------------------------------------------

Export data to netCDF - Exports the selected dataset from the current cell to a
file in netCDF format.  A dialog box will appear to let you select the name and
location of the netCDF file.  If the file exists, it will be overwritten.

Export serialized data - Exports the selected dataset from the current cell to
a file in serialized data format (the "VisAD" form).  A dialog box will appear
to let you select the name and location of the serialized data file.  If the
file exists, it will be overwritten.
-------------------------------------------------------------------------------
WARNING: Exporting a cell as serialized data is a handy and portable way to
         store data, but each time the VisAD Data class hierarchy changes, old
         serialized data files become obsolete and will no longer load
         properly.  For long term storage of your data, use the "Export data to
         netCDF" command.
-------------------------------------------------------------------------------

Export data to HDF5 - Exports the selected dataset from the current cell to a
file in HDF-5 format.  A dialog box will appear to let you select the name and
location of the HDF-5 file.  If the file exists, it will be overwritten.
-------------------------------------------------------------------------------
Note: You must have the HDF-5 file adapter native C code compiled in order to
export data sets of this type.  See the SpreadSheet web page for information on
how to compile this native code.
-------------------------------------------------------------------------------

Take JPEG snapshot - Takes a snapshot of the current cell and saves it to a
file in JPEG format.  A dialog box will appear to let you select the name and
location of the JPEG file.  If the file exists, it will be overwritten.

Exit - Quits the VisAD SpreadSheet User Interface.

2.2.2 Edit Menu

Here are the commands from the Edit menu:

Cut - Moves the current cell to the clipboard.

Copy - Copies the current cell to the clipboard.

Paste - Copies the cell in the clipboard to the current cell.

Clear - Clears the current cell.

2.2.3 Setup Menu

Here are the commands from the Setup menu:

New - Clears all spreadsheet cells; starts from scratch.

Open - Opens a "spreadsheet file".  Spreadsheet files are small, containing
only the instructions needed to recreate a spreadsheet.  They do not contain
any actual data, but rather the file names, URLs, RMI addresses, formulas,
dimensionality information, mappings, and control information of the cells.

Save - Saves a spreadsheet file under the current name.  

Save as - Saves a spreadsheet file under a new name.

2.2.4 Cell Menu

3-D (Java3D) - Sets the current cell's display dimension to 3-D.  This setting
requires Java3D.  If you do not have Java3D installed, this option will be
grayed out.

2-D (Java2D) - Sets the current cell's display dimension to 2-D.  This uses
Java2D, which comes with the JDK.  However, in this mode, nothing can be mapped
to ZAxis, Latitude, or Alpha.  For computers without 3-D acceleration, this
mode will provide much better performance, but the display quality will not be
as good as 2-D (Java3D).  If you do not have Java3D installed, this is the only
available mode.

2-D (Java3D) - Sets the current cell's display dimension to 2-D.  This requires
Java3D.  In this mode, nothing can be mapped to ZAxis or Latitude (but things
can be mapped to Alpha).  On computers with 3-D acceleration, this mode will
probably provide better performance than 2-D (Java2D).  It also has better
display quality than 2-D (Java2D).  If you do not have Java3D installed, this
option will be grayed out.

Add data object - Blanks out the formula bar, allowing you to type in a new
data source (e.g., filename, URL or formula).

Remove data object - Removes the data object currently selected (use the
drop-down formula bar list to specify a data object).

Print cell - Prints the current cell to the printer.  Choosing this option
causes a dialog box to appear that lets you specify your printer settings
before the cell is actually printed.

Edit Mappings - Brings up a dialog box which lets you change how the current
cell's Data objects are mapped to the Display.  Click a RealType object on the
left (or from the MathType display at the top), then click a display icon from
the display panel in the center of the dialog.  The "Current Mappings" box on
the lower right will change to reflect which mappings you've currently set up.
When you've set up all the mappings to your liking, click the Done button and
the SpreadSheet will try to display the data objects.  To close the dialog box
without applying any of the changes you made to the mappings, click the Cancel
button.  You can also highlight items from the "Current Mappings" box, then
click "Clear selected" to remove those mappings from the list, or click "Clear
all" to clear all mappings from the list and start from scratch.

Reset orientation - Resets the current cell's display projection to the
original orientation, size and location.

Show controls - Displays the set of controls relevant to the current cell
(these controls are displayed by default, but could become hidden at a later
time).  This option is not a checkbox, but rather just redisplays the controls
for the current cell if they have been closed by the user.

2.2.5 Layout Menu

Here are the commands from the Layout menu:

Add column - Creates a new column and places it at the right edge of the
spreadsheet.

Add row - Creates a new row and places it at the bottom edge of the
spreadsheet.

Delete column - Deletes the column to which the currently selected cell
belongs.  If a cell depends on any of the cells in the column, the delete
column operation will fail.

Delete row - Deletes the row to which the currently selected cell belongs.
If a cell depends on any of the cells in the row, the delete row operation will
fail.

Tile cells - Resizes the cells to equal sizes, so that they fit exactly within
the visible frame, if possible (if there are a lot of cells, they may not all
fit within the visible frame).

2.2.6 Options Menu

Here are the commands from the Options menu:

Auto-switch to 3-D - If this option is checked, cells will automatically switch
to 3-D display mode when mappings are used that require 3-D display mode.  In
addition, it will switch to mode 2-D (Java3D) from mode 2-D (Java2D) if
anything is mapped to Alpha or RGBA.  If you do not have Java3D installed, this
option is grayed out.  Otherwise, this option is checked by default.

Auto-detect mappings - If this option is checked, the SpreadSheet will attempt
to detect a good set of mappings for a newly loaded data set and automatically
apply them.  This option is checked by default.

Auto-display controls - If this option is checked, the SpreadSheet will
automatically display the controls relevant to a cell's data whenever that
cell's mappings change, or the cell becomes highlighted.  If this option is
unchecked, use the "Show VisAD controls" menu item or toolbar button to display
the controls.  This option is checked by default.

2.3 Toolbars

2.3.1 Main Toolbar

The main toolbar provides shortcuts to the following menu items:
    File Import and File Export (netCDF),
    Edit Cut, Edit Copy and Edit Paste,
    Cell 3-D (Java3D), Cell 2-D (Java3D) and Cell 2-D (Java2D),
    Cell Edit mappings, Cell Reset orientation and Cell Show controls,
    Layout Tile cells.

The main toolbar has tool tips so each button can be easily identified.

2.3.2 Formula Toolbar

2.3.2.1 Description and Usage

The formula toolbar is used for entering file names, URLs, RMI addresses,
and formulas for the current cell.  If you enter the name of a file in the
formula text box, the SpreadSheet will attempt to import the data from that
file.  If you enter a URL, the SpreadSheet will try to download and import the
data from that URL (however, VisAD only supports loading GIF, JPEG and PNG
files from URLs right now).  If you enter an RMI address, the SpreadSheet will
try to import the data from that RMI address (see section 2.4).  If you enter a
formula, it will attempt to parse and evaluate that formula.  If a formula
entered is invalid for some reason, the answer cannot be computed, or the file,
URL, or RMI address entered does not exist, the cell will have an explanation
(i.e., a list of error messages) displayed inside instead of the normal data
box.  If the data box appears, the cell was computed successfully and mappings
can be set up.

The down arrow to the right of the formula text box brings up a drop-down list
of all data objects currently loaded in this cell.  The data object you choose
from this list becomes the current dataset for the cell (e.g., when you choose
to export data from the File menu, this is the dataset that will be exported).

You can remove the current dataset by clicking the formula bar's delete button
("Del"), located just to the left of the formula text box.

Clicking the formula bar's add button ("Add") will blank out the formula text
box, allowing you to type in a new filename, URL, RMI address or formula.

2.3.2.2 How To Enter Formulas

To reference the data objects of a cell, keep in mind that each column is a
letter (the first column is 'A', the second is 'B', and so on), and each row is
a number (the first row is '1', the second is '2', and so on).  So, the cell on
the top-left is A1, the cell on A1's right is B1, and the cell directly below
A1 is A2, etc.  Each dataset of a cell has an associated variable name, which
you can determine by examining the formula bar's drop-down list of datasets for
that cell.  For example, the first data object you load into cell A1 will be
called "A1d1", the second will be called "A1d2", etc.

Type your formula in the formula text field.  Once you've typed in a formula,
press Enter to apply the formula.  The new dataset will be added to the formula
bar's drop-down list, and the dataset will appear in the cell.

2.3.2.3 Formula Syntax

Formulas are case insensitive.

Any of the following can be used in formula construction:

1) Formulas can use any of the basic operators:
       + add,  - subtract,  * multiply,  / divide,  % remainder,  ^ power

2) Formulas can use any of the following binary functions:
       max, min, atan2, atan2Degrees

3) Formulas can use any of the following unary functions:
       abs, acos, acosDegrees, asin, asinDegrees, atan, atanDegrees, ceil,
       cos, cosDegrees, domainMultiply, exp, floor, log, rint, round, sin,
       sinDegrees, sqrt, tan, tanDegrees, negate

4) Unary minus syntax (e.g., B2d1 * -A1d1) is supported.

5) Derivatives are supported with the syntax:
       d(DATA)/d(TYPE)
            OR
       derive(DATA, TYPE)
   where DATA is a Function, and TYPE is the name of a RealType present in
   the Function's domain.  This syntax calls Function's derivative() method
   with an error_type of Data.NO_ERRORS.

6) Function evaluation is supported with the syntax:
       DATA1(DATA2)
            OR
       (DATA1)(DATA2)
   where DATA1 is a Function and DATA2 is a Real or a RealTuple.
   This syntax calls Function's evaluate() method.

7) You can obtain an individual sample from a Field with the syntax:
       DATA[N]
   where DATA is the Field, and N is a literal integer.
   Use DATA[0] for the first sample of DATA.
   This syntax calls Field's getSample() method.

8) You can obtain one component of a Tuple with the syntax:
       DATA.N
   where DATA is a Tuple and N is a literal integer.
   Use DATA.0 for the first Tuple component of DATA.
   This syntax calls Tuple's getComponent() method.

9) You can extract part of a field with the syntax:
      extract(DATA, N)
   where DATA is a Field and N is a literal integer.
   This syntax calls Field's extract() method.

10) You can combine multiple fields with the syntax:
       combine(DATA1, DATA2, ..., DATAN)
    where DATA1 through DATAN are Fields.
    This syntax calls FieldImpl's combine() method.

11) You can perform a domain factoring with the syntax:
       domainFactor(DATA, TYPE)
    where DATA is a FieldImpl, and TYPE is the name of a RealType present
    in the FieldImpl's domain.
    This syntax calls FieldImpl's domainFactor() method.

2.3.2.4 Linking to External Java Code

You can link to an external Java method with the syntax:

    link(package.Class.Method(DATA1, DATA2, ..., DATAN))

where package.Class.Method is the fully qualified method name and DATA1
through DATAN are each Data objects or RealType objects.

Keep the following points in mind when writing an external Java method
that you wish to link to the SpreadSheet:

1) The signature of the linked method must be public and static and must return
   a Data object.  In addition, the class to which the method belongs must be
   public.  The method must have only Data and RealType parameters (if any).

2) The method can contain one array argument (Data[] or RealType[]).  In this
   way, a linked method can support a variable number of arguments.  For
   example, a method with the signature "public static Data max(Data[] d)"
   that is part of a class called Util could be linked into a SpreadSheet cell
   with any number of arguments; e.g.,
       link(Util.max(A1d1, A2d1))
       link(Util.max(A2d1, C3d2, B1d4, A1d2))
   would both be correct references to the max method.

2.3.2.5 Examples of Valid Formulas

Here are some examples of valid formulas for cell A1:
    sqrt(A2d1 + B2d2^5 - min(B1d1, -C1d1))
    d(A2d1 + A2d2)/d(ImageElement)
    A2d1(A3d1)
    C2d2.6[0]
    (B1d2 * C1d1)(A3d4).1
    C2d10 - 5*link(com.happyjava.vis.Linked.crunch(A6d1, C3d11, B5d15))

2.4 Remote Collaboration

2.4.1 Creating a SpreadSheet RMI server

The first step in collaboration is to create a SpreadSheet RMI server.
To launch the SpreadSheet in collaborative mode, type:

    java -mx64m visad.ss.SpreadSheet -server name

where "name" is the desired name for the RMI server.  If the server is created
successfully, the title bar will contain the server name in parentheses.

Once your SpreadSheet is operating as an RMI server, other SpreadSheets can
work with it collaboratively.

2.4.2 Sharing individual SpreadSheet cells

Any VisAD SpreadSheet has the capability to import data objects from an RMI
server.  Simply type the RMI address into the SpreadSheet's formula bar.
The format of the RMI address is:

    rmi://rmi.address/name/data

where "rmi.address" is the IP address of the RMI server, "name" is the name of
the RMI server, and "data" is the name of the data object desired.

For example, suppose that the machine at address www.ssec.wisc.edu is running
an RMI server called "VisADServ" using a SpreadSheet with two cells, A1 and B1.
A SpreadSheet on another machine could import data from cell B1 of VisADServ
by typing the following RMI address in the formula bar:

    rmi://www.ssec.wisc.edu/VisADServ/B1d1

Just like file names, URLs, and formulas, the SpreadSheet will load the data,
showing the data box if the import is successful, or displaying error messages
within the cell if there is a problem.

2.4.3 Cloning entire SpreadSheets

The VisAD SpreadSheet also allows for a more powerful form of collaboration:
the cloning of entire SpreadSheets from a SpreadSheet RMI server.  To clone a
SpreadSheet RMI server, type:

    java -mx64m visad.ss.SpreadSheet -client rmi.address/name

Where "rmi.address" is the IP address of the RMI server and "name" is the RMI
server's name.  The resulting SpreadSheet will have the same cell layout as
the SpreadSheet RMI server and the same data with the same mappings.  In
addition, it will be linked so that any changes to the SpreadSheet will be
propagated to the server and all its clones.

Note that if a SpreadSheet RMI server does not support Java3D, none of its
clones will be able to either.  Thus, for maximum functionality, it is best
to make sure that the machine chosen to be the RMI server supports Java3D.

2.4.4 Creating a SpreadSheet slave

SpreadSheet clones need not re-render the server's data locally.
Alternatively, the clone can be a slave, receiving image data from the server
and sending mouse events back to the server so that users can still interact
with the displays.  To launch a SpreadSheet clone as a slave, type:

    java -mx64m visad.ss.SpreadSheet -slave rmi.address/name

Note that the command line is the same as a regular clone except that the
parameter is "-slave" instead of "-client".  Some features that are available
to a regular clone are disabled for a slave, since only images of the data are
sent to the slave, not the data itself.

Slaves have a very slow update rate (taking a snapshot of a Java3D display on
the server is very slow, and each snapshot must then be sent across the
network). However, slaves are very useful for visualizing 3-D displays on a
machine that does not have Java3D, and they also conserve large amounts of
memory, since large datasets are not sent to the slave.

2.5 Undocumented Features

Obviously, if they're undocumented, you won't find them in this README!
However, creating the javadoc for the visad.ss package should help in
deciphering it, since the source is liberally commented.  You may also wish to
create the javadoc for visad.formula, a package that is heavily used by the
SpreadSheet.

In addition, you can obtain help with the SpreadSheet's command line options by
using the "-help" command line option.

3. Known Bugs

The following bugs have been discovered:

1) On certain machine configurations, the SpreadSheet may sometimes lock up
   on startup (or when a toolbar button first becomes grayed out) due to a
   MediaTracker bug (#4332685). Try running the SpreadSheet with a different
   number of rows and columns on startup. If you still have trouble, you can
   use the "-bugfix" command line flag to disable the SpreadSheet's toolbar.
   This workaround will keep the SpreadSheet from locking up on startup, but
   you will not have the convenience of the toolbar. Of course, all
   functionality is still accessible from the menus.

2) Due to a workaround to improve the functionality of the formula bar, the
   backspace key sometimes causes two characters to be deleted from the formula
   instead of one.

3) When importing certain netCDF data sets, a series of errors beginning with
   "Couldn't decode attribute" may be displayed.  These are warnings the netCDF
   loader prints about unit types.  The SpreadSheet will still import the
   netCDF data set correctly (i.e., these warnings can be safely ignored).

4) With JDK 1.2 under Windows, the first time a data set is imported, an error
   beginning with "A nonfatal internal JIT (3.00.078(x)) error 'regvar' has
   occurred" is displayed.  This error occurs whenever a VisAD application
   makes use of the visad.data.DefaultFamily.open() method, and is a problem
   with the Symantec JIT compiler for Windows.  This error is harmless and data
   sets are still imported correctly (i.e., ignore this error message).

   This error no longer appears in JDK 1.3, since the JVM no longer uses the
   Symantec JIT compiler, but instead uses Sun's Hotspot compiler.

5) The SpreadSheet may not import certain data sets correctly, due to
   incomplete implementations in VisAD file adapter forms.

If you find a bug in the SpreadSheet user interface not listed above,
please send e-mail to visad-list@ssec.wisc.edu describing the problem,
preferably with a detailed description of how to recreate the problem.

If you have any suggestions for features that you would find useful,
please send e-mail to visad-list@ssec.wisc.edu describing the feature.
