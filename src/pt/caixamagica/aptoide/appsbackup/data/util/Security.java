package pt.caixamagica.aptoide.appsbackup.data.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;


public class Security {
	private Security(){}

	/**
	 * Useful to convert the digest to the hash
	 * 
	 * @author rafael
	 * @param b
	 * @return
	 */
	public static String byteArrayToHexString(byte[] b) {
		StringBuilder result = new StringBuilder("");
		for (int i=0; i < b.length; i++) {
			result.append(Integer.toString( (b[i] & 0xff) + 0x100 , 16).substring( 1 ));
		}
		return result.toString();
	}

	public static String computeHmacSha1(String value, String keyString) throws InvalidKeyException, IllegalStateException, UnsupportedEncodingException, NoSuchAlgorithmException {
		Log.d("AptoideAppsBackup-Security", "computeHmacSha1 "+keyString+" : "+value);
		SecretKeySpec key = new SecretKeySpec((keyString).getBytes("UTF-8"), "HmacSHA1");
		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(key);

		byte[] bytes = mac.doFinal(value.getBytes("UTF-8"));

		String output = new String(stringToHex(bytes));
		Log.d("AptoideAppsBackup-Security", "computeHmacSha1 output: "+output);
		return output;

	}

	private static String stringToHex(byte[] data) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}
}