package com.apress.king;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

public class HelloUniverse extends UiApplication
{
    
    public void start()
    {
        MainScreen main = new MainScreen();
        LabelField label = new LabelField("Hello, Universe");
        main.add(label);
        UiApplication app = UiApplication.getUiApplication(); 
        app.pushScreen(main);
        app.enterEventDispatcher();
    }

    public static void main(String[] args)
    {
        (new HelloUniverse()).start();
    }
}
