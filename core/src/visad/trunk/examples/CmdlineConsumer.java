public interface CmdlineConsumer
{
  /**
   * Method used to initialize any instance variables which may be
   * changed by a cmdline option.<br>
   * <br>
   * This is needed because arguments are processed inside the
   * constructor.  This means that the first line in the constructor
   * of classes which extend this class will be <tt>super(args)</tt>,
   * which gets run <em>before</em> any instance variables for that
   * class are initialized.
   */
  void initializeArgs();

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
  int checkOption(String mainName, char ch, String arg);

  /**
   * A short string included in the usage message to indicate
   * valid options.  An example might be <tt>"[-t type]"</tt>.
   *
   * @return A <em>very</em> terse description string.
   */
  String optionUsage();

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
  int checkKeyword(String mainName, int thisArg, String[] args);

  /**
   * A short string included in the usage message to indicate
   * valid keywords.<br>
   * <br>
   * An example might be <tt>"[username] [password]"</tt>.
   *
   * @return A <em>very</em> terse description string.
   */
  String keywordUsage();

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
  boolean finalizeArgs(String mainName);
}
