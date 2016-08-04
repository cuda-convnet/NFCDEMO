package com.rthitech;

/**
 * @author weixin E-mail:weixin@rthitech.com.cn
 * @date ����ʱ�䣺2015-12-16 ����4:06:19
 * @version 1.0
 */

public class Utils {


	/**
	 * �ַ�����ת��Ϊ16�����ַ���
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
	 * * ��16�����ַ���ת�����ֽ����� * @param hex * @return
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
	 * ʮ������ת���ַ���
	 * 
	 * @param String
	 *            str Byte�ַ���(Byte֮���޷ָ��� ��:[616C6B])
	 * @return String ��Ӧ���ַ���
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
	 * �ַ����� ��0
	 * 
	 * @param str
	 *            ��Ҫ��0���ַ���
	 * @param strLength
	 *            ��Ҫ����λ��
	 * @return String ���ز�0����ַ���
	 */
	public static String addZeroForNum(String str, int strLength) {
		int strLen = str.length();
		StringBuffer sb = null;
		while (strLen < strLength) {
			sb = new StringBuffer();
			sb.append("0").append(str);// ����0
			// sb.append(str).append("0"); //���Ҳ�0
			str = sb.toString();
			strLen = str.length();
		}
		return str;
	}

	/**
	 * У��ͽ���
	 * 
	 * @param number
	 * @return String ����CRC
	 */
	public static String hexAddSum(String number) {
		int m = 0;
		String sum = "";
		for (int i = 0; i < number.length() / 2; i++) {
			m = m + Integer.parseInt(number.substring(i * 2, i * 2 + 2), 16);
		}
		sum = addZeroForNum(Integer.toHexString(m), 2);
		// �׳����
		if (sum.length() > 2) {
			sum = sum.substring(sum.length() - 2, sum.length());
		}
		return sum;
	}
}
