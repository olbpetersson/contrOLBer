package com.example.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

public class TheMouse extends Activity implements OnTouchListener, OnKeyListener, Runnable{
	Socket socket;
	PrintWriter out;
	BufferedReader in;
	String ipString, portString;
	int port, width, height;
	ImageView touchPadImg;
	float tempX, tempY;
	boolean down = false;
	private static ProgressDialog progressDialog;
	private static final int HANDLER_PD_CONNECT = 0, HANDLER_TOAST_MESSAGE = 1;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle bundle){
		
		super.onCreate(bundle);
		Display display = getWindowManager().getDefaultDisplay();

		width = display.getWidth();
		height = display.getHeight();
		
		
		touchPadImg = new ImageView(this);
		touchPadImg.setBackgroundResource(R.drawable.mouseback);
		touchPadImg.setOnTouchListener(this);
		touchPadImg.setOnKeyListener(this);
		touchPadImg.setFocusable(true);
		touchPadImg.setFocusableInTouchMode(true);
		
		setContentView(touchPadImg);
		portString = getIntent().getExtras().getString("port");
		ipString = getIntent().getExtras().getString("ip");
		port = Integer.parseInt(portString);
		touchPadImg.requestFocus();				
	}
	
	private void initThread(){
		new Thread(this).start();	
	}
	private void startConnect(String ip, int port) {
		Message pdMsg = mHandler.obtainMessage(HANDLER_PD_CONNECT);
		pdMsg.obj = true;
		pdMsg.sendToTarget();
		
		try {
			socket = new Socket();
			socket.connect((new InetSocketAddress(ip,port)), 5000);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(!socket.isConnected()){
			Message toastMsg = mHandler.obtainMessage(HANDLER_TOAST_MESSAGE);
			toastMsg.sendToTarget();
		
		//	pd.dismiss();
		}
		else{
			pdMsg = mHandler.obtainMessage(HANDLER_PD_CONNECT);
			pdMsg.obj = false;
			pdMsg.sendToTarget();
			touchPadImg.requestFocus();
		}
	
	}

	public boolean onTouch(View v, MotionEvent me) {

		if(out.checkError()){
			Message toastMsg = mHandler.obtainMessage(HANDLER_TOAST_MESSAGE);
			toastMsg.sendToTarget();
		}
		touchPadImg.requestFocus();
		
		switch(me.getAction()){
			case MotionEvent.ACTION_DOWN:
				if(me.getX() < width/2 && me.getY() < height/4)
					out.println("downleft");
				else if(me.getX() > width/2 && me.getY() < height/4)
					out.println("downright");
					break;
			case MotionEvent.ACTION_MOVE:
				if(me.getY() > height/4){
					
					if(!down){
					tempX = me.getX();
					tempY = me.getY();
					out.println("movemouse");
					down=true;
					}
				
					
					float finalY = (float) (tempY-(me.getY()));
					float finalX = (float)  (me.getX() - tempX );
					tempY = me.getY();
					tempX = me.getX();
					
					
					out.println(finalX*2);
					out.println(finalY*1);
					
				}
				break;
			case MotionEvent.ACTION_UP:
				out.println("up");
				out.println("up");
			
				down = false;
				break;
		
		}
		return true;
	}
	
	@Override
	public void onPause(){
		super.onPause();
		if (progressDialog != null)
			progressDialog.dismiss();
		try {
			if(in != null && out != null && socket != null){
				in.close();
				out.close();
				socket.close();
			}
		} catch (IOException e) {
	
			e.printStackTrace();
		} 
		
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		initThread();
	}

	public boolean onKey(View v, int arg1, KeyEvent e) {
	
		touchPadImg.requestFocus();
		switch(e.getAction()){
			case(KeyEvent.ACTION_DOWN):
				Log.i("KEYCODE: ", ""+e.getKeyCode());
				if(e.getKeyCode() == 57){
					out.println(57);					
				}
				else if(e.getKeyCode() == 24 || e.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP){
					out.println("scrollup");
					Log.i("WROTE: ", "SCROLLUP");
					
				}
				else if(e.getKeyCode() == 25 || e.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN){
					out.println("scrolldown");
					
				}
				
				out.println("keydown");
				out.println(e.getKeyCode());
				break;
		}
		if(e.getKeyCode() == KeyEvent.KEYCODE_BACK)
			return false;
		else
			return true;
	}

	public void run() {
		
		startConnect(ipString,port);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.keyboard_menu, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    	case R.id.keyboard_settings: 
	    		InputMethodManager inputMgr = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	    		inputMgr.toggleSoftInput(0, 0);
	    		return true;
	    }
	    return super.onOptionsItemSelected(item);
	    
	}
	private Handler mHandler = new Handler(){
		
		public void handleMessage(Message msg){
			switch (msg.what){
            case HANDLER_PD_CONNECT:
	                boolean show = (Boolean) msg.obj;
	                if (show && !TheMouse.this.isFinishing()) {
	                        progressDialog = ProgressDialog.show(
	                                        TheMouse.this, "Connecting",
	                                        "Trying to connect...");
	                        progressDialog.setCancelable(true);
	                        progressDialog.setCanceledOnTouchOutside(false);
	                } else if (!show && progressDialog.isShowing()) {
	                        progressDialog.dismiss();
	                }
	                break;
            case HANDLER_TOAST_MESSAGE: 
            	Toast.makeText(TheMouse.this, "Unable to connect", Toast.LENGTH_LONG).show();
            	
            	finish();
            	break;
				}
		}
	};

}
