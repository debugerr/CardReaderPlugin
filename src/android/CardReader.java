package com.kraftbyte.cardreader;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.LOG;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.MessageFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.util.Log;

import com.acs.smartcard.Features;
import com.acs.smartcard.PinModify;
import com.acs.smartcard.PinProperties;
import com.acs.smartcard.PinVerify;
import com.acs.smartcard.ReadKeyOption;
import com.acs.smartcard.Reader;
import com.acs.smartcard.Reader.OnStateChangeListener;
import com.acs.smartcard.TlvProperties;


public class CardReader extends CordovaPlugin {
	private static final String TAG = "CardReaderPlugin";
	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";	
	private static final String[] propertyStrings = { "Unknown", "wLcdLayout",
            "bEntryValidationCondition", "bTimeOut2", "wLcdMaxCharacters",
            "wLcdMaxLines", "bMinPINSize", "bMaxPINSize", "sFirmwareID",
            "bPPDUSupport", "dwMaxAPDUDataSize", "wIdVendor", "wIdProduct" };
	
	private static CallbackContext cbTagRead;
	private static CallbackContext initCBContext;
	private UsbManager mManager;
    private Reader mReader;
    private PendingIntent mPermissionIntent;

	@Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        
        if (action.equals("addListener")) {
			cbTagRead = callbackContext;			
			return true;
		}
		else if (action.equals("removeListener")) {
			cbTagRead = null;
			return true;
		}
		else if (action.equals("init")) {
			init(callbackContext);
			return true;
		}

        return false;
    }

	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
	// Get USB manager
        mManager = (UsbManager) this.cordova.getActivity().getSystemService(Context.USB_SERVICE);

		final CordovaInterface finalCordova = this.cordova;

        // Initialize reader
        mReader = new Reader(mManager);
        mReader.setOnStateChangeListener(new OnStateChangeListener() {

            @Override
            public void onStateChange(int slotNum, int prevState, int currState) {

                if (prevState < Reader.CARD_UNKNOWN
                        || prevState > Reader.CARD_SPECIFIC) {
                    prevState = Reader.CARD_UNKNOWN;
                }

                if (currState < Reader.CARD_UNKNOWN
                        || currState > Reader.CARD_SPECIFIC) {
                    currState = Reader.CARD_UNKNOWN;
                }

				//String cmd = MessageFormat.format(javaScriptEventTemplate, "nfc_cardreader_tag", Integer.toString(currState));
				final String cmd = MessageFormat.format("from {0} to {1}, slot: {2}", Integer.toString(prevState), Integer.toString(currState), Integer.toString(slotNum));
				Log.v(TAG, cmd);

				finalCordova.getActivity().runOnUiThread(new Runnable() {
					public void run() {
						cbTagRead.success(cmd);
					}
				});				
            }
        });
		// Register receiver for USB permission
        mPermissionIntent = PendingIntent.getBroadcast(this.cordova.getActivity(), 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        cordova.getActivity().registerReceiver(mReceiver, filter);

		super.initialize(cordova, webView);
	}

	private void init(CallbackContext callbackContext) {
		
		initCBContext = callbackContext;
		for (UsbDevice device : mManager.getDeviceList().values()) {
            mManager.requestPermission(device, mPermissionIntent);
            break;            
        }
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

			public void onReceive(Context context, Intent intent) {

				try 
				{  
					String action = intent.getAction();

					if (ACTION_USB_PERMISSION.equals(action)) {

						synchronized (this) {

							UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

							if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

								if (device != null) {
									// Open reader
									Log.d(TAG, "Opening reader: " + device.getDeviceName());
									//mReader.open(device);

									cordova.getActivity().runOnUiThread(new Runnable() {
										public void run() {
											initCBContext.success();
										}
									});									
								}

							} else {
								Log.d(TAG, "Permission denied for device " + device.getDeviceName());
							}
						}

					} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {

						synchronized (this) {

							UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

							if (device != null && device.equals(mReader.getDevice())) {
	
								// Close reader
								mReader.close();
							}
						}
					}
				} 
				catch (final Exception e) {
					cordova.getActivity().runOnUiThread(new Runnable() {
						public void run() {
							initCBContext.error(e.toString());
						}
					});						
				}
			}
		};
}