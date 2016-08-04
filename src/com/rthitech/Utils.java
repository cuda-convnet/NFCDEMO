package com.rthitech;

/**
 * @author weixin E-mail:weixin@rthitech.com.cn
 * @date 创建时间：2015-12-16 下午4:06:19
 * @version 1.0
 */

public class Utils {


	/**
	 * 字符序列转换为16进制字符串
	 * 
	 * @param src
	 * @return
	 */
	static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("0x");
		if (src == null || src.length <= 0) {
			return null;
		}
		char[] buffer = new char[2];
		for (int i = 0; i < src.length; i++) {
			buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
			buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
			System.out.println(buffer);
			stringBuilder.append(buffer);
		}
		return stringBuilder.toString();
	}

	/**
	 * 
	 * * 把16进制字符串转换成字节数组 * @param hex * @return
	 * */
	public static byte[] hexStringToByte(String hex) {
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}

	private static byte toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}

	public static String str2HexStr(String str) {

		char[] chars = "0123456789ABCDEF".toCharArray();
		StringBuilder sb = new StringBuilder("");
		byte[] bs = str.getBytes();
		int bit;

		for (int i = 0; i < bs.length; i++) {
			bit = (bs[i] & 0x0f0) >> 4;
			sb.append(chars[bit]);
			bit = bs[i] & 0x0f;
			sb.append(chars[bit]);
			sb.append(' ');
		}
		return sb.toString().trim();
	}

	/**
	 * 十六进制转换字符串
	 * 
	 * @param String
	 *            str Byte字符串(Byte之间无分隔符 如:[616C6B])
	 * @return String 对应的字符串
	 */
	public static String hexStr2Str(String hexStr) {
		String str = "0123456789ABCDEF";
		char[] hexs = hexStr.toCharArray();
		byte[] bytes = new byte[hexStr.length() / 2];
		int n;

		for (int i = 0; i < bytes.length; i++) {
			n = str.indexOf(hexs[2 * i]) * 16;
			n += str.indexOf(hexs[2 * i + 1]);
			bytes[i] = (byte) (n & 0xff);
		}
		return new String(bytes);
	}

	/**
	 * 字符串左 补0
	 * 
	 * @param str
	 *            需要补0的字符串
	 * @param strLength
	 *            需要补的位数
	 * @return String 返回补0后的字符串
	 */
	public static String addZeroForNum(String str, int strLength) {
		int strLen = str.length();
		StringBuffer sb = null;
		while (strLen < strLength) {
			sb = new StringBuffer();
			sb.append("0").append(str);// 往左补0
			// sb.append(str).append("0"); //往右补0
			str = sb.toString();
			strLen = str.length();
		}
		return str;
	}

	/**
	 * 校验和结算
	 * 
	 * @param number
	 * @return String 计算CRC
	 */
	public static String hexAddSum(String number) {
		int m = 0;
		String sum = "";
		for (int i = 0; i < number.length() / 2; i++) {
			m = m + Integer.parseInt(number.substring(i * 2, i * 2 + 2), 16);
		}
		sum = addZeroForNum(Integer.toHexString(m), 2);
		// 抛出溢出
		if (sum.length() > 2) {
			sum = sum.substring(sum.length() - 2, sum.length());
		}
		return sum;
	}
}
