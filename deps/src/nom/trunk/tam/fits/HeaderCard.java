package nom.tam.fits;

 /*
  * Copyright: Thomas McGlynn 1997-1998.
  * This code may be used for any purpose, non-commercial
  * or commercial so long as this copyright notice is retained
  * in the source code or included in or referred to in any
  * derived software.
  * Many thanks to David Glowacki (U. Wisconsin) for substantial
  * improvements, enhancements and bug fixes -- including
  * this class.
  */


/** This class describes methods to access and manipulate the individual
  * cards for a FITS Header.
  */
public class HeaderCard
{
  /** The keyword part of the card (set to null if there's no keyword) */
  String key;

  /** The value part of the card (set to null if there's no value) */
  String value;

  /** The comment part of the card (set to null if there's no comment) */
  String comment;

  /** A flag indicating whether or not this is a string value */
  boolean isString;

  /** Maximum length of a FITS keyword field */
  private static final int MAX_KEYWORD_LENGTH = 8;

  /** Maximum length of a FITS value field */
  private static final int MAX_VALUE_LENGTH = 70;

  /** padding for building card images */
  private String space40 = "                                        ";

  /** Create a HeaderCard from its component parts
    * @param key keyword (null for a comment)
    * @param value value (null for a comment or keyword without an '=')
    * @param comment comment
    * @exception HeaderCardException for any invalid keyword or value
    */
  public HeaderCard(String key, String value, String comment)
	throws HeaderCardException
  {
    if (key == null && value != null) {
      throw new HeaderCardException("Null keyword with non-null value");
    }

    if (key != null && key.length() > MAX_KEYWORD_LENGTH) {
      throw new HeaderCardException("Keyword too long");
    }

    if (value != null) {
      value = value.trim();

      if (value.length() > MAX_VALUE_LENGTH) {
	throw new HeaderCardException("Value too long");
      }

      if (value.charAt(0) == '\'') {
	if (value.charAt(value.length()-1) != '\'') {
	  throw new HeaderCardException("Missing end quote in string value");
	}

	value = value.substring(1,value.length()-1).trim();

	isString = true;
      }
    }

    this.key = key;
    this.value = value;
    this.comment = comment;
  }

  /** Create a HeaderCard from a FITS card image
    * @param card the 80 character card image
    */
  public HeaderCard(String card)
  {
    key = null;
    value = null;
    comment = null;
    isString = false;


    // We are going to assume that the value has no blanks in
    // it unless it is enclosed in quotes.  Also, we assume that
    // a / terminates the string (except inside quotes)

    // treat short lines as special keywords
    if (card.length() < 9) {
      key = card;
      return;
    }

    // extract the key
    key = card.substring(0, 8).trim();

    // if it's an empty key, assume the remainder of the card is a comment
    if (key.length() == 0) {
      key = "";
      comment = card.substring(8);
      return;
    }

    // Non-key/value pair lines are treated as keyed comments
    if (!card.substring(8,10).equals("= ")) {
      comment = card.substring(8);
      return;
    }

    // extract the value/comment part of the string
    String valcom = card.substring(10).trim();

    // if there's no value/comment part, we're done
    if (valcom.length() == 0) {
      value = "";
      return;
    }

    int vend = -1;
    boolean quote = false;

    // If we have a ' then find the matching quote.
    if (valcom.charAt(0) == '\'') {

      int offset = 1;
      while (offset < valcom.length()) {

	// look for next single-quote character
	vend = valcom.indexOf("'", offset);;

	// if the quote character is the last character on the line...
	if (vend == valcom.length()-1) {
	  break;
	}

	// if we didn't find a matching single-quote...
	if (vend == -1) {
	  // pretend this is a comment card
	  key = null;
	  comment = card;
	  return;
	}

	// if this isn't an escaped single-quote, we're done
	if (valcom.charAt(vend+1) != '\'') {
	  break;
	}

	// skip past escaped single-quote
	offset = vend+2;
      }

      // break apart character string
      value = valcom.substring(1, vend).trim();
      isString = true;
    }

    // look for a / to terminate the field.
    int slashLoc = valcom.indexOf('/');
    if (slashLoc != -1) {
      comment = valcom.substring(slashLoc+1).trim();
      valcom = valcom.substring(0, slashLoc).trim();
    }

    // if we didn't already save a string value, do it now
    if (!isString) {
      value = valcom;
    }
  }

  /** Does this card contain a string value?
    */
  public boolean isStringValue()
  {
    return isString;
  }

  /** Is this a key/value card?
    */
  public boolean isKeyValuePair()
  {
    return (key != null && value != null);
  }

  /** Return the keyword from this card
    */
  public String getKey()
  {
    return key;
  }

  /** Return the value from this card
    */
  public String getValue()
  {
    return value;
  }

  /** Return the comment from this card
    */
  public String getComment()
  {
    return comment;
  }

  /** Return the 80 character card image
    */
  public String toString()
  {
    StringBuffer buf = new StringBuffer(80);

    // start with the keyword, if there is one
    if (key != null) {
      buf.append(key);
    }

    // fill keyword field with blanks
    while (buf.length() < 8) {
      buf.append(' ');
    }

    if (value != null) {
      buf.append("= ");

      if (isString) {
	// left justify the string inside the quotes
	buf.append('\'');
	buf.append(value);
	while (buf.length() < 19) {
	  buf.append(' ');
	}
	buf.append('\'');
      } else {
	int offset = buf.length();
	buf.append(value);

	// right justify the value field to column 30
	while (buf.length() < 30) {
	  buf.insert(offset, ' ');
	}
      }

      // if there's a comment, add a comment delimiter
      if (comment != null) {
	buf.append(" / ");
      }
    } else if (comment != null && comment.startsWith("= ")) {
      buf.append("  ");
    }

    // finally, add any comment
    if (comment != null) {
      buf.append(comment);
    }

    // make sure the final string is exactly 80 characters long
    if (buf.length() > 80) {
      buf.setLength(80);
    } else {

      if (buf.length() < 40) {
	buf.append(space40);
      }

      if (buf.length() < 80) {
	buf.append(space40.substring(0, 80 - buf.length()));
      }
    }

    return buf.toString();
  }
}
