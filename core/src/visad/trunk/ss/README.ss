 
                   VisAD SpreadSheet User Interface README file
                                15 September 1998
                                        
                                Table of Contents

1. The visad.ss Package
  1.1 Description
  1.2 Compiling and Running
  1.3 Source Files
    1.3.1 BasicSSCell
    1.3.2 FancySSCell
    1.3.3 Formula
    1.3.4 FormulaCell
    1.3.5 MappingDialog
    1.3.6 SpreadSheet
    1.3.7 SSLayout
2. Features of the SpreadSheet User Interface
  2.1 Basic Commands
  2.2 Menu Commands
    2.2.1 File Menu
    2.2.2 Edit Menu
    2.2.3 Setup Menu
    2.2.4 Display Menu
    2.2.5 Window Menu
  2.3 Toolbars
    2.3.1 Main Toolbar
    2.3.2 Formula Toolbar
      2.3.2.1 Description
      2.3.2.2 How To Enter Formulas
  2.4 Undocumented Features
3. Known Bugs
4. Future Plans


1. The visad.ss Package

1.1 Description
    This README file explains what the visad.ss package is, what it does (and
what it will do), and how to use it.
    The visad.ss package is a "generic" spreadsheet user interface for VisAD.
It is intended to be poweful and flexible, and it can be used to visualize
many types of data, without any programming.  It supports many features of a
traditional spreadsheet, such as formulas.  The package also provides a class
structure such that developers can easily create their own user interfaces
using spreadsheet cells from the visad.ss package.

1.2 Compiling and Running
    To compile the package, type the following from the visad/ss directory:

        javac -J-mx32m *.java

    To run the spreadsheet user interface, type:

        java -mx64m visad.ss.SpreadSheet

    You can optionally specify the number of spreadsheet cells with:

        java -mx64m visad.ss.SpreadSheet (cols) (rows)

    where (cols) is the number of columns, and (rows) is the number of rows.
    The default is four cells (two columns, two rows).

    The spreadsheet user interface requires a lot of memory (at least 32 MB),
    especially if you want to work with large data sets.  If you receive an
    OutOfMemoryError, you should increase the amount of memory allocated to
    the program (increase the ### in "-mx###m").

1.3 Source Files
    The following source files are part of the visad.ss package:
      - BasicSSCell.java
      - FancySSCell.java
      - Formula.java
      - FormulaCell.java
      - MappingDialog.java
      - SpreadSheet.java
      - SSLayout.java

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

1.3.3 Formula
    This class is designed to convert formulas to postfix notation for
evaluation on a stack.  It is used by FormulaCell.

1.3.4 FormulaCell
    This class is used internally by BasicSSCell to evaluate formulas.

1.3.5 MappingDialog
    This class is a dialog box allowing the user to specify ScalarMaps for
    the current data set.

1.3.6 SpreadSheet
    This is the main spreadsheet user interface class.  It manages
    multiple FancySSCells.

1.3.7 SSLayout
    This is the layout manager for the spreadsheet cells and their labels.

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
      Import data -  Brings up a dialog box that allows the user to select a
                     file for the spreadsheet to import to the current cell.
                     Currently, VisAD supports the following file types:
                         GIF, JPEG, netCDF, HDF-EOS, FITS, and Vis5D.
                     Note that you must have the HDF-EOS and Vis5D file
                     adapter native C code compiled in order to import data
                     sets of those types.
      Exit -         Quits the VisAD SpreadSheet User Interface.

2.2.2 Edit Menu
    Here are the commands from the Edit menu:
      Cut -   Moves the current cell to the clipboard.
      Copy -  Copies the current cell to the clipboard.
      Paste - Copies the cell in the clipboard to the current cell.
      Clear - Clears the current cell.

2.2.3 Setup Menu
    Here are the commands from the Setup menu:
      New -     Clears all spreadsheet cells;  starts from scratch.
      Open -    Opens a "spreadsheet file."  Spreadsheet files are small,
                containing only the instructions needed to recreate a
                spreadsheet.  They do not contain any actual data, but rather
                the file names and formulas of the cells.
      Save -    Saves a "spreadsheet file" under the current name.  
      Save as - Saves a "spreadsheet file" under a new name.

2.2.4 Display Menu
    Here are the commands from the Display menu:
      Edit Mappings - Brings up a dialog box which lets you change how the Data
                      object is mapped to the Display.  Click a RealType object
                      on the left (or from the MathType display at the top),
                      then click a display icon from the display panel in the
                      center of the dialog.  The "Current Mappings" box on the
                      lower right will change to reflect which mappings you've
                      currently set up.  When you've set up all the mappings to
                      your liking, click the Done button and the spreadsheet
                      will try to display the data object.  To close the dialog
                      box without applying any of the changes you made to the
                      mappings, click the Cancel button.  You can also
                      highlight items from the "Current Mappings" box, then
                      click "Clear selected" to remove those mappings from the
                      list, or click "Clear all" to clear all mappings from the
                      list and start from scratch.
      3-D (Java3D) -  Sets the current cell's display dimension to 3-D.  This
                      setting requires Java3D.
      2-D (Java2D) -  Sets the current cell's display dimension to 2-D.  This
                      uses Java2D, which comes with JDK 1.2beta3.  However, in
                      this mode, nothing can be mapped to ZAxis, Latitude, or
                      Alpha.  For computers without 3-D acceleration, this mode
                      will provide much better performance, but the display
                      quality will not be as good as 2-D (Java3D).  This
                      setting is the default, so that non-Java3D-enabled
                      computers can still use the spreadsheet.
      2-D (Java3D) -  Sets the current cell's display dimension to 2-D.  This
                      requires Java3D.  In this mode, nothing can be mapped to
                      ZAxis or Latitude (but things can be mapped to Alpha).
                      On computers with 3-D acceleration, this mode will
                      probably provide better performance than 2-D (Java2D).
                      It also has better display quality than 2-D (Java2D).

      ** The rest of the commands from the Display menu are "quick-maps."
         They scan the current cell's data object for a valid flat function
         and map it in a preset fashion.  These built-in "mapping schemes"
         provide common visualization ScalarMap combinations.  If for some
         reason the quick-maps do not work, you will have to edit the mappings
         manually with the "Edit Mappings" option in the Display menu.

2.2.5 Window Menu
    Here are the commands from the Window menu:
      Show VisAD controls -            Displays the set of controls relevant
                                       to the current cell (these controls
                                       are displayed by default, but could
                                       become hidden at a later time).
      Show formula evaluation errors - If this option is checked, dialog boxes
                                       will pop up explaining why any formulas
                                       entered are illegal or could not be
                                       evaluated.  If this option is not
                                       checked, the only notification of an
                                       error is a large X through the current
                                       cell.

2.3 Toolbars

2.3.1 Main Toolbar
    The main toolbar provides shortcuts to the following menu items:
        File Import, Edit Cut, Edit Copy, Edit Paste,
        Display Edit Mappings, and Window Show VisAD Controls.
The main toolbar has tool tips so each button can be easily identified.

2.3.2 Formula Toolbar

2.3.2.1 Description
    The formula toolbar is used for entering file names and formulas for the
current cell.  If you enter the name of a file in the formula text box, the
spreadsheet will attempt to import the data from that file.  If you enter a
formula, it will attempt to parse and evaluate that formula.  If a formula
entered is invalid for some reason, the answer cannot be computed, or the file
entered does not exist, the cell will have a large X through it instead of the
normal data box.  If the data box appears, the cell was computed successfully
and mappings can be set up.

2.3.2.2 How To Enter Formulas
    To reference cells, keep in mind that each column is a letter (the first
column is 'A', the second is 'B', and so on), and each row is a number (the
first row is '1', the second is '2', and so on).  So, the cell on the top-left
is A1, the cell on A1's right is B1, and the cell directly below A1 is A2, etc.

Any of the following can be used in formula construction:

1) Formulas can use any of the basic operators:
       + add,  - subtract,  * multiply,  / divide,  % remainder,  ^ power

2) Formulas can use any of the following binary functions:
       MAX, MIN, ATAN2, ATAN2DEGREES

3) Formulas can use any of the following unary functions:
       ABS, ACOS, ACOSDEGREES, ASIN, ASINDEGREES, ATAN, ATANDEGREES, CEIL,
       COS, COSDEGREES, EXP, FLOOR, LOG, RINT, ROUND, SIN, SINDEGREES, SQRT,
       TAN, TANDEGREES, NEGATE

4) Unary minus syntax (e.g., B2 * -A1) is supported.

5) Derivatives are supported with the syntax:
       d(DATA)/d(TYPE)
   where DATA is a Function, and TYPE is the name of a RealType present in
   the Function's domain.  This syntax calls Function's derivative() method
   with an error_type of Data.NO_ERRORS.

6) Function evaluation is supported with the syntax:
       DATA1(DATA2)
   where DATA1 is a Function and DATA2 is a Real or a RealTuple.
   This syntax calls Function's evaluate() method.

7) You can obtain an individual sample from a Field with the syntax:
       DATA(N)
   where DATA is the Field, and N is a literal integer.
   Use DATA(0) for the first sample of DATA.
   This syntax calls Field's getSample() method.

8) You can obtain one component of a Tuple with the syntax:
       DATA.N
   where DATA is a Tuple and N is a literal integer.
   Use DATA.0 for the first Tuple component of DATA.
   This syntax calls Tuple's getComponent() method.

9) You can extract part of a field with the syntax:
       EXTRACT(DATA, N)
   where DATA is a Field and N is a literal integer.
   This syntax calls Field's extract() method.

10) Formulas are not case sensitive.

Some examples of valid formulas for cell A1 are:
    SQRT(A2 + B2^5 - MIN(B1, -C1))
    d(A2 + B2)/d(ImageElement)
    A2(A3)
    C2.6
    (B1 * C1)(A3).1

Once you've typed in a formula, press Enter or click the green check box button
to the left of the formula entry text box to apply the formula.  The red X
button will cancel your entry, restoring the formula to its previous state.
The open folder button to the right of the formula entry text box is a shortcut
to the File menu's Import Data menu item.

2.4 Undocumented Features
    Obviously, if they're undocumented, you won't find them in this README!
    However, creating the javadoc for the visad.ss package should help in
    deciphering it, since the source is heavily commented.

3. Known Bugs
    The following bugs have been discovered and have not yet been fixed:
      1) Clicking a cell with an illegal file name or formula (one with a
         large X through it) will not highlight that cell.  The arrow keys
         must be used to select it.
      2) The spreadsheet will not import certain data sets correctly, due to
         incomplete implementations in VisAD file adapter forms.
      3) Error messages are displayed when the user clicks on a button that
         doesn't make sense (such as trying to set up mappings for an empty
         cell).  These buttons should just be grayed out.
      4) There is no way to change the number of spreadsheet rows and columns
         while the spreadsheet is running;  you must quit the spreadsheet and
         specify a new setting on the command line.
      5) When resizing cells, if a cell is made to be as small as it can be
         in one or more dimensions, some extra space or a scroll bar will
         appear in the bottom or right-hand corners of the spreadsheet window.

    If you find a bug in the spreadsheet user interface not listed above,
please send e-mail to curtis@ssec.wisc.edu describing the problem, preferably
with a detailed description of how to recreate the problem.

4. Future Plans
    Here's what's coming in the future:
      1) Spreadsheet column and row addition and deletion
      2) Multiple data per cell
      3) Direct manipulation support
      4) Distributed Cells, Data, etc. (such as data import from http address)
      5) Remote spreadsheet cloning with collaboration
      6) Formula enhancements, including composition of multiple Data objects
         (such as creating an animation from multiple spreadsheet cells), and
         dynamic linkage of Java code into formulas
      7) Quick-map enhancements, including more built-in quick-maps (such as
         contour and animation), and user-defined quick-maps
      8) Misc. user interface enhancements
      9) And of course, bug fixes

    If you have any suggestions for features that you would find useful,
please send e-mail to curtis@ssec.wisc.edu describing the feature.
