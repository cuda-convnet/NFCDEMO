package com.rthitech;

/**
 * @author weixin E-mail:weixin@rthitech.com.cn
 * @date ����ʱ�䣺2015-12-16 ����4:06:19
 * @version 1.0
 */

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private List<Map<String, Object>> mData;
	private int flag;
	private Button bntNfc;
	private ProgressDialog dialog;
	private Handler mainHandler;
	public String Buymoney;// ������
	public String BuyTimes;// �������
	public String BuyType;// ��������
	public static String title[] = new String[] { "����", "ˮ��", "���", "����" };
	public static String money[] = new String[] { "-", "-", "-", "" };
	public static String times[] = new String[] { "-", "-", "-", "-" };
	public static String status[] = new String[] { "-", "-", "-", "-" };
	public String File1_water; // ˮ��Ӧ���ļ�1
	public String File1_gas;// ����Ӧ���ļ�1
	public String File1_electric;// ���Ӧ���ļ�1
	public String Money_water; // ˮ��Ǯ��
	public String Money_gas; // ����Ǯ��
	public String Money_electric; // ���Ǯ���ļ�
	public String File_water_F = "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"; // ˮ��д�ļ�
	public String File_gas_F = "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"; // ����д�ļ�
	public String File_electric_F = "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";// ���д�ļ�
	public int WriterFlag = 0;// д����ʶ 0-��д��1-дˮ��2-д�磻3-д��
	public String WriterMoney_electric;// д��Ǯ���ļ�
	public String WriterMoney_Gas;// д��Ǯ���ļ�
	public String WriterMoney_Water;// дˮǮ���ļ�
	public String WriterFile1_electric;// д������ļ�
	public String WriterFile1_Gas;// д��������ļ�
	public String WriterFile1_Water;// дˮ�����ļ�

	NfcAdapter nfcAdapter;
	PendingIntent pendingIntent;
	IntentFilter ndef;
	IntentFilter[] intentFiltersArray;
	String[][] techListsArray;

	private CardLogic server;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mData = getData();
		ListView listView = (ListView) findViewById(R.id.listView);
		MyAdapter adapter = new MyAdapter(this);
		listView.setAdapter(adapter);
		mainHandler = new Handler();
		TextView cardid = (TextView) findViewById(R.id.cardid);
		cardid.setText("��Ƭ���к�:" + "-");
		cardid.setTextColor(android.graphics.Color.BLUE);

		// ��ȡĬ�ϵ�NFC������
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (nfcAdapter == null) {
			Toast.makeText(this, "�豸��֧��NFC��", Toast.LENGTH_LONG).show();
			// finish();
			// return;
		}
		if (!nfcAdapter.isEnabled()) {
			Toast.makeText(this, "����ϵͳ������������NFC���ܣ�", Toast.LENGTH_LONG).show();
			// finish();
			// return;
		}

		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
		try {
			ndef.addDataType("*/*");
		} catch (MalformedMimeTypeException e) {
			throw new RuntimeException("fail", e);
		}
		intentFiltersArray = new IntentFilter[] { ndef, };
		techListsArray = new String[][] { new String[] { IsoDep.class.getName() } };

		// ��Ӱ��� ��ť����
		Button btn_help = (Button) findViewById(R.id.btnhelp);
		btn_help.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showToast_help();
			}
		});

		// ��Ӱ���NFC ��ť����
		Button btn_nfc = (Button) findViewById(R.id.btnNfc);
		btn_nfc.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivityForResult(new Intent(
						android.provider.Settings.ACTION_WIRELESS_SETTINGS), 0);
			}
		});

		// ��Ӱ����˳� ��ť����
		Button btn_exit = (Button) findViewById(R.id.btnExit);
		btn_exit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		nfcAdapter.enableForegroundDispatch(this, pendingIntent,
				intentFiltersArray, techListsArray);

		refreshStatus();
	}

	@Override
	public void onPause() {
		super.onPause();
		nfcAdapter.disableForegroundDispatch(this);

	}

	public void onNewIntent(Intent intent) {
		Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		processIntent(intent);
	}

	@SuppressWarnings("static-access")
	private void processIntent(Intent intent) {
		// ȡ����װ��intent�е�TAG
		Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		for (String tech : tagFromIntent.getTechList()) {
			System.out.println(tech);
		}

		IsoDep mfc = IsoDep.get(tagFromIntent);

		// ��Ƭ������
		try {
			mfc.connect();
			 String[] sCardFile_Water = new String[10];
			 String[] sCardFile_Electric = new String[10];
			 String[] sCardFile_Gas = new String[10];
			 int run = 0;
		
			 if (WriterFlag != 0) {
			 // д��
			 try {
			 // ˮд��
			 if (WriterFlag == 1) {
			 run = server.WriteWaterCpuCard(mfc, "",
			 WriterFile1_Water, File_water_F,
			 WriterMoney_Water, "");
			 }
			 // ��д��
			 if (WriterFlag == 2) {
			 run = server.WriteElectricCpuCard(mfc, "",
			 WriterFile1_electric, WriterMoney_electric, "",
			 "", File_electric_F);
			 }
			 // ��д��
			 if (WriterFlag == 3) {
			 run = server.WriteGasCpuCard(mfc, "",
			 WriterFile1_Gas, File_gas_F, WriterMoney_Gas,
			 "");
			 }
			 } catch (Exception e) {
			 showToast("������룺" + run);
			 }
			
			 if (run != 0) {
			 showToast("������룺" + run);
			 return;
			 } else {
			
			 updateUi(WriterFlag);
			
			 WriterFlag = 0;
			 }
			
			 } else {
			 // ��ȡˮ���ļ���Ϣ
			 run = server.ReadWaterCpuCard(mfc, sCardFile_Water);
			 if (run == 0) {
			 File1_water = sCardFile_Water[2];
			 Money_water = sCardFile_Water[4];
			 money[1] = String.valueOf(Integer.parseInt(
			 Money_water.substring(0, 8), 16) / 10000);
			 times[1] = String.valueOf(Integer.parseInt(
			 Money_water.substring(9, 16), 16));
			 if (sCardFile_Water[3].equals(File_water_F)) {
			 status[1] = "δ���";
			 } else {
			 status[1] = "�Ѳ��";
			 }
			
			 } else {
			 showToast("������룺  " + run);
			 return;
			 }
			
			 // ��ȡ����ļ���Ϣ
			 run = server.ReadElectricCpuCard(mfc, sCardFile_Electric);
			 if (run == 0) {
			 File1_electric = sCardFile_Electric[2];
			 Money_electric = sCardFile_Electric[3];
			 money[2] = String.valueOf(Integer.parseInt(
			 Money_electric.substring(0, 8), 16) / 100);
			 times[2] = String.valueOf(Integer.parseInt(
			 Money_electric.substring(9, 16), 16));
			 if (sCardFile_Electric[6].equals(File_electric_F)) {
			 status[2] = "δ���";
			 } else {
			 status[2] = "�Ѳ��";
			 }
			 } else {
			 showToast("������룺  " + run);
			 return;
			 }
			
			 // ��ȡ�����ļ���Ϣ
			 run = server.ReadGasCpuCard(mfc, sCardFile_Gas);
			 if (run == 0) {
			 File1_gas = sCardFile_Gas[2];
			 Money_gas = sCardFile_Gas[4];
			 money[3] = String.valueOf(Integer.parseInt(
			 Money_gas.substring(0, 8), 16) / 10000);
			 times[3] = String.valueOf(Integer.parseInt(
			 Money_gas.substring(9, 16), 16));
			 if (sCardFile_Gas[3].equals(File_gas_F)) {
			 status[3] = "δ���";
			 } else {
			 status[3] = "�Ѳ��";
			 }
			 } else {
			 showToast("������룺  " + run);
			 return;
			 }
			
			 // ���÷�����
			 vibrator();
			 // ����UI
			 refreshed(sCardFile_Water[0]);

			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("tag", e.toString());
		} finally {
		}

	}

	private void vibrator() {
		/*
		 * �������𶯴�С����ͨ���ı�pattern���趨���������ʱ��̫�̣���Ч�����ܸо�����
		 */
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		long[] pattern = { 10, 400 }; // ֹͣ ���� ֹͣ ����
		vibrator.vibrate(pattern, -1); // �ظ����������pattern ���ֻ����һ�Σ�index��Ϊ-1
	}

	private void refreshed(String cardnum) {

		TextView cardid = (TextView) findViewById(R.id.cardid);
		cardid.setText("��Ƭ���к�:");
		cardid.setTextColor(android.graphics.Color.BLUE);
		cardid.setText("��Ƭ���к�:" + cardnum);
		mData = getData();
		ListView listView = (ListView) findViewById(R.id.listView);
		MyAdapter adapter = new MyAdapter(this);
		listView.setAdapter(adapter);

	}

	private void showToast_help() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("����");
		builder.setMessage(Html
				.fromHtml("<div>1.ʹ��ǰ����ȷ��NFC�������Ѿ����ã�����ͨ�������á���ť��ϵͳ���öԻ�����е�������</div><div>2.����ʱ�뽫��Ƭ�����ֻ�NFC��������һ�������ֻ���ǣ��������������֣��Ա�����ȡ��Ϣ��</div><p /><div>3.ĿǰAndroidϵͳ��NFCӲ������Щ��Ƭ�ļ����Բ��Ǻܺã�����ʧ���ʽϸߣ���Ҫ������ȡ���б�Ҫ��ʱ���볢�Թرմ�����Ӳ����Ȼ���������á�</div><p />"));
		builder.setPositiveButton("�˳�", null);
		builder.show();

	}

	// ��ȡ��̬�������� �����������ط�����(json��)
	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < title.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("title", title[i]);
			map.put("money", "��" + money[i]);
			map.put("times", times[i]);
			map.put("status", status[i]);
			list.add(map);
		}

		return list;
	}

	public class MyAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public MyAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		// ע��ԭ��getView�����е�int position�����Ƿ�final�ģ����ڸ�Ϊfinal
		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {

				holder = new ViewHolder();

				// �������Ϊ��vlist��ȡview ֮���view���ظ�ListView

				convertView = mInflater.inflate(R.layout.vlist, null);
				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.money = (TextView) convertView.findViewById(R.id.money);
				holder.status = (TextView) convertView
						.findViewById(R.id.status);
				holder.viewBtn = (Button) convertView
						.findViewById(R.id.view_btn);
				holder.viewbuy = (Button) convertView
						.findViewById(R.id.view_buy);
				convertView.setTag(holder);

			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.title.setText((String) mData.get(position).get("title"));
			holder.money.setText((String) mData.get(position).get("money"));
			holder.status.setText((String) mData.get(position).get("status"));

			holder.viewBtn.setTag(position);
			holder.viewbuy.setTag(position);
			// ��Button��ӵ����¼� ���Button֮��ListView��ʧȥ���� ��Ҫ��ֱ�Ӱ�Button�Ľ���ȥ��
			holder.viewBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (money[position] == "-") {
						showToast("���ȶ���");
						return;
					}
					showInfo(position);
				}
			});
			holder.viewbuy.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					if (status[position] == "-") {
						showToast("���ȶ���");
						return;
					}

					if (status[position] == "δ���") {
						showToast("����ǰ���Ȳ��");
						return;
					}
					showBuy(position);
				}
			});
			return convertView;
		}

	}

	private void refreshStatus() {

		final String tip;
		if (nfcAdapter == null)
			tip = "δ����NFC�豸";
		else if (nfcAdapter.isEnabled())
			tip = "NFC�豸����";
		else
			tip = "NFC�豸����";
		setTitle("һ��ͨ��ֵApp---" + tip);

	}

	private void UserBuy(int i) throws Exception {
		showpProgressDialog();

		if (i == 0) {
			// ����д��
			new Thread(new WriterCard_transit()).start();
		}
		if (i == 1) {
			// ˮд��
			new Thread(new WriterCard_water()).start();
		}
		if (i == 2) {
			// ��д��
			new Thread(new WriterCard_electric()).start();
		}
		if (i == 3) {
			// ��д��
			new Thread(new WriterCard_gas()).start();
		}
	}

	public class WriterCard_transit implements Runnable {
		public void run() {
			// ����д������

		}
	}

	public class WriterCard_water implements Runnable {
		public void run() {
			final String CRC;
			final String file_temp;
			file_temp = File1_water.substring(2, 34) + "02"
					+ File1_water.substring(36, File1_water.length() - 4);
			CRC = Utils.hexAddSum(file_temp);
			WriterFile1_Water = "68" + file_temp + CRC + "16";

			WriterMoney_Water = Utils.addZeroForNum(
					Integer.toHexString(Integer.parseInt(Buymoney) * 10000), 8)
					+ Utils.addZeroForNum(
							Integer.toHexString(Integer.parseInt(BuyTimes) + 1),
							8);
			WriterFlag = 1;
		}
	}

	public class WriterCard_electric implements Runnable {
		public void run() {
			// ����д������
			final String CRC;
			final String file_temp;
			file_temp = File1_electric.substring(2,
					(File1_electric.length() - 6)) + "02";
			CRC = Utils.hexAddSum(file_temp);
			WriterFile1_electric = File1_electric.substring(0,
					File1_electric.length() - 6)
					+ "02" + CRC + "16";
			WriterMoney_electric = Utils.addZeroForNum(
					Integer.toHexString(Integer.parseInt(Buymoney) * 100), 8)
					+ Utils.addZeroForNum(
							Integer.toHexString(Integer.parseInt(BuyTimes) + 1),
							8);
			WriterFlag = 2;

		}
	}

	class WriterCard_gas implements Runnable {
		public void run() {
			final String CRC;
			final String file_temp;
			file_temp = File1_gas.substring(2, 34) + "02"
					+ File1_gas.substring(36, File1_gas.length() - 4);
			CRC = Utils.hexAddSum(file_temp);
			WriterFile1_Gas = "68" + file_temp + CRC + "16";

			WriterMoney_Gas = Utils.addZeroForNum(
					Integer.toHexString(Integer.parseInt(Buymoney) * 10000), 8)
					+ Utils.addZeroForNum(
							Integer.toHexString(Integer.parseInt(BuyTimes) + 1),
							8);
			WriterFlag = 3;

		}
	}

	private void updateUi(int i) {
		dialog.dismiss();
		showSuccess("��ֵ���ͣ�" + BuyType + "<br>" + "��ֵ��" + Buymoney + "<br>"
				+ "��ֵ������" + (Integer.parseInt(BuyTimes) + 1));
		times[i] = String.valueOf(Integer.parseInt(BuyTimes) + 1);
		money[i] = Buymoney;
		status[i] = "δ���";
		mData = getData();
		ListView listView = (ListView) findViewById(R.id.listView);
		MyAdapter adapter = new MyAdapter(this);
		listView.setAdapter(adapter);
	}

	private void showToast(String data) {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("��ʾ��Ϣ");
		builder.setMessage(Html.fromHtml(data.toString()));
		builder.setPositiveButton("ȷ��", null);
		builder.show();
	}

	private void showSuccess(String data) {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("��ֵ�ɹ�");
		builder.setMessage(Html.fromHtml(data.toString()));
		builder.setPositiveButton("ȷ��", null);
		builder.show();
	}

	private void showpProgressDialog() throws Exception {
		dialog = new ProgressDialog(this);
		// ���ý�������񣬷��ΪԲ�Σ���ת��
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		// ����ProgressDialog ����
		dialog.setTitle("��ֵ����");
		// ����ProgressDialog ��ʾ��Ϣ
		dialog.setMessage(Html.fromHtml("�뽫��Ƭ����NFC��Ӧ��"));
		// ����ProgressDialog ����ͼ��
		dialog.setCancelable(false);
		dialog.show();
	}

	// ��ȡ���������
	public final class ViewHolder {
		public TextView title;
		public TextView money;
		public TextView times;
		public TextView status;
		public Button viewBtn;
		public Button viewbuy;
	}

	public void showInfo(int position) {
		new AlertDialog.Builder(this)
				.setTitle(title[position] + "����")
				.setMessage(
						"��ֵ������" + times[position] + "   ��ֵ���:  ��"
								+ money[position])
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).show();
	}

	public void showBuy(final int position) {
		LayoutInflater flater = getLayoutInflater();
		View view = flater.inflate(R.layout.self_layout,
				(ViewGroup) findViewById(R.id.dialogbuy));
		final EditText ed_times = (EditText) view.findViewById(R.id.times);
		final EditText ed_money = (EditText) view.findViewById(R.id.money);
		ed_money.setFocusable(true);
		ed_money.setFocusableInTouchMode(true);
		ed_money.requestFocus();
		// ��ȡ�������
		ed_times.setText(times[position]);
		new AlertDialog.Builder(this).setTitle(title[position] + "��ֵ")
				.setView(view)
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (ed_money.getText().toString().equals("")) {
							showToast("�����빺����");
							return;
						}
						// ��ֵ
						Buymoney = ed_money.getText().toString();
						BuyTimes = ed_times.getText().toString();
						BuyType = title[position];
						try {
							// ���ó�ֵ�ӿ�
							UserBuy(position);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				})
				.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

					}
				}).show();
	}
}
