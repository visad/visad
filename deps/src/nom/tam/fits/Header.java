package nom.tam.fits;

/* Copyright: Thomas McGlynn 1997-1998.
 * This code may be used for any purpose, non-commercial
 * or commercial so long as this copyright notice is retained
 * in the source code or included in or referred to in any
 * derived software.
 * Many thanks to David Glowacki (U. Wisconsin) for substantial
 * improvements, enhancements and bug fixes.
 */

import java.io.*;
import java.util.*;
import nom.tam.util.*;

/** This class is used to maintain linked lists
  * of headers cards with the same keyword.
  */
class KeyChain
{
  HeaderCard card;
  KeyChain next;

  public KeyChain(HeaderCard card)
  {
    this.card = card;
    next = null;
  }
}


/* This class is used to search efficiently for
 * cards using a hash table.
 */
class KeyHash
{
  /** The hashed keys
    */
  private Hashtable hash = new Hashtable();

  /** Initialize a null KeyHash. */
  public KeyHash()
  {
  }

  /** Add a card to the hash.
    * @param card The new card.
    */
  public final void add(HeaderCard card)
  {
    // try to get the key for this card
    String key = card.getKey();
    if (key == null) {
      // must not have a key, so there's nothing to add
      return;
    }
    key = key.toUpperCase();

    // create a wrapper for this card
    KeyChain kc = new KeyChain(card);

    // find this key entry
    KeyChain front = (KeyChain )hash.get(key);
    if (front == null) {
      // add the new key
      hash.put(key, kc);
    } else {
      // find the end of the chain
      while (front.next != null) {
	front = front.next;
      }

      // add this card to the end of the chain
      front.next = kc;
    }
  }

  /* Delete a card from the hash.
   * @param card The card to be deleted.
   */
  public final void delete(HeaderCard card)
  {
    // try to get the key for this card
    String key = card.getKey();
    if (key == null) {
      // must not have a key, so there's nothing to delete
      return;
    }
    key = key.toUpperCase();

    // find this key entry
    KeyChain front = (KeyChain )hash.get(key);
    if (front != null) {

      // search through the chain
      KeyChain prev = null;
      while (front != null) {

	// if we found a match...
	if (front.card.equals(card)) {
	  if (prev != null) {

	    // take this card out of the chain
	    prev.next = front.next;
	  } else if (front.next != null) {

	    // there's a new first card in the chain
	    hash.put(key, front.next);
	  } else {

	    // this must have been the only card for this key
	    hash.remove(key);
	  }

	  // removed the card
	  return;
	}

	// move on down the chain
	prev = front;
	front = front.next;
      }
    }

    // hmmm ... shouldn't have gotten here
    return;
  }

  /** Replace one card with another.
    * @param oldCard The card to be replaced.
    * @param newCard The new card to take to its place.
    */
  public final void replace(HeaderCard oldCard, HeaderCard newCard)
  {
    String oldKey = oldCard.getKey();
    if (oldKey == null) {
      add(newCard);
      return;
    }

    String newKey = newCard.getKey();
    if (newKey == null) {
      delete(oldCard);
      return;
    }

    if (!oldKey.equalsIgnoreCase(newKey)) {
      delete(oldCard);
      add(newCard);
      return;
    }

    // find the old key entry
    String key = oldKey.toUpperCase();
    KeyChain front = (KeyChain )hash.get(key);
    if (front == null) {
      // hmmm ... didn't find the old card ... start a new chain
      hash.put(key, new KeyChain(newCard));
      return;
    }

    // find the end of the chain
    while (true) {

      // if we found the old card, replace it with the new card
      if (front.card.equals(oldCard)) {
	front.card = newCard;
	return;
      }

      // if this is the end of the chain...
      if (front.next == null) {
	break;
      }

      // try the next card in the chain
      front = front.next;
    }

    // hmmm .. didn't find the old card ... add new card at the end
    front.next = new KeyChain(newCard);
  }

  /** Find a given card by its key.  If more than one
    * is in the header, just return the first.
    * @param key The FITS keyword.
    * @return The matching card or <code> null </code> if not found.
    */
  public final HeaderCard find(String key)
  {
    if (key != null) {
      KeyChain kc = (KeyChain )hash.get(key.trim().toUpperCase());
      if (kc != null) {
	return kc.card;
      }
    }

    return null;
  }

  public final boolean contains(String key)
  {
    if (key != null) {
      if (hash.containsKey(key.toUpperCase())) {
	return true;
      }
    }

    return false;
  }
}


/** This class stores a set of FITS header cards.
  */
class CardTable
{
  /** The actual cards stored as a Vector of HeaderCards
    */
  private Vector cards;

  /** The hashed keys
    */
  private KeyHash keymap;

  /** Create an empty table to hold FITS cards.
    * @param initialCapacity the initial capacity of the table.
    * @param capacityIncrement the amount by which the capacity is
    *				increased when the table overflows.
    */
  public CardTable(int initialCapacity, int capacityIncrement)
  {
    cards = new Vector(initialCapacity, capacityIncrement);
    keymap = new KeyHash();
  }

  /** Create an empty table to hold FITS cards.
    */
  public CardTable()
  {
    this(360,180);
  }

  /** Add the specified card to the end of this table.
    * @param card the card to be added.
    */
  public final void addElement(HeaderCard card)
  {
    cards.addElement(card);
    keymap.add(card);
  }

  /** Return the card found at the specified index.
    * @param index an index into this table.
    * @throws ArrayIndexOutOfBoundsException if an invalid index was given.
    */
  public final HeaderCard elementAt(int index)
  {
    return (HeaderCard )cards.elementAt(index);
  }

  /** Searches for the first occurence of the given card.
    * @param card the card be found.
    * @return the index of the first occurrence of the card;
    *		returns -1 if the object is not found.
    */
  public final int indexOf(HeaderCard card)
  {
    return cards.indexOf(card);
  }

  /** Inserts the specified card at the specified index.
    *
    * The index must be greater than or equal to 0 and less than or equal to
    * the current size of the vector.
    * @param card the card be inserted.
    * @param index where the card should be inserted.
    * @throws ArrayIndexOutOfBoundsException if the index was invalid.
    */
  public final void insertElementAt(HeaderCard card, int index)
  {
    cards.insertElementAt(card, index);
    keymap.add(card);
  }

  /** Removes the card at the specified index.
    *
    * The index must be greater than or equal to 0 and less than or equal to
    * the current size of the vector.
    * @param index of the card to be removed.
    * @throws ArrayIndexOutOfBoundsException if the index was invalid.
    */
  public final void removeElementAt(int index)
  {
    HeaderCard oldCard = (HeaderCard )cards.elementAt(index);
    cards.removeElementAt(index);
    keymap.delete(oldCard);
  }

  /** Replace the card at the specified index with the specified card.
    *
    * The index must be greater than or equal to 0 and less than or equal to
    * the current size of the vector.
    * @param index of the card to be replaced.
    * @throws ArrayIndexOutOfBoundsException if the index was invalid.
    */
  public final void setElementAt(HeaderCard card, int index)
  {
    HeaderCard oldCard = (HeaderCard )cards.elementAt(index);
    cards.setElementAt(card, index);
    keymap.replace(oldCard, card);
  }

  /** Returns the number of cards in this table.
    * @return the number of cards.
    */
  public final int size()
  {
    return cards.size();
  }

  /** Finds the first occurence of the specified key.
    * @param key the keyword to be found.
    * @return the first card matching this keyword;
    *		returns -1 if the keyword was not found.
    */
  public final HeaderCard findKey(String key)
  {
    return keymap.find(key);
  }

  /** Tests if the specified keyword is present in this table.
    * @param key the keyword to be found.
    * @return <CODE>true<CODE> if the specified keyword is present in this
    *		table; <CODE>false<CODE> otherwise.
    */
  public final boolean containsKey(String key)
  {
    return keymap.contains(key);
  }
}

/** This class describes methods to access and manipulate the header
  * for a FITS HDU.
  */
public class Header extends Object {

    /** The actual header data stored as a Vector of character strings
      */
    private CardTable  cards = new CardTable();

    /** The mark is used to describe where actions on the header
      * should take place.  The mark points to the 'current' card
      * in the header.  A value of -1 is used to indicate
      * that insertions should occur at the beginning of the
      * header.
      */
    private int mark = -2;

    /** Create a Header with no card images.
      */
    public Header (){
    }

    public int size() {
        return cards.size();
    }

    /** Create a header and populate it from the input stream
      * @param is  The input stream where header information is expected.
      */
    public Header(BufferedDataInputStream is)
	throws TruncatedFileException, IOException
    {
	read(is);
    }

    /** Create a header and initialize it with a vector of strings.
      * @param newCards Card images to be placed in the header.
      */
    public Header(String[] newCards) {

      for (int i=0;  i < newCards.length; i += 1) {
           cards.addElement(new HeaderCard(newCards[i]));
      }

    }


    /** Calculate the unpadded size of the data segment from
      * the header information.  Note that this algorithm is
      * not correct for Random Groups format data.
      * @return the unpadded data segment size.
      */
    public int trueDataSize() {
	if (!isValidHeader()) {
          return 0;
	}

	int size = 1;

	int naxis = getIntValue("NAXIS", 0);
	for (int axis = 1; axis <= naxis; axis += 1) {
	    int nval = getIntValue("NAXIS"+axis, 0);
	    if (axis != 1 || nval != 0) {
	        size *= nval;
	    }
	}

	size += getIntValue("PCOUNT", 0);
	size *= getIntValue("GCOUNT", 1);
	size *= Math.abs(getIntValue("BITPIX", 0))/8;

        return size;
    }

    /** Return the size of the data including any needed padding.
      * @return the data segment size including any needed padding.
      */

    public int paddedDataSize() {
	  return ((trueDataSize() + 2879)/2880)*2880;
    }

    /** Return the size of the header data including padding.
      * @return the header size including any needed padding.
      */
    public int headerSize() {
       if (!isValidHeader()) {
           return 0;
       }

       return ((cards.size()*80 + 2879)/2880) * 2880;
    }


    /** Is this a valid header.  This routine provides only
      * minimal checking currently.
      * @return <CODE>true</CODE> for a valid header,
      *		<CODE>false</CODE> otherwise.
      */
    public boolean isValidHeader() {
	// Probably should do something more sophisticated than this...
	return (cards != null && cards.size() >= 5);
    }


    /** Get the n'th card image in the header
      * @return the card image; return <CODE>null</CODE> if the n'th card
      *		does not exist.
      */
    public String getCard(int n) {
        try {
	    if (n >= 0 && n < cards.size()) {
	      return ((HeaderCard) cards.elementAt(n)).toString();
	    }
        } catch (NoSuchElementException e) {
	}

        return null;
    }

    /** Get the n'th key in the header.
      * @return the card image; return <CODE>null</CODE> if the n'th key
      *		does not exist.
      */
    public String getKey(int n) {

        String card = getCard(n);
        if (card == null) {
            return null;
        }

        String key = card.substring(0,8);
        if (key.charAt(0) == ' ') {
           return "";
        }


        if (key.indexOf(' ') >= 1) {
            key = key.substring(0,key.indexOf(' '));
        }
        return key;
    }

    /** Get the <CODE>long</CODE> value associated with the given key.
      * @param key   The header key.
      * @param dft   The default value to be returned if the key cannot be found.
      * @return the associated value.
      */
    public long getLongValue(String key, long dft) {

	HeaderCard fcard = findCard(key);
	if (fcard == null) {
	  return dft;
	}

	try {
	    String v = fcard.getValue();
	    if (v != null) {
	      return Long.parseLong(v);
	    }
	} catch (NumberFormatException e) {
	}

	return dft;
    }

    /** Get the <CODE>double</CODE> value associated with the given key.
      * @param key The header key.
      * @param dft The default value to return if the key cannot be found.
      * @return the associated value.
      */
    public double  getDoubleValue(String key, double dft) {

	HeaderCard fcard = findCard(key);
	if (fcard == null) {
	  return dft;
	}

	try {
	    String v = fcard.getValue();
	    if (v != null) {
	      return new Double(v).doubleValue();
	    }
	} catch (NumberFormatException e) {
	}

	return dft;
    }

    /** Get the <CODE>boolean</CODE> value associated with the given key.
      * @param key The header key.
      * @param dft The value to be returned if the key cannot be found
      *            or if the parameter does not seem to be a boolean.
      * @return the associated value.
      */
    public boolean getBooleanValue(String key, boolean dft) {

	HeaderCard fcard = findCard(key);
	if (fcard == null) {
	  return dft;
	}

	String val = fcard.getValue();
	if (val == null) {
	  return dft;
	}

	if (val.equals("T")) {
	    return true;
	} else if (val.equals("F")) {
	    return false;
	} else {
          return dft;
      }
    }

    /** Get the <CODE>long</CODE> value associated with the given key.
      * @param key The header key.
      * @return The associated value or 0 if not found.
      */
    public long getLongValue(String key) {
	return getLongValue(key, 0L);
    }

    /** Get the <CODE>double</CODE> value associated with the given key.
      * @param key The header key.
      * @return The associated value or 0.0 if not found.
      */
    public double getDoubleValue(String key) {
	return getDoubleValue(key, 0.);
    }

    /** Get the <CODE>boolean</CODE> value associated with the given key.
      * @param The header key.
      * @return The value found, or false if not found or if the
      *         keyword is not a logical keyword.
      */
    public boolean getBooleanValue(String key) {
	return getBooleanValue(key, false);
    }

    /** Get the value associated with the key as an int.
      * @param key The header key.
      * @param dft The value to be returned if the key is not found.
      */
    public int getIntValue(String key, int dft) {
        return (int) getLongValue(key, (long) dft);
    }

    /** Get the <CODE>int</CODE> value associated with the given key.
      * @param key The header key.
      * @return The associated value or 0 if not found.
      */
    public int getIntValue(String key) {
        return (int) getLongValue(key);
    }

    /** Get the <CODE>float</CODE> value associated with the given key.
      * @param key The header key.
      * @param dft The value to be returned if the key is not found.
      */
    public float getFloatValue(String key, float dft) {
        return (float) getDoubleValue(key, dft);
    }

    /** Get the <CODE>float</CODE> value associated with the given key.
      * @param key The header key.
      * @return The associated value or 0.0 if not found.
      */
    public float getFloatValue(String key) {
        return (float) getDoubleValue(key);
    }

    /** Get the <CODE>String</CODE> value associated with the given key.
      * @param key The header key.
      * @return The associated value or null if not found or if the value is not a string.
      */
    public String  getStringValue(String key) {

	HeaderCard fcard = findCard(key);
	if (fcard == null || !fcard.isStringValue()) {
	  return null;
	}

	return fcard.getValue();
    }

    /** Add a card image to the header after the mark if set.
      * @param fcard The card to be added.
      */
    protected void addLine(HeaderCard fcard) {

        if (fcard != null) {

            if (markSet() && getMark() < cards.size()-1) {

                cards.insertElementAt(fcard, getMark()+1);
                setMark(getMark() + 1);

            } else {
                cards.addElement(fcard);
            }
        }
    }


    /** Add a card image to the header after the mark if set.
      * @param card The card to be added.
      * @exception HeaderCardException If the card is not valid.
      */
    protected void addLine(String card)
	throws HeaderCardException
    {
      addLine(new HeaderCard(card));
    }

    /** Create a header by reading the information from the input stream.
      * @param dis The input stream to read the data from.
      * @return <CODE>null</CODE> if there was a problem with the header;
      *		otherwise return the header read from the input stream.
      */
    public static Header readHeader(BufferedDataInputStream dis)
	throws TruncatedFileException, IOException
    {
	Header myHeader = new Header();
        try {
            myHeader.read(dis);
        } catch (EOFException e) {
            // An EOF exception is thrown only if the EOF was detected
            // when reading the first card.  In this case we want
            // to return a null.
            return null;
        }
        return myHeader;
    }

    /** Read a stream for header data.
      * @param dis The input stream to read the data from.
      * @return <CODE>null</CODE> if there was a problem with the header;
      *		otherwise return the header read from the input stream.
      */

    public void read(BufferedDataInputStream dis)
	throws TruncatedFileException, IOException
    {
	byte[] buffer = new byte[80];

	boolean firstCard = true;
	while (true) {

	  int len;
          int need=80;
          try {
              while (need > 0) {
                  len = dis.read(buffer, 80-need, need);
                  if (len == 0) {
                    throw new TruncatedFileException();
                  }
                  need -= len;
              }
	  } catch (EOFException e) {
              // Rethrow the EOF if we're at the beginning of the header,
              // otherwise we have a FITS error.
	      if (firstCard) {
		  throw e;
	      }
	      throw new TruncatedFileException(e.getMessage());
	  }

	    HeaderCard fcard = new HeaderCard(new String(buffer));
	    if (firstCard) {
	      String key = fcard.getKey();
	      if (key == null ||
		  (!key.equals("SIMPLE") && !key.equals("XTENSION")))
	      {
		throw new IOException("Not a FITS file");
	      }
	    }

	    // save card
	    addLine(fcard);
	    if (!fcard.isKeyValuePair()) {
	      String endKey = fcard.getKey();
	      if (endKey != null && endKey.equals("END") ){
		break;
	      }
	    }

	    // we're past the first card now
	    firstCard = false;
	  }

      // Read to the end of the current FITS block.
	int blanks = 36 - cards.size() % 36;
	if (blanks != 36) {
	    while (blanks>0) {
	        int len;
              int need=80;
              try {
                  while (need > 0) {
                      len = dis.read(buffer, 80-need, need);
                      if (len == 0) {
			  throw new TruncatedFileException();
                      }
                      need -= len;
                  }
	        } catch (EOFException e) {
		    throw new TruncatedFileException(e.getMessage());
	        }
              blanks -= 1;
          }
      }
    }

    /** Find the card associated with a given key.
      * If found this sets the mark to the card, otherwise it
      * unsets the mark.
      * @param key The header key.
      * @return <CODE>null</CODE> if the keyword could not be found;
      *		return the HeaderCard object otherwise.
      */
    protected HeaderCard findCard(String key) {

      HeaderCard card = cards.findKey(key);
      if (card == null) {
	unsetMark();
	return null;
      }

      int newMark = cards.indexOf(card);
      setMark(newMark);
      return card;
    }

    /** Find the card associated with a given key.
      * If found this sets the mark to the card, otherwise it
      * unsets the mark.
      * @param key The header key.
      * @return <CODE>null</CODE> if the keyword could not be found;
      *		return the card image otherwise.
      */
    public String findKey(String key) {
      HeaderCard card = findCard(key);
      if (card == null) {
	return null;
      }

      return card.toString();
    }

    /** Replace the key with a new key.  Typically this is used
      * when deleting or inserting columns so that TFORMx -> TFORMx-1
      * @param oldKey The old header keyword.
      * @param newKey the new header keyword.
      * @return <CODE>true</CODE> if the card was replaced.
      * @exception HeaderCardException If <CODE>newKey</CODE> is not a
      *            valid FITS keyword.
      */
    boolean replaceKey(String oldKey, String newKey)
	throws HeaderCardException
    {

        HeaderCard oldCard = findCard(oldKey);
        if (oldCard == null) {
            return false;
        }

        String v = oldCard.getValue();
        if (v != null && oldCard.isStringValue()) {
          v = "'" + v + "'";
        }

        String c = oldCard.getComment();

        HeaderCard newCard = new HeaderCard(newKey, v, c);
        cards.setElementAt(newCard,getMark());

        return true;
    }

    /** Write the current header (including any needed padding) to the
      * output stream.
      * @param dos The output stream to which the data is to be written.
      * @exception FitsException if the header could not be written.
      */
    public void write (BufferedDataOutputStream dos) throws FitsException {

      checkEnd();
      if (cards.size() <= 0) {
          return;
      }

      String[] header = new String[cards.size()];
      for (int i = 0; i < cards.size(); i++) {
	header[i] = ((HeaderCard )cards.elementAt(i)).toString();
      }

      try {
          dos.writePrimitiveArray(header);

	    int pad = 36 - cards.size()%36;
	    if (pad != 36) {
                String blankBuffer =
"                                                                                ";
	        for (int i=0; i<pad; i += 1) {
		      dos.writeBytes(blankBuffer);
              }
	    }
      } catch (IOException e) {
          throw new FitsException("IO Error writing header: " + e);
      }

    }

    /** Add or replace a key with the given boolean value and comment.
      * @param key     The header key.
      * @param val     The boolean value.
      * @param comment A comment to append to the card.
      * @exception HeaderCardException If the parameters cannot build a
      *            valid FITS card.
      */
    public void addBooleanValue(String key, boolean val, String comment)
	throws HeaderCardException
    {
        String tf;
        if (val) {
            tf = "T";
        } else {
            tf = "F";
        }
        replaceCard(key,tf,comment);
    }

    /** Add or replace a key with the given float value and comment.
      * @param key     The header key.
      * @param val     The float value.
      * @param comment A comment to append to the card.
      * @exception HeaderCardException If the parameters cannot build a
      *            valid FITS card.
      */
    public void addFloatValue(String key, float val, String comment)
	throws HeaderCardException
    {
        String sval = ""+val;
        replaceCard(key,sval,comment);
    }

    /** Add or replace a key with the given double value and comment.
      * @param key     The header key.
      * @param val     The double value.
      * @param comment A comment to append to the card.
      * @exception HeaderCardException If the parameters cannot build a
      *            valid FITS card.
      */
    public void addDoubleValue(String key, double val, String comment)
	throws HeaderCardException
    {
        String sval = ""+val;
        replaceCard(key, sval, comment);
    }

    /** Add or replace a key with the given string value and comment.
      * @param key     The header key.
      * @param val     The string value.
      * @param comment A comment to append to the card.
      * @exception HeaderCardException If the parameters cannot build a
      *            valid FITS card.
      */

    public void addStringValue(String key, String val, String comment)
	throws HeaderCardException
    {
        if (val == null) {
             val = "";
        }
        if (val.length() < 8) {
             val = (val+"        ").substring(0,8);
        } else if (val.length() > 67) {
             val = val.substring(0,67);
        }
        val = "'"+val+"'";
        replaceCard(key, val, comment);
    }

    /** Add or replace a key using the preformatted value.  If the
      * key is not found, then add the card after the current mark or at
      * the end if the mark is not set.
      * @param key     The header key.
      * @param val     The string which will follow the "= " on the
      *                card.  This routine is called by the various
      *                addXXXValue routines after they have formatted the
      *                value as a string.
      * @param comment A comment to append to the card.
      * @exception HeaderCardException If the parameters cannot build a
      *            valid FITS card.
      */
    public void replaceCard(String key, String val, String comment)
	throws HeaderCardException
    {

	HeaderCard fcard = new HeaderCard(key, val, comment);

        int oldMark = getMark();
        findCard(key);

        if (markSet() ) {
            cards.setElementAt(fcard, getMark());
        } else {
            setMark(oldMark);
            insertCard(fcard);
        }
    }

    /** Add or replace a key with the given int value and comment.
      * @param key     The header key.
      * @param val     The int value.
      * @param comment A comment to append to the card.
      * @exception HeaderCardException If the parameters cannot build a
      *            valid FITS card.
      */
    public void addIntValue(String key, int val, String comment)
	throws HeaderCardException
    {
         addLongValue(key, (long) val, comment);
    }

    /** Add or replace a key with the given long value and comment.
      * @param key     The header key.
      * @param val     The long value.
      * @param comment A comment to append to the card.
      * @exception HeaderCardException If the parameters cannot build a
      *            valid FITS card.
      */
    public void addLongValue(String key, long val, String comment)
	throws HeaderCardException
    {
         String sval = ""+val;
         replaceCard(key, sval, comment);
    }

    /** insert the given card either at the current mark or at the end of
      * the header.
      * @param fcard the card to insert.
      */
    private void insertCard(HeaderCard fcard) {
         if (markSet()  && getMark() < cards.size()-1) {
             cards.insertElementAt(fcard, getMark()+1);
             mark += 1;
         } else {
             cards.addElement(fcard);
             unsetMark();
         }
     }

    /** Format the key, value and comment fields for the FITS data.
      * @param key The header keyword.
      * @param val The value associated with the key expressed as a string.
      * @param comment A comment to put on the field.
      * @exception HeaderCardException If the parameters cannot build a
      *            valid FITS card.
      */
    public static String formatFields(String key, String val, String comment)
	throws HeaderCardException
    {
      return new HeaderCard(key,val,comment).toString();
    }

    /** Insert or add a card to the header.  Insert after the mark
      * if set, or at the end of the header if not set.
      * @param card   The card to be inserted.
      */
    public void insertCard(String card) {
         insertCard(new HeaderCard(card));
    }

    /** Add a line to the header using the COMMENT style, i.e., no '='
      * in column 9.
      * @param header The comment style header.
      * @param value  A string to follow the header.
      * @exception HeaderCardException If the parameters cannot build a
      *            valid FITS card.
      */
    public void insertCommentStyle(String header, String value)
	throws HeaderCardException
    {
         insertCard(new HeaderCard(header, null, value));
    }

    /** Add a COMMENT line.
      * @param value The comment.
      * @exception HeaderCardException If the parameter is not a
      *            valid FITS comment.
      */

    public void insertComment(String value)
	throws HeaderCardException
    {
         insertCommentStyle("COMMENT", value);
    }

    /** Add a HISTORY line.
      * @param value The history record.
      * @exception HeaderCardException If the parameter is not a
      *            valid FITS comment.
      */
    public void insertHistory(String value)
	throws HeaderCardException
    {
         insertCommentStyle("HISTORY", value);
    }

    /** Is the mark set?
      * @return <CODE>true</CODE> if the mark is set.
      */
    public boolean markSet() {
        return mark >= -1;
    }

    /** Get the current mark.
      * A value of -2 indicates that the mark is not set and new cards are
      * appended to the end of the header.
      * A value of -1 indicates that the mark is set such that the next card
      * should be inserted at the beginning of the header.  Otherwise cards
      * should be inserted after the mark.  The mark is typically set to
      * a card whenever an operation (i.e., insert or modify) is made on
      * the card.  Thus a series of inserts will result in cards
      * a sequential series of cards in the same order.
      * @return the current mark.
      */
    public int getMark() {
        return mark;
    }

    /** Set the mark to the given value.
      * @param The index of the card the mark is to be set to.
      */
    public void setMark(int newMark) {
        mark = newMark;
    }

    /** Unset the mark.  Inserts should now be done as appends
      * to the end of the header.
      */
    public void unsetMark() {
        mark = -2;
    }

    /** Delete the card associated with the given key.
      * Nothing occurs if the key is not found, though
      * this will unset the mark.  The mark is left pointing
      * at the following card if successful (or unset if this
      * was the last card).
      *
      * @param key The header key.
      */
    public void deleteKey(String key) {

        findCard(key);

        if (markSet()) {
            cards.removeElementAt(getMark());
        }

        // After a delete we want to point to the card after the deleted cards
        // so that an immediately following insert will replace the original
        // card.  This means just leave the mark alone except that we have
        // to check if we just deleted the last card.

        if (getMark() >= cards.size() ) {
            unsetMark();
        }
    }

    /** Remove the card at the given index.  The mark is left pointing
      * at the next card or unset if this was the last card.
      *
      * @param i The index of the card to be removed.
      */
    public void removeCardAt(int i) {
         if (i < cards.size() && i >= 0) {
             cards.removeElementAt(i);
         }
         if (i < cards.size()) {
             setMark(i);
         } else {
             unsetMark();
         }
    }

    /** Tests if the specified keyword is present in this table.
      * @param key the keyword to be found.
      * @return <CODE>true<CODE> if the specified keyword is present in this
      *		table; <CODE>false<CODE> otherwise.
      */
    public final boolean containsKey(String key)
    {
	return cards.containsKey(key);
    }

    /** Create keywords such that this Header describes the given
      * data.
      * @param o  The data object to be described.
      * @exception FitsException if the data was not valid for this header.
      */
    public void pointToData(Data o) throws FitsException {

        if (o instanceof ImageData) {
            pointToImage(o.getData());
        } else {
            throw new FitsException("Cannot point to class:"+o.getClass().getName());
        }
    }

    /** Create keywords such that this header describes the given
      * image data.
      * @param o The image to be described.
      * @exception FitsException if the object does not contain
      *		valid image data.
      */
    protected void pointToImage(Object o) throws FitsException {

        if (o == null) {
            nullImage();
        }

        String classname = o.getClass().getName();

        int[] dimens = ArrayFuncs.getDimensions(o);
        if (dimens == null || dimens.length == 0) {
            throw new FitsException("Image data object not array");
        }

        int bitpix;
        switch (classname.charAt(dimens.length)) {
          case 'B':
            bitpix = 8;
            break;
          case 'S':
            bitpix = 16;
            break;
          case 'I':
            bitpix = 32;
            break;
          case 'J':
            bitpix = 64;
            break;
          case 'F':
            bitpix = -32;
            break;
          case 'D':
            bitpix = -64;
            break;
          default:
            throw new FitsException("Invalid Object Type for FITS data");
        }

	// if this is neither a primary header nor an image extension,
	//  make it a primary header
        if (!getBooleanValue("SIMPLE")) {
            String str = getStringValue("XTENSION");
            if (str == null || !str.equals("IMAGE") || getMark() != 0) {
                setSimple(true);
            }
        }

        setBitpix(bitpix);
        setNaxes(dimens.length);

        for (int i=1; i<=dimens.length; i += 1) {
            if (dimens[i-1] == -1) {
                throw new FitsException("Unfilled array for dimension: "+i);
            }
            setNaxis(i, dimens[i-1]);
        }
        setPcount(0);
        setGcount(1);
        setExtend(true);
    }


    /** Create a header for a null image.
      */
    void nullImage() {

        setSimple(true);
        setBitpix(8);
        setNaxes(0);
        setPcount(0);
        setGcount(0);
        setExtend(true);

        // Get rid of any NAXIS junk that's around.
        for (int i=1; i<9; i += 1) {
            deleteKey("NAXIS"+i);
        }
    }

    /** Set the SIMPLE keyword to the given value.
      * @param val The boolean value -- Should be true for FITS data.
      */
    void setSimple(boolean val) {
        deleteKey("SIMPLE");
        deleteKey("XTENSION");
        if (cards.size() >= 0) {
            setMark(-1);
        }
        try {
	  addBooleanValue("SIMPLE", val, "Java FITS: " + new Date());
	} catch (HeaderCardException e) {
	  throw new RuntimeException("Impossible error: " + e.getMessage());
	}
    }

    /** Set the XTENSION keyword to the given value.
      * @param val The name of the extension. "IMAGE" and "BINTABLE" are supported.
      */
    void setXtension(String val) {
        deleteKey("SIMPLE");
        deleteKey("XTENSION");
        if (cards.size() >= 0) {
            setMark(-1);
        }
        try {
	  addStringValue("XTENSION", val, "Java FITS: " + new Date());
	} catch (HeaderCardException e) {
	  throw new RuntimeException("Impossible error: " + e.getMessage());
	}
    }

    /** Set the BITPIX value for the header.
      * @param val.  The following values are permitted by FITS conventions:
      * <ul>
      * <li> 8  -- signed bytes data.  Also used for tables.
      * <li> 16 -- signed short data.
      * <li> 32 -- signed int data.
      * <li> -32 -- IEEE 32 bit floating point numbers.
      * <li> -64 -- IEEE 64 bit floating point numbers.
      * </ul>
      * These Fits classes also support BITPIX=64 in which case data
      * is signed 64 bit long data.
      */
    void setBitpix(int val) {
        if (cards.size() > 1) {
           setMark(0);
        }
        try {
	  addIntValue("BITPIX", val, null);
	} catch (HeaderCardException e) {
	  throw new RuntimeException("Impossible error: " + e.getMessage());
	}
    }

    /** Set the value of the NAXIS keyword
      * @param val The dimensionality of the data.
      */
    void setNaxes(int val) {
        if (cards.size() > 2) {
           setMark(1);
        }
        try {
	  addIntValue("NAXIS", val, "Dimensionality");
	} catch (HeaderCardException e) {
	  throw new RuntimeException("Impossible error: " + e.getMessage());
	}
    }

    /** Ensure that the header has exactly one END keyword in
      * the appropriate location.
      */
    void checkEnd() {

        // Get rid of any END keywords that are not at the end
        // of the header.
	HeaderCard blankCard = null;
        for (int i=0; i<cards.size(); i += 1) {
            HeaderCard card = (HeaderCard) cards.elementAt(i);
	    try {
	      if (!card.isKeyValuePair() && card.getKey().equals("END")) {
                if (i == cards.size() - 1) {
                    return;
                } else {
                    if (blankCard == null) {
		      blankCard = new HeaderCard(null, null, null);
		    }
		    cards.setElementAt(blankCard, i);
                }
	      }
	    } catch (HeaderCardException e) {
            }
        }
        unsetMark();
        try {
	  addLine("END");
	} catch (HeaderCardException e) {
	  throw new RuntimeException("Impossible error: " + e.getMessage());
	}
    }

    /** Set the NAXISn keywords.
      * @param dim The dimension being set.
      * @param val The length along that dimension.
      */
    void setNaxis(int dim, int val) {
        if (dim <= 0) {
            return;
        }
        if (cards.size() > 2+dim) {
            setMark(1+dim);
        }
        try {
	  addIntValue("NAXIS"+dim, val, null);
	} catch (HeaderCardException e) {
	  throw new RuntimeException("Impossible error: " + e.getMessage());
	}
    }

    /** Set the group count (GCOUNT) keyword.
      * This should be 1 except for the unsupported random-groups data.
      * @param val the number of groups in the data.
      */
    void setGcount(int val) {
        try {
	  addIntValue("GCOUNT", val, "Number of Groups");
	} catch (HeaderCardException e) {
	  throw new RuntimeException("Impossible error: " + e.getMessage());
	}
    }

    /** Set the Parameter count (PCOUNT) keyword.
      * This is normally 0 except when random-groups data is used or when
      * the variable length columns convention is used with binary tables.
      * @param val The number of parameters.
      */
    void setPcount(int val) {
        try {
	  addIntValue("PCOUNT", val, "Group params/Variable cols buffer");
	} catch (HeaderCardException e) {
	  throw new RuntimeException("Impossible error: " + e.getMessage());
	}
    }

    /** Set the EXTEND keyword.  This is only placed in the
      * primary header and should always be <CODE>true</CODE>.
      * Just in case we do provide for setting it to <CODE>false</CODE>.
      * @param val the value assigned to the EXTEND keyword.
      */
    void setExtend(boolean val) {
        try {
	  addBooleanValue("EXTEND", val, "Can there be extensions?");
	} catch (HeaderCardException e) {
	  throw new RuntimeException("Impossible error: " + e.getMessage());
	}
    }

    /** See if the current header is an array and if so turn
      * it into an IMAGE extension.
      *
      * @return whether the transformation could be done.
      */
    protected boolean primaryToImage() {

        if (getBooleanValue("SIMPLE") && getMark() == 0) {
            setXtension("IMAGE");
            return true;
        }

        String str = getStringValue("XTENSION");
        if (str == null) {
            setXtension("IMAGE");
            return true;
        }

        if (str.equals("IMAGE")) {
            return true;
        }

        return false;
    }

    /** See if the current header is for an an array and if so
      * turn it into a primary array.
      *
      * @return whether the transformation could be done.
      */

    protected boolean imageToPrimary() {
        if (getBooleanValue("SIMPLE") && getMark() == 0) {
            return true;
        }

        String str = getStringValue("XTENSION");
        if (str == null) {
            setSimple(true);
            return true;
        }

        if (str.equals("IMAGE") && getMark() == 0) {
            setSimple(true);
            return true;
        }

        return false;
    }

    /** Dump the header to a given stream.
      * @param ps the stream to which the card images are dumped.
      */
    protected void dumpHeader(PrintStream ps) {
        for (int i=0; i<cards.size(); i += 1) {
            ps.println(cards.elementAt(i));
        }
    }
}
