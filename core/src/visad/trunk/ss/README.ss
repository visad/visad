                   VisAD Spread Sheet User Interface README file
                                  25 March 1999
 
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
    1.3.6 SSCellListener
    1.3.7 SSLayout
    1.3.8 SSMethod
    1.3.9 SSRealType
2. Features of the SpreadSheet User Interface
  2.1 Basic Commands
  2.2 Menu Commands
    2.2.1 File Menu
    2.2.2 Edit Menu
    2.2.3 Setup Menu
    2.2.4 Display Menu
    2.2.5 Options Menu
  2.3 Toolbars
    2.3.1 Main Toolbar
    2.3.2 Formula Toolbar
      2.3.2.1 Description
      2.3.2.2 How To Enter Formulas
      2.3.2.3 Formula Syntax
      2.3.2.4 Linking to External Java Code
      2.3.2.5 Examples of Valid Formulas
  2.4 Remote Collaboration
  2.5 Undocumented Features
3. Known Bugs
4. Future Plans


1. The visad.ss Package

1.1 Description

This README file explains what the visad.ss package is, what it does,
and how to use it.

The visad.ss package is a "generic" spreadsheet user interface for VisAD.
It is intended to be poweful and flexible, and it can be used to visualize
many types of data, without any programming.  It supports many features of a
traditional spreadsheet, such as formulas.  The package also provides a class
structure such that developers can easily create their own user interfaces
using Spread Sheet cells from the visad.ss package.

For up-to-date information about the VisAD Spread Sheet, see the VisAD
Spread Sheet web page at http://www.ssec.wisc.edu/~curtis/ss.html

For up-to-date information about VisAD in general, see the VisAD web page
at http://www.ssec.wisc.edu/~billh/visad.html

1.2 Compiling and Running

To compile the package, type the following from the visad/ss directory:

    javac -J-mx32m *.java

To run the Spread Sheet user interface, type:

    java -mx64m visad.ss.SpreadSheet

You can optionally specify the number of spreadsheet cells with:

    java -mx64m visad.ss.SpreadSheet (cols) (rows)

where (cols) is the number of columns, and (rows) is the number of rows.
The default is four cells (two columns, two rows).

The Spread Sheet user interface requires a lot of memory (at least 32 MB),
especially if you want to work with large data sets.  If you receive an
OutOfMemoryError, you should increase the amount of memory allocated to
the program (increase the ### in "-mx###m").

1.3 Source Files

The following source files are part of the visad.ss package:
    - BasicSSCell.java
    - FancySSCell.java
    - MappingDialog.java
    - SpreadSheet.java
    - SSCellChangeEvent.java
    - SSCellListener.java
    - SSLayout.java
    - SSMethod.java
    - SSRealType.java

The following included GIF files are needed by the package:
    - cancel.gif
    - copy.gif
    - cut.gif
    - display.gif
    - import.gif
    - mappings.gif
    - ok.gif
    - open.gif
    - paste.gif
    - save.gif
    - show.gif

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
the current data set.

1.3.4 SpreadSheet

This is the main Spread Sheet user interface class.  It manages
multiple FancySSCells.

1.3.5 SSCellChangeEvent

An event signifying a data or dimension change in an SSCell.

1.3.6 SSCellChangeListener

An interface for classes that wish to be informed when an SSCell changes.

1.3.7 SSLayout

This is the layout manager for the spreadsheet cells and their labels.

1.3.8 SSMethod

A wrapper for java.lang.reflect.Method so that it may be used in the
visad.formula package.

1.3.9 SSRealType

A wrapper for visad.RealType so that it may be used in the
visad.formula package.

2. Features of the SpreadSheet User Interface

2.1 Basic Commands

The spreadsheet cell with the yellow border is the current, highlighted
cell.  Any operation you perform (such as importing a data set), will affect
the highlighted cell.  To change which cell is highlighted, click inside the
desired cell with a mouse button, or press the arrow keys.  You can also
resize the spreadsheet cells, to allow some cells to be larger than others,
by dragging the yellow block between cell labels.

2.2 Menu Commands

2.2.1 File Menu

Here are the commands from the File menu:

Import data - Brings up a dialog box that allows the user to select a file for
the Spread Sheet to import to the current cell.  Currently, VisAD supports the
following file types:
    GIF, JPEG, netCDF, HDF-EOS, FITS, Vis5D, McIDAS, and serialized data.
-------------------------------------------------------------------------------
Note: You must have the HDF-EOS and Vis5D file adapter native C code compiled
      in order to import data sets of those types.  See the Spread Sheet web
      page for information on how to compile this native code.
-------------------------------------------------------------------------------

Export data to netCDF - Exports the current cell to a file in netCDF format.
A dialog box will appear to let you select the name and location of the netCDF
file.  If the file exists, it will be overwritten.

Export serialized data - Exports the current cell to a file in serialized data
format (the "VisAD" form).  A dialog box will appear to let you select the name
and location of the serialized data file.  If the file exists, it will be
overwritten.
-------------------------------------------------------------------------------
WARNING: Exporting a cell as serialized data is a handy and portable way to
         store data, but each time the VisAD Data class hierarchy changes, old
         serialized data files become obsolete and will no longer load
         properly.  For long term storage of your data, use the "Export data to
         netCDF" command.
-------------------------------------------------------------------------------

Exit - Quits the VisAD SpreadSheet User Interface.

2.2.2 Edit Menu

Here are the commands from the Edit menu:

Cut - Moves the current cell to the clipboard.

Copy - Copies the current cell to the clipboard.

Paste - Copies the cell in the clipboard to the current cell.

Clear - Clears the current cell.

2.2.3 Setup Menu

Here are the commands from the Setup menu:

New - Clears all spreadsheet cells;  starts from scratch.

Open - Opens a "spreadsheet file."  Spreadsheet files are small, containing
only the instructions needed to recreate a spreadsheet.  They do not contain
any actual data, but rather the file names, URLs, RMI addresses, formulas,
dimensionality information, and mappings of the cells.

Save - Saves a "spreadsheet file" under the current name.  

Save as - Saves a "spreadsheet file" under a new name.

2.2.4 Display Menu

Here are the commands from the Display menu:

Edit Mappings - Brings up a dialog box which lets you change how the Data
object is mapped to the Display.  Click a RealType object on the left (or from
the MathType display at the top), then click a display icon from the display
panel in the center of the dialog.  The "Current Mappings" box on the lower
right will change to reflect which mappings you've currently set up.  When
you've set up all the mappings to your liking, click the Done button and the
Spread Sheet will try to display the data object.  To close the dialog box
without applying any of the changes you made to the mappings, click the Cancel
button.  You can also highlight items from the "Current Mappings" box, then
click "Clear selected" to remove those mappings from the list, or click "Clear
all" to clear all mappings from the list and start from scratch.

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

2.2.5 Options Menu

Here are the commands from the Options menu:

Auto-switch to 3-D - If this option is checked, cells will automatically switch
to 3-D display mode when mappings are used that require 3-D display mode.  In
addition, it will switch to mode 2-D (Java3D) from mode 2-D (Java2D) if
anything is mapped to Alpha or RGBA.  If you do not have Java3D installed, this
option is grayed out.  Otherwise, this option is checked by default.

Auto-detect mappings - If this option is checked, the Spread Sheet will attempt
to detect a good set of mappings for a newly loaded data set and automatically
apply them.  This option is checked by default.

Auto-display controls - If this option is checked, the Spread Sheet will
automatically display the controls relevant to a cell's data whenever that
cell's mappings change, or the cell becomes highlighted.  If this option is
unchecked, use the "Show VisAD controls" menu item or toolbar button to display
the controls.  This option is checked by default.

Show formula and RMI evaluation errors - If this option is checked, dialog boxes
will pop up explaining why any formulas entered are illegal or could not be
evaluated, as well as reporting any errors from RMI address imports.  If this
option is not checked, the only notification of an error is a large X through
the current cell.

Show VisAD controls - Displays the set of controls relevant to the current cell
(these controls are displayed by default, but could become hidden at a later
time).  This option is not a checkbox, but rather just redisplays the Controls
for the current cell if they have been closed by the user.

2.3 Toolbars

2.3.1 Main Toolbar

The main toolbar provides shortcuts to the following menu items:
    File Import, Edit Cut, Edit Copy, Edit Paste, Display Edit Mappings,
    and Options Show VisAD Controls.

The main toolbar has tool tips so each button can be easily identified.

2.3.2 Formula Toolbar

2.3.2.1 Description

The formula toolbar is used for entering file names, URLs, RMI addresses,
and formulas for the current cell.  If you enter the name of a file in the
formula text box, the Spread Sheet will attempt to import the data from that
file.  If you enter a URL, the Spread Sheet will try to download and import the
data from that URL.  If you enter an RMI address, the Spread Sheet will try to
import the data from that RMI address (see section 2.4).  If you enter a
formula, it will attempt to parse and evaluate that formula.  If a formula
entered is invalid for some reason, the answer cannot be computed, or the file,
URL, or RMI address entered does not exist, the cell will have a large X
through it instead of the normal data box.  If the data box appears, the cell
was computed successfully and mappings can be set up.

2.3.2.2 How To Enter Formulas

To reference cells, keep in mind that each column is a letter (the first
column is 'A', the second is 'B', and so on), and each row is a number (the
first row is '1', the second is '2', and so on).  So, the cell on the top-left
is A1, the cell on A1's right is B1, and the cell directly below A1 is A2, etc.

Type your formula in the formula text field.  Once you've typed in a
formula, press Enter or click the green check box button to the left of the
formula entry text box to apply the formula.  The red X button will cancel your
entry, restoring the formula to its previous state.  The open folder button to
the right of the formula entry text box is a shortcut to the File menu's Import
Data menu item.

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

4) Unary minus syntax (e.g., B2 * -A1) is supported.

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
that you wish to link to the Spread Sheet:

1) The signature of the linked method must be public and static and must return
   a Data object.  In addition, the class to which the method belongs must be
   public.  The method must have only Data and RealType parameters (if any).

2) The method can contain one array argument (Data[] or RealType[]).  In this
   way, a linked method can support a variable number of arguments.  For
   example, a method with the signature "public static Data max(Data[] d)"
   that is part of a class called Util could be linked into a Spread Sheet cell
   with any number of arguments; e.g.,
       link(Util.max(A1, A2))
       link(Util.max(A2, C3, B1, A1))
   would both be correct references to the max method.

2.3.2.5 Examples of Valid Formulas

Here are some examples of valid formulas for cell A1:
    sqrt(A2 + B2^5 - min(B1, -C1))
    d(A2 + B2)/d(ImageElement)
    A2(A3)
    C2.6[0]
    (B1 * C1)(A3).1
    C2 - 5*link(com.happyjava.vis.Linked.crunch(A6, C3, B5))

2.4 Remote Collaboration

Any VisAD Spread Sheet has the capability to import RMI addresses of the
form:

    rmi://rmi.address/ServerName/Cell

where "rmi.address" is the address of the RMI server, ServerName is the RMI
server's name, and Cell is the cell from which data is desired.

For example, suppose that the machine at address www.ssec.wisc.edu is
running an RMI server called "VisADServ" using a Spread Sheet with two cells,
A1 and B1.  A Spread Sheet on another machine could import data from cell B1
of VisADServ using the following RMI address in the formula bar:

    rmi://www.ssec.wisc.edu/VisADServ/B1

Just like file names, URLs, and formulas, the Spread Sheet will load the data,
showing the data box if the import was successful, or displaying a large X in
the cell if there was a problem.

In addition to having RMI address importing capability, the Spread Sheet
can be set to export its cells as RMI objects like in the example above.  To
enable remote collaboration, simply launch the Spread Sheet with the "-server"
parameter:

    java -mx64m visad.ss.SpreadSheet -server server_name

where "server_name" is the desired name for the Spread Sheet's RMI server.
In the example above, the command would be:

    java -mx64m visad.ss.SpreadSheet 2 1 -server VisADServ

The "-mx64m" allows Java to use up to 64 MB of memory, the "2 1" specifies
that the Spread Sheet should have a 2 x 1 grid of cells, and the
"-server VisADServ" enables remote collaboration between this Spread Sheet
and other Spread Sheets.

2.5 Undocumented Features

Obviously, if they're undocumented, you won't find them in this README!
However, creating the javadoc for the visad.ss package should help in
deciphering it, since the source is heavily commented.  You may also wish to
create the javadoc for the visad.formula package, since it is heavily used
by the Spread Sheet.

3. Known Bugs

The following bugs have been discovered and have not yet been fixed:

1) The Spread Sheet will not import certain data sets correctly, due to
   incomplete implementations in VisAD file adapter forms.

2) There is no way to change the number of rows and columns while the
   Spread Sheet is running;  you must quit the Spread Sheet and specify
   a new setting on the command line.

3) When resizing cells, if a cell is made to be as small as it can be
   in one or more dimensions, some extra space or a scroll bar will
   appear in the bottom or right-hand corners of the Spread Sheet window.

If you find a bug in the Spread Sheet user interface not listed above,
please send e-mail to curtis@ssec.wisc.edu and whibbard@macc.wisc.edu
describing the problem, preferably with a detailed description of how to
recreate the problem.

4. Future Plans

Here's what's coming in the future:

1) Spreadsheet column and row addition and deletion

2) Multiple data per cell

3) Direct manipulation support

4) Distributed Cells (including Displays)

5) Misc. user interface enhancements

6) And of course, bug fixes

If you have any suggestions for features that you would find useful,
please send e-mail to curtis@ssec.wisc.edu and whibbard@macc.wisc.edu
describing the feature.
