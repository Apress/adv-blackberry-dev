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

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;

public class RecordingScreen extends MainScreen implements PlayerListener,
        MediaGrabberResource
{
    public static final int RECORD_AUDIO = 1;
    public static final int RECORD_PICTURE = 2;
    public static final int RECORD_VIDEO = 3;

    public static final int STATE_WAITING = 1;
    public static final int STATE_READY = 2;
    public static final int STATE_RECORDING = 3;
    public static final int STATE_RECORDED = 4;
    public static final int STATE_RECORDED_IMAGE = 5;

    private static ResourceBundle r = ResourceBundle.getBundle(BUNDLE_ID,
            BUNDLE_NAME);

    private volatile int state = STATE_WAITING;

    private int type;
    private String location;

    private Player player;
    private RecordControl recorder;
    private VideoControl video;
    private Field cameraView;

    private LabelField status;

    private ByteArrayOutputStream dataOut;

    private String filename;
    private String contentType;
    private String message;
    private byte[] data;

    private MenuItem goItem = new MenuItem(r.getString(I18N_COMMAND_GO), 0, 0)
    {
        public void run()
        {
            go();
        }
    };
    private MenuItem stopItem = new MenuItem(r.getString(I18N_COMMAND_STOP), 0,
            0)
    {
        public void run()
        {
            stop();
        }
    };
    private MenuItem doneItem = new MenuItem(r.getString(I18N_COMMAND_RETURN),
            0, 0)
    {
        public void run()
        {
            close();
        }
    };
    private MenuItem playItem = new MenuItem(r.getString(I18N_COMMAND_PLAY), 0,
            0)
    {
        public void run()
        {
            play();
        }
    };
    private MenuItem showItem = new MenuItem(r.getString(I18N_COMMAND_SHOW), 0,
            0)
    {
        public void run()
        {
            showPicture();
        }
    };
    private MenuItem sendItem = new MenuItem(r.getString(I18N_COMMAND_SEND), 0,
            0)
    {
        public void run()
        {
            send(false);
        }
    };
    private MenuItem sendEncryptedItem = new MenuItem(r
            .getString(I18N_COMMAND_SEND_ENCRYPTED), 0, 0)
    {
        public void run()
        {
            send(true);
        }
    };

    public RecordingScreen(int type, String location)
    {
        this.type = type;
        this.location = location;
        status = new LabelField(r.getString(I18N_STATUS_WAITING));
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
            status.setText(r.getString(I18N_STATUS_READY));
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
            else if (state == STATE_RECORDED)
            {
                menu.add(playItem);
                menu.add(sendItem);
                menu.add(sendEncryptedItem);
            }
            else if (state == STATE_RECORDED_IMAGE)
            {
                menu.add(showItem);
                menu.add(sendItem);
                menu.add(sendEncryptedItem);
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
                status.setText(r.getString(I18N_STATUS_RECORDING));
            }
        }
    }

    private void takeSnapShot()
    {
        try
        {
            data = video
                    .getSnapshot("encoding=jpeg&width=640&height=480&quality=normal");
            if (data != null)
            {
                String file = location + "/image.jpg";
                writeToFile(data, file);
                status.setText(r.getString(I18N_STATUS_IMAGE_TAKEN));
                filename = "image.jpg";
                contentType = "image/jpeg";
                message = r.getString(I18N_PICTURE_EMAIL_TEXT);
                state = STATE_RECORDED_IMAGE;
            }
            else
            {
                status.setText(r.getString(I18N_ERROR_NO_PICTURE_DATA));
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
                data = dataOut.toByteArray();
                if (type == RECORD_AUDIO)
                {
                    String file = location + "/audio.amr";
                    writeToFile(data, file);
                    contentType = "audio/amr";
                    message = r.getString(I18N_SOUND_EMAIL_TEXT);
                    filename = "audio.amr";
                }
                else
                {
                    String file = location + "/video.3gp";
                    writeToFile(data, file);
                    contentType = "video/3gp";
                    message = r.getString(I18N_VIDEO_EMAIL_TEXT);
                    filename = "video.3gp";
                }
                status.setText(r.getString(I18N_STATUS_SAVED));
                state = STATE_RECORDED;
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

    private void play()
    {
        Screen playback = new PlayingScreen(location + "/" + filename, message);
        UiApplication.getUiApplication().pushScreen(playback);
    }

    private void send(boolean encrypt)
    {
        SendingScreen sending = new SendingScreen(contentType, filename,
                message, data, encrypt);
        UiApplication.getUiApplication().pushScreen(sending);
    }

    private void showPicture()
    {
        Bitmap taken = Bitmap.createBitmapFromBytes(data, 0, data.length, 1);
        Screen reviewer = new MainScreen();
        BitmapField bitmap = new BitmapField(taken);
        reviewer.add(bitmap);
        UiApplication.getUiApplication().pushScreen(reviewer);
    }
}
