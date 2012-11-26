package com.dc.drawing;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

@TargetApi(11)
public class ClientDialogFragment extends DialogFragment {
    
	public String ipAddress;
	public int port;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();

	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    builder.setView(inflater.inflate(R.layout.connect_dialogue_box, null))
	    // Add action buttons
	           .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {	         
	            	   //EditText text = (EditText) ClientDialogFragment.this.getDialog().findViewById(R.id.ipaddress);	            	   
	            	   //Log.d("IP FUCKER: ", text.getText().toString());
	                   mListener.onClientDialogPositiveClick(ClientDialogFragment.this);
	               }
	           })
	           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   ClientDialogFragment.this.getDialog().cancel();
	               }
	           });
	    
	    return builder.create();
	}
	
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface ClientNoticeDialogListener {
        public void onClientDialogPositiveClick(DialogFragment dialog);
        public void onClientDialogNegativeClick(DialogFragment dialog);
    }
    
    // Use this instance of the interface to deliver action events
    ClientNoticeDialogListener mListener;
    
    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (ClientNoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
}
