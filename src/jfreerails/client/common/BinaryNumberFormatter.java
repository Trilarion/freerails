/*
 * BinaryNumberFormatter.java
 *
 * Created on 30 April 2003, 20:23
 */

package jfreerails.client.common;

/**
 *
 * @author  Luke
 */
public class BinaryNumberFormatter {
   
    public static String format(int i, int bits){
        int maxValue = 1 << (bits);
       
        if(i<0){
            throw new IllegalArgumentException("i must be greater than 0.  It was "+i);
        }
        if(i >= maxValue){
			throw new IllegalArgumentException("i must be less than "+maxValue+".  It was "+i);
        }
        String s = Integer.toString(i+maxValue, 2);
        return s.substring(1);        
    }
    
}
