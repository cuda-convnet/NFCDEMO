package com.rthitech;

/**
 * @author weixin E-mail:weixin@rthitech.com.cn
 * @date 创建时间：2015-12-16 下午4:06:19
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
	public String Buymoney;// 购买金额
	public String BuyTimes;// 购买次数
	public String BuyType;// 购买类型
	public static String title[] = new String[] { "公交", "水表", "电表", "气表" };
	public static String money[] = new String[] { "-", "-", "-", "" };
	public static String times[] = new String[] { "-", "-", "-", "-" };
	public static String status[] = new String[] { "-", "-", "-", "-" };
	public String File1_water; // 水表应用文件1
	public String File1_gas;// 气表应用文件1
	public String File1_electric;// 电表应用文件1
	public String Money_water; // 水表钱包
	public String Money_gas; // 气表钱包
	public String Money_electric; // 电表钱包文件
	public String File_water_F = "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"; // 水表返写文件
	public String File_gas_F = "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"; // 气表返写文件
	public String File_electric_F = "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";// 电表返写文件
	public int WriterFlag = 0;// 写卡标识 0-不写；1-写水；2-写电；3-写气
	public String WriterMoney_electric;// 写电钱包文件
	public String WriterMoney_Gas;// 写气钱包文件
	public String WriterMoney_Water;// 写水钱包文件
	public String WriterFile1_electric;// 写电参数文件
	public String WriterFile1_Gas;// 写气电参数文件
	public String WriterFile1_Water;// 写水参数文件

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
		cardid.setText("卡片序列号:" + "-");
		cardid.setTextColor(android.graphics.Color.BLUE);

		// 获取默认的NFC控制器
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (nfcAdapter == null) {
			Toast.makeText(this, "设备不支持NFC！", Toast.LENGTH_LONG).show();
			// finish();
			// return;
		}
		if (!nfcAdapter.isEnabled()) {
			Toast.makeText(this, "请在系统设置中先启用NFC功能！", Toast.LENGTH_LONG).show();
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

		// 添加帮助 按钮监听
		Button btn_help = (Button) findViewById(R.id.btnhelp);
		btn_help.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showToast_help();
			}
		});

		// 添加帮助NFC 按钮监听
		Button btn_nfc = (Button) findViewById(R.id.btnNfc);
		btn_nfc.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivityForResult(new Intent(
						android.provider.Settings.ACTION_WIRELESS_SETTINGS), 0);
			}
		});

		// 添加帮助退出 按钮监听
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
		// 取出封装在intent中的TAG
		Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		for (String tech : tagFromIntent.getTechList()) {
			System.out.println(tech);
		}

		IsoDep mfc = IsoDep.get(tagFromIntent);

		// 卡片读操作
		try {
			mfc.connect();
			 String[] sCardFile_Water = new String[10];
			 String[] sCardFile_Electric = new String[10];
			 String[] sCardFile_Gas = new String[10];
			 int run = 0;
		
			 if (WriterFlag != 0) {
			 // 写卡
			 try {
			 // 水写卡
			 if (WriterFlag == 1) {
			 run = server.WriteWaterCpuCard(mfc, "",
			 WriterFile1_Water, File_water_F,
			 WriterMoney_Water, "");
			 }
			 // 电写卡
			 if (WriterFlag == 2) {
			 run = server.WriteElectricCpuCard(mfc, "",
			 WriterFile1_electric, WriterMoney_electric, "",
			 "", File_electric_F);
			 }
			 // 气写卡
			 if (WriterFlag == 3) {
			 run = server.WriteGasCpuCard(mfc, "",
			 WriterFile1_Gas, File_gas_F, WriterMoney_Gas,
			 "");
			 }
			 } catch (Exception e) {
			 showToast("错误代码：" + run);
			 }
			
			 if (run != 0) {
			 showToast("错误代码：" + run);
			 return;
			 } else {
			
			 updateUi(WriterFlag);
			
			 WriterFlag = 0;
			 }
			
			 } else {
			 // 读取水表文件信息
			 run = server.ReadWaterCpuCard(mfc, sCardFile_Water);
			 if (run == 0) {
			 File1_water = sCardFile_Water[2];
			 Money_water = sCardFile_Water[4];
			 money[1] = String.valueOf(Integer.parseInt(
			 Money_water.substring(0, 8), 16) / 10000);
			 times[1] = String.valueOf(Integer.parseInt(
			 Money_water.substring(9, 16), 16));
			 if (sCardFile_Water[3].equals(File_water_F)) {
			 status[1] = "未插表";
			 } else {
			 status[1] = "已插表";
			 }
			
			 } else {
			 showToast("错误代码：  " + run);
			 return;
			 }
			
			 // 读取电表文件信息
			 run = server.ReadElectricCpuCard(mfc, sCardFile_Electric);
			 if (run == 0) {
			 File1_electric = sCardFile_Electric[2];
			 Money_electric = sCardFile_Electric[3];
			 money[2] = String.valueOf(Integer.parseInt(
			 Money_electric.substring(0, 8), 16) / 100);
			 times[2] = String.valueOf(Integer.parseInt(
			 Money_electric.substring(9, 16), 16));
			 if (sCardFile_Electric[6].equals(File_electric_F)) {
			 status[2] = "未插表";
			 } else {
			 status[2] = "已插表";
			 }
			 } else {
			 showToast("错误代码：  " + run);
			 return;
			 }
			
			 // 读取气表文件信息
			 run = server.ReadGasCpuCard(mfc, sCardFile_Gas);
			 if (run == 0) {
			 File1_gas = sCardFile_Gas[2];
			 Money_gas = sCardFile_Gas[4];
			 money[3] = String.valueOf(Integer.parseInt(
			 Money_gas.substring(0, 8), 16) / 10000);
			 times[3] = String.valueOf(Integer.parseInt(
			 Money_gas.substring(9, 16), 16));
			 if (sCardFile_Gas[3].equals(File_gas_F)) {
			 status[3] = "未插表";
			 } else {
			 status[3] = "已插表";
			 }
			 } else {
			 showToast("错误代码：  " + run);
			 return;
			 }
			
			 // 调用蜂鸣器
			 vibrator();
			 // 更新UI
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
		 * 想设置震动大小可以通过改变pattern来设定，如果开启时间太短，震动效果可能感觉不到
		 */
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		long[] pattern = { 10, 400 }; // 停止 开启 停止 开启
		vibrator.vibrate(pattern, -1); // 重复两次上面的pattern 如果只想震动一次，index设为-1
	}

	private void refreshed(String cardnum) {

		TextView cardid = (TextView) findViewById(R.id.cardid);
		cardid.setText("卡片序列号:");
		cardid.setTextColor(android.graphics.Color.BLUE);
		cardid.setText("卡片序列号:" + cardnum);
		mData = getData();
		ListView listView = (ListView) findViewById(R.id.listView);
		MyAdapter adapter = new MyAdapter(this);
		listView.setAdapter(adapter);

	}

	private void showToast_help() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("帮助");
		builder.setMessage(Html
				.fromHtml("<div>1.使用前请先确认NFC传感器已经启用（可以通过“设置”按钮打开系统设置对话框进行调整）。</div><div>2.读卡时请将卡片靠近手机NFC传感器（一般是在手机后盖），并持续几秒种，以便程序读取信息。</div><p /><div>3.目前Android系统的NFC硬件与有些卡片的兼容性不是很好，读卡失败率较高，需要反复读取。有必要的时候请尝试关闭传感器硬件，然后重新启用。</div><p />"));
		builder.setPositiveButton("退出", null);
		builder.show();

	}

	// 获取动态数组数据 可以由其他地方传来(json等)
	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < title.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("title", title[i]);
			map.put("money", "￥" + money[i]);
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

		// 注意原本getView方法中的int position变量是非final的，现在改为final
		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {

				holder = new ViewHolder();

				// 可以理解为从vlist获取view 之后把view返回给ListView

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
			// 给Button添加单击事件 添加Button之后ListView将失去焦点 需要的直接把Button的焦点去掉
			holder.viewBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (money[position] == "-") {
						showToast("请先读卡");
						return;
					}
					showInfo(position);
				}
			});
			holder.viewbuy.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					if (status[position] == "-") {
						showToast("请先读卡");
						return;
					}

					if (status[position] == "未插表") {
						showToast("购买前请先插表");
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
			tip = "未发现NFC设备";
		else if (nfcAdapter.isEnabled())
			tip = "NFC设备正常";
		else
			tip = "NFC设备禁用";
		setTitle("一卡通充值App---" + tip);

	}

	private void UserBuy(int i) throws Exception {
		showpProgressDialog();

		if (i == 0) {
			// 公交写卡
			new Thread(new WriterCard_transit()).start();
		}
		if (i == 1) {
			// 水写卡
			new Thread(new WriterCard_water()).start();
		}
		if (i == 2) {
			// 电写卡
			new Thread(new WriterCard_electric()).start();
		}
		if (i == 3) {
			// 气写卡
			new Thread(new WriterCard_gas()).start();
		}
	}

	public class WriterCard_transit implements Runnable {
		public void run() {
			// 调用写卡函数

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
			// 调用写卡函数
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
		showSuccess("充值类型：" + BuyType + "<br>" + "充值金额：" + Buymoney + "<br>"
				+ "充值次数：" + (Integer.parseInt(BuyTimes) + 1));
		times[i] = String.valueOf(Integer.parseInt(BuyTimes) + 1);
		money[i] = Buymoney;
		status[i] = "未插表";
		mData = getData();
		ListView listView = (ListView) findViewById(R.id.listView);
		MyAdapter adapter = new MyAdapter(this);
		listView.setAdapter(adapter);
	}

	private void showToast(String data) {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("提示信息");
		builder.setMessage(Html.fromHtml(data.toString()));
		builder.setPositiveButton("确认", null);
		builder.show();
	}

	private void showSuccess(String data) {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("充值成功");
		builder.setMessage(Html.fromHtml(data.toString()));
		builder.setPositiveButton("确认", null);
		builder.show();
	}

	private void showpProgressDialog() throws Exception {
		dialog = new ProgressDialog(this);
		// 设置进度条风格，风格为圆形，旋转的
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		// 设置ProgressDialog 标题
		dialog.setTitle("充值操作");
		// 设置ProgressDialog 提示信息
		dialog.setMessage(Html.fromHtml("请将卡片放入NFC感应区"));
		// 设置ProgressDialog 标题图标
		dialog.setCancelable(false);
		dialog.show();
	}

	// 提取出来方便点
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
				.setTitle(title[position] + "详情")
				.setMessage(
						"充值次数：" + times[position] + "   充值金额:  ￥"
								+ money[position])
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
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
		// 获取购买次数
		ed_times.setText(times[position]);
		new AlertDialog.Builder(this).setTitle(title[position] + "充值")
				.setView(view)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (ed_money.getText().toString().equals("")) {
							showToast("请输入购买金额");
							return;
						}
						// 赋值
						Buymoney = ed_money.getText().toString();
						BuyTimes = ed_times.getText().toString();
						BuyType = title[position];
						try {
							// 调用充值接口
							UserBuy(position);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

					}
				}).show();
	}
}
