package com.apress.king;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.util.Arrays;

public class MediaGrabber extends UiApplication
{

    public static void main(String[] args)
    {
        MediaGrabber app = new MediaGrabber();
        app.begin();
    }

    public void begin()
    {
        MainScreen s = new MainScreen();
        LabelField label = new LabelField("Kilroy Was Here");
        s.add(label);
        pushScreen(s);
        (new WebChecker()).start();
        enterEventDispatcher();
    }

    private class WebChecker extends Thread
    {
        public void run()
        {
            HttpConnection http = null;
            FileConnection file = null;
            InputStream is = null;
            OutputStream os = null;
            try
            {
                http = (HttpConnection) Connector.open(
                        "http://www.google.com;deviceside=true",
                        Connector.READ_WRITE, true);
                is = http.openInputStream();
                // Read the first 4 kilobytes.
                byte[] networkBuffer = new byte[4096];
                is.read(networkBuffer);
                is.close();
                http.close();
                file = (FileConnection) Connector
                        .open("file:///store/home/user/last.html");
                if (file.exists())
                {
                    System.out.println("We last checked Google on "
                            + new Date(file.lastModified()));
                    byte[] fileBuffer = new byte[4096];
                    is = file.openInputStream();
                    is.read(fileBuffer);
                    is.close();
                    if (Arrays.equals(networkBuffer, fileBuffer))
                    {
                        System.out.println("Google hasn't changed.");
                    }
                    else
                    {
                        System.out.println("Google's doing something new.");

                    }
                    file.delete();
                }
                else
                {
                    System.out.println("Looks like the first time we've run!");
                }
                file.create();
                os = file.openOutputStream();
                os.write(networkBuffer);
            }
            catch (IOException ioe)
            {
                System.err.println("An I/O error occurred: " + ioe);
            }
            catch (Exception e)
            {
                System.err.println("An unexpected error occurred: " + e);
            }
            finally
            {
                try
                {
                    if (os != null)
                        os.close();
                    if (file != null)
                        file.close();
                    if (is != null)
                        is.close();
                    if (http != null)
                        http.close();
                }
                catch (Exception e)
                {
                    // Ignore
                }
            }
        }
    }

}
