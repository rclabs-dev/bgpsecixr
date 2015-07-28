package net.floodlightcontroller.bgpsecixr;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

public class BGPSecIXRUtils {

	/*
    public static Boolean getBit(byte b, int bit){
        return (b & (1 << bit)) != 0;
    }*/
	
	/** 
	 * Convert Hex to binary and pad with zero if need to complete one octect
	 * Use to decode "Total Path Attribute Length" bits
	 * @param hexNumber
	 * @return
	 */
	public static boolean isBit(byte[] num, int pos){       
        String binFlags = Integer.toBinaryString((bytesToInt(num)));
        while (binFlags.length() < 8){
           binFlags = "0" + binFlags;
        }
        //System.out.println("Flags in binary: "  + binFlags);
		if (binFlags.substring(pos,pos+1).equals("1"))
			return true;
        return false;
	}
	
	/** 
	 * Convert Hex to binary and pad with zero if need to complete one octect
	 * For use to decode "Total Path Attribute Length" bits
	 * @param hexNumber
	 * @return
	 */
	public static String bitFlags(String hexNumber){       
        String binFlags = Integer.toBinaryString((hexToDec(hexNumber)));
        while (binFlags.length() < 8){
           binFlags = "0" + binFlags;
        }
        //System.out.println("Flags in binary: "  + binFlags);
		return binFlags.substring(3,4);
	}	
	
	/**
	 * Convert Hex to Decimal
	 * @param hexNumber
	 * @return
	 */
	public static int hexToDec(String hexNumber) {
		return Integer.parseInt(hexNumber, 16);
	}
		
	// Converts byte array into String
	public static String bytesToHexString(byte[] bytes) {
		if(bytes == null) {
			return "null";
		}
		String ret = "";
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < bytes.length; i++) {
			if(i > 0) {
				ret += ":";
			}
			short u8byte = (short) (bytes[i] & 0xff);
			String tmp = Integer.toHexString(u8byte);
			if(tmp.length() == 1) {
				buf.append("0");
			}
			buf.append(tmp);
		}
		ret = buf.toString();
	    return ret;
	}		
	
	/**
	 * Returns a new byte array from given byte array, starting at start index with the size of the length parameter.
	 * Byte array given as parameter stays untouched.
	 *
	 * @param bytes original byte array
	 * @param startIndex beginning index, inclusive
	 * @param length how many bytes should be in the sub-array
	 * @return a new byte array that is a sub-array of the original
	 */
	public static byte[] subByte(final byte[] bytes, final int startIndex, final int length) {
		if (!checkLength(bytes, length) || !checkStartIndex(bytes, startIndex, length)) {
			throw new IllegalArgumentException("Cannot create subByte, invalid arguments: Length: " + length + " startIndex: " + startIndex);
	    }
	    final byte[] res = new byte[length];
	    System.arraycopy(bytes, startIndex, res, 0, length);
	    return res;
	}		
	    
	private static boolean checkLength(final byte[] bytes, final int length) {
	    return length > 0 && bytes.length > 0 && length <= bytes.length;
	}

	private static boolean checkStartIndex(final byte[] bytes, final int startIndex, final int length) {
	    return startIndex >= 0 && startIndex < bytes.length && (startIndex + length <= bytes.length);
	}

	/**
	 * Converts byte array to Integer. If there are less bytes in the array as required (4), the method will push
	 * adequate number of zero bytes prepending given byte array.
	 *
	 * @param bytes array to be converted to int
	 * @return int
	*/
	public static int bytesToInt(final byte[] bytes) {
		if (bytes.length > Integer.SIZE / Byte.SIZE) {
			throw new IllegalArgumentException("Cannot convert bytes to integer. Byte array too big.");
	    }
	    byte[] res = new byte[Integer.SIZE / Byte.SIZE];
	    if (bytes.length != Integer.SIZE / Byte.SIZE) {
	    	System.arraycopy(bytes, 0, res, Integer.SIZE / Byte.SIZE - bytes.length, bytes.length);
	    } else {
	        res = bytes;
	    }
	    final ByteBuffer buff = ByteBuffer.wrap(res);
	    	return buff.getInt();
	    }
		
	/**
	 * Check if everything in the string is number
	 * @param typeField a string with two bytes that contains type of message
	 * @return true if everything is a number or false if otherwise
	 */
	public static boolean isNumber(String typeField) {  
		for (int i = 0; i < typeField.length(); i++)
			if (!(Character.isDigit(typeField.charAt(i))))   
				return false;  
			return true;  
	}  	
	
	public static void getBytes(byte[] source, int srcBegin, int srcEnd, byte[] destination,
		     int dstBegin) {
		   System.arraycopy(source, srcBegin, destination, dstBegin, srcEnd - srcBegin);
		 }
	

		 /**
		  * Return a new byte array containing a sub-portion of the source array
		  * 
		  * @param srcBegin
		  *          The beginning index (inclusive)
		  * @param srcEnd
		  *          The ending index (exclusive)
		  * @return The new, populated byte array
		  */
		 public static byte[] subbytes(byte[] source, int srcBegin, int srcEnd) {
		   byte destination[];
		   destination = new byte[srcEnd - srcBegin];
		   getBytes(source, srcBegin, srcEnd, destination, 0);
		   return destination;
		 }

		 /**
		  * Return a new byte array containing a sub-portion of the source array
		  * 
		  * @param srcBegin
		  *          The beginning index (inclusive)
		  * @return The new, populated byte array
		  */
		 public static byte[] subByte(byte[] source, int srcBegin) {
		   return subbytes(source, srcBegin, source.length);
		 }	
		 
		 public static boolean isIPv4AddressValid(String cidr) {
			 if(cidr == null) {
				 return false;
			 }

			 String values[] = cidr.split("/");
			 Pattern ipv4Pattern = Pattern
					 .compile("(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])");
			 Matcher mm = ipv4Pattern.matcher(values[0]);
			 
			 if(!mm.matches()) {
				 return false;
			 }
			 
			 if(values.length >= 2) {
			 int prefix = Integer.valueOf(values[1]);
			 	if((prefix < 0) || (prefix > 32)) {
			 		return false;
			    }
			 }
			 return true;
		 }		 

			/**
			 * Convert hex IP Address format to conventional format
			 * @param ip
			 * @return
			 */
		 public static String hexToIPDec(String ip) {
			 InetAddress convAddr = null;
			 for (int i = ip.length(); i < 8; i += 2)
				 ip = ip + "00";
			 	 try {
			 		 convAddr = InetAddress.getByAddress(DatatypeConverter.parseHexBinary(ip));
			 	 } catch (UnknownHostException e) {
			 		 e.printStackTrace();
			 	 }
			 return convAddr.getHostAddress().toString();
		}
		 
		public static String byteToIPDec(byte[] ip) {
			String ipConv = bytesToHexString(ip);
			InetAddress convAddr = null;
			for (int i = ipConv.length(); i < 8; i += 2)
				ipConv = ipConv + "00";
			 	try {
			 		convAddr = InetAddress.getByAddress(DatatypeConverter.parseHexBinary(ipConv));
			 	} catch (UnknownHostException e) {
			 		e.printStackTrace();
			 	}
			return convAddr.getHostAddress().toString();
		}		
		
		public static byte[] hexStrToByteArray(String s) {
		    byte data[] = new byte[s.length()/2];
		    for(int i=0;i < s.length();i+=2) {
		        data[i/2] = (Integer.decode("0x"+s.charAt(i)+s.charAt(i+1))).byteValue();
		    }
		    return data;
		}
		
		static public byte[] cloneArray(byte[] byteValue) {
			    byte[] b = new byte[byteValue.length];
			    System.arraycopy(byteValue, 0, b, 0, byteValue.length);
			    return b;
			  }
		
}
