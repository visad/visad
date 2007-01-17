/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

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

package visad;

public class MessageEvent
{
  public static final int ID_GENERIC = 1;

  private int id, originator;
  private String str;
  private RemoteData data;

  /**
   * Create a message event with the given ID
   *
   * @param id Message ID
   */
  public MessageEvent(int id)
  {
    this(id, -1, null, null);
  }

  /**
   * Create a message event with the given string
   *
   * @param str Message <tt>String</tt>
   */
  public MessageEvent(String str)
  {
    this(ID_GENERIC, -1, str, null);
  }

  /**
   * Create a message event with the given data
   *
   * @param data Message <tt>Data</tt>
   */
  public MessageEvent(RemoteData data)
  {
    this(ID_GENERIC, -1, null, data);
  }

  /**
   * Create a message event with the given ID and string
   *
   * @param id Message ID
   * @param str Message <tt>String</tt>
   */
  public MessageEvent(int id, String str)
  {
    this(id, -1, str, null);
  }

  /**
   * Create a message event with the given ID and data
   *
   * @param id Message ID
   * @param data Message <tt>Data</tt>
   */
  public MessageEvent(int id, RemoteData data)
  {
    this(id, -1, null, data);
  }

  /**
   * Create a message event with the given string and data
   *
   * @param str Message <tt>String</tt>
   * @param data Message <tt>Data</tt>
   */
  public MessageEvent(String str, RemoteData data)
  {
    this(ID_GENERIC, -1, str, data);
  }


  /**
   * Create a message event with the given ID, string and data
   *
   * @param id Message ID
   * @param str Message <tt>String</tt>
   * @param data Message <tt>Data</tt>
   */
  public MessageEvent(int id, String str, RemoteData data)
  {
    this(id, -1, str, data);
  }

  /**
   * Create a message event with the given IDs, string and data
   *
   * @param id Message ID
   * @param originator Originator ID.
   * @param str Message <tt>String</tt>
   * @param data Message <tt>Data</tt>
   */
  public MessageEvent(int id, int originator, String str, RemoteData data)
  {
    this.id = id;
    this.originator = originator;
    this.str = str;
    this.data = data;
  }

  /**
   * Get this message's ID.
   *
   * @return The message ID.
   */
  public int getId() { return id; }

  /**
   * Get the ID of the originator of this message.
   *
   * @return The originator's ID.
   */
  public int getOriginatorId() { return originator; }

  /**
   * Get the string associated with this message.
   *
   * @return The message string.
   */
  public String getString() { return str; }

  /**
   * Get the data associated with this message.
   *
   * @return The message <tt>Data</tt>.
   */
  public RemoteData getData() { return data; }

  public String toString()
  {
    StringBuffer buf = new StringBuffer(getClass().getName());

    boolean needComma = false;

    buf.append('[');
    if (id != ID_GENERIC) {
      if (needComma) buf.append(',');
      buf.append("id=");
      buf.append(id);
      needComma = true;
    }
    if (originator != -1) {
      if (needComma) buf.append(',');
      buf.append("originator=");
      buf.append(originator);
      needComma = true;
    }
    if (str != null) {
      if (needComma) buf.append(',');
      buf.append("str=\"");
      buf.append(str);
      buf.append("\"");
      needComma = true;
    }
    if (data != null) {
      if (needComma) buf.append(',');
      buf.append("data=");
      try {
        buf.append(data.local());
      } catch(Exception e) {
        buf.append('?');
        buf.append(e.getMessage());
        buf.append('?');
      }
      needComma = true;
    }
    buf.append(']');
    return buf.toString();
  }
}
