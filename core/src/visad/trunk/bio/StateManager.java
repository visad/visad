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
 * StateManager contains information needed to recreate a VisBio
 * program state, in the case of a program crash or other error.
 */
public class StateManager {

  // -- FIELDS --

  /** VisBio frame. */
  private VisBio bio;

  /** Temp file for storing temporary state information. */
  private File state;

  /** Temp file for storing undo state information. */
  private File oldState;

  /** Thread for saving state information. */
  private Thread saveThread;

  /** Is a state save needed? */
  private boolean dirty = false;

  /** Is state currently being restored? */
  private boolean restoring = false;


  // -- CONSTRUCTORS --

  /** Constructs a VisBio state management object. */
  public StateManager(VisBio biovis) {
    this(biovis, "visbio.tmp", "vb-old.tmp");
  }

  /** Constructs a VisBio state management object. */
  public StateManager(VisBio biovis, String state, String oldState) {
    bio = biovis;
    this.state = new File(state);
    this.oldState = new File(oldState);
  }


  // -- API METHODS --

  /** Restores the latest state from the temp file. */
  public void restoreState() { restoreState(state); }

  /** Restores the previous state from the backup temp file. */
  public void undo() {
    restoreState(oldState);
    File temp = oldState;
    oldState = state;
    state = temp;
  }

  /** Saves the current state to the temp file. */
  public void saveState() {
    if (oldState.exists()) oldState.delete();
    if (state.exists()) state.renameTo(oldState);
    saveState(state);
  }

  /** Restores the state from the given state file. */
  public void restoreState(File stateFile) {
    restoring = true;
    /* CTR: TEMP */ System.out.println("Restoring state from " + stateFile);
    try {
      BufferedReader fin = new BufferedReader(new FileReader(stateFile));
      bio.restoreState(fin);
      fin.close();
    }
    catch (IOException exc) { exc.printStackTrace(); }
    catch (VisADException exc) { exc.printStackTrace(); }
    restoring = false;
  }

  /** Saves the current state to the temp file. */
  public void saveState(File stateFile) {
    if (restoring) return;
    dirty = true;
    if (saveThread == null) {
      final File fstate = stateFile;
      saveThread = new Thread(new Runnable() {
        public void run() {
          while (dirty) {
            /* CTR: TEMP */ System.out.println("Saving state to " + fstate);
            try {
              dirty = false;
              PrintWriter fout = new PrintWriter(new FileWriter(fstate));
              bio.saveState(fout);
              fout.close();
            }
            catch (IOException exc) { exc.printStackTrace(); }
            catch (VisADException exc) { exc.printStackTrace(); }
          }
        }
      });
    }
    if (!saveThread.isAlive()) saveThread.start();
  }

  /**
   * Checks whether the state file already exists, and if so,
   * asks the user whether to restore the previous state.
   */
  public void checkState() {
    if (!state.exists()) return;
    int ans = JOptionPane.showConfirmDialog(bio,
      "It appears that VisBio crashed last time. " +
      "Attempt to restore the previous state?", "VisBio",
      JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    if (ans != JOptionPane.YES_OPTION) return;
    restoreState();
  }

  /** Deletes state-related temp files. */
  public void destroy() {
    if (state.exists()) state.delete();
    if (oldState.exists()) oldState.delete();
  }

}
