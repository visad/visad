/*
 *  gnu/regexp/RE.java
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
import java.util.Vector;

class IntPair {
  public int first, second;
}

class CharUnit {
  public char ch;
  public boolean bk;
}

/**
 * RE provides the user interface for compiling and matching regular
 * expressions.
 * <P>
 * A regular expression object (class RE) is compiled by constructing it
 * from a String, StringBuffer or character array, with optional 
 * compilation flags (below)
 * and an optional syntax specification (see RESyntax; if not specified,
 * <code>RESyntax.RE_SYNTAX_PERL5</code> is used).
 * <P>
 * Various methods attempt to match input text against a compiled
 * regular expression.  These methods are:
 * <LI><code>isMatch</code>: returns true if the input text in its entirety
 * matches the regular expression pattern.
 * <LI><code>getMatch</code>: returns the first match found in the input text,
 * or null if no match is found.
 * <LI><code>getAllMatches</code>: returns an array of all non-overlapping 
 * matches found in the input text.  If no matches are found, the array is
 * zero-length.
 * <LI><code>substitute</code>: substitute the first occurence of the pattern
 * in the input text with a replacement string (which may include
 * metacharacters $0-$9, see REMatch.substituteInto).
 * <LI><code>substituteAll</code>: same as above, but repeat for each match
 * before returning.
 * <LI><code>getMatchEnumeration</code>: returns an REMatchEnumeration object
 * that allows iteration over the matches (see REMatchEnumeration for some
 * reasons why you may want to do this instead of using <code>getAllMatches</code>.
 * <P>
 * These methods all have similar argument lists.  The input can be a
 * String, a character array, a StringBuffer or an InputStream of some sort.
 * Note that
 * when using an InputStream, the stream read position cannot be guaranteed
 * after attempting a match (this is not a bug, but a consequence of the way
 * regular expressions work).  Using an REMatchEnumeration can eliminate most
 * positioning problems.
 * <P>
 * The optional index argument specifies the offset from the beginning of the
 * text at which the search should start (see the descriptions of some of
 * the execution flags for how this can affect positional pattern operators).
 * For an InputStream, this means an offset from the current read position,
 * so subsequent calls with the same index argument on an InputStream will not
 * necessarily be accessing the same position on the stream, whereas repeated
 * searches at a given index in a fixed string will return consistent
 * results.
 * <P>
 * You can optionally affect the execution environment by using a
 * combination of execution flags (constants listed below).
 *
 * @author <A HREF="mailto:wes@cacas.org">Wes Biggs</A>
 * @version 1.0.8, 21 March 1999
 */

public class RE extends REToken {
  // This String will be returned by getVersion()
  private static final String s_version = "1.0.8";

  // These are, respectively, the first and last tokens in our linked list
  // If there is only one token, firstToken == lastToken
  private REToken firstToken, lastToken;

  // This is the number of subexpressions in this regular expression,
  // with a minimum value of zero.  Returned by getNumSubs()
  private int m_numSubs;

  /**
   * Compilation flag. Do  not  differentiate  case.   Subsequent
   * searches  using  this  RE will be case insensitive.
   */
  public static final int REG_ICASE = 2;

  /**
   * Compilation flag. The match-any-character operator (dot)
   * will match a newline character.  When set this overrides the syntax
   * bit RE_DOT_NEWLINE (see RESyntax for details).  This is equivalent to
   * the "/s" operator in Perl.
   */
  public static final int REG_DOT_NEWLINE = 4;

  /**
   * Compilation flag. Use multiline mode.  In this mode, the ^ and $
   * anchors will match based on newlines within the input. This is
   * equivalent to the "/m" operator in Perl.
   */
  public static final int REG_MULTILINE = 8;

  /**
   * Execution flag.
   * The match-beginning operator (^) will not match at the beginning
   * of the input string. Useful for matching on a substring when you
   * know the context of the input is such that position zero of the
   * input to the match test is not actually position zero of the text.
   * <P>
   * This example demonstrates the results of various ways of matching on
   * a substring.
   * <P>
   * <CODE>
   * String s = "food bar fool";<BR>
   * RE exp = new RE("^foo.");<BR>
   * REMatch m0 = exp.getMatch(s);<BR>
   * REMatch m1 = exp.getMatch(s.substring(8));<BR>
   * REMatch m2 = exp.getMatch(s.substring(8),0,RE.REG_NOTBOL); <BR>
   * REMatch m3 = exp.getMatch(s,8);                            <BR>
   * REMatch m4 = exp.getMatch(s,8,RE.REG_ANCHORINDEX);         <BR>
   * <P>
   * // Results:<BR>
   * //  m0 = "food"<BR>
   * //  m1 = "fool"<BR>
   * //  m2 = null<BR>
   * //  m3 = null<BR>
   * //  m4 = "fool"<BR>
   * </CODE>
   */
  public static final int REG_NOTBOL = 16;

  /**
   * Execution flag.
   * The match-end operator ($) does not match at the end
   * of the input string. Useful for matching on substrings.
   */
  public static final int REG_NOTEOL = 32;

  /**
   * Execution flag.
   * The match-beginning operator (^) matches not at position 0
   * in the input string, but at the position the search started at
   * (based on the index input given to the getMatch function).  See
   * the example under REG_NOTBOL.
   */
  public static final int REG_ANCHORINDEX = 64;

  /** Returns a string representing the version of the gnu.regexp package. */
  public static final String version() {
    return s_version;
  }

  /**
   * Constructs a regular expression pattern buffer without any compilation
   * flags set, and using the default syntax (RESyntax.RE_SYNTAX_PERL5).
   *
   * @param pattern A regular expression pattern, in the form of a String,
   *   StringBuffer or char[].
   * @exception REException The input pattern could not be parsed.
   * @exception IllegalArgumentException The pattern was not a String, 
   *   StringBuffer or char[].
   * @exception NullPointerException The pattern was null.
   */
  public RE(Object pattern) throws REException {
    this(pattern,0,RESyntax.RE_SYNTAX_PERL5,0,0);
  }

  /**
   * Constructs a regular expression pattern buffer using the specified
   * compilation flags and the default syntax (RESyntax.RE_SYNTAX_PERL5).
   *
   * @param pattern A regular expression pattern, in the form of a String,
   *   StringBuffer, or char[].
   * @param cflags The logical OR of any combination of the compilation flags listed above.
   * @exception REException The input pattern could not be parsed.
   * @exception IllegalArgumentException The pattern was not a String, 
   *   StringBuffer or char[].
   * @exception NullPointerException The pattern was null.
   */
  public RE(Object pattern, int cflags) throws REException {
    this(pattern,cflags,RESyntax.RE_SYNTAX_PERL5,0,0);
  }

  /**
   * Constructs a regular expression pattern buffer using the specified
   * compilation flags and regular expression syntax.
   *
   * @param pattern A regular expression pattern, in the form of a String,
   *   StringBuffer, or char[].
   * @param cflags The logical OR of any combination of the compilation flags listed above.
   * @param syntax The type of regular expression syntax to use.
   * @exception REException The input pattern could not be parsed.
   * @exception IllegalArgumentException The pattern was not a String, 
   *   StringBuffer or char[].
   * @exception NullPointerException The pattern was null.
   */
  public RE(Object pattern, int cflags, RESyntax syntax) throws REException {
    this(pattern,cflags,syntax,0,0);
  }

  // internal constructor used for alternation
  private RE(REToken f_first, REToken f_last,int f_subs, int f_subIndex) {
    super(f_subIndex); // ???
    firstToken = f_first;
    lastToken = f_last;
    m_numSubs = f_subs;
  }

  // Actual constructor implementation
  private RE(Object patternObj, int cflags, RESyntax syntax, int myIndex, int nextSub) throws REException {
    super(myIndex); // Subexpression index of this token.
    char[] pattern;
    if (patternObj instanceof String) {
      pattern = ((String) patternObj).toCharArray();
    } else if (patternObj instanceof char[]) {
      pattern = (char[]) patternObj;
    } else if (patternObj instanceof StringBuffer) {
      pattern = new char [((StringBuffer) patternObj).length()];
      ((StringBuffer) patternObj).getChars(0,pattern.length,pattern,0);
    } else throw new IllegalArgumentException("Invalid class for pattern");

    int pLength = pattern.length;

    m_numSubs = 0; // Number of subexpressions in this token.
    Vector branches = null;

    // linked list of tokens (sort of -- some closed loops can exist)
    firstToken = lastToken = null;

    // Precalculate these so we don't pay for the math every time we
    // need to access them.
    boolean insens = ((cflags & REG_ICASE) > 0);

    // Parse pattern into tokens.  Does anyone know if it's more efficient
    // to use char[] than a String.charAt()?  I'm assuming so.

    // index tracks the position in the char array
    int index = 0;

    // this will be the current parse character (pattern[index])
    CharUnit unit = new CharUnit();

    // This is used for {x,y} calculations
    IntPair minMax = new IntPair();

    // Buffer a token so we can create a TokenRepeated, etc.
    REToken currentToken = null;
    char ch;

    while (index < pLength) {
      // read the next character unit (including backslash escapes)
      index = getCharUnit(pattern,index,unit);

      // ALTERNATION OPERATOR
      //  \| or | (if RE_NO_BK_VBAR) or newline (if RE_NEWLINE_ALT)
      //  not available if RE_LIMITED_OPS is set

      // TODO: the '\n' literal here should be a test against REToken.newline,
      // which unfortunately may be more than a single character.
      if ( ( (unit.ch == '|' && (syntax.get(RESyntax.RE_NO_BK_VBAR) ^ unit.bk))
	     || (syntax.get(RESyntax.RE_NEWLINE_ALT) && (unit.ch == '\n') && !unit.bk) )
	   && !syntax.get(RESyntax.RE_LIMITED_OPS)) {
	// make everything up to here be a branch. create vector if nec.
	if (branches == null) branches = new Vector();
	addToken(currentToken);
	branches.addElement(new RE(firstToken,lastToken,m_numSubs,m_subIndex));
	firstToken = lastToken = currentToken = null;
      }
      
      // INTERVAL OPERATOR:
      //  {x} | {x,} | {x,y}  (RE_INTERVALS && RE_NO_BK_BRACES)
      //  \{x\} | \{x,\} | \{x,y\} (RE_INTERVALS && !RE_NO_BK_BRACES)
      //
      // OPEN QUESTION: 
      //  what is proper interpretation of '{' at start of string?

      else if ((unit.ch == '{') && syntax.get(RESyntax.RE_INTERVALS) && (syntax.get(RESyntax.RE_NO_BK_BRACES) ^ unit.bk)) {
	if (currentToken == null) throw new REException("{ without preceding token",REException.REG_EBRACE,index);
	  
	index = getMinMax(pattern,index,minMax,syntax);
	if ((currentToken.getMinimumLength() == 0) && (minMax.second == Integer.MAX_VALUE))
	  throw new REException("repeated argument may be empty",REException.REG_BADRPT,index);
	currentToken = setRepeated(currentToken,minMax.first,minMax.second,index);  
      }
      
      // LIST OPERATOR:
      //  [...] | [^...]

      else if ((unit.ch == '[') && !unit.bk) {
	Vector options = new Vector();
	boolean negative = false;
	char lastChar = 0;
	if (index == pLength) throw new REException("unmatched [",REException.REG_EBRACK,index);
	
	// Check for initial caret, negation
	if ((ch = pattern[index]) == '^') {
	  negative = true;
	  if (++index == pLength) throw new REException("no end of list",REException.REG_EBRACK,index);
	  ch = pattern[index];
	}

	// Check for leading right bracket literal
	if (ch == ']') {
	  lastChar = ch;
	  if (++index == pLength) throw new REException("no end of list",REException.REG_EBRACK,index);
	}

	while ((ch = pattern[index++]) != ']') {
	  if ((ch == '-') && (lastChar != 0)) {
	    if (index == pLength) throw new REException("no end of list",REException.REG_EBRACK,index);
	    if ((ch = pattern[index]) == ']') {
	      options.addElement(new RETokenChar(m_subIndex,lastChar,insens));
	      lastChar = '-';
	    } else {
	      options.addElement(new RETokenRange(m_subIndex,lastChar,ch,insens));
	      lastChar = 0;
	      index++;
	    }
          } else if ((ch == '\\') && syntax.get(RESyntax.RE_BACKSLASH_ESCAPE_IN_LISTS)) {
            if (index == pLength) throw new REException("no end of list",REException.REG_EBRACK,index);
	    int posixID = -1;
	    boolean negate = false;
	    if (syntax.get(RESyntax.RE_CHAR_CLASS_ESC_IN_LISTS)) {
	      switch (pattern[index]) {
	      case 'D':
		negate = true;
	      case 'd':
		posixID = RETokenPOSIX.DIGIT;
		break;
	      case 'S':
		negate = true;
	      case 's':
		posixID = RETokenPOSIX.SPACE;
		break;
	      case 'W':
		negate = true;
	      case 'w':
		posixID = RETokenPOSIX.ALNUM;
		break;
	      }
	    }
	    if (lastChar != 0) options.addElement(new RETokenChar(m_subIndex,lastChar,insens));
	    
	    if (posixID != -1) {
	      options.addElement(new RETokenPOSIX(m_subIndex,posixID,insens,negate));
	    } else {
	      lastChar = pattern[index];
	    }
	    ++index;
	  } else if ((ch == '[') && (syntax.get(RESyntax.RE_CHAR_CLASSES)) && (pattern[index] == ':')) {
	    StringBuffer posixSet = new StringBuffer();
	    index = getPosixSet(pattern,index+1,posixSet);
	    int posixId = RETokenPOSIX.intValue(posixSet.toString());
	    if (posixId != -1)
	      options.addElement(new RETokenPOSIX(m_subIndex,posixId,insens,false));
	  } else {
	    if (lastChar != 0) options.addElement(new RETokenChar(m_subIndex,lastChar,insens));
	    lastChar = ch;
	  }
	  if (index == pLength) throw new REException("no end of list",REException.REG_EBRACK,index);
	} // while in list
	// Out of list, index is one past ']'
	    
	if (lastChar != 0) options.addElement(new RETokenChar(m_subIndex,lastChar,insens));
	    
	// Create a new RETokenOneOf
	addToken(currentToken);
	options.trimToSize();
	currentToken = new RETokenOneOf(m_subIndex,options,negative);
      }

      // SUBEXPRESSIONS
      //  (...) | \(...\) depending on RE_NO_BK_PARENS

      else if ((unit.ch == '(') && (syntax.get(RESyntax.RE_NO_BK_PARENS) ^ unit.bk)) {
	boolean pure = false;
	boolean comment = false;
	if ((index+1 < pLength) && (pattern[index] == '?')) {
	  switch (pattern[index+1]) {
	  case ':':
	    if (syntax.get(RESyntax.RE_PURE_GROUPING)) {
	      pure = true;
	      index += 2;
	    }
	    break;
	  case '#':
	    if (syntax.get(RESyntax.RE_COMMENTS)) {
	      comment = true;
	    }
	    break;
	  }
	}

	// find end of subexpression
	int endIndex = index;
	int nextIndex = index;
	int nested = 0;

	while ( ((nextIndex = getCharUnit(pattern,endIndex,unit)) > 0)
		&& !(nested == 0 && (unit.ch == ')') && (syntax.get(RESyntax.RE_NO_BK_PARENS) ^ unit.bk)) )
	  if ((endIndex = nextIndex) >= pLength)
	    throw new REException("no end of subexpression",REException.REG_ESUBREG,index-1);
	  else if (unit.ch == '(' && (syntax.get(RESyntax.RE_NO_BK_PARENS) ^ unit.bk))
	    nested++;
	  else if (unit.ch == ')' && (syntax.get(RESyntax.RE_NO_BK_PARENS) ^ unit.bk))
	    nested--;

	// endIndex is now position at a ')','\)' 
	// nextIndex is end of string or position after ')' or '\)'

	if (comment) index = nextIndex;
	else { // not a comment
	  // create RE subexpression as token.
	  addToken(currentToken);
	  if (!pure) {
	    nextSub++;
	    m_numSubs++;
	  }

	  int useIndex = pure ? 0 : nextSub;

	  currentToken = new RE(String.valueOf(pattern,index,endIndex-index).toCharArray(),cflags,syntax,useIndex,nextSub);
	  nextSub += ((RE) currentToken).getNumSubs();
	  m_numSubs += ((RE) currentToken).getNumSubs();
	  index = nextIndex;
	} // not a comment
      } // subexpression
    
      // UNMATCHED RIGHT PAREN
      // ) or \)?  need to implement throw exception if
      // !syntax.get(RESyntax.RE_UNMATCHED_RIGHT_PAREN_ORD)
      else if (!syntax.get(RESyntax.RE_UNMATCHED_RIGHT_PAREN_ORD) && ((unit.ch == ')') && (syntax.get(RESyntax.RE_NO_BK_PARENS) ^ unit.bk))) {
	throw new REException("unmatched right paren",REException.REG_EPAREN,index);
      }

      // START OF LINE OPERATOR
      //  ^

      else if ((unit.ch == '^') && !unit.bk) {
	addToken(currentToken);
	currentToken = null;
	addToken(new RETokenStart(m_subIndex,(cflags & REG_MULTILINE) > 0));
      }

      // END OF LINE OPERATOR
      //  $

      else if ((unit.ch == '$') && !unit.bk) {
	addToken(currentToken);
	currentToken = null;
	addToken(new RETokenEnd(m_subIndex,(cflags & REG_MULTILINE) > 0));
      }

      // MATCH-ANY-CHARACTER OPERATOR (except possibly newline and null)
      //  .

      else if ((unit.ch == '.') && !unit.bk) {
	addToken(currentToken);
	currentToken = new RETokenAny(m_subIndex,syntax.get(RESyntax.RE_DOT_NEWLINE) || ((cflags & REG_DOT_NEWLINE) > 0),syntax.get(RESyntax.RE_DOT_NOT_NULL));
      }

      // ZERO-OR-MORE REPEAT OPERATOR
      //  *

      else if ((unit.ch == '*') && !unit.bk) {
	if ((currentToken == null) || (currentToken.getMinimumLength() == 0))
	  throw new REException("repeated argument may be empty",REException.REG_BADRPT,index);
	currentToken = setRepeated(currentToken,0,Integer.MAX_VALUE,index);
      }

      // ONE-OR-MORE REPEAT OPERATOR
      //  + | \+ depending on RE_BK_PLUS_QM
      //  not available if RE_LIMITED_OPS is set

      else if ((unit.ch == '+') && !syntax.get(RESyntax.RE_LIMITED_OPS) && (!syntax.get(RESyntax.RE_BK_PLUS_QM) ^ unit.bk)) {
	if ((currentToken == null) || (currentToken.getMinimumLength() == 0))
	  throw new REException("repeated argument may be empty",REException.REG_BADRPT,index);
	currentToken = setRepeated(currentToken,1,Integer.MAX_VALUE,index);
      }

      // ZERO-OR-ONE REPEAT OPERATOR / STINGY MATCHING OPERATOR
      //  ? | \? depending on RE_BK_PLUS_QM
      //  not available if RE_LIMITED_OPS is set
      //  stingy matching if RE_STINGY_OPS is set and it follows a quantifier

      else if ((unit.ch == '?') && !syntax.get(RESyntax.RE_LIMITED_OPS) && (!syntax.get(RESyntax.RE_BK_PLUS_QM) ^ unit.bk)) {
	if (currentToken == null) throw new REException("? without preceding token",REException.REG_BADRPT,index);

	// Check for stingy matching on RETokenRepeated
	if ((currentToken instanceof RETokenRepeated) && (syntax.get(RESyntax.RE_STINGY_OPS)))
	  ((RETokenRepeated) currentToken).makeStingy();
	else
	  currentToken = setRepeated(currentToken,0,1,index);
      }
	
      // BACKREFERENCE OPERATOR
      //  \1 \2 \3 \4 ...
      // not available if RE_NO_BK_REFS is set

      else if (unit.bk && Character.isDigit(unit.ch) && !syntax.get(RESyntax.RE_NO_BK_REFS)) {
	addToken(currentToken);
	currentToken = new RETokenBackRef(m_subIndex,Character.digit(unit.ch,10),insens);
      }

      // START OF STRING OPERATOR
      //  \A if RE_STRING_ANCHORS is set
      
      else if (unit.bk && (unit.ch == 'A') && syntax.get(RESyntax.RE_STRING_ANCHORS)) {
	addToken(currentToken);
	currentToken = new RETokenStart(m_subIndex,false);
      }
      
      // DIGIT OPERATOR
      //  \d if RE_CHAR_CLASS_ESCAPES is set
      
      else if (unit.bk && (unit.ch == 'd') && syntax.get(RESyntax.RE_CHAR_CLASS_ESCAPES)) {
	addToken(currentToken);
	currentToken = new RETokenPOSIX(m_subIndex,RETokenPOSIX.DIGIT,insens,false);
      }

      // NON-DIGIT OPERATOR
      //  \D

	else if (unit.bk && (unit.ch == 'D') && syntax.get(RESyntax.RE_CHAR_CLASS_ESCAPES)) {
	  addToken(currentToken);
	  currentToken = new RETokenPOSIX(m_subIndex,RETokenPOSIX.DIGIT,insens,true);
	}

	// NEWLINE ESCAPE
        //  \n

	else if (unit.bk && (unit.ch == 'n')) {
	  addToken(currentToken);
	  currentToken = new RETokenChar(m_subIndex,'\n',false);
	}

	// RETURN ESCAPE
        //  \r

	else if (unit.bk && (unit.ch == 'r')) {
	  addToken(currentToken);
	  currentToken = new RETokenChar(m_subIndex,'\r',false);
	}

	// WHITESPACE OPERATOR
        //  \s if RE_CHAR_CLASS_ESCAPES is set

	else if (unit.bk && (unit.ch == 's') && syntax.get(RESyntax.RE_CHAR_CLASS_ESCAPES)) {
	  addToken(currentToken);
	  currentToken = new RETokenPOSIX(m_subIndex,RETokenPOSIX.SPACE,insens,false);
	}

	// NON-WHITESPACE OPERATOR
        //  \S

	else if (unit.bk && (unit.ch == 'S') && syntax.get(RESyntax.RE_CHAR_CLASS_ESCAPES)) {
	  addToken(currentToken);
	  currentToken = new RETokenPOSIX(m_subIndex,RETokenPOSIX.SPACE,insens,true);
	}

	// TAB ESCAPE
        //  \t

	else if (unit.bk && (unit.ch == 't')) {
	  addToken(currentToken);
	  currentToken = new RETokenChar(m_subIndex,'\t',false);
	}

	// ALPHANUMERIC OPERATOR
        //  \w

	else if (unit.bk && (unit.ch == 'w') && syntax.get(RESyntax.RE_CHAR_CLASS_ESCAPES)) {
	  addToken(currentToken);
	  currentToken = new RETokenPOSIX(m_subIndex,RETokenPOSIX.ALNUM,insens,false);
	}

	// NON-ALPHANUMERIC OPERATOR
        //  \W

	else if (unit.bk && (unit.ch == 'W') && syntax.get(RESyntax.RE_CHAR_CLASS_ESCAPES)) {
	  addToken(currentToken);
	  currentToken = new RETokenPOSIX(m_subIndex,RETokenPOSIX.ALNUM,insens,true);
	}

	// END OF STRING OPERATOR
        //  \Z

	else if (unit.bk && (unit.ch == 'Z') && syntax.get(RESyntax.RE_STRING_ANCHORS)) {
	  addToken(currentToken);
	  currentToken = new RETokenEnd(m_subIndex,false);
	}

	// NON-SPECIAL CHARACTER (or escape to make literal)
        //  c | \* for example

	else {  // not a special character
	  addToken(currentToken);
	  currentToken = new RETokenChar(m_subIndex,unit.ch,insens);
	} 
      } // end while

    // Add final buffered token if applicable
    addToken(currentToken);
      
    if (branches != null) {
      branches.addElement(new RE(firstToken,lastToken,m_numSubs,m_subIndex));
      branches.trimToSize(); // compact the Vector
      firstToken = lastToken = new RETokenOneOf(m_subIndex,branches,false);
    }
  }

  private static int getCharUnit(char[] input, int index, CharUnit unit) throws REException {
    unit.ch = input[index++];
    if (unit.bk = (unit.ch == '\\'))
      if (index < input.length)
	unit.ch = input[index++];
      else throw new REException("\\ at end of pattern.",REException.REG_ESCAPE,index);
    return index;
  }

  /**
   * Checks if the input in its entirety is an exact match of
   * this regular expression.
   *
   * @param input The input text.
   * @exception IllegalArgumentException The input text was not a String, char[], or InputStream.
   */
  public boolean isMatch(Object input) {
    return isMatch(input,0,0);
  }
  
  /**
   * Checks if the input string, starting from index, is an exact match of
   * this regular expression.
   *
   * @param input The input text.
   * @param index The offset index at which the search should be begin.
   * @exception IllegalArgumentException The input text was not a String, char[], StringBuffer or InputStream.
   */
  public boolean isMatch(Object input,int index) {
    return isMatch(input,index,0);
  }
  

  /**
   * Checks if the input, starting from index and using the specified
   * execution flags, is an exact match of this regular expression.
   *
   * @param input The input text.
   * @param index The offset index at which the search should be begin.
   * @param eflags The logical OR of any execution flags above.
   * @exception IllegalArgumentException The input text was not a String, char[], StringBuffer or InputStream.
   */
  public boolean isMatch(Object input,int index,int eflags) {
    return isMatchImpl(makeCharIndexed(input,index),index,eflags);
  }

  private boolean isMatchImpl(CharIndexed input, int index, int eflags) {
    if (firstToken == null)  // Trivial case
      return (input.charAt(0) == CharIndexed.OUT_OF_BOUNDS);
    int[] i = firstToken.match(input,0,eflags,new REMatch(m_numSubs,index));
    return (i != null) && (input.charAt(i[0]) == CharIndexed.OUT_OF_BOUNDS);
  }
    
  /**
   * Returns the maximum number of subexpressions in this regular expression.
   * If the expression contains branches, the value returned will be the
   * maximum subexpressions in any of the branches.
   */
  public int getNumSubs() {
    return m_numSubs;
  }

  // Overrides REToken.setUncle
  void setUncle(REToken f_uncle) {
    lastToken.setUncle(f_uncle);
  }

  // Overrides REToken.chain
  boolean chain(REToken f_next) {
    super.chain(f_next);
    if (lastToken != null) lastToken.setUncle(f_next);
    return true;
  }
    
  /**
   * Returns the minimum number of characters that could possibly
   * constitute a match of this regular expression.
   */
  public int getMinimumLength() {
    int min = 0;
    REToken t = firstToken;
    if (t == null) return 0;
    do {
      min += t.getMinimumLength();
    } while ((t = t.m_next) != null);
    return min;
  }

  /**
   * Returns an array of all matches found in the input.
   *
   * @param input The input text.
   * @exception IllegalArgumentException The input text was not a String, char[], StringBuffer or InputStream.
   */
  public REMatch[] getAllMatches(Object input) {
    return getAllMatches(input,0,0);
  }

  /**
   * Returns an array of all matches found in the input,
   * beginning at the specified index position.
   *
   * @param input The input text.
   * @param index The offset index at which the search should be begin.
   * @exception IllegalArgumentException The input text was not a String, char[], StringBuffer or InputStream.
   */
  public REMatch[] getAllMatches(Object input, int index) {
    return getAllMatches(input,index,0);
  }

  /**
   * Returns an array of all matches found in the input string,
   * beginning at the specified index position and using the specified
   * execution flags.
   *
   * @param input The input text.
   * @param index The offset index at which the search should be begin.
   * @param eflags The logical OR of any execution flags above.
   * @exception IllegalArgumentException The input text was not a String, char[], StringBuffer or InputStream.
   */
  public REMatch[] getAllMatches(Object input, int index, int eflags) {
    return getAllMatchesImpl(makeCharIndexed(input,index),index,eflags);
  }

  // this has been changed since 1.03 to be non-overlapping matches
  private REMatch[] getAllMatchesImpl(CharIndexed input, int index, int eflags) {
    Vector all = new Vector();
    REMatch m = null;
    while ((m = getMatchImpl(input,index,eflags,null)) != null) {
      all.addElement(m);
      index = m.getEndIndex();
      if (m.end[0] == 0) {   // handle pathological case of zero-length match
	index++;
	input.move(1);
      } else {
	input.move(m.end[0]);
      }
    }
    REMatch[] mset = new REMatch[all.size()];
    all.copyInto(mset);
    return mset;
  }
  
  /* Implements abstract method REToken.match() */
  int[] match(CharIndexed input, int index, int eflags, REMatch mymatch) { 
    if (firstToken == null) return new int[] { index }; // Trivial case
    /*
    if ((mymatch.start[m_subIndex] == -1) 
       	|| (mymatch.start[m_subIndex] > index))
    */
    int oldstart = mymatch.start[m_subIndex];
    mymatch.start[m_subIndex] = index;
    int[] newIndex = firstToken.match(input,index,eflags,mymatch);
    if (newIndex == null) { 
	mymatch.start[m_subIndex] = oldstart;
    } else {
      // If this match succeeded, then whole rest of string is good,
      // and newIndex[0] is the end of the match AT THIS LEVEL

      // We need to make list of all possible nexts.
      int[] doables = new int[0];
      int[] thisResult;
      for (int i = 0; i < newIndex.length; i++) {
	thisResult = next(input,newIndex[i],eflags,mymatch);
	if (thisResult != null) {
	  int[] temp = new int[doables.length + thisResult.length];
	  System.arraycopy(doables,0,temp,0,doables.length);
	  for (int j = 0; j < thisResult.length; j++) {
	    temp[doables.length + j] = thisResult[j];
	  }
	  doables = temp;
	}
      }
      return (doables.length == 0) ? null : doables;
    }
    return null;
  }
  
  /**
   * Returns the first match found in the input.
   *
   * @param input The input text.
   * @exception IllegalArgumentException The input text was not a String, char[], StringBuffer or InputStream.
   */
  public REMatch getMatch(Object input) {
    return getMatch(input,0,0);
  }
  
  /**
   * Returns the first match found in the input, beginning
   * the search at the specified index.
   *
   * @param input The input text.
   * @exception IllegalArgumentException The input text was not a String, char[], StringBuffer or InputStream.
   */
  public REMatch getMatch(Object input, int index) {
    return getMatch(input,index,0);
  }
  
  /**
   * Returns the first match found in the input, beginning
   * the search at the specified index, and using the specified
   * execution flags.  If no match is found, returns null.
   *
   * @param input The input text.
   * @param index The offset index at which the search should be begin.
   * @param eflags The logical OR of any execution flags above.
   * @exception IllegalArgumentException The input text was not a String, char[], StringBuffer or InputStream.
   */
  public REMatch getMatch(Object input, int index, int eflags) {
    return getMatch(input,index,eflags,null);
  }

  /**
   * Returns the first match found in the input, beginning
   * the search at the specified index, and using the specified
   * execution flags.  If no match is found, returns null.  If a StringBuffer
   * is provided and is non-null, the contents of the input text from the index to the
   * beginning of the match (or to the end of the input, if there is no match)
   * are appended to the StringBuffer.
   *
   * @param input The input text.
   * @param index The offset index at which the search should be begin.
   * @param eflags The logical OR of any execution flags above.
   * @param buffer The StringBuffer to save pre-match text in.
   * @exception IllegalArgumentException The input text was not a String, char[], StringBuffer or InputStream.
   */
  public REMatch getMatch(Object input, int index, int eflags, StringBuffer buffer) {
    return getMatchImpl(makeCharIndexed(input,index),index,eflags,buffer);
  }

  REMatch getMatchImpl(CharIndexed input, int index, int eflags, StringBuffer buffer) {
    // check if input is at a valid position
    if (!input.isValid()) return null;
    REMatch mymatch = new REMatch(m_numSubs,index);
    do {
      int[] result = match(input,0,eflags,mymatch);
      if (result != null) {
	mymatch.end[0] = result[0]; // may break leftmost longest
	mymatch.finish(input);
	return mymatch;
      }
      mymatch.clear(++index);
      if (buffer != null) buffer.append(input.charAt(0));
    } while (input.move(1));

    return null;
  }

  /**
   * Returns an REMatchEnumeration that can be used to iterate over the
   * matches found in the input text.
   *
   * @param input The input text.
   * @exception IllegalArgumentException The input text was not a String, char[], StringBuffer or InputStream.
   */
  public REMatchEnumeration getMatchEnumeration(Object input) {
    return getMatchEnumeration(input,0,0);
  }


  /**
   * Returns an REMatchEnumeration that can be used to iterate over the
   * matches found in the input text.
   *
   * @param input The input text.
   * @param index The offset index at which the search should be begin.
   * @exception IllegalArgumentException The input text was not a String, char[], StringBuffer or InputStream.
   */
  public REMatchEnumeration getMatchEnumeration(Object input, int index) {
    return getMatchEnumeration(input,index,0);
  }

  /**
   * Returns an REMatchEnumeration that can be used to iterate over the
   * matches found in the input text.
   *
   * @param input The input text.
   * @param index The offset index at which the search should be begin.
   * @param eflags The logical OR of any execution flags above.
   * @exception IllegalArgumentException The input text was not a String, char[], StringBuffer or InputStream.
   */
  public REMatchEnumeration getMatchEnumeration(Object input, int index, int eflags) {
    return new REMatchEnumeration(this,makeCharIndexed(input,index),index,eflags);
  }


  /**
   * Substitutes the replacement text for the first match found in the input.
   *
   * @param input The input text.
   * @param replace The replacement text, which may contain $x metacharacters (see REMatch.substituteInto).
   * @exception IllegalArgumentException The input text was not a String, char[], StringBuffer or InputStream.
   */
  public String substitute(Object input,String replace) {
    return substitute(input,replace,0,0);
  }

  /**
   * Substitutes the replacement text for the first match found in the input
   * beginning at the specified index position.
   *
   * @param input The input text.
   * @param replace The replacement text, which may contain $x metacharacters (see REMatch.substituteInto).
   * @param index The offset index at which the search should be begin.
   * @exception IllegalArgumentException The input text was not a String, char[], StringBuffer or InputStream.
   */
  public String substitute(Object input,String replace,int index) {
    return substitute(input,replace,index,0);
  }

  /**
   * Substitutes the replacement text for the first match found in the input
   * string, beginning at the specified index position and using the
   * specified execution flags.
   *
   * @param input The input text.
   * @param replace The replacement text, which may contain $x metacharacters (see REMatch.substituteInto).
   * @param index The offset index at which the search should be begin.
   * @param eflags The logical OR of any execution flags above.
   * @exception IllegalArgumentException The input text was not a String, char[], StringBuffer or InputStream.
   */
  public String substitute(Object input,String replace,int index,int eflags) {
    return substituteImpl(makeCharIndexed(input,index),replace,index,eflags);
  }

  private String substituteImpl(CharIndexed input,String replace,int index,int eflags) {
    StringBuffer buffer = new StringBuffer();
    REMatch m = getMatchImpl(input,index,eflags,buffer);
    if (m==null) return buffer.toString();
    buffer.append(m.substituteInto(replace));
    if (input.move(m.end[0])) {
      do {
	buffer.append(input.charAt(0));
      } while (input.move(1));
    }
    return buffer.toString();
  }
  
  /**
   * Substitutes the replacement text for each non-overlapping match found 
   * in the input text.
   *
   * @param input The input text.
   * @param replace The replacement text, which may contain $x metacharacters (see REMatch.substituteInto).
   * @exception IllegalArgumentException The input text was not a String, char[], StringBuffer or InputStream.
   */
  public String substituteAll(Object input,String replace) {
    return substituteAll(input,replace,0,0);
  }

  /**
   * Substitutes the replacement text for each non-overlapping match found 
   * in the input text, starting at the specified index.
   *
   * @param input The input text.
   * @param replace The replacement text, which may contain $x metacharacters (see REMatch.substituteInto).
   * @param index The offset index at which the search should be begin.
   * @exception IllegalArgumentException The input text was not a String, char[], StringBuffer or InputStream.
   */
  public String substituteAll(Object input,String replace,int index) {
    return substituteAll(input,replace,index,0);
  }
 
  /**
   * Substitutes the replacement text for each non-overlapping match found 
   * in the input text, starting at the specified index and using the
   * specified execution flags.
   *
   * @param input The input text.
   * @param replace The replacement text, which may contain $x metacharacters (see REMatch.substituteInto).
   * @param index The offset index at which the search should be begin.
   * @param eflags The logical OR of any execution flags above.
   * @exception IllegalArgumentException The input text was not a String, char[], StringBuffer or InputStream.
   */
  public String substituteAll(Object input,String replace,int index,int eflags) {
    return substituteAllImpl(makeCharIndexed(input,index),replace,index,eflags);
  }

  private String substituteAllImpl(CharIndexed input,String replace,int index,int eflags) {
    StringBuffer buffer = new StringBuffer();
    REMatch m;
    while ((m = getMatchImpl(input,index,eflags,buffer)) != null) {
      buffer.append(m.substituteInto(replace));
      index = m.getEndIndex();
      if (m.end[0] == 0) {
	char ch = input.charAt(0);
	if (ch != CharIndexed.OUT_OF_BOUNDS) 
	  buffer.append(ch);
	input.move(1);
      } else {
	input.move(m.end[0]);
      }
    }
    return buffer.toString();
  }
  
  /* Helper function for constructor */
  private void addToken(REToken next) {
    if (next == null) return;
    if (firstToken == null)
      lastToken = firstToken = next;
    else
      // if chain returns false, it "rejected" the token due to
      // an optimization, and next was combined with lastToken
      if (lastToken.chain(next)) lastToken = next;
  }

  private static REToken setRepeated(REToken current, int min, int max, int index) throws REException {
    if (current == null) throw new REException("repeat preceding token",REException.REG_BADRPT,index);
    return new RETokenRepeated(current.m_subIndex,current,min,max);
  }

  private static int getPosixSet(char[] pattern,int index,StringBuffer buf) {
    // Precondition: pattern[index-1] == ':'
    // we will return pos of closing ']'.
    int i;
    for (i=index; i<(pattern.length-1); i++) {
      if ((pattern[i] == ':') && (pattern[i+1] == ']'))
	return i+2;
      buf.append(pattern[i]);
    }
    return index; // didn't match up
  }

  private int getMinMax(char[] input,int index,IntPair minMax,RESyntax syntax) throws REException {
    // Precondition: input[index-1] == '{', minMax != null

    if (index == input.length) throw new REException("no matching brace",REException.REG_EBRACE,index);
	
    int min,max=0;
    CharUnit unit = new CharUnit();
    StringBuffer buf = new StringBuffer();
    
    // Read string of digits
    while (((index = getCharUnit(input,index,unit)) != input.length)
	   && Character.isDigit(unit.ch))
      buf.append(unit.ch);

    // Check for {} tomfoolery
    if (buf.length() == 0) throw new REException("bad brace construct",REException.REG_EBRACE,index);

    min = Integer.parseInt(buf.toString());
	
    if ((unit.ch == '}') && (syntax.get(RESyntax.RE_NO_BK_BRACES) ^ unit.bk))
      max = min;
    else if ((unit.ch == ',') && !unit.bk) {
      buf = new StringBuffer();
      // Read string of digits
      while (((index = getCharUnit(input,index,unit)) != input.length)
	     && Character.isDigit(unit.ch))
	buf.append(unit.ch);

      if (!((unit.ch == '}') && (syntax.get(RESyntax.RE_NO_BK_BRACES) ^ unit.bk)))
	throw new REException("expected end of interval",REException.REG_EBRACE,index);

      // This is the case of {x,}
      if (buf.length() == 0) max = Integer.MAX_VALUE;
      else max = Integer.parseInt(buf.toString());
    } else throw new REException("invalid character in brace expression",REException.REG_EBRACE,index);

    // We know min and max now, and they are valid.

    minMax.first = min;
    minMax.second = max;

    // return the index following the '}'
    return index;
  }

   /**
    * Return a human readable form of the compiled regular expression,
    * useful for debugging.
    */
   public String toString() {
     StringBuffer sb = new StringBuffer();
     dump(sb);
     return sb.toString();
   }

  void dump(StringBuffer os) {
    os.append('(');
    if (m_subIndex == 0)
      os.append("?:");
    if (firstToken != null)
      firstToken.dumpAll(os);
    os.append(')');
  }

  // Cast input appropriately or throw exception
  private static CharIndexed makeCharIndexed(Object input, int index) {
    if (input instanceof String)
      return new CharIndexedString((String) input,index);
    else if (input instanceof char[])
      return new CharIndexedCharArray((char[]) input,index);
    else if (input instanceof StringBuffer)
      return new CharIndexedStringBuffer((StringBuffer) input,index);
    else if (input instanceof InputStream)
      return new CharIndexedInputStream((InputStream) input,index);
    else throw new IllegalArgumentException("Invalid class for input text");
  }
}
