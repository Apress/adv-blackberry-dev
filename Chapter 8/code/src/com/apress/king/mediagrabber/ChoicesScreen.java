package com.apress.king.mediagrabber;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;

public class ChoicesScreen extends MainScreen
{
    private BasicEditField location = new BasicEditField("Location:",
            "file:///SDCard/BlackBerry", 100, Field.FIELD_VCENTER
                    | BasicEditField.FILTER_URL);
    private MenuItem audioItem = new MenuItem("Record Sound", 0, 0)
    {
        public void run()
        {
            launchRecorder(RecordingScreen.RECORD_AUDIO);
        }
    };
    private MenuItem pictureItem = new MenuItem("Take a Picture", 0, 0)
    {
        public void run()
        {
            launchRecorder(RecordingScreen.RECORD_PICTURE);
        }
    };
    private MenuItem videoItem = new MenuItem("Record Video", 0, 0)
    {
        public void run()
        {
            launchRecorder(RecordingScreen.RECORD_VIDEO);
        }
    };
    private MenuItem launchVideoItem = new MenuItem("Play Media", 0, 0)
    {
        public void run()
        {
            launchPlayer();
        }
    };

    public ChoicesScreen()
    {
        setTitle("MediaGrabber");
        add(new LabelField(
                "Please enter a location, then select a choice from the menu."));
        add(location);
    }

    public void close()
    {
        location.setDirty(false);
        super.close();
    }

    public void makeMenu(Menu menu, int instance)
    {
        if (instance == Menu.INSTANCE_DEFAULT)
        {
            String property = System.getProperty("supports.audio.capture");
            if (property != null && property.equals("true"))
            {
                menu.add(audioItem);
            }
            property = System.getProperty("video.snapshot.encodings");
            if (property != null && property.length() > 0)
            {
                menu.add(pictureItem);
            }
            property = System.getProperty("supports.video.capture");
            if (property != null && property.equals("true"))
            {
                menu.add(videoItem);
            }
            menu.add(launchVideoItem);
        }

        super.makeMenu(menu, instance);
    }

    private void launchRecorder(int type)
    {
        String directory = location.getText();
        RecordingScreen screen = new RecordingScreen(type, directory);
        UiApplication.getUiApplication().pushScreen(screen);
    }

    private void launchPlayer()
	{
        String url = location.getText();
	    PlayingScreen screen = new PlayingScreen(url, "Playing " + url);
	    UiApplication.getUiApplication().pushScreen(screen);
	}

    public boolean onSavePrompt()
    {
        return true;
    }

}
