package com.rthitech;

import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class DesInterface {
	
	private static final String AlgDes = "DES/ECB/NoPadding";
	
	private static final String Alg3Des = "DESede/ECB/NoPadding";
	
	private static byte toByte(char cChr) {
		
		byte bChr = (byte) "0123456789ABCDEF".indexOf(cChr);
		
		if (bChr == -1) {
			
			bChr = (byte) "0123456789abcdef".indexOf(cChr);
		}
		
		return bChr;
	}
	
	private static String DataNot(String sDataIn) {
		
		byte[] bDataIn = AscToHex(sDataIn);
		
		if (bDataIn == null) {
			
			return null;
		}
		
		byte[] bDataOut = new byte[bDataIn.length];
		
		for (int i = 0; i < bDataIn.length; i++) {
			
			bDataOut[i] = (byte)(0xFF - bDataIn[i] & 0xFF);
		}
		
		Locale DefLoc = Locale.getDefault();
		  
		String sDataOut = HexToAsc(bDataOut).toUpperCase(DefLoc);
		
		return sDataOut;
	}
	
	private static String DataXor(String sDataIn1, String sDataIn2) {
	
		if (sDataIn1.length() == 0x00 || sDataIn1.length() % 0x02 != 0x00) {
			
			return null;
		}
		
		if (sDataIn2.length() == 0x00 || sDataIn2.length() % 0x02 != 0x00) {
			
			return null;
		}
		
		if (sDataIn1.length() != sDataIn2.length()) {
			
			return null;
		}
		
		byte[] bDataIn1 = AscToHex(sDataIn1);
		
		byte[] bDataIn2 = AscToHex(sDataIn2);
		
		for (int i = 0; i < bDataIn1.length; i++) {
			
			bDataIn1[i] = (byte) (bDataIn1[i] ^ bDataIn2[i]);
		}
		
		String sTmpBuf = HexToAsc(bDataIn1);
		
		return sTmpBuf;
	}
	
	private static String HexToAsc(byte[] bHex) {
		
		StringBuilder Sb = new StringBuilder("");
		
		if (bHex == null || bHex.length == 0) {
			
			return null;
		}
		
		char[] bChr = new char[2];
		
		for (int i = 0; i < bHex.length; i++) {
			
			bChr[0] = Character.forDigit((bHex[i] >>> 4) & 0x0F, 16);
			
			bChr[1] = Character.forDigit(bHex[i] & 0x0F, 16);
			
			Sb.append(bChr);
		}
		
		Locale DefLoc = Locale.getDefault();
		
		return Sb.toString().toUpperCase(DefLoc);
	}
	
	private static byte[] AscToHex(String sAsc) {
		
		if (sAsc.length() % 2 != 0) {
			
			return null;
		}
		
		int iLen = (sAsc.length() / 2);
		
		byte[] bResult = new byte[iLen];
		
		char[] achar = sAsc.toCharArray();
		
		for (int i = 0; i < iLen; i++) {
			
			int iPos = i * 2;
			
			bResult[i] = (byte) (toByte(achar[iPos]) << 4 | toByte(achar[iPos + 1]));
		}
		
		return bResult;
	}
	

	public static int iSymmInterface(int iType, String sKey, String sIV, String sDiv, String sInput, String[] sOutput) {
		
		int iRet = 0;
		
		if (sKey == null) {
			
			return 2001;
		}
		
		String[] sTmpKey = new String[0x01];
		
		if (iType == 0 || iType == 2 || iType == 3 || iType == 4 || iType == 5) {
			
			int iDivLen = (sDiv == null ? 0 : sDiv.length());
			
			if (iDivLen != 0) {
				
				iRet = DesDiversify(sKey, sDiv, sTmpKey);
				
				if (iRet != 0) {
				
					return iRet;
				}
			}
			else {
				
				sTmpKey[0x00] = sKey;
			}
		}
		
		if (iType == 0) {			/* 计算鉴别数据*/
			
			return Des(0x00, sTmpKey[0x00], sInput, sOutput);
		}
		
		else if (iType == 1) {		/* 秘钥分散*/
			
			return DesDiversify(sTmpKey[0x00], sDiv, sOutput);
		}
		
		else if (iType == 2) {		/* 不补位加密*/
			
			return DesEnc(sTmpKey[0x00], sInput, sOutput);
		}
		
		else if (iType == 3) {
			
			return DesEncWithPadding(sTmpKey[0x00], sInput, sOutput);
		}
		
		else if (iType == 4) {
			
			return DesDec(sTmpKey[0x00], sInput, sOutput);
		}
		
		else if (iType == 5) {
			
			return DesMac(sTmpKey[0x00], sIV, sInput, sOutput);
		}

		return 1002;
	}
	
	private static int Des(int iMode, String sKey, String sDataIn, String[] sDataOut) {
		
		if (sKey.length() != 0x10 && sKey.length() != 0x20) {
			
			return 3001;
		}
		
		if (sDataIn.length() != 0x10) {
		
			return 3002;
		}
		
		if (sDataOut == null) {
			
			return 3003;
		}
		
		byte[] bKey;
		
		byte[] bDataIn;
		
		if (sKey.length() == 0x20) {
			
			sKey = sKey + sKey.substring(0x00, 0x10);
		}
	
		bKey = AscToHex(sKey);
		
		bDataIn = AscToHex(sDataIn);
		
		try {
		
		  SecretKey DesKey;
		  
		  Cipher hCipher;
		  
		  if (sKey.length() == 0x10) {
			  
			  DesKey = new SecretKeySpec(bKey, AlgDes);
			  
			  hCipher = Cipher.getInstance(AlgDes);
		  }
		  else {
				
			  DesKey = new SecretKeySpec(bKey, Alg3Des);
			  
			  hCipher = Cipher.getInstance(Alg3Des);
		  }
		  
		  if (iMode == 0x00) {
			
			  hCipher.init(Cipher.ENCRYPT_MODE, DesKey);
			  
			  byte[] bDataOut = hCipher.doFinal(bDataIn);
			  
			  sDataOut[0x00] = HexToAsc(bDataOut);

			  Locale DefLoc = Locale.getDefault();
			  
			  sDataOut[0x00] = sDataOut[0x00].toUpperCase(DefLoc);
			  
			  return 0;
		  }
		  
		  else {
		  
			  hCipher.init(Cipher.DECRYPT_MODE, DesKey);
			  
			  byte[] bDataOut = hCipher.doFinal(bDataIn);
			  
			  sDataOut[0x00] = HexToAsc(bDataOut);
			  
			  Locale DefLoc = Locale.getDefault();
			  
			  sDataOut[0x00] = sDataOut[0x00].toUpperCase(DefLoc);
			  
			  return 0;
		  }
		
		} 
		catch (Exception e) {
		
			return 3009;
		}
	}
	
	private static int DesDiversify(String sKey, String sDataIn, String[] sDataOut) {
		
		int iDataInLen = sDataIn.length();
		
		if (iDataInLen == 0 || iDataInLen % 0x10 != 0) {
		
			return 3101;
		}
		
		int iKeyLen = sKey.length();
		
		int iRet = 0;
		
		if (iKeyLen == 0x10) {
			
			String sTmpKey = sKey;
			
			String[] sDst = new String[0x01];
			
			for (int i = 0; i < iDataInLen / 0x10; i++) {
				
				iRet = Des(0x00, sTmpKey, sDataIn.substring(i * 0x10, (i + 1) * 0x10), sDst);
				
				if (iRet != 0) {
					
					return iRet;
				}
				
				sTmpKey = sDst[0x00];
			}
			
			sDataOut[0x00] = sTmpKey;
			
			return 0;
		}
		else {		
			
			String sTmpKey = sKey;
			
			String[] sDst = new String[0x01];
			
			String sTmpDst;
			
			for (int i = 0; i < iDataInLen / 0x10; i++) {
				
				iRet = Des(0x00, sTmpKey, sDataIn.substring(i * 0x10, (i + 1) * 0x10), sDst);
				
				if (iRet != 0) {
					
					return iRet;
				}
				
				sTmpDst = sDst[0x00];
				
				String sTmpData = DataNot(sDataIn.substring(i * 0x10, (i + 1) * 0x10));
				
				if (sTmpData == null) {
					
					return 3102;
				}
				
				iRet = Des(0x00, sTmpKey, sTmpData, sDst);
				
				if (iRet != 0) {
					
					return iRet;
				}
				
				sTmpDst = sTmpDst + sDst[0x00];
				
				sTmpKey = sTmpDst;
			}
			
			sDataOut[0x00] = sTmpKey;
			
			return 0;
		}
	}
	
	private static int DesEnc(String sKey, String sDataIn, String[] sDataOut) {
		
		int iDataInLen = sDataIn.length();
		
		if (iDataInLen == 0 || iDataInLen % 0x10 != 0) {
		
			return 3103;
		}
		
		int iRet = 0;
		
		String sTmpData = "";
		
		for (int i = 0; i < iDataInLen / 0x10; i++) {
			
			String[] sDst = new String[0x01];
			
			iRet = Des(0x00, sKey, sDataIn.substring(i * 0x10, (i + 0x01) * 0x10), sDst);
			
			if (iRet != 0) {
				
				return 3104;
			}
			
			sTmpData = sTmpData + sDst[0x00];
		}
		
		sDataOut[0x00] = sTmpData;
		
		return 0;
	}
	
	private static String FormatLC(int iLen) {
		
		String sLC = Integer.toHexString(iLen);  
		
		int iChrLen = sLC.length();  
		
		if( iChrLen == 1) {
			
			return "0" + sLC;  
		}
		else {

			return sLC.substring(iChrLen - 2, iChrLen);  
		}
	}

	private static int DesEncWithPadding(String sKey, String sDataIn, String[] sDataOut) {
		
		int iDataInLen = sDataIn.length();
		
		if (iDataInLen == 0 || iDataInLen % 0x02 != 0) {
		
			return 3103;
		}
		
		int iRet = 0;
		
		String sTmpData = FormatLC(sDataIn.length() / 2) + sDataIn;
		
		String sTmpBuf = "";
		
		if (sTmpData.length() % 0x10 != 0x00) {
			
			sTmpData += "80";
		}
		
		while (sTmpData.length() % 0x10 != 0x00) {
			
			sTmpData += "00";
		}
		
		for (int i = 0; i < sTmpData.length() / 0x10; i++) {
			
			String[] sDst = new String[0x01];
			
			iRet = Des(0x00, sKey, sTmpData.substring(i * 0x10, (i + 0x01) * 0x10), sDst);
			
			if (iRet != 0) {
				
				return 3104;
			}
			
			sTmpBuf = sTmpBuf + sDst[0x00];
		}
		
		sDataOut[0x00] = sTmpBuf;
		
		return 0;
	}	
	
	private static int DesDec(String sKey, String sDataIn, String[] sDataOut) {
		
		int iDataInLen = sDataIn.length();
		
		if (iDataInLen == 0 || iDataInLen % 0x10 != 0) {
		
			return 3103;
		}
		
		int iRet = 0;
		
		String sTmpData = "";
		
		for (int i = 0; i < iDataInLen / 0x10; i++) {
			
			String[] sDst = new String[0x01];
			
			iRet = Des(0x01, sKey, sDataIn.substring(i * 0x10, (i + 0x01) * 0x10), sDst);
			
			if (iRet != 0) {
				
				return 3104;
			}
			
			sTmpData = sTmpData + sDst[0x00];
		}
		
		sDataOut[0x00] = sTmpData;
		
		return 0;
	}
	
	private static int DesMac(String sKey, String sIV, String sDataIn, String[] sDataOut) {
		
		int iKeyLen = sKey.length();
		
		if (iKeyLen != 0x10 && iKeyLen != 0x20) {
			
			return 4001;
		}
		
		String sKeyL = "";
		String sKeyR = "";
		
		if (iKeyLen == 0x10) {
		
			sKeyL = sKey;
		}
		else {
			
			sKeyL = sKey.substring(0x00, 0x10);
			
			sKeyR = sKey.substring(0x10, 0x20);
		}
		
		int iIVLen = sIV.length();
		
		if (iIVLen == 0x08) {
			
			sIV = sIV + "00000000";
		}
		else if (iIVLen == 0x10) {
				
		}
		else {
			
			return 4002;
		}
		
		sDataIn += "80";
		
		while (sDataIn.length() % 0x10 != 0x00) {
			
			sDataIn += "00";
		}
		
		String sTmpBuf = DataXor(sIV, sDataIn.substring(0x00, 0x10));
		
		String[] sDst = new String[0x01];
		
		int iRet = Des(0x00, sKeyL, sTmpBuf, sDst);
		
		if (iRet != 0) {
			
			return 4003;
		}
		
		sTmpBuf = sDst[0x00];
		
		int iCount = sDataIn.length() / 0x10;
		
		for(int i = 1; i < iCount; i++) {
			
			sTmpBuf = DataXor(sTmpBuf, sDataIn.substring(i * 0x10, (i + 1) * 0x10));
			
			iRet = Des(0x00, sKeyL, sTmpBuf, sDst);
			
			if (iRet != 0) {
				
				return 4003;
			}
			
			sTmpBuf = sDst[0x00];
			
		}
		
		if (iKeyLen == 0x20) {
			
			iRet = Des(0x01, sKeyR, sTmpBuf, sDst);
			if (iRet != 0) {
			
				return 4004;
			}
			
			sTmpBuf = sDst[0x00];
			
			iRet = Des(0x00, sKeyL, sTmpBuf, sDst);
			if (iRet != 0) {
				
				return 4005;
			}
			
			sTmpBuf = sDst[0x00];
		}
		
		sDataOut[0x00] = sDst[0x00].substring(0x00, 0x08);
		
		return 0;
	}
}
