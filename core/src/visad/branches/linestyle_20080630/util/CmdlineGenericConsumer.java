/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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

/**
 * Simple implementation of CmdlineConsumer interface which
 * can be <tt>extend</tt>ed to easily add command-line parsing
 * to an application.
 */
public class CmdlineGenericConsumer
  implements CmdlineConsumer
{
  /**
   * Method used to initialize any instance variables which may be
   * changed by a cmdline option.<br>
   * <br>
   * This is needed when arguments are processed inside the
   * constructor.  If the first line in the constructor in a class
   * which extends this class is <tt>super(args)</tt>,
   * {@link visad.util.CmdlineParser CmdlineParser} will be run
   * <em>before</em> any instance variables for the extending
   * class are initialized.<br>
   * <br>
   * To ensure all instance variables are properly initialized,
   * place the initialization code in the initializeArgs() method.
   */
  public void initializeArgs() { }

  /**
   * Handle subclass-specific command line options and their arguments.<br>
   * <br>
   * If <tt>-abc -d efg -h -1 -i</tt> is specified, this
   * method will be called a maximum of 5 times:<ul>
   * <li><tt>checkOption(mainName, 'a', "bc");</tt>
   * <li><tt>checkOption(mainName, 'd', "efg");</tt>
   * <li><tt>checkOption(mainName, 'h', "-1");</tt>
   * <li><tt>checkOption(mainName, '1', "-i");</tt>
   * <li><tt>checkOption(mainName, 'i', null);</tt>
   * </ul>
   * <br>
   * Note that either of the last two method calls may not
   * happen if the preceeding method call claims to have used
   * the following argument (by returning <tt>2</tt>.<br>
   * <br>
   * For example,
   * if the third call (where <tt>ch</tt> is set to <tt>'h'</tt>)
   * returns <tt>0</tt> or <tt>1</tt>, the next call will contain
   * <tt>'1'</tt> and <tt>"-i"</tt>.  If, however, the third call
   * returns <tt>2</tt>, the next call will contain <tt>'i'</tt>
   * and <tt>null</tt>.
   *
   * @param mainName The name of the main class (useful for
   *                 error messages.)
   * @param ch Option character.  If <tt>-a</tt> is specified
   *           on the command line, <tt>'a'</tt> would be passed to
   *           this method.)
   * @param arg The argument associated with this option.
   *
   * @return less than <tt>0</tt> to indicate an error<br>
   *         <tt>0</tt> to indicate that this option is not used by this
   *         class<br>
   *         <tt>1</tt> to indicate that only the option was used<br>
   *         <tt>2</tt> or greater to indicate that both the option and the
   *         argument were used
   */
  public int checkOption(String mainName, char ch, String arg)
  {
    return 0;
  }

  /**
   * A short string included in the usage message to indicate
   * valid options.  An example might be <tt>"[-t type]"</tt>.
   *
   * @return A <em>very</em> terse description string.
   */
  public String optionUsage() { return ""; }

  /**
   * Handle subclass-specific command line options and their arguments.
   *
   * @param mainName The name of the main class (useful for
   *                 error messages.)
   * @param thisArg The index of the current keyword.
   * @param args The full list of arguments.
   *
   * @return less than <tt>0</tt> to indicate an error<br>
   *         <tt>0</tt> to indicate that this argument is not used by this
   *         class<br>
   *         <tt>1 or more</tt> to indicate the number of arguments used<br>
   */
  public int checkKeyword(String mainName, int thisArg, String[] args)
  {
    return 0;
  }

  /**
   * A short string included in the usage message to indicate
   * valid keywords.<br>
   * <br>
   * An example might be <tt>"[username] [password]"</tt>.
   *
   * @return A <em>very</em> terse description string.
   */
  public String keywordUsage() { return ""; }

  /**
   * Validate arguments after argument parsing has finished.<br>
   * <br>
   * This is useful for verifying that all required keywords and
   * options have been specified, that options don't conflict
   * with one another, etc.
   *
   * @param mainName The name of the main class (useful for
   *                 error messages.)
   *
   * @return <tt>true</tt> if everything is correct, <tt>false</tt>
   *         if there is a problem.
   */
  public boolean finalizeArgs(String mainName)
  {
    return true;
  }
}
