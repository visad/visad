/*
 * VisAD system for interactive analysis and visualization of numerical
 * data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
 * Rink, Dave Glowacki, and Steve Emmerson.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License in file NOTICE for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * $Id: BadRepositoryException.java,v 1.2 1998-02-23 14:33:09 steve Exp $
 */

package visad.data;


import visad.VisADException;


/**
 * Exception thrown when there's something wrong with the repository.
 */
public class BadRepositoryException extends VisADException
{
    /**
     * Construct an exception with a message.
     */
    public BadRepositoryException(String msg)
    {
	super(msg);
    }
}
