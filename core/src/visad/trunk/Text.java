
//
// Text.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad;

/**
   Text is the class of VisAD scalar data for text strings.  The null
   pointer is used to indicate missing.  Text objects are immutable.<P>
*/
public class Text extends Scalar {

  private String Value;

  /** construct a Text object with the missing value */
  public Text(TextType type) throws VisADException {
    super(type);
    if (!(type instanceof TextType)) throw new TypeException("Text: bad type");
    Value = null; // set initial value to missing
  }

  /** construct a Text object */
  public Text(TextType type, String value) throws VisADException {
    super(type);
    if (!(type instanceof TextType)) throw new TypeException("Text: bad type");
    Value = value;
  }

  /** construct a Text object with the generic TEXT type (TextType.Generic) */
  public Text(String value) {
    super(TextType.Generic);
    Value = value;
  }

  // used by clone
  private Text(TextType type, String value, boolean b) {
    super(type);
    Value = value;
  }

  public String getValue() {
    return Value;
  }

  public boolean isMissing() {
    return (Value == null);
  }

  public Data binary(Data data, int op, int sampling_mode, int error_mode)
              throws VisADException {
    if (data instanceof Text && op == ADD) {
      return new Text((TextType) Type, Value + ((Text) data).getValue());
    }
    else {
      throw new TypeException("Text.binary: types don't match");
    }
  }

  public Data unary(int op, int sampling_mode, int error_mode)
              throws VisADException {
    throw new TypeException("Text.unary");
  }

  public DataShadow computeRanges(ShadowType type, DataShadow shadow)
         throws VisADException {
    return shadow;
  }

  public Object clone() {
    return new Text((TextType) Type, Value, true);
  }

  public String toString() {
    return Value;
  }

  public String longString(String pre) {
    return pre + "Text: Value = " + Value +
           "  (TypeName = " + ((RealType) Type).getName() + ")\n";
  }

  /**
   * Compares this Text to another.
   * @param object		The other Text to compare against.  It shall be
   *				a Text.
   * @return                    A negative integer, zero, or a positive integer
   *                            depending on whether this Text is considered
   *                            less than, equal to, or greater than the other
   *                            Text, respectively.
   */
  public int compareTo(Object object) {
    return getValue().compareTo(((Text)object).getValue());
  }

  /**
   * Indicates if this Text is semantically identical to an object.
   * @param obj			The object.
   * @return			<code>true</code> if and only if this Text
   *				is semantically identical to the object.
   */
  public boolean equals(Object obj) {
    return obj != null && obj instanceof Text &&
      ((Text)obj).getValue().equals(getValue());
  }
}

