package nom.tam.fits;

/*
 * Copyright: Thomas McGlynn 1997-1998.
 * This code may be used for any purpose, non-commercial
 * or commercial so long as this copyright notice is retained
 * in the source code or included in or referred to in any
 * derived software.
 */


public class FitsException extends Exception {

    public FitsException () {
        super();
    }

    public FitsException (String msg) {
        super(msg);
    }

}
