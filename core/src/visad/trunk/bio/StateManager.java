//
// StateManager.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

package visad.bio;

import java.io.*;
import javax.swing.JOptionPane;
import visad.VisADException;

/**
 * StateManager contains information needed to recreate a BioVisAD
 * program state, in the case of a program crash or other error.
 */
public class StateManager {

  // -- FIELDS --

  /** BioVisAD frame. */
  private BioVisAD bio;

  /** Temp file for storing temporary state information. */
  private File state;

  /** Temp file for storing temporary measurement information. */
  private File lines;

  /** Temp file for storing undo measurement information. */
  private File oldLines;

  /** Thread for saving state information. */
  private Thread saveThread;

  /** Is a state save needed? */
  private boolean stateDirty = false;

  /** Is a measurement save needed? */
  private boolean measureDirty = false;

  /** Is state currently being restored? */
  private boolean restoring = false;


  // -- CONSTRUCTORS --

  /** Constructs a BioVisAD state management object. */
  public StateManager(BioVisAD biovis) {
    this(biovis, "biovisad.tmp", "lines.tmp", "linesold.tmp");
  }

  /** Constructs a BioVisAD state management object. */
  public StateManager(BioVisAD biovis,
    String state, String lines, String oldLines)
  {
    bio = biovis;
    this.state = new File(state);
    this.lines = new File(lines);
    this.oldLines = new File(oldLines);
  }


  // -- API METHODS --

  /** Restores the last state written to the temp file. */
  public void restoreState() {
    restoring = true;
    try {
      BufferedReader fin = new BufferedReader(new FileReader(state));
      bio.restoreState(fin);
      fin.close();
      if (lines.exists()) new MeasureDataFile(bio, lines).read();
    }
    catch (IOException exc) { exc.printStackTrace(); }
    catch (VisADException exc) { exc.printStackTrace(); }
    restoring = false;
  }

  /** Restores the previous measurement state. */
  public void undo() {
    restoring = true;
    try {
      if (oldLines.exists()) {
        new MeasureDataFile(bio, oldLines).read();
        File temp = oldLines;
        oldLines = lines;
        lines = temp;
      }
    }
    catch (IOException exc) { exc.printStackTrace(); }
    catch (VisADException exc) { exc.printStackTrace(); }
    restoring = false;
  }

  /** Saves the current state to the temp file. */
  public void saveState(boolean doState) {
    if (restoring) return;
    stateDirty = stateDirty || doState;
    measureDirty = measureDirty || !doState;
    if (saveThread == null || !saveThread.isAlive()) {
      saveThread = new Thread(new Runnable() {
        public void run() {
          while (stateDirty || measureDirty) {
            try {
              if (stateDirty) {
                stateDirty = false;
                PrintWriter fout = new PrintWriter(new FileWriter(state));
                bio.saveState(fout);
                fout.close();
              }
              if (measureDirty) {
                measureDirty = false;
                if (oldLines.exists()) oldLines.delete();
                if (lines.exists()) lines.renameTo(oldLines);
                new MeasureDataFile(bio, lines).write(); // do measurements
              }
            }
            catch (IOException exc) { exc.printStackTrace(); }
            catch (VisADException exc) { exc.printStackTrace(); }
          }
        }
      });
      saveThread.start();
    }
  }

  /**
   * Checks whether the state file already exists, and if so,
   * asks the user whether to restore the previous state.
   */
  public void checkState() {
    if (!state.exists() && !lines.exists()) return;
    int ans = JOptionPane.showConfirmDialog(bio,
      "It appears that BioVisAD crashed last time. " +
      "Attempt to restore the previous state?", "BioVisAD",
      JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    if (ans != JOptionPane.YES_OPTION) return;
    restoreState();
  }

  /** Deletes state-related temp files. */
  public void destroy() {
    if (state.exists()) state.delete();
    if (lines.exists()) lines.delete();
    if (oldLines.exists()) oldLines.delete();
  }

}
