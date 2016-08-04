package com.rthitech;
import java.util.Locale;
import android.nfc.tech.IsoDep;

public class CardLogic {
	
	private static byte toByte(char cChr) {

		byte bChr = (byte) "0123456789ABCDEF".indexOf(cChr);
		
		if (bChr == -1) {
			
			bChr = (byte) "0123456789abcdef".indexOf(cChr);
		}	
		return bChr;
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
		
		int iLen = (sAsc.length() / 2);
		
		byte[] bResult = new byte[iLen];
		
		char[] achar = sAsc.toCharArray();
		
		for (int i = 0; i < iLen; i++) {
			
			int iPos = i * 2;
			
			byte HSB = (byte) (toByte(achar[iPos]) << 4);
			
			byte LSB = (byte) (toByte(achar[iPos + 1]));
			
			bResult[i] = (byte) (HSB | LSB);
			
			bResult[i] = (byte) (toByte(achar[iPos]) << 4 | toByte(achar[iPos + 1]));
		}
		return bResult;
	}
	
	private static int CpuComm(IsoDep Nfc, String sApduSend, String[] sApduRecv) {
		
		if (sApduSend.length() % 2 != 0 || sApduSend.length() == 0) {
			
			return 1001;
		}
		
		byte[] bApduSend = new byte[1024];
		
		bApduSend = AscToHex(sApduSend);
		
		if (bApduSend.length == 0x00) {
			
			return 1002;
		}
		
		try {
			
			byte[] bApduRecv = Nfc.transceive(bApduSend);
			
			int iRecvLen = bApduRecv.length;
			
			if (iRecvLen == 0x00) {
			
				return 1003;
			}
			
			
			byte bRet = bApduRecv[iRecvLen - 2];
			
			if ( bRet != (byte)0x90) {
				
				sApduRecv[0x00] = HexToAsc(bApduRecv);
				
				return 1004;
			}
			else {
			
				sApduRecv[0x00] = HexToAsc(bApduRecv);
				
				if (iRecvLen != 0x02) {
					
					sApduRecv[0x00] = sApduRecv[0x00].substring(0x00, sApduRecv[0x00].length() - 0x04);
				}
								
				return 0;
			}
		} 
		catch (Exception e) {
			
			e.printStackTrace();
		} 
		finally {
			
		}
		
		return 0;
	}
	
	/* sCardFile[0x00] : 卡片序列号
	 * sCardFile[0x01] : 公共信息文件
	 * sCardFile[0x02] : 用户卡参数信息文件
	 * sCardFile[0x03] : 用户卡钱包文件
	 * sCardFile[0x04] : 费率1文件
	 * sCardFile[0x05] : 费率2文件
	 * sCardFile[0x06] : 用户卡返写信息文件
	 */
	public static int ReadElectricCpuCard(IsoDep Nfc, String[] sCardFile) {
		
		if (sCardFile == null || sCardFile.length < 0x07) {
			
			return 2001;
		}
		
		String[] sApduRecv = new String[0x01];
		
		int iRet = CpuComm(Nfc, "00A40000023F00", sApduRecv);
		if (iRet != 0) {
		
			return 2002;
		}
		
		iRet = CpuComm(Nfc, "00B0990108", sApduRecv);
		if (iRet != 0) {
		
			return 2003;
		}
		
		/* 保存卡片序列号*/
		sCardFile[0x00] = sApduRecv[0x00];
		
		
		iRet = CpuComm(Nfc, "00B0810027", sApduRecv);
		if (iRet != 0) {
		
			return 2003;
		}
		sCardFile[0x01] = sApduRecv[0x00];
		
		iRet = CpuComm(Nfc, "00A4000002DF01", sApduRecv);
		if (iRet != 0) {
		
			return 2004;
		}
		
		iRet = CpuComm(Nfc, "00B081002D", sApduRecv);
		if (iRet != 0) {
		
			return 2005;
		}
		/* 保存用户卡参数信息文件数据*/
		sCardFile[0x02] = sApduRecv[0x00];
		
		iRet = CpuComm(Nfc, "00B0820008", sApduRecv);
		if (iRet != 0) {
		
			return 2006;
		}
		/* 保存用户卡钱包文件*/
		sCardFile[0x03] = sApduRecv[0x00];
		
		iRet = CpuComm(Nfc, "00B0830080", sApduRecv);
		if (iRet != 0) {
		
			return 2007;
		}
		/* 保存费率1文件*/
		sCardFile[0x04] = sApduRecv[0x00];
		
		iRet = CpuComm(Nfc, "00B0838082", sApduRecv);
		if (iRet != 0) {
		
			return 2008;
		}
		/* 保存费率1文件*/
		sCardFile[0x04] = sCardFile[0x03] + sApduRecv[0x00];
		
		iRet = CpuComm(Nfc, "00B0840080", sApduRecv);
		if (iRet != 0) {
		
			return 2009;
		}
		/* 保存费率2文件*/
		sCardFile[0x05] = sApduRecv[0x00];
		
		iRet = CpuComm(Nfc, "00B0848082", sApduRecv);
		if (iRet != 0) {
		
			return 2010;
		}
		/* 保存费率2文件*/
		sCardFile[0x05] = sCardFile[0x04] + sApduRecv[0x00];
		
		iRet = CpuComm(Nfc, "00B0850031", sApduRecv);
		if (iRet != 0) {
		
			return 2011;
		}
		/* 保存用户卡返写文件*/
		sCardFile[0x06] = sApduRecv[0x00];
		
		return 0x00;
	}
	
	/* sCardFile[0x00] : 卡片序列号
	 * sCardFile[0x01] : 公共信息文件
	 * sCardFile[0x02] : 用户卡参数信息文件
	 * sCardFile[0x03] : 用户卡返写文件
	 * sCardFile[0x04] : 费率1文件
	 * sCardFile[0x05] : 阶梯文件
	 */
	public static int ReadWaterCpuCard(IsoDep Nfc, String[] sCardFile) {
		
		if (sCardFile == null || sCardFile.length < 0x06) {
			
			return 2101;
		}
		
		String[] sApduRecv = new String[0x01];
		
		int iRet = CpuComm(Nfc, "00A40000023F00", sApduRecv);
		if (iRet != 0) {
		
			return 2102;
		}
		
		iRet = CpuComm(Nfc, "00B0990108", sApduRecv);
		if (iRet != 0) {
		
			return 2103;
		}
		
		/* 保存卡片序列号*/
		sCardFile[0x00] = sApduRecv[0x00];
		
		iRet = CpuComm(Nfc, "00B0810027", sApduRecv);
		if (iRet != 0) {
		
			return 2003;
		}
		sCardFile[0x01] = sApduRecv[0x00];
		
		iRet = CpuComm(Nfc, "00A4000002DF02", sApduRecv);
		if (iRet != 0) {
		
			return 2104;
		}
		
		iRet = CpuComm(Nfc, "00B0810032", sApduRecv);
		if (iRet != 0) {
		
			return 2105;
		}
		/* 保存用户卡参数信息文件数据*/
		sCardFile[0x02] = sApduRecv[0x00];
		
		iRet = CpuComm(Nfc, "00B082007C", sApduRecv);
		if (iRet != 0) {
		
			return 2106;
		}
		/* 保存用户卡返写文件*/
		sCardFile[0x03] = sApduRecv[0x00];
		
		iRet = CpuComm(Nfc, "00B0830008", sApduRecv);
		if (iRet != 0) {
		
			return 2107;
		}
		/* 保存用户卡钱包文件*/
		sCardFile[0x04] = sApduRecv[0x00];
		
		iRet = CpuComm(Nfc, "00B099006A", sApduRecv);
		if (iRet != 0) {
		
			return 2108;
		}
		/* 保存用户卡费率文件*/
		sCardFile[0x05] = sApduRecv[0x00];
		
		return 0x00;
	}

	/* sCardFile[0x00] : 卡片序列号
	 * sCardFile[0x01] : 公共信息文件
	 * sCardFile[0x02] : 用户卡参数信息文件
	 * sCardFile[0x03] : 用户卡返写文件
	 * sCardFile[0x04] : 费率1文件
	 * sCardFile[0x05] : 阶梯文件
	 */
	public static int ReadGasCpuCard(IsoDep Nfc, String[] sCardFile) {
		
		if (sCardFile == null || sCardFile.length < 0x06) {
			
			return 2201;
		}
		
		String[] sApduRecv = new String[0x01];
		
		int iRet = CpuComm(Nfc, "00A40000023F00", sApduRecv);
		if (iRet != 0) {
		
			return 2202;
		}
		
		iRet = CpuComm(Nfc, "00B0990108", sApduRecv);
		if (iRet != 0) {
		
			return 2203;
		}
		
		/* 保存卡片序列号*/
		sCardFile[0x00] = sApduRecv[0x00];
		
		iRet = CpuComm(Nfc, "00B0810027", sApduRecv);
		if (iRet != 0) {
		
			return 2203;
		}
		sCardFile[0x01] = sApduRecv[0x00];
		
		iRet = CpuComm(Nfc, "00A4000002DF03", sApduRecv);
		if (iRet != 0) {
		
			return 2204;
		}
		
		iRet = CpuComm(Nfc, "00B0810032", sApduRecv);
		if (iRet != 0) {
		
			return 2205;
		}
		/* 保存用户卡参数信息文件数据*/
		sCardFile[0x02] = sApduRecv[0x00];
		
		iRet = CpuComm(Nfc, "00B082007C", sApduRecv);
		if (iRet != 0) {
		
			return 2206;
		}
		/* 保存用户卡返写文件*/
		sCardFile[0x03] = sApduRecv[0x00];
		
		iRet = CpuComm(Nfc, "00B0830008", sApduRecv);
		if (iRet != 0) {
		
			return 2207;
		}
		/* 保存用户卡钱包文件*/
		sCardFile[0x04] = sApduRecv[0x00];
		
		iRet = CpuComm(Nfc, "00B087006A", sApduRecv);
		if (iRet != 0) {
		
			return 2208;
		}
		/* 保存用户卡费率文件*/
		sCardFile[0x05] = sApduRecv[0x00];
		
		return 0x00;
	}
	
	/* sMFFile : 公共信息文件
	 * sFile1 : 用户卡参数信息文件文件
	 * sFile2 : 用户卡钱包文件
	 * sFile3 : 费率1文件
	 * sFile4 : 费率2文件
	 * sFile5 : 返写文件
	 */
	public static int WriteElectricCpuCard(IsoDep Nfc, String sMFFile, String sFile1, String sFile2, String sFile3, String sFile4, String sFile5) {
		
		int iRet = 0;
		
		int iMFFileLen = (sMFFile == null ? 0 : sMFFile.length());
		int iFile1Len = (sFile1 == null ? 0 : sFile1.length());
		int iFile2Len = (sFile2 == null ? 0 : sFile2.length());
		int iFile3Len = (sFile3 == null ? 0 : sFile3.length());
		int iFile4Len = (sFile4 == null ? 0 : sFile4.length());
		int iFile5Len = (sFile5 == null ? 0 : sFile5.length());
		
		if (iMFFileLen != 0x00 && iMFFileLen != 0x27 * 2) {
			
			return 5000;
		}
		
		if (iFile1Len != 0x00 && iFile1Len != 0x2D * 2) {
		
			return 5000;
		}
		
		if (iFile2Len != 0x08 * 2) {
			
			return 5000;
		}
		
		if (iFile3Len != 0x00 && iFile3Len != 0x102 * 2) {
			
			return 5000;
		}
		
		if (iFile4Len != 0x00 && iFile4Len != 0x102 * 2) {
			
			return 5000;
		}
		
		if (iFile5Len != 0x00 && iFile5Len != 0x31 * 2) {
			
			return 5000;
		}
		
		String[] sApduRecv = new String[0x01];
		
		iRet = CpuComm(Nfc, "00A40000023F00", sApduRecv);
		if (iRet != 0) {
			
			return 5001;
		}
		
		iRet = CpuComm(Nfc, "00B0990108", sApduRecv);
		if (iRet != 0) {
			
			return 5002;
		}
		
		iRet = CpuComm(Nfc, "00A4000002DF01", sApduRecv);
		if (iRet != 0) {
			
			return 5003;
		}
		
		iRet = CpuComm(Nfc, "0084000008", sApduRecv);
		if (iRet != 0) {
			
			return 5004;
		}
		
		String sRandom = sApduRecv[0x00];
		
		iRet = CpuComm(Nfc, "0088000108" + sRandom, sApduRecv);
		if (iRet != 0) {
			
			return 5005;
		}
		
		String Enc1 = sApduRecv[0x00];
		
		iRet = DesInterface.iSymmInterface(0x00, "FF2C95243FF745A3BABA96321C62AAE6", "", "", sRandom, sApduRecv);
		if (iRet != 0) {
			
			return 5006;
		}
		
		String Enc2 = sApduRecv[0x00];
		
		if (!Enc1.equals(Enc2)) {
			
			return 5007;
		}
		
		iRet = CpuComm(Nfc, "0084000008", sApduRecv);
		if (iRet != 0) {
			
			return 5008;
		}
		
		sRandom = sApduRecv[0x00];
		
		iRet = DesInterface.iSymmInterface(0x00, "D7ED3B5ABFCF59A874DCE8B2AB8F132D", "", "", sRandom, sApduRecv);
		if (iRet != 0) {
			
			return 5009;
		}
		
		String Enc = sApduRecv[0x00];
		
		iRet = CpuComm(Nfc, "0082000208" + Enc, sApduRecv);
		if (iRet != 0) {
			
			return 5010;
		}
		
		iRet = CpuComm(Nfc, "0084000004", sApduRecv);
		if (iRet != 0) {
			
			return 5011;
		}
		
		sRandom = sApduRecv[0x00];
		
		String sMac;
		
		if (iFile1Len != 0x00) {
			
			iRet = DesInterface.iSymmInterface(0x05, "FF2C95243FF745A3BABA96321C62AAE6", sRandom, "", "04D6810031" + sFile1, sApduRecv);
			if (iRet != 0) {
				
				return 5012;
			}
			
			sMac = sApduRecv[0x00];
			
			iRet = CpuComm(Nfc, "04d6810031" + sFile1 + sMac, sApduRecv);
			if (iRet != 0) {
				
				return 5013;
			}
		}
		
		if (iFile2Len != 0x00) {
			
			iRet = DesInterface.iSymmInterface(0x05, "FF2C95243FF745A3BABA96321C62AAE6", sRandom, "", "04D682000C" + sFile2, sApduRecv);
			if (iRet != 0) {
				
				return 5014;
			}
			
			sMac = sApduRecv[0x00];
			
			iRet = CpuComm(Nfc, "04D682000C" + sFile2 + sMac, sApduRecv);
			if (iRet != 0) {
				
				return 5015;
			}
		}
		
		if (iFile3Len != 0x00) {
			
			/* 写第一段*/
			String sTMpBuf = sFile3.substring(0x00, 0x80 * 2);
			
			iRet = DesInterface.iSymmInterface(0x05, "FF2C95243FF745A3BABA96321C62AAE6", sRandom, "", "04D6830084" + sTMpBuf, sApduRecv);
			if (iRet != 0) {
				
				return 5016;
			}
			
			sMac = sApduRecv[0x00];
			
			iRet = CpuComm(Nfc, "04D6830084" + sTMpBuf + sMac, sApduRecv);
			if (iRet != 0) {
				
				return 5017;
			}
			
			/* 写第二段*/
			sTMpBuf = sFile3.substring(0x80 * 2);
			
			iRet = DesInterface.iSymmInterface(0x05, "FF2C95243FF745A3BABA96321C62AAE6", sRandom, "", "04D6838086" + sTMpBuf, sApduRecv);
			if (iRet != 0) {
				
				return 5018;
			}
			
			sMac = sApduRecv[0x00];
			
			iRet = CpuComm(Nfc, "04D6838086" + sTMpBuf + sMac, sApduRecv);
			if (iRet != 0) {
				
				return 5019;
			}
		}
		
		if (iFile4Len != 0x00) {
			
			/* 写第一段*/
			String sTMpBuf = sFile4.substring(0x00, 0x80 * 2);
			
			iRet = DesInterface.iSymmInterface(0x05, "FF2C95243FF745A3BABA96321C62AAE6", sRandom, "", "04D6840084" + sTMpBuf, sApduRecv);
			if (iRet != 0) {
				
				return 5020;
			}
			
			sMac = sApduRecv[0x00];
			
			iRet = CpuComm(Nfc, "04D6840084" + sTMpBuf + sMac, sApduRecv);
			if (iRet != 0) {
				
				return 5021;
			}
			
			/* 写第二段*/
			sTMpBuf = sFile4.substring(0x80 * 2);
			
			iRet = DesInterface.iSymmInterface(0x05, "FF2C95243FF745A3BABA96321C62AAE6", sRandom, "", "04D6848086" + sTMpBuf, sApduRecv);
			if (iRet != 0) {
				
				return 5022;
			}
			
			sMac = sApduRecv[0x00];
			
			iRet = CpuComm(Nfc, "04D6848086" + sTMpBuf + sMac, sApduRecv);
			if (iRet != 0) {
				
				return 5023;
			}
		}
		
		if (iFile5Len != 0x00) {
			
			iRet = DesInterface.iSymmInterface(0x05, "FF2C95243FF745A3BABA96321C62AAE6", sRandom, "", "04D6850035" + sFile5, sApduRecv);
			if (iRet != 0) {
				
				return 5024;
			}
			
			sMac = sApduRecv[0x00];
			
			iRet = CpuComm(Nfc, "04D6850035" + sFile5+ sMac, sApduRecv);
			if (iRet != 0) {
				
				return 5025;
			}
		}
		
		if (iMFFileLen != 0x00) {
			
			iRet = CpuComm(Nfc, "00A40000023F00", sApduRecv);
			if (iRet != 0) {
				
				return 5026;
			}
			
			iRet = CpuComm(Nfc, "00D6810027" + sMFFile, sApduRecv);
			if (iRet != 0) {
				
				return 5027;
			}
		}

		return 0;
	}
	
	/* sMFFile : 公共信息文件
	 * sFile1 : 用户卡参数信息文件文件
	 * sFile2 : 用户卡返写文件
	 * sFile3 : 用户卡钱包文件
	 * sFile4 : 用户卡阶梯文件
	 */
	public static int WriteWaterCpuCard(IsoDep Nfc, String sMFFile, String sFile1, String sFile2, String sFile3, String sFile4) {
		
		int iRet = 0;
		
		int iMFFileLen = (sMFFile == null ? 0 : sMFFile.length());
		int iFile1Len = (sFile1 == null ? 0 : sFile1.length());
		int iFile2Len = (sFile2 == null ? 0 : sFile2.length());
		int iFile3Len = (sFile3 == null ? 0 : sFile3.length());
		int iFile4Len = (sFile4 == null ? 0 : sFile4.length());
		
		if (iMFFileLen != 0x00 && iMFFileLen != 0x27 * 2) {
			
			return 5100;
		}
		
		if (iFile1Len != 0x00 && iFile1Len != 0x32 * 2) {
		
			return 5100;
		}
		
		if (iFile2Len != 0x00 && iFile2Len != 0x7C * 2) {
			
			return 5100;
		}
		
		if (iFile3Len != 0x08 * 2) {
			
			return 5100;
		}
		
		if (iFile4Len != 0x00 && iFile4Len != 0x6A * 2) {
			
			return 5100;
		}
		
		String[] sApduRecv = new String[0x01];
		
		iRet = CpuComm(Nfc, "00A40000023F00", sApduRecv);
		if (iRet != 0) {
			
			return 5101;
		}
		
		iRet = CpuComm(Nfc, "00B0990108", sApduRecv);
		if (iRet != 0) {
			
			return 5102;
		}
		
		iRet = CpuComm(Nfc, "00A4000002DF02", sApduRecv);
		if (iRet != 0) {
			
			return 5103;
		}
		
		iRet = CpuComm(Nfc, "0084000008", sApduRecv);
		if (iRet != 0) {
			
			return 5104;
		}
		
		String sRandom = sApduRecv[0x00];
		
		iRet = CpuComm(Nfc, "0088000108" + sRandom, sApduRecv);
		if (iRet != 0) {
			
			return 5105;
		}
		
		String Enc1 = sApduRecv[0x00];
		
		iRet = DesInterface.iSymmInterface(0x00, "454862D5CDFA2C33923AB927DAA8567F", "", "", sRandom, sApduRecv);
		if (iRet != 0) {
			
			return 5106;
		}
		
		String Enc2 = sApduRecv[0x00];
		
		if (!Enc1.equals(Enc2)) {
			
			return 5107;
		}
		
		iRet = CpuComm(Nfc, "0084000004", sApduRecv);
		if (iRet != 0) {
			
			return 5108;
		}
		
		sRandom = sApduRecv[0x00];
		
		String sMac;
		
		if (iFile1Len != 0x00) {
			
			iRet = DesInterface.iSymmInterface(0x05, "A4ABCC07BA92F9874918AA3AF09B632B", sRandom, "", "04D6810036" + sFile1, sApduRecv);
			if (iRet != 0) {
				
				return 5109;
			}
			
			sMac = sApduRecv[0x00];
			
			iRet = CpuComm(Nfc, "04d6810036" + sFile1 + sMac, sApduRecv);
			if (iRet != 0) {
				
				return 5110;
			}
		}
		
		if (iFile2Len != 0x00) {
			
			iRet = DesInterface.iSymmInterface(0x05, "A4ABCC07BA92F9874918AA3AF09B632B", sRandom, "", "04D6820080" + sFile2, sApduRecv);
			if (iRet != 0) {
				
				return 5111;
			}
			
			sMac = sApduRecv[0x00];
			
			iRet = CpuComm(Nfc, "04D6820080" + sFile2 + sMac, sApduRecv);
			if (iRet != 0) {
				
				return 5112;
			}
		}
		
		if (iFile3Len != 0x00) {
			
			iRet = DesInterface.iSymmInterface(0x05, "A4ABCC07BA92F9874918AA3AF09B632B", sRandom, "", "04D683000C" + sFile3, sApduRecv);
			if (iRet != 0) {
				
				return 5113;
			}
			
			sMac = sApduRecv[0x00];
			
			iRet = CpuComm(Nfc, "04D683000C" + sFile3 + sMac, sApduRecv);
			if (iRet != 0) {
				
				return 5114;
			}
		}
		
		if (iFile4Len != 0x00) {
			
			iRet = DesInterface.iSymmInterface(0x05, "A4ABCC07BA92F9874918AA3AF09B632B", sRandom, "", "04D699006E" + sFile4, sApduRecv);
			if (iRet != 0) {
				
				return 5115;
			}
			
			sMac = sApduRecv[0x00];
			
			iRet = CpuComm(Nfc, "04D699006E" + sFile4 + sMac, sApduRecv);
			if (iRet != 0) {
				
				return 5116;
			}
		}
		
		if (iMFFileLen != 0x00) {
			
			iRet = CpuComm(Nfc, "00A40000023F00", sApduRecv);
			if (iRet != 0) {
				
				return 5117;
			}
			
			iRet = CpuComm(Nfc, "00D6810027" + sMFFile, sApduRecv);
			if (iRet != 0) {
				
				return 5118;
			}
		}

		return 0;
	}
	
	/* sMFFile : 公共信息文件
	 * sFile1 : 用户卡参数信息文件文件
	 * sFile2 : 用户卡返写文件
	 * sFile3 : 用户卡钱包文件
	 * sFile4 : 用户卡阶梯文件
	 */
	public static int WriteGasCpuCard(IsoDep Nfc, String sMFFile, String sFile1, String sFile2, String sFile3, String sFile4) {
		
		int iRet = 0;
		
		int iMFFileLen = (sMFFile == null ? 0 : sMFFile.length());
		int iFile1Len = (sFile1 == null ? 0 : sFile1.length());
		int iFile2Len = (sFile2 == null ? 0 : sFile2.length());
		int iFile3Len = (sFile3 == null ? 0 : sFile3.length());
		int iFile4Len = (sFile4 == null ? 0 : sFile4.length());
		
		if (iMFFileLen != 0x00 && iMFFileLen != 0x27 * 2) {
			
			return 5200;
		}
		
		if (iFile1Len != 0x00 && iFile1Len != 0x32 * 2) {
		
			return 5200;
		}
		
		if (iFile2Len != 0x00 && iFile2Len != 0x7C * 2) {
			
			return 5200;
		}
		
		if (iFile3Len != 0x08 * 2) {
			
			return 5200;
		}
		
		if (iFile4Len != 0x00 && iFile4Len != 0x6A * 2) {
			
			return 5200;
		}
		
		String[] sApduRecv = new String[0x01];
		
		iRet = CpuComm(Nfc, "00A40000023F00", sApduRecv);
		if (iRet != 0) {
			
			return 5201;
		}
		
		iRet = CpuComm(Nfc, "00B0990108", sApduRecv);
		if (iRet != 0) {
			
			return 5202;
		}
		
		iRet = CpuComm(Nfc, "00A4000002DF03", sApduRecv);
		if (iRet != 0) {
			
			return 5203;
		}
		
		iRet = CpuComm(Nfc, "0084000008", sApduRecv);
		if (iRet != 0) {
			
			return 5204;
		}
		
		String sRandom = sApduRecv[0x00];
		
		iRet = CpuComm(Nfc, "0088000108" + sRandom, sApduRecv);
		if (iRet != 0) {
			
			return 5205;
		}
		
		String Enc1 = sApduRecv[0x00];
		
		iRet = DesInterface.iSymmInterface(0x00, "454862D5CDFA2C33923AB927DAA8567F", "", "", sRandom, sApduRecv);
		if (iRet != 0) {
			
			return 5206;
		}
		
		String Enc2 = sApduRecv[0x00];
		
		if (!Enc1.equals(Enc2)) {
			
			return 5207;
		}
		
		iRet = CpuComm(Nfc, "0084000004", sApduRecv);
		if (iRet != 0) {
			
			return 5208;
		}
		
		sRandom = sApduRecv[0x00];
		
		String sMac;
		
		if (iFile1Len != 0x00) {
			
			iRet = DesInterface.iSymmInterface(0x05, "A4ABCC07BA92F9874918AA3AF09B632B", sRandom, "", "04D6810036" + sFile1, sApduRecv);
			if (iRet != 0) {
				
				return 5209;
			}
			
			sMac = sApduRecv[0x00];
			
			iRet = CpuComm(Nfc, "04d6810036" + sFile1 + sMac, sApduRecv);
			if (iRet != 0) {
				
				return 5210;
			}
		}
		
		if (iFile2Len != 0x00) {
			
			iRet = DesInterface.iSymmInterface(0x05, "A4ABCC07BA92F9874918AA3AF09B632B", sRandom, "", "04D6820080" + sFile2, sApduRecv);
			if (iRet != 0) {
				
				return 5211;
			}
			
			sMac = sApduRecv[0x00];
			
			iRet = CpuComm(Nfc, "04D6820080" + sFile2 + sMac, sApduRecv);
			if (iRet != 0) {
				
				return 5212;
			}
		}
		
		if (iFile3Len != 0x00) {
			
			iRet = DesInterface.iSymmInterface(0x05, "A4ABCC07BA92F9874918AA3AF09B632B", sRandom, "", "04D683000C" + sFile3, sApduRecv);
			if (iRet != 0) {
				
				return 5213;
			}
			
			sMac = sApduRecv[0x00];
			
			iRet = CpuComm(Nfc, "04D683000C" + sFile3 + sMac, sApduRecv);
			if (iRet != 0) {
				
				return 5214;
			}
		}
		
		if (iFile4Len != 0x00) {
			
			iRet = DesInterface.iSymmInterface(0x05, "A4ABCC07BA92F9874918AA3AF09B632B", sRandom, "", "04D687006E" + sFile4, sApduRecv);
			if (iRet != 0) {
				
				return 5215;
			}
			
			sMac = sApduRecv[0x00];
			
			iRet = CpuComm(Nfc, "04D687006E" + sFile4 + sMac, sApduRecv);
			if (iRet != 0) {
				
				return 5216;
			}
		}
		
		if (iMFFileLen != 0x00) {
			
			iRet = CpuComm(Nfc, "00A40000023F00", sApduRecv);
			if (iRet != 0) {
				
				return 5217;
			}
			
			iRet = CpuComm(Nfc, "00D6810027" + sMFFile, sApduRecv);
			if (iRet != 0) {
				
				return 5218;
			}
		}

		return 0;
	}
}
