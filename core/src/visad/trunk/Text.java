//
// Text.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

  public String toString() {
    return Value;
  }

  public String longString(String pre) {
    return pre + "Text: Value = " + Value +
           "  (TypeName = " + ((TextType) Type).getName() + ")\n";
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
    if (obj == null || !(obj instanceof Text)) {
      return false;
    }

    String objValue = ((Text)obj).getValue();

    if (Value == null) {
      return (objValue == null);
    }

    if (objValue == null) {
      return false;
    }

    return objValue.equals(Value);
  }
}

