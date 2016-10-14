package com.apress.king.mediagrabber;

import java.util.*;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;

public class StatusUpdater implements Runnable
{
    private LabelField status;
    private String message;
    private UiApplication app;
    private static Vector messages = new Vector();

    public StatusUpdater(LabelField status)
    {
        this.status = status;
        app = UiApplication.getUiApplication();
    }

    public void sendDelayedMessage(String message)
    {
        messages.addElement(message);
        this.message = message;
        app.invokeLater(this);
    }

    public static String getLog()
    {
        StringBuffer result = new StringBuffer();
        Enumeration lines = messages.elements();
        while (lines.hasMoreElements())
        {
            String line = (String) lines.nextElement();
            result.append(line);
            result.append("\n");
        }
        return result.toString();
    }

    public void run()
    {
        status.setText(message);
    }

}
