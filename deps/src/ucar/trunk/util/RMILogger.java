// $Id: RMILogger.java,v 1.1.1.2 2000-08-28 21:54:47 dglo Exp $
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
import java.rmi.server.RemoteServer;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * This is a concrete implementation of the <code>Logger</code> interface
 * which retains consistancy and interoperability with the logging
 * done by <code>java.rmi.server.RemoteServer</code>
 * <p>
 * If the log level of this is set to a value greater than
 * <code>Logger.NOTICE</code>, then rmi server logging is turned on,
 * directed to the same output stream.
 */
public class
RMILogger
	extends AbstractLogger
		implements Logger // redundant spec helps javadoc
{
	/**
	 * Construct a logger that prints messages of
	 * priority up to <code>maxLevel</code> on <code>logStream</code>.
	 */
	public
	RMILogger(int maxLevel, OutputStream logStream)
	{
		maxLevel_ = maxLevel;
		setLog(logStream);
	}

	/**
	 * Default construct prints messages of
	 * priority up to <code>Logger.NOTICE</code>
	 * on <code>System.err</code>.
	 */
	public
	RMILogger()
	{
		this(Logger.NOTICE, System.err);
	}

	private void
	makeConsistent()
	{
		if(maxLevel_ > Logger.NOTICE
				&& RemoteServer.getLog() != logStream_)
			RemoteServer.setLog(logStream_);
	}

	/**
	 * Set the OutputStream where log messages will be printed.
	 * If the log level is greater than <code>Logger.NOTICE</code>,
	 * then <code>java.rmi.server.RemoteServer.setLog(logStream)</code>
	 * is called.
	 * @see java.rmi.server.RemoteServer#setLog
	 */
	public synchronized void
	setLog(OutputStream logStream)
	{
		if(logStream instanceof PrintStream)
			logStream_ = (PrintStream) logStream;
		else
			logStream_ = new PrintStream(logStream);
		makeConsistent();
	}

	/**
	 * Control the verbosity of this Logger.
	 * Messages tagged with level above
	 * <code>maxLevel</code> are discarded.
	 */
	public synchronized void
	logUpTo(int maxLevel)
	{
		maxLevel_ = maxLevel;
		makeConsistent();
	}

	/**
	 * Arrange to log the <code>message</code>
	 * at the given <code>level</code>.
	 */
	public synchronized void
	log(int level, String message)
			throws IOException
	{
		if(level > maxLevel_)
			return;
		// else
		PrintStream ps = RemoteServer.getLog();
		if(ps == null)
			ps = logStream_;
		ps.println(message);
	}
	
	private int maxLevel_;
	private PrintStream logStream_;
}
