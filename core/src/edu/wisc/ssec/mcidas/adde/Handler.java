//
// Handler.java
//

/*
This source file is part of the edu.wisc.ssec.mcidas package and is
Copyright (C) 1998 - 2023 by Tom Whittaker, Tommy Jasmin, Tom Rink,
Don Murray, James Kelly, Bill Hibbard, Dave Glowacki, Curtis Rueden
and others.
 
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

package edu.wisc.ssec.mcidas.adde;

/**
 * This class defines the openConnection method, which is
 * used to create an AddeURLConnection.  Note that this 
 * class is automatically loaded when a URL of this type
 * is created, you don't have explicitly create an object.<P> 
 * 
 * The class name allows this to work if the system property
 * java.protocol.handler.pkgs includes the edu.wisc.ssec.mcidas
 * package.
 * @see java.net.URL  for more information
 *
 * @author Don Murray, UCAR/Unidata
 */

public class Handler extends AddeURLStreamHandler {}
