package com.apress.king.mediagrabber;

import net.rim.device.api.ui.UiApplication;

public class MediaGrabber extends UiApplication 
{

	public static void main(String[] args) 
	{
		new MediaGrabber().enterEventDispatcher();
	}
	
	private MediaGrabber()
	{
		pushScreen(new ChoicesScreen());
	}

}
