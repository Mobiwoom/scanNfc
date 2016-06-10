package com.delcasda.myplugin;

import java.util.Collection;
import java.util.Calendar;
import java.io.IOException;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;
import android.widget.Toast;
import android.content.Intent;

import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.parser.EmvParser;
import com.github.devnied.emvnfccard.utils.AtrUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import android.nfc.NfcAdapter;
import fr.devnied.bitlib.BytesUtils;

public class Echo extends CordovaPlugin {

    private CallbackContext connectionCallbackContext  = null;
    private Context ctx;
    
    	private Provider mProvider = new Provider();

	/**
	 * Emv card
	 */
	private EmvCard mReadCard;
    /**
     * Constructor.
     */
    public Echo() {
    }

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    
    Log.d("Java action=",action);
     if (action.equals("setNFC")) {
                                Log.d("[execute]","action onNewIntent pepe");
                                this.connectionCallbackContext = callbackContext;
                                PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                                pluginResult.setKeepCallback(true);
                                connectionCallbackContext.sendPluginResult(pluginResult);
                                return true;
                        } else if (action.equals("echo")) {
        String message = args.getString(0);
        this.echo(message, callbackContext);
        return true;
    }
    return false;
}

private void sendUpdate(String cardNumber){
      if (connectionCallbackContext != null) {
          PluginResult result = new PluginResult(PluginResult.Status.OK, cardNumber);
          result.setKeepCallback(true);
          connectionCallbackContext.sendPluginResult(result);
         }
}

private void echo(String message, CallbackContext callbackContext) {
     Log.d("echo in message=",message);
    if (message != null && message.length() > 0) {
        Log.d("toast","1");
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this.cordova.getActivity().getApplicationContext());
        Toast.makeText(cordova.getActivity().getApplicationContext(), "NFC is " + String.valueOf(adapter.isEnabled()), Toast.LENGTH_LONG).show();
        callbackContext.success(message);
    } else {
        callbackContext.error("Expected one non-empty string argument.");
        
    }
}

public void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);
    
    Log.d("[onNewIntent]","pepe 1");
 
		ctx = cordova.getActivity().getApplicationContext();
	//	fctx = arg0;
		
		final Tag mTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
    
    Log.d("[onNewIntent]","pepe 2");
		if (mTag != null) {

       Log.d("[onNewIntent]","pepe 3");
			new SimpleAsyncTask(this.cordova.getActivity().getApplicationContext()) {

				/**
				 * Tag comm
				 */
				private IsoDep mTagcomm;

				/**
				 * Emv Card
				 */
				private EmvCard mCard;
				
				/**
	      * Last Ats
	       */
	       private byte[] lastAts;

				/**
				 * Boolean to indicate exception
				 */
				private boolean mException;

				@Override
				protected void onPreExecute() {
					super.onPreExecute();
					
					mProvider.getLog().setLength(0);
          
           Log.d("[onNewIntent]","pepe 4");
					// Show dialog
					/*if (mDialog == null) {
						mDialog = ProgressDialog.show(arg0, getString(R.string.card_reading),
								getString(R.string.card_reading_desc), true, false);
					} else {
						mDialog.show();
					}*/
				}

				@Override
				protected void doInBackground() {

         Log.d("[onNewIntent]","pepe 5");
					mTagcomm = IsoDep.get(mTag);
					if (mTagcomm == null) {
						//CroutonUtils.display(HomeActivity.this, getText(R.string.error_communication_nfc), CoutonColor.BLACK);
						return;
					}
					mException = false;

					try {
						mReadCard = null;
						// Open connection
						mTagcomm.connect();
						lastAts = getAts(mTagcomm);

						mProvider.setmTagCom(mTagcomm);

						EmvParser parser = new EmvParser(mProvider, true);
						mCard = parser.readEmvCard();
						if (mCard != null) {
							
							//Log.e("pepe this.contex=", this.context.toString());
							
							//AtrUtils.context = this.context;
							
							mCard.setAtrDescription(extractAtsDescription(lastAts));
							
							Log.e("pepe onPostExecute", "extra");
						}

					} catch (IOException e) {
						mException = true;
					} finally {
						// close tagcomm
						IOUtils.closeQuietly(mTagcomm);
					}
				}

				@Override
				protected void onPostExecute(final Object result) {
					// close dialog
					/*if (mDialog != null) {
						mDialog.cancel();
					}*/
					Log.e("pepe onPostExecute", "1" );
					

					if (!mException) {
						if (mCard != null) {
							if (StringUtils.isNotBlank(mCard.getCardNumber())) {
								//CroutonUtils.display(HomeActivity.this, getText(R.string.card_read), CoutonColor.GREEN);
								
								JSONObject obj = new JSONObject();
								
								try {
									obj.put("cardNumber", mCard.getCardNumber());	
                  
                  //sendUpdate(mCard.getCardNumber());
									
									if (mCard.getExpireDate() !=null) {
										Calendar cal = Calendar.getInstance();
										cal.setTime(mCard.getExpireDate());
										String month = String.valueOf(cal.get(Calendar.MONTH)+1);//january=0
										String year = String.valueOf(cal.get(Calendar.YEAR));
																				
										//obj.put("expireDate", mCard.getExpireDate().toString());
										obj.put("expireMonth",month);
										obj.put("expireYear",year.substring(year.length()-2));
									}
									if (StringUtils.isNotBlank(mCard.getHolderFirstname())) {
										obj.put("firstName", mCard.getHolderFirstname());
									}
									if (StringUtils.isNotBlank(mCard.getHolderLastname())) {
										obj.put("lastName", mCard.getHolderLastname());
									}
									if (mCard.getType() != null) {
										obj.put("type", mCard.getType().toString());
									}
									
									obj.put("leftPinTry", String.valueOf(mCard.getLeftPinTry()));
									
									
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
								Log.e("pepe card number", mCard.getCardNumber());
								
								mReadCard = mCard;
								        
                sendUpdate(obj.toString());
								/*if(fctx != null){
									fctx.dispatchStatusEventAsync(NFCAne.CARD_READED,obj.toString());
								} */
							} else if (mCard.isNfcLocked()) {
								//CroutonUtils.display(HomeActivity.this, getText(R.string.nfc_locked), CoutonColor.ORANGE);
							}
						} else {
							//CroutonUtils.display(HomeActivity.this, getText(R.string.error_card_unknown), CoutonColor.BLACK);
						}
					} else {
						//CroutonUtils.display(HomeActivity.this, getResources().getText(R.string.error_communication_nfc), CoutonColor.BLACK);
					}

					//refreshContent();
				}

			}.execute();
		}	
			
	
	}
	
	private byte[] getAts(final IsoDep pIso) {
		byte[] ret = null;
		if (pIso.isConnected()) {
			// Extract ATS from NFC-A
			ret = pIso.getHistoricalBytes();
			if (ret == null) {
				// Extract ATS from NFC-B
				ret = pIso.getHiLayerResponse();
			}
		}
		return ret;
	}
	
	/**
	 * Method used to get description from ATS
	 *
	 * @param pAts
	 *            ATS byte
	 */
	public Collection<String> extractAtsDescription(final byte[] pAts) {		
		
		//Log.e("pepe ctx=", ctx.toString());
		//return AtrUtils.getDescriptionFromAts(BytesUtils.bytesToString(pAts),ctx);
    return AtrUtils.getDescriptionFromAts(BytesUtils.bytesToString(pAts));
	}

}