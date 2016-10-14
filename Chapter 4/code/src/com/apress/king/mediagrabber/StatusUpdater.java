package com.apress.king.mediagrabber;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;

public class StatusUpdater implements Runnable 
{
    private LabelField status;
    private String message;
    private UiApplication app;
    
    public StatusUpdater(LabelField status)
    {
        this.status = status;
        app = UiApplication.getUiApplication();
    }
    
    public void sendDelayedMessage(String message)
    {
        this.message = message;
        app.invokeLater(this);
    }
    
    public void run()
    {
        status.setText(message);
    }
	
}
