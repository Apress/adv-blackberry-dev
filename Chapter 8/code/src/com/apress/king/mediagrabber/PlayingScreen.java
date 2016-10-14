package com.apress.king.mediagrabber;

import java.io.InputStream;

import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.VideoControl;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;

public class PlayingScreen extends MainScreen implements PlayerListener
{
    
    public static final int STATE_WAITING = 1;
    public static final int STATE_LOADING = 2;
    public static final int STATE_LOADED = 3;
    
    private int state = STATE_WAITING;
    
    private UiApplication app;
    private LabelField status;

    private InputStream source;
    private String type;
    private String location;

    private Player player;
    private StatusUpdater updater;

    private MenuItem startItem = new MenuItem("Start", 0, 0)
    {
        public void run()
        {
            start();
        }
    };

    private MenuItem playItem = new MenuItem("Resume", 0, 0)
    {
        public void run()
        {
            try
            {
                player.start();
            }
            catch (MediaException e)
            {
                status.setText("Couldn't resume: " + e);
            }
        }
    };
    
    private MenuItem pauseItem = new MenuItem("Pause", 0, 0)
    {
        public void run()
        {
            try
            {
                player.stop();
            }
            catch (MediaException e)
            {
                status.setText("Couldn't pause: " + e);
            }
        }
    };
    
    private MenuItem rewindItem = new MenuItem("Rewind", 0, 0)
    {
        public void run()
        {
            try
            {
                player.setMediaTime(0);
            }
            catch (MediaException e)
            {
                status.setText("Couldn't rewind: " + e);
            }
        }
    };

    public PlayingScreen(String location, String message)
    {
        this(message);
        this.location = location;
    }

    public PlayingScreen(InputStream in, String type, String message)
    {
        this(message);
        this.source = in;
        this.type = type;
    }
    
    private PlayingScreen(String message)
    {
        add(new LabelField(message));
        status = new LabelField("Waiting.");
        add(status);
        app = UiApplication.getUiApplication();
        updater = new StatusUpdater(status);
    }
    
    public boolean onClose()
    {
        if (player != null)
        {
            player.close();
        }
        return super.onClose();
    }
    
    public void makeMenu(Menu menu, int instance)
    {
        if (instance == Menu.INSTANCE_DEFAULT)
        {
            if (state == STATE_WAITING)
            {
                menu.add(startItem);
            }
            else if (state == STATE_LOADED)
            {
                if (player.getState() == Player.STARTED)
                {
                    menu.add(pauseItem);
                }
                else
                {
                    menu.add(playItem);
                }
                menu.add(rewindItem);
            }
        }
        super.makeMenu(menu, instance);
    }

    private void start()
    {
        state = STATE_LOADING;
        status.setText("Loading");
        if (player == null)
        {
            (new Thread()
            {
                public void run()
                {
                    try
                    {
                        if (location != null)
                        {
                            player = Manager.createPlayer(location);
                        }
                        else
                        {
                            player = Manager.createPlayer(source, type);
                        }
                        player.addPlayerListener(PlayingScreen.this);
                        player.realize();
                        state = STATE_LOADED;
                        VideoControl vc = (VideoControl)player.getControl
                            ("VideoControl");
                        if (vc != null)
                        {
                            Field video = (Field) vc.initDisplayMode(
                                    VideoControl.USE_GUI_PRIMITIVE,
                                    "net.rim.device.api.ui.Field");
                            add(video);
                        }
                        player.start();
                    }
                    catch (Exception e)
                    {
                    	updater.sendDelayedMessage("Error: " + e);
                    }
                }
            }).start();
        }
    }

    public void playerUpdate(Player player, String event, Object eventData)
    {
        if (event.equals(PlayerListener.END_OF_MEDIA))
        {
            app.invokeLater(new Runnable()
            {
                public void run()
                {
                    close();
                }
            });
        }
        else
        {
            updater.sendDelayedMessage(event);
        }
    }
    
}
