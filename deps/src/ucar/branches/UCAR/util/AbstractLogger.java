/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.util;
import java.io.IOException;

/**
 * Partial implementation of Logger.
 * @see Logger
 */
public abstract class
AbstractLogger
		implements Logger
{
	/**
	 * Implementation hook, override to do something
	 * about IOException in Logger shorthand methods.
	 * Default does nothing.	
	 */
	protected void
	logLogException(IOException ioe, String message)
	{
	}


	/**
	 * @see Logger#logDebug
	 */
	public void
	logError(String message)
	{
		try {
			log(Logger.ERR, message);
		}
		catch (IOException ioe)
		{
			logLogException(ioe, message);
		}
	}

	/**
	 * @see Logger#logDebug
	 */
	public void
	logNotice(String message)
	{
		try {
			log(Logger.NOTICE, message);
		}
		catch (IOException ioe)
		{
			logLogException(ioe, message);
		}
	}

	/**
	 * @see Logger#logDebug
	 */
	public void
	logInfo(String message)
	{
		try {
			log(Logger.INFO, message);
		}
		catch (IOException ioe)
		{
			logLogException(ioe, message);
		}
	}

	/**
	 * @see Logger#logDebug
	 */
	public void
	logDebug(String message)
	{
		try {
			log(Logger.DEBUG, message);
		}
		catch (IOException ioe)
		{
			logLogException(ioe, message);
		}
	}
}
