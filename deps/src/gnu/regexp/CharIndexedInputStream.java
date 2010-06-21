/*
 *  gnu/regexp/CharIndexedReader.java
 *  Copyright (C) 1998 Wes Biggs
 *
 *  This library is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Library General Public License as published
 *  by the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package gnu.regexp;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

// FIXME: Integer.MAX_VALUE is a hack
// TODO: move(x) shouldn't rely on calling next() x times

class CharIndexedInputStream implements CharIndexed {
  private static final int BUFFER_INCREMENT = 1024;

  private BufferedInputStream br;
  private int m_index, m_end, m_bufsize;
  private char cached;
  
  CharIndexedInputStream(InputStream str, int index) {
    if (str instanceof BufferedInputStream) br = (BufferedInputStream) str;
    else br = new BufferedInputStream(str,BUFFER_INCREMENT);
    m_bufsize = BUFFER_INCREMENT;
    m_index = -1;
    m_end = Integer.MAX_VALUE; // end is unknown
    next();
    if (index > 0) move(index);
  }

  private boolean next() {
    if (m_end == 1) return false;
    m_end--; // closer to end
    try {
      if (m_index != -1) {
	br.reset();
      }
      int i = br.read();
      br.mark(m_bufsize);
      if (i == -1) {
	m_end = 1;
	cached = CharIndexed.OUT_OF_BOUNDS;
	return false;
      }
      cached = (char) i;
      m_index = 1;
    } catch (IOException e) { 
      e.printStackTrace();
      cached = CharIndexed.OUT_OF_BOUNDS;
      return false; 
    }
    return true;
  }

  public char charAt(int index) {
    if (index == 0) return cached;
    if (index >= m_end) return CharIndexed.OUT_OF_BOUNDS;
    if (index >= m_bufsize) {
      // Allocate more space in the buffer.
      try {
	while (m_bufsize <= index) m_bufsize += BUFFER_INCREMENT;
	br.reset();
	br.mark(m_bufsize);
	br.skip(index-1);
      } catch (IOException e) { }
    } else if (m_index != index) {
      try {
	br.reset();
	br.skip(index-1);
      } catch (IOException e) { }
    }
    char ch = CharIndexed.OUT_OF_BOUNDS;

    try {
      int i = br.read();
      m_index = index+1; // m_index is index of next pos relative to charAt(0)
      if (i == -1) {
	// set flag that next should fail next time?
	m_end = index;
	return ch;
      }
      ch = (char) i;
    } catch (IOException ie) { }

    return ch;
  }

  public boolean move(int index) {
    // move read position [index] clicks from 'charAt(0)'
    boolean retval = true;
    while (retval && (index-- > 0)) retval = next();
    return retval;
  }

  public boolean isValid() {
    return (cached != CharIndexed.OUT_OF_BOUNDS);
  }
}

