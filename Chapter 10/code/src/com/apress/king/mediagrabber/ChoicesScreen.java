package com.apress.king.mediagrabber;

import net.rim.device.api.i18n.*;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.MainScreen;

public class ChoicesScreen extends MainScreen implements MediaGrabberResource
{
    private static ResourceBundle r = ResourceBundle.getBundle(BUNDLE_ID,
            BUNDLE_NAME);

    private BasicEditField location = new BasicEditField(r
            .getString(I18N_PROMPT_LOCATION), "file:///SDCard/BlackBerry", 100,
            Field.FIELD_VCENTER | BasicEditField.FILTER_URL);
    private MenuItem audioItem = new MenuItem(r
            .getString(MediaGrabber.I18N_CHOICE_RECORD_SOUND), 0, 0)
    {
        public void run()
        {
            launchRecorder(RecordingScreen.RECORD_AUDIO);
        }
    };
    private MenuItem pictureItem = new MenuItem(r
            .getString(MediaGrabber.I18N_CHOICE_TAKE_PICTURE), 0, 0)
    {
        public void run()
        {
            launchRecorder(RecordingScreen.RECORD_PICTURE);
        }
    };
    private MenuItem videoItem = new MenuItem(r
            .getString(MediaGrabber.I18N_CHOICE_RECORD_VIDEO), 0, 0)
    {
        public void run()
        {
            launchRecorder(RecordingScreen.RECORD_VIDEO);
        }
    };
    private MenuItem launchVideoItem = new MenuItem(r
            .getString(MediaGrabber.I18N_CHOICE_PLAY_MEDIA), 0, 0)
    {
        public void run()
        {
            launchPlayer();
        }
    };

    public ChoicesScreen()
    {
        setTitle("MediaGrabber");
        add(new LabelField(r.getString(MediaGrabber.I18N_INSTRUCTIONS)));
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
        String pattern = r.getString(MediaGrabber.I18N_PLAY_MEDIA_TITLE);
        MessageFormat format = new MessageFormat("");
        format.setLocale(Locale.getDefaultForSystem());
        Object[] arguments = new String[]{url};
        format.applyPattern(pattern);
        String formatted = format.format(arguments);
        PlayingScreen screen = new PlayingScreen(url, formatted);
        UiApplication.getUiApplication().pushScreen(screen);
    }

    public boolean onSavePrompt()
    {
        return true;
    }

}
