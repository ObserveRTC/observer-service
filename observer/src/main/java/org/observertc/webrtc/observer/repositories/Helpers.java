package org.observertc.webrtc.observer.repositories;

import java.math.BigInteger;

/**
 * This class is a emporary collector class
 * for stuff we use for either on-the fly debug
 * and no place or when we don't know where to place
 * the particular functions yet.
 * <p>
 * Please always do static for that function and give a comment
 * for later decide where to put that particular function
 */
public class Helpers {

	/**
	 * Convert any byte array into a hexadecimial format and returns with the string
	 *
	 * @param hash
	 * @return
	 */
	public static String toHexString(byte[] hash) {
		// Convert byte array into signum representation  
		BigInteger number = new BigInteger(1, hash);

		// Convert message digest into hex value  
		StringBuilder hexString = new StringBuilder(number.toString(16));

		// Pad with leading zeros 
		while (hexString.length() < 32) {
			hexString.insert(0, '0');
		}

		return hexString.toString();
	}
}
