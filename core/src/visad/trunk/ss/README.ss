 
                   VisAD SpreadSheet User Interface README file
                                   9 July 1998
                                        
                                Table of Contents

1. The visad.ss Package
  1.1 Overview
  1.2 Description
  1.3 Compiling and Running
  1.4 Source Files
    1.4.1 BasicSSCell
    1.4.2 FancySSCell
    1.4.3 Formula
    1.4.4 FormulaCell
    1.4.5 MappingDialog
    1.4.6 SpreadSheet
    1.4.7 VisADNode
2. Features of the SpreadSheet User Interface
  2.1 Basic Commands
  2.2 Menu Commands
    2.2.1 File Menu
    2.2.2 Edit Menu
    2.2.3 Setup Menu
    2.2.4 Display Menu
  2.3 Toolbars
    2.3.1 Main Toolbar
    2.3.2 Formula Toolbar
      2.3.2.1 Description
      2.3.2.2 How To Enter Formulas
  2.4 Undocumented Features
3. Known Bugs
4. Future Plans


1. The visad.ss Package

1.1 Overview
    This README file explains what the visad.ss package is, what it does (and
what it will do), and how to use it.  It is currently being included in the
VisAD distribution ONLY to provide examples of VisAD functionality.  The
package is not in a finished state and any of the code is subject to change.

1.2 Description
    The visad.ss package is a "generic" spreadsheet user interface for VisAD.
It is intended to be poweful and flexible, and it can be used to visualize
many types of data.  It supports many features of a traditional spreadsheet,
such as formulas.  The package also provides a class structure such that
developers can easily create their own user interface using spreadsheet cells
from the visad.ss package.

1.3 Compiling and Running
    To compile the package, type the following from the visad/ss directory:

        javac -J-mx32m *.java

    To run the spreadsheet user interface, type:

        java -mx64m visad.ss.SpreadSheet

    The spreadsheet user interface requires a lot of memory (at least 32 MB),
    especially if you want to work with large data sets.  If you receive an
    OutOfMemoryError, you should increase the amount of memory allocated to
    the program (increase the ### in "-mx###m").

1.4 Source Files
    The following source files are part of the visad.ss package:
      - BasicSSCell.java
      - FancySSCell.java
      - Formula.java
      - FormulaCell.java
      - MappingDialog.java
      - SpreadSheet.java
      - VisADNode.java

    The following GIF files also come with the package, for the
    spreadsheet's toolbars:
      - cancel.gif
      - copy.gif
      - cut.gif
      - import.gif
      - mappings.gif
      - new.gif
      - ok.gif
      - open.gif
      - paste.gif
      - save.gif

1.4.1 BasicSSCell
    This class can be instantiated and added to a JFC user interface.  It
represents a single spreadsheet cell with some basic capabilities.  It is
designed to be "quiet" (i.e., it throws exceptions rather than displaying
errors in error message dialog boxes).

1.4.2 FancySSCell
    This class is an extension of BasicSSCell that can be instantiated and
added to a JFC user interface to provide all of the capabilities of a
BasicSSCell, plus some additional, "fancy" capabilities.  It is designed to
be "loud" (i.e., it displays errors in error message dialog boxes rather
than throwing exceptions).

1.4.3 Formula
    This class is designed to parse formulas and convert them to postfix
notation for evaluation.  It is used by FormulaCell, although its methods
could prove useful to any class needing to work with formulas.

1.4.4 FormulaCell
    This class is used internally by BasicSSCell to evaluate formulas.

1.4.5 MappingDialog
    This class is used internally by FancySSCell to set up ScalarMaps.

1.4.6 SpreadSheet
    This is the main spreadsheet user interface class.  It manages
    multiple FancySSCells.

1.4.7 VisADNode
    This is an extension of DefaultMutableTreeNode used by MappingDialog.

2. Features of the SpreadSheet User Interface

2.1 Basic Commands
    The spreadsheet cell with the blue border is the current, highlighted
cell.  Any operation you perform (such as importing a data set), will affect
the highlighted cell.  To change which cell is highlighted, click inside the
desired cell with a mouse button, or press the arrow keys.

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
                      object is mapped to the Display.  Click a Display object
                      on the left, such as DisplayXAxis for the X axis, then
                      click a leaf on the MathType tree on the right.  The
                      "Current Mappings" box on the lower right will change to
                      reflect which mappings you've currently set up.  When
                      you've set up all the mappings to your liking, click the
                      Done button and the spreadsheet will try to display the
                      data object.
      3-D (Java3D) -  Sets the current cell's display dimension to 3-D.  This
                      setting requires Java3D.
      2-D (Java2D) -  Sets the current cell's display dimension to 2-D.  This
                      uses Java2D, which comes with JDK 1.2beta3.  However, in
                      this mode, nothing can be mapped to ZAxis, Latitude, or
                      Alpha.  For computers without OpenGL 3-D acceleration,
                      this mode will provide much better performance, but the
                      display quality will not be as good as 2-D (Java3D).
                      This setting is the default, so that non-Java3D-enabled
                      computers can still use the spreadsheet.
      2-D (Java3D) -  Sets the current cell's display dimension to 2-D.  This
                      requires Java3D.  In this mode, nothing can be mapped to
                      ZAxis or Latitude (but things can be mapped to Alpha).
                      On computers with OpenGL 3-D acceleration, this mode will
                      probably provide better performance than 2-D (Java2D).
                      It also has better display quality than 2-D (Java2D).

      ** The rest of the commands from the Display menu are "quick-maps."
         They scan the current cell's data object for a valid flat function
         and map it in a preset fashion.  These built-in "mapping schemes"
         provide common visualization ScalarMap combinations.  If for some
         reason the quick-maps do not work, you will have to edit the mappings
         manually with the "Edit Mappings" option in the Display menu.

2.3 Toolbars

2.3.1 Main Toolbar
    The main toolbar provides shortcuts to the following menu items:
        File Import, Edit Cut, Edit Copy, Edit Paste, and Display Edit.
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

Formulas can use any of the basic operators:
    + add,   - subtract,   * multiply,   / divide,   % remainder,   ^ power

Formulas can use any of the following binary functions:
    MAX, MIN, ATAN2, ATAN2DEGREES

Formulas can use any of the following unary functions:
    ABS, ACOS, ACOSDEGREES, ASIN, ASINDEGREES, ATAN, ATANDEGREES, CEIL, COS,
    COSDEGREES, EXP, FLOOR, LOG, RINT, ROUND, SIN, SINDEGREES, SQRT, TAN,
    TANDEGREES, NEGATE

Formulas are not case sensitive.

Note that unary minus syntax (e.g., "B2 * -A1") is not supported.  Instead,
the unary function "NEGATE" must be used (e.g., "B2 * NEGATE(A1)").

An example of a valid formula for cell A1 is:
    SQRT(A2 + B2^5 - MIN(B1, C1))

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
      1) The spreadsheet's cell labels have scrollbars obscuring them when
         the spreadsheet window is resized small enough, even though the
         scrollbar policy for the labels is set to *_SCROLLBAR_NEVER.  This
         appears to be a bug in Swing.
      2) Error messages are displayed when the user clicks on a button that
         doesn't make sense (such as trying to set up mappings for an empty
         cell).  These buttons should just be grayed out.
      3) In rare cases, the spreadsheet will lock up when strange formula
         cases occur.  For example, setting cell A1 = A1, cell B1 = A1, then
         trying to clear cell A1 will sometimes lock up the spreadsheet.
      4) The spreadsheet will not import certain data sets correctly, due to
         incomplete implementations in VisAD file adapter forms.
      5) Sometimes the SpreadSheet crashes with an "Invalid instruction"
         error in Windows NT (and possibly other operating systems).  This
         problem is probably due to bugs in Java3D, JDK 1.2beta4, or
         Windows NT rather than VisAD or SpreadSheet.
      6) There is no way to change the number of spreadsheet rows and columns
         without changing the NumVisX and NumVisY variables in
         SpreadSheet.java and then recompiling.
      7) Clicking a cell with an illegal file name or formula (one with a
         large X through it) will not highlight that cell.  The arrow keys
         must be used to select it.

    If you find a bug in the spreadsheet user interface not listed above,
please send e-mail to curtis@ssec.wisc.edu describing the problem,
preferably with a detailed description of how to recreate the problem.

4. Future Plans
    Here's what's coming in the future:
      1) Spreadsheet column and row addition and deletion
      2) Control widgets (such as when something is mapped to IsoContour)
      3) Remote spreadsheet cloning with collaboration
      4) Distributed Cells, Data, etc. (such as data import from http address)
      5) Direct manipulation support
      6) Multiple data per cell
      7) Dynamic linkage of Java code into formulas
      8) Formula enhancements, including derivatives, extraction of pieces of
         a Data object (such as a single function from a multi-function file),
         and composition of multiple Data objects (such as creating an
         animation from multiple spreadsheet cells)
      9) More "quick-maps," such as contour and animation combinations.
     10) Misc. user interface enhancements
     11) And of course, bug fixes.

    If you have any suggestions for features that you would find useful,
please send e-mail to curtis@ssec.wisc.edu describing the feature.
