// $Id: Logger.java,v 1.1.1.3 2000-08-28 21:54:47 dglo Exp $
/*
 * Copyright 1997-2000 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
package ucar.util;
import java.io.IOException;

/**
 * This interface provides logging functions for daemon applications
 * such as servers. It looks a lot like UCAR's <code>ulog(3)</code>
 * C language interface, which in turn looks like the UNIX
 * <code>syslog(3C)</code> client interface.
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
 * Note: By default, the shorthand methods are silent
 * in the face of internal exceptions.
 *
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.3 $ $Date: 2000-08-28 21:54:47 $
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
