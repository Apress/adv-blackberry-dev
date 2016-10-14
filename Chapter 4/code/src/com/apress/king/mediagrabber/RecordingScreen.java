package com.apress.king.mediagrabber;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.RecordControl;
import javax.microedition.media.control.VideoControl;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;

public class RecordingScreen extends MainScreen implements PlayerListener
{
    public static final int RECORD_AUDIO = 1;
    public static final int RECORD_PICTURE = 2;
    public static final int RECORD_VIDEO = 3;

    public static final int STATE_WAITING = 1;
    public static final int STATE_READY = 2;
    public static final int STATE_RECORDING = 3;

    private volatile int state = STATE_WAITING;

    private int type;
    private String location;

    private Player player;
    private RecordControl recorder;
    private VideoControl video;
    private Field cameraView;

    private LabelField status;

    private ByteArrayOutputStream dataOut;

    private MenuItem goItem = new MenuItem("Go", 0, 0)
    {
        public void run()
        {
            go();
        }
    };
    private MenuItem stopItem = new MenuItem("Stop", 0, 0)
    {
        public void run()
        {
            stop();
        }
    };
    private MenuItem doneItem = new MenuItem("Return", 0, 0)
    {
        public void run()
        {
            close();
        }
    };

    public RecordingScreen(int type, String location)
    {
        this.type = type;
        this.location = location;
        status = new LabelField("Waiting");
        add(status);
        dataOut = new ByteArrayOutputStream();
        initMedia();
    }

    public void initMedia()
    {
        try
        {
            switch (type)
            {
            case RECORD_AUDIO:
                player = Manager.createPlayer("capture://audio");
                player.start();
                break;
            case RECORD_PICTURE:
            case RECORD_VIDEO:
                player = Manager.createPlayer("capture://video");
                player.start();
                video = (VideoControl) player.getControl("VideoControl");
                cameraView = (Field) video.initDisplayMode(
                                VideoControl.USE_GUI_PRIMITIVE,
                                "net.rim.device.api.ui.Field");
                add(cameraView);
                break;
            }
            player.addPlayerListener(this);
            state = STATE_READY;
            status.setText("Ready");
        }
        catch (MediaException me)
        {
            status.setText(me.getMessage());
        }
        catch (IOException ioe)
        {
            status.setText(ioe.getMessage());
        }
    }

    public void makeMenu(Menu menu, int instance)
    {
        if (instance == Menu.INSTANCE_DEFAULT)
        {
            if (state == STATE_READY)
            {
                menu.add(goItem);
            }
            else if (state == STATE_RECORDING)
            {
                menu.add(stopItem);
            }
            menu.add(doneItem);
        }
        super.makeMenu(menu, instance);
    }

    private void go()
    {
        if (type == RECORD_PICTURE)
        {
            takeSnapShot();
        }
        else
        {
            recorder = (RecordControl) player.getControl("RecordControl");
            if (recorder != null)
            {
                recorder.setRecordStream(dataOut);
                recorder.startRecord();
                state = STATE_RECORDING;
                status.setText("Recording");
            }
        }
    }

    private void takeSnapShot()
    {
        try
        {
            byte[] imageData = video
                            .getSnapshot("encoding=jpeg&width=640&height=480&quality=normal");
            if (imageData != null)
            {
                String file = location + "/image.jpg";
                writeToFile(imageData, file);
                status.setText("Image taken");
                // Bitmap taken = Bitmap.createBitmapFromBytes(imageData, 0,
                // imageData.length, 1);
                // Screen reviewer = new MainScreen();
                // BitmapField bitmap = new BitmapField(taken);
                // reviewer.add(bitmap);
                // UiApplication.getUiApplication().pushScreen(reviewer);
                send("image.jpg", "image/jpeg", "Here's a picture!", imageData);
            }
            else
            {
                status.setText("Please try again later.");
            }
        }
        catch (IOException ioe)
        {
            status.setText(ioe.getMessage());
        }
        catch (MediaException me)
        {
            status.setText(me.getMessage());
        }
    }

    private void writeToFile(byte[] data, String fileName) throws IOException
    {
        FileConnection file = null;
        OutputStream output = null;
        try
        {
            file = (FileConnection) Connector.open(fileName,
                            Connector.READ_WRITE);
            if (file.exists())
            {
                file.delete();
            }
            file.create();
            output = file.openOutputStream();
            output.write(data);
        }
        finally
        {
            if (output != null)
            {
                output.close();
            }
            if (file != null)
            {
                file.close();
            }
        }
    }

    private void stop()
    {
        try
        {
            if (type == RECORD_AUDIO || type == RECORD_VIDEO)
            {
                recorder.commit();
                if (type == RECORD_AUDIO)
                {
                    String file = location + "/audio.amr";
                    writeToFile(dataOut.toByteArray(), file);
                    // play(file, "Recorded Audio");
                    send("audio.amr", "audio/amr", "Here's some sound!",
                                    dataOut.toByteArray());
                }
                else
                {
                    String file = location + "/video.3gp";
                    writeToFile(dataOut.toByteArray(), file);
                    // play(file, "Recorded Video");
                    send("video.3gp", "video/3gp", "Here's a video!", dataOut
                                    .toByteArray());
                }
                status.setText("Data saved");
                state = STATE_READY;
            }
        }
        catch (IOException ioe)
        {
            status.setText(ioe.getMessage());
        }
        finally
        {
            if (dataOut != null)
            {
                try
                {
                    dataOut.close();
                }
                catch (Exception e)
                {
                    
                }
            }
            dataOut = new ByteArrayOutputStream();
        }
    }

    public void playerUpdate(Player player, String event, Object data)
    {
        System.out.println("playerUpdate: " + event);
    }

    private void play(String location, String message)
    {
        Screen playback = new PlayingScreen(location, message);
        UiApplication.getUiApplication().pushScreen(playback);
    }

    private void send(String location, String contentType, String message,
                    byte[] data)
    {
        SendingScreen sending = new SendingScreen(contentType, location,
                        message, data);
        UiApplication.getUiApplication().pushScreen(sending);
    }

}
