package org.apache.cordova.cardreader;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
	
	private UsbManager mManager;
    private Reader mReader;
    private PendingIntent mPermissionIntent;

	@Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        
        if (action.equals("addListener")) {
			callbackContext.success();
		}
		else if (action.equals("removeListener")) {
			callbackContext.success();			
		}
		else if (action.equals("init")) {
			init(callbackContext);
			return true;
		}
        return true;
    }

	private void init(CallbackContext callbackContext) {
 		
		// Get USB manager
        mManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);

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
            }
        });		
		callbackContext.success();
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

			public void onReceive(Context context, Intent intent) {

				String action = intent.getAction();

				if (ACTION_USB_PERMISSION.equals(action)) {

					synchronized (this) {

						UsbDevice device = (UsbDevice) intent
								.getParcelableExtra(UsbManager.EXTRA_DEVICE);

						if (intent.getBooleanExtra(
								UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

							if (device != null) {
								// Open reader
								Log.d(TAG, "Opening reader: " + device.getDeviceName());
								mReader.open(device);
							}

						} else {
							Log.d(TAG, "Permission denied for device " + device.getDeviceName());
						}
					}

				} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {

					synchronized (this) {

						// Update reader list
						for (UsbDevice device : mManager.getDeviceList().values()) {
							if (mReader.isSupported(device)) {
								
							}
						}

						UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

						if (device != null && device.equals(mReader.getDevice())) {
	
							// Close reader
							mReader.close();
						}
					}
				}
			}
		};
}