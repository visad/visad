/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.util;
import java.io.IOException;
import java.rmi.server.RemoteServer;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 */
public class
RMILogger
	extends AbstractLogger
{
	public
	RMILogger(int maxLevel, OutputStream logStream)
	{
		maxLevel_ = maxLevel;
		setLog(logStream);
	}

	public
	RMILogger()
	{
		this(Logger.NOTICE, System.err);
	}

	private void
	makeConsistant()
	{
		if(maxLevel_ > Logger.NOTICE
				&& RemoteServer.getLog() != logStream_)
			RemoteServer.setLog(logStream_);
	}

	public synchronized void
	setLog(OutputStream logStream)
	{
		if(logStream instanceof PrintStream)
			logStream_ = (PrintStream) logStream;
		else
			logStream_ = new PrintStream(logStream);
		makeConsistant();
	}

	/**
	 * @see Logger#logUpTo
	 */
	public synchronized void
	logUpTo(int maxLevel)
	{
		maxLevel_ = maxLevel;
		makeConsistant();
	}

	/**
	 * @see Logger#logUpTo
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
