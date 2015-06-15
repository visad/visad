/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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

package visad.util;

import java.util.ArrayList;

/**
 * Parse command-line arguments passed to the initial main() method
 * of an application.<br>
 * <br>
 * To use this, a class would implement <tt>CmdlineConsumer</tt>,
 * then add the following code snippet to the constructor
 * (assuming the constructor is supplied a list of arguments
 *  named <tt>'args'</tt>):<br>
 * <pre>
 * <code>
 *    CmdlineParser cmdline = new CmdlineParser(this);
 *    if (!cmdline.processArgs(args)) {
 * </code>
 * <i>
 *      complain about errors, exit, etc.
 * </i>
 * <code>
 *    }
 * </code>
 *
 */
public class CmdlineParser
{
  private String mainName;
  private ArrayList list;

  /**
   * Create a command-line parser.
   *
   * @param mainClass The class in which the main() method lives.
   */
  public CmdlineParser(Object mainClass)
  {
    String className = mainClass.getClass().getName();
    int pt = className.lastIndexOf('.');
    final int ds = className.lastIndexOf('$');
    if (ds > pt) {
      pt = ds;
    }

    mainName = className.substring(pt == -1 ? 0 : pt + 1);
    list = null;

    if (mainClass instanceof CmdlineConsumer) {
      addConsumer((CmdlineConsumer )mainClass);
    }
  }

  /**
   * Add a command-line argument/keyword consumer.
   *
   * consumer Class which implements <tt>CmdlineConsumer</tt>.
   */
  public void addConsumer(CmdlineConsumer consumer)
  {
    if (list == null) {
      list = new ArrayList();
    }

    list.add(consumer);
  }

  /**
   * Get the name of the main class.
   *
   * @return main class name
   */
  public String getMainClassName() { return mainName; }

  /**
   * Pass all options/keywords on to all
   * {@link CmdlineConsumer CmdlineConsumer}s.
   *
   * @param args Array of command-line arguments passed to main() method.
   */
  public boolean processArgs(String[] args)
  {
    boolean usage = false;

    // if the are no consumers or arguments, we're done
    if (list == null || args == null) {
      return true;
    }

    // add consumers from newest to oldest
    CmdlineConsumer[] consumers = new CmdlineConsumer[list.size()];
    for (int c = 0; c < consumers.length; c++) {
      consumers[c] = (CmdlineConsumer )list.get(consumers.length - (c + 1));
    }

    for (int c = 0; c < consumers.length; c++) {
      consumers[c].initializeArgs();
    }

    for (int i = 0; !usage && i < args.length; i++) {
      if (args[i].length() > 0 && args[i].charAt(0) == '-') {
        char ch = args[i].charAt(1);

        String str, result;

        boolean strInOption = false;
        if (args[i].length() > 2) {
          str = args[i].substring(2);
          strInOption = true;
        } else if ((i + 1) < args.length) {
          str = args[i+1];
        } else {
          str = null;
        }

        int handled;
        for (int c = 0; c < consumers.length; c++) {
          handled = consumers[c].checkOption(mainName, ch, str);
          if (handled > 0) {
            if (handled > 1) {
              if (strInOption) {
                handled = 1;
              } else {
                handled = 2;
              }
            }
            i += (handled - 1);
            break;
          } else {
            if (handled == 0) {
              System.err.println(mainName + ": Unknown option \"-" + ch +
                                 "\"");
            }

            usage = true;
          }
        }
      } else {
        int handled;
        for (int c = 0; c < consumers.length; c++) {
          handled = consumers[c].checkKeyword(mainName, i, args);
          if (handled > 0) {
            i += (handled - 1);
            break;
          } else {
            if (handled == 0) {
              System.err.println(mainName + ": Unknown keyword \"" +
                                 args[i] + "\"");
            }

            usage = true;
          }
        }
      }
    }

    for (int c = 0; !usage && c < consumers.length; c++) {
      usage |= !consumers[c].finalizeArgs(mainName);
    }

    if (usage) {
      StringBuffer buf = new StringBuffer("Usage: " + mainName);
      for (int c = 0; c < consumers.length; c++) {
        buf.append(consumers[c].optionUsage());
      }
      for (int c = 0; c < consumers.length; c++) {
        buf.append(consumers[c].keywordUsage());
      }
      System.err.println(buf.toString());
    }

    return !usage;
  }
}
