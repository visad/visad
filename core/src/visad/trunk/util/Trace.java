//
// Trace.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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

package visad.util;

import java.lang.reflect.Method;

/**
 * This class provides a hook into the IDV Trace facility. It uses reflection to call
 * the  corresponding ucar.unidata.util.Trace methods if that class can be found. 
 * Else, nothing happens.
 * To use this first call: Trace.startTrace();
 * <p>
 * You can bracket a code segment with:<pre>
 *  Trace.call1("some label");
 *  ... code
 *  Trace.call2("some label");
 * </pre>
 * This matches the "some label". It will indent subsequent Trace calls to show nesting.
 * You can also just print something out with:<pre>
 * Trace.msg("some message");
 * </pre>
 * The output looks like:<pre>
 * 196  996   &gt;ImageRenderer type.doTransform
 * 11   99      &gt;ImageRenderer build texture length: 4503200
 * 2    15        &gt;ImageRenderer new byte
 * 691  15683     &lt;ImageRenderer new byte ms: 690
 * 1    1         &gt;ImageRenderer color_bytes
 * 345  99        &lt;ImageRenderer color_bytes ms: 344
 * 0    2       &lt;ImageRenderer build texture ms: 1037
 * </pre>
 * The first column is the elapsed time since the last print line. The second column is the 
 * memory delta (note: GC can make this negative). For the call1/call2 pairs the ms:... shows
 * the time spent in the block.
 *
 */

public  class Trace
{

    private static boolean doneReflectionLookup = false;

    private static Method call1Method;
    private static Method call2Method;
    private static Method msgMethod;
    private static Method startMethod;


    public static void main(String[]args) {
        //        Trace.startTrace();
        Trace.call1("hello");
        Trace.msg("hello there");
        Trace.call2("hello");
    }

    /** Try to load the ucar Trace facility via reflection
     *
     * @return Was successful
     */
    private static boolean checkReflection() {
        if(!doneReflectionLookup) {
            try {
                Class c = Class.forName("ucar.unidata.util.Trace");
                call1Method = c.getDeclaredMethod("call1", new Class[]{String.class,String.class});
                call2Method = c.getDeclaredMethod("call2", new Class[]{String.class,String.class});
                msgMethod = c.getDeclaredMethod("msg", new Class[]{String.class});
                startMethod = c.getDeclaredMethod("startTrace", new Class[]{});
            } catch(Exception exc){
            }
            doneReflectionLookup = true;
        }
        return msgMethod!=null;
    }


    /** 
     * Bracket a block of code with call1("some unique msg"); ...code...; call2("some unique msg"); 
     * Where "some unique msg" is used to match up the call1/call2 pairs
     */
    public static void call1(String msg) {
        call1(msg,"");
    }

    /** Bracket a block of code with call1("some unique msg"); ...code...; call2("some unique msg");
     *  Append extra to the end of the line
     */
    public static void call1(String msg, String extra) {
        if(!checkReflection()) return;
        try {
            call1Method.invoke(null, new Object[]{msg,extra});
        } catch(Exception iae){
            System.err.println("Trace.call1:" + iae);
        }
    }

    /** Call this to start tracing */
    public static void startTrace() {
        if(!checkReflection()) {
            System.err.println("Could not start tracing");
            return;
        }
        try {
            startMethod.invoke(null, new Object[]{});
        } catch(Exception iae){
            System.err.println("Trace.startTrace:" + iae);
        }
    }



    /** Bracket a block of code with call1("some unique msg"); ...code...; call2("some unique msg"); */
    public static void call2(String msg) {
        call2(msg,"");
    }

    /** Close the call */
    public static void call2(String msg, String extra) {
        if(!checkReflection()) return;
        try {
            call2Method.invoke(null, new Object[]{msg,extra});
        } catch(Exception iae){
            System.err.println("Trace.call2:" + iae);
        }
    }


    /** Print out a line */
    public static void msg(String msg) {
        if(!checkReflection()) return;
        try {
            msgMethod.invoke(null, new Object[]{msg});
        } catch(Exception iae){
            System.err.println("Trace.msg:" + iae);
        }
    }

}

