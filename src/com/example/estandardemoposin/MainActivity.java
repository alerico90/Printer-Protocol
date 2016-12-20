package com.example.estandardemoposin;

import java.util.HashMap;

//PRT
import java.util.HashMap;
import java.util.Iterator;
import PRTAndroidSDK.PRTAndroidPrint;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
	Button printTextInTextboxB;
	Button printInvoiceExampleB;
	Button printFileB;
	Button printBarCode;
	EditText textInputTB;
	Button wifiB;
	EditText textWifi;
	boolean Respuesta = false;
	
	//FromPRTSDKA
	private Spinner spnPrinterList=null;
	private Button btnBT=null;
	private Button btnDisplayRemainingPower=null;
	private Button btnWiFi=null;
	private Button btnUSB=null;
	private EditText edtIP = null;
	private Button btnPrint=null;
	private TextView txtTips=null;
	private TextView txtRemainingPower=null;
	private EditText edtPrintText = null;
	private Spinner spinnerLanguage     = null;	
	
	private String ConnectType="";
	private Context thisCon=null;
	private ArrayAdapter arrPrinterList; 
	private static PRTAndroidPrint PRT=null;
	private BluetoothAdapter mBluetoothAdapter;
	private String strBTAddress="";

	private UsbManager mUsbManager=null;	
	private UsbDevice device=null;
	private static final String ACTION_USB_PERMISSION = "com.android.example.PRTSDKApp";
	private PendingIntent mPermissionIntent=null;
	//FINISHED PRT
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		printTextInTextboxB = (Button)findViewById(R.id.button1);	
		printInvoiceExampleB = (Button)findViewById(R.id.button2);	
		printFileB = (Button)findViewById(R.id.button3);	
		textInputTB = (EditText)findViewById(R.id.editText1);
		printBarCode = (Button)findViewById(R.id.button5);
		wifiB = (Button)findViewById(R.id.button4);
		textWifi = (EditText)findViewById(R.id.editText2);
		btnUSB = (Button)findViewById(R.id.Button01);
		thisCon=this.getApplicationContext();
		
		
		mPermissionIntent = PendingIntent.getBroadcast(thisCon, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		thisCon.registerReceiver(mUsbReceiver, filter);
		
	wifiB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				try{
					cleanPRT();
					connectPrinterWifi();
					Toast.makeText(getApplicationContext(), "Impresora está conectada por Wifi", Toast.LENGTH_SHORT).show();
				}
				catch(Exception e){
					Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
				}
					
			}
		});
	
	btnUSB.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v)
		{
			try{
				cleanPRT();
				connectPrinterUSB();
			}
			catch(Exception e){
				Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
			}
				
		}
	});
		
	printTextInTextboxB.setOnClickListener(new View.OnClickListener() {
		@Override
			public void onClick(View v) 
			{
			try{
				PRT.PRTReset();
				/*PRT.PRTSendString(textInputTB.getText().toString());
				PRT.PRTReset();	
				PRT.PRTFeedLines(240);*/
				
				byte data[] = null;
				byte sendText[]=new byte[3];
				
				sendText[0]=0x1B;
				sendText[1]=0x74;
				if(PRT.WriteData(sendText, sendText.length)==-1)
					return;
				String strPrintText=textInputTB.getText().toString();
				
				data = (thisCon.getString(R.string.originalsize) + " " + strPrintText+"\n").getBytes();
				if(PRT.WriteData(data, data.length)==-1)
					return;
				if(!PRT.PRTFeedLines(20))
					return;
				if(!PRT.PRTReset())
					return;
																					
				//width£¬height£¬bold£¬underline£¬minifont
				if(!PRT.PRTFormatString(false,true,false,false,false))
					return;						
				data = (thisCon.getString(R.string.heightsize) +  " " + strPrintText+"\n").getBytes();
				if(PRT.WriteData(data, data.length)==-1)
					return;
				if(!PRT.PRTFeedLines(20))
					return;
				if(!PRT.PRTReset())
					return;
										
				PRT.PRTFormatString(true,false,false,false,false);
				data = (thisCon.getString(R.string.widthsize) +  " " + strPrintText+"\n").getBytes();
				PRT.WriteData(data, data.length);
				PRT.PRTFeedLines(20);
				PRT.PRTReset();
				
				PRT.PRTFormatString(true,true,false,false,false);
				data = (thisCon.getString(R.string.heightwidthsize) +  " " + strPrintText+"\n").getBytes();
				PRT.WriteData(data, data.length);
				PRT.PRTFeedLines(20);
				PRT.PRTReset();
				
				PRT.PRTFormatString(false,false,true,false,false);
				data = (thisCon.getString(R.string.bold) +  " " + strPrintText+"\n").getBytes();
				PRT.WriteData(data, data.length);
				PRT.PRTFeedLines(20);
				PRT.PRTReset();
				
				PRT.PRTFormatString(false,false,false,true,false);
				data = (thisCon.getString(R.string.underline) +  " " + strPrintText+"\n").getBytes();
				PRT.WriteData(data, data.length);
				PRT.PRTFeedLines(20);
				PRT.PRTReset();	
				
				if(PRT.PRTCapPrintMiniFont())
				{
					PRT.PRTFormatString(false,false,false,false,true);
					data = (thisCon.getString(R.string.minifront) +  " " + strPrintText+"\n").getBytes();							
					PRT.WriteData(data, data.length);
					PRT.PRTFeedLines(500);
				}
				PRT.PRTFeedLines(500);
				PRT.PRTFeedLines(500);
				if (PRT.PRTCapPaperCut())
				{										
					PRT.PRTPaperCut(true);	//true:half cut
				}
	
			}
			catch(Exception e){
				Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
			}			
			}
        });
	
	printInvoiceExampleB.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v)
		{
				Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
		}
	});
	
	printFileB.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v)
		{
				Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
		}
	});
	
	printBarCode.setOnClickListener(new View.OnClickListener() {
		@Override
			public void onClick(View v) 
			{
			try{
				PRT.PRTReset();
				//print barcode
				if(PRT.PRTCapPrintBarcode())
				{
					if(!Barcode_BC_UPCA())
						return;
					Barcode_BC_UPCE();
					Barcode_BC_EAN8();							
					Barcode_BC_EAN13();
					Barcode_BC_CODE93();
					Barcode_BC_CODE39();
					Barcode_BC_CODEBAR();		
					Barcode_BC_ITF();
					Barcode_BC_CODE128();
				}	
				PRT.PRTPrintBarcode2(0,5,49,8,48,"0123456789abcdef0123456789abcdef");
				PRT.PRTFeedLines(240);	
				PRT.PRTFeedLines(240);
			if (PRT.PRTCapPaperCut())
			{										
				PRT.PRTPaperCut(true);	//true:half cut
			}	
			}
			catch(Exception e){
				Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
			}			
			}
        });
	
	}	
	
	private void cleanPRT(){
		if(PRT!=null)
		{
			PRT.PRTReset();
			PRT.CloseProt();
		}
	}
	
	private void connectPrinterUSB(){
		try{
			
		{  						
			if(PRT!=null)
			{
				PRT.CloseProt();
			}						
			PRT=new PRTAndroidPrint(thisCon,"USB","HPRT TP801");	
			mUsbManager = (UsbManager) thisCon.getSystemService(Context.USB_SERVICE);				
	  		HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();  		
	  		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
	  		
	  		boolean HavePrinter=false;		  
	  		while(deviceIterator.hasNext())
	  		{
	  		    device = deviceIterator.next();
	  		    int count = device.getInterfaceCount();
	  		    for (int i = 0; i < count; i++) 
	  	        {
	  		    	UsbInterface intf = device.getInterface(i); 
	  	            if (intf.getInterfaceClass() == 7) 
	  	            {
	  	            	HavePrinter=true;
	  	            	mUsbManager.requestPermission(device, mPermissionIntent);		  	            	
	  	            }
	  	        }
	  		}
	  		if(!HavePrinter)
	  			Toast.makeText(thisCon, R.string.connect_usb_printer, Toast.LENGTH_LONG).show();
        }
		}
		catch(Exception e) {
			Toast.makeText(thisCon, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
	private boolean connectPrinterWifi(){
		PRT=new PRTAndroidPrint(thisCon,"WiFi","HPRT TP801");
		try{
			Respuesta = PRT.OpenPort(textWifi.getText().toString() + ",9100"); //textWifi.getText().toString()
			if(Respuesta == false)
			{
				PRT=null;
				Toast.makeText(thisCon, "Impresora no fue conectada por Wifi", Toast.LENGTH_SHORT).show();
			}
			else
			{					
				Toast.makeText(thisCon, "Impresora fue conectada por Wifi", Toast.LENGTH_SHORT).show();
			}			
		}
		catch(Exception e){
			Toast.makeText(thisCon, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		return Respuesta;
	}
	
	private void connectPrinterSerial(){

	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() 
	{
	    public void onReceive(Context context, Intent intent) 
	    {
	        String action = intent.getAction();	       
	        //Toast.makeText(thisCon, "now:"+System.currentTimeMillis(), Toast.LENGTH_LONG).show();
	        //»ñÈ¡·ÃÎÊUSBÉè±¸È¨ÏÞ
	        if (ACTION_USB_PERMISSION.equals(action))
	        {
		        synchronized (this) 
		        {		        	
		            device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
			        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
			        {			        	
			        	if(!PRT.OpenPort("",device))
						{					
			        		PRT=null;
							txtTips.setText(thisCon.getString(R.string.connecterr));
							Toast.makeText(thisCon, thisCon.getString(R.string.connecterr)+PRT.GetPrinterName(), Toast.LENGTH_SHORT).show();					
		                	return;
						}
						else
						{
							txtTips.setText("Printer:"+spnPrinterList.getSelectedItem().toString().trim());
							Toast.makeText(thisCon, thisCon.getString(R.string.connected), Toast.LENGTH_SHORT).show();
		                	return;
						}	
			        }		
			        else
			        {			        	
			        	return;
			        }
		        }
		    }
	        //¶Ï¿ªÁ¬½Ó
	        if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) 
	        {
	            device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
	            if (device != null) 
	            {	                
	            	PRT.ClosePort();
	            }
	        }
	    }
	};
	private boolean Barcode_BC_UPCA()
	{		
		PRT.PRTSendString("BC_UPCA:\n");		
		return PRT.PRTPrintBarcode(PRTAndroidPrint.BC_UPCA,
				 PRTAndroidPrint.BC_DEFAULT, 
				 PRTAndroidPrint.BC_DEFAULT, 
				 PRTAndroidPrint.BC_HRIBELOW, 
		 		 "075678164125");
	}
	
	private boolean Barcode_BC_UPCE()
	{		
		PRT.PRTSendString("BC_UPCE:\n");		
		return PRT.PRTPrintBarcode(PRTAndroidPrint.BC_UPCE,
				 PRTAndroidPrint.BC_DEFAULT, 
				 PRTAndroidPrint.BC_DEFAULT, 
				 PRTAndroidPrint.BC_HRIBELOW, 
		 		 "01227000009");//04252614 
	}
	
	private boolean Barcode_BC_EAN8()
	{		
		PRT.PRTSendString("BC_EAN8:\n");		
		return PRT.PRTPrintBarcode(PRTAndroidPrint.BC_EAN8,
				 PRTAndroidPrint.BC_DEFAULT, 
				 PRTAndroidPrint.BC_DEFAULT, 
				 PRTAndroidPrint.BC_HRIBELOW, 
		 		 "04210009");
	}
	
	private boolean Barcode_BC_EAN13()
	{		
		PRT.PRTSendString("BC_EAN13:\n");		
		return PRT.PRTPrintBarcode(PRTAndroidPrint.BC_EAN13,
				 PRTAndroidPrint.BC_DEFAULT, 
				 PRTAndroidPrint.BC_DEFAULT, 
				 PRTAndroidPrint.BC_HRIBELOW, 
		 		 "6901028075831");
	}
	
	private boolean Barcode_BC_CODE93()
	{		
		PRT.PRTSendString("BC_CODE93:\n");		
		return PRT.PRTPrintBarcode(PRTAndroidPrint.BC_CODE93,
				 PRTAndroidPrint.BC_DEFAULT, 
				 PRTAndroidPrint.BC_DEFAULT, 
				 PRTAndroidPrint.BC_HRIBELOW, 
		 		 "TEST93");
	}
	
	private boolean Barcode_BC_CODE39()
	{		
		PRT.PRTSendString("BC_CODE39:\n");		
		return PRT.PRTPrintBarcode(PRTAndroidPrint.BC_CODE39,
				 PRTAndroidPrint.BC_DEFAULT, 
				 PRTAndroidPrint.BC_DEFAULT, 
				 PRTAndroidPrint.BC_HRIBELOW, 
		 		 "123456789");
	}
	
	private boolean Barcode_BC_CODEBAR()
	{		
		PRT.PRTSendString("BC_CODEBAR:\n");		
		return PRT.PRTPrintBarcode(PRTAndroidPrint.BC_CODEBAR,
				 PRTAndroidPrint.BC_DEFAULT, 
				 PRTAndroidPrint.BC_DEFAULT, 
				 PRTAndroidPrint.BC_HRIBELOW, 
		 		 "A40156B");
	}
	
	private boolean Barcode_BC_ITF()
	{		
		PRT.PRTSendString("BC_ITF:\n");		
		return PRT.PRTPrintBarcode(PRTAndroidPrint.BC_ITF,
				 PRTAndroidPrint.BC_DEFAULT, 
				 PRTAndroidPrint.BC_DEFAULT, 
				 PRTAndroidPrint.BC_HRIBELOW, 
 		 		 "123456789012");
	}
	
	private boolean Barcode_BC_CODE128()
	{		
		PRT.PRTSendString("BC_CODE128:\n");		
		return PRT.PRTPrintBarcode(PRTAndroidPrint.BC_CODE128,
				 PRTAndroidPrint.BC_DEFAULT, 
				 PRTAndroidPrint.BC_DEFAULT, 
				 PRTAndroidPrint.BC_HRIBELOW, 
		 		 "{BS/N:{C\014\042\070\116{A3");	// decimal 1234 = octonary 1442
	}

	    
}
