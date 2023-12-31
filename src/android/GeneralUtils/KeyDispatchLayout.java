package org.apache.cordova.floatOver.GeneralUtils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.RelativeLayout;

import org.apache.cordova.floatOver.Services.FloatOverService;

public class KeyDispatchLayout extends RelativeLayout {

	Context context;



	public KeyDispatchLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		Log.d("MyRelativelayout", "dispatchKeyEvent dispatchKeyEvent dispatchKeyEvent");
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK)
		{

			((FloatOverService)context).stopSelf();
		}


		return super.dispatchKeyEvent(event);
	}

}
