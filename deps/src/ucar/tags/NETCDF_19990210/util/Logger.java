/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.util;
import java.io.IOException;

/**
 * This interface provides logging functions for daemon applications
 * such as servers. It looks a lot like UCAR's ulog(3) C language
 * interface, which in turn looks like UNIX syslog(3C) client
 * interface.
 * <p>	
 * Log messages are tagged with a numeric logging level,
 * selected from the ordered list of constants below.
 * Higher levels are more verbose. An implementation would
 * use the level of a message to decide where and whether to
 * write the message.
 * <p>
 * The three lowest logging levels,
 * 	<code>EMERG</code>,
 *	<code>ALERT</code>, and
 *	<code>CRIT</code>,
 * should probably never be
 * assigned by user (non-kernel or non JVM?) code.
 * <p>
 * @note: The shorthand methods are silent in the face
 * of internal exceptions.
 *
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.2 $ $Date: 2000-08-28 21:45:48 $
 */
public interface
Logger
{
	/**
	 * Log level for messages indicating that
	 * the system is unusable.
	 * Included only for syslog compatiblity.
	 */
	public static final int EMERG     = 0;
	/**
	 * Log level for messages indicating that
	 * action must be taken immediately.
	 * Included only for syslog compatiblity.
	 */
	public static final int ALERT     = 1;
	/**
	 * Log level for messages indicating
	 * critical conditions.
	 * Included only for syslogd compatiblity.
	 */
	public static final int CRIT      = 2;
	/**
	 * Log level for error messages.
	 * Included only for syslog compatiblity.
	 */
	public static final int ERR       = 3;
	/**
	 * Log level for warnings.
	 */
	public static final int WARNING   = 4;
	/**
	 * Log level for messages indicating a
	 * normal but significant condition.
	 */
	public static final int NOTICE    = 5;
	/**
	 * Log level for informational (verbose) messages.
	 */
	public static final int INFO      = 6;
	/**
	 * Log level for debug messages
	 */
	public static final int DEBUG     = 7;

	/**
	 * Control the verbosity of the implementation.
	 * Messages tagged with level above
	 * <code>maxLevel</code> may be discarded.
	 */
	public void
	logUpTo(int maxLevel);

	/**
	 * Arrange to log the <code>message</code>
	 * at the given <code>level</code>.
	 *
	 * @param level Int value which is one of
	 * 	<code>EMERG</code>,
	 *	<code>ALERT</code>,
	 *	<code>CRIT</code>,
	 *	<code>ERR</code>,
	 *	<code>WARNING</code>,
	 *	<code>NOTICE</code>,
	 *	<code>INFO</code>, or
	 *	<code>DEBUG</code>.
	 * @param String message to be logged.
	 */
	public void
	log(int level, String message)
			throws IOException;

	/**
	 * Shorthand for <code>log(Logger.ERR, message)</code>.
	 * @see #ERR
	 * @see #log
	 */
	public void
	logError(String message);

	/**
	 * Shorthand for <code>log(Logger.NOTICE, message)</code>.
	 * @see #NOTICE
	 * @see #log
	 */
	public void
	logNotice(String message);

	/**
	 * Shorthand for <code>log(Logger.INFO, message)</code>.
	 * @see #INFO
	 * @see #log
	 */
	public void
	logInfo(String message);

	/**
	 * Shorthand for <code>log(Logger.DEBUG, message)</code>.
	 * @see #DEBUG
	 * @see #log
	 */
	public void
	logDebug(String message);
}
