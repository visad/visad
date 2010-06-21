// $Id: AbstractLogger.java,v 1.1.1.2 2000-08-28 21:54:47 dglo Exp $
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
 * Partial implementation of Logger.
 * Implements the shorthand <code>logXXXX(message)</code> methods in terms
 * of the primitive <code>log(XXXX, message)</code> method.
 * @see Logger
 */
public abstract class
AbstractLogger
		implements Logger
{
	/**
	 * Implementation hook to deal with internal exceptions.
	 * This implementation does nothing.	
	 * Override this method to do something
	 * about <code>IOException</code> in the <code>Logger</code>
	 * shorthand methods implemented here.
	 */
	protected void
	logLogException(IOException ioe, String message)
	{
	}


	/**
	 * Log the <code>message</code>
	 * at priority <code>Logger.ERR</code>.
	 * @see Logger#ERR
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
	 * Log the <code>message</code>
	 * at priority <code>Logger.NOTICE</code>.
	 * @see Logger#NOTICE
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
	 * Log the <code>message</code>
	 * at priority <code>Logger.INFO</code>.
	 * @see Logger#INFO
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
	 * Log the <code>message</code>
	 * at priority <code>Logger.DEBUG</code>.
	 * @see Logger#DEBUG
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
