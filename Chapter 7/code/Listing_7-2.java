import java.io.IOException;
import java.util.*;

import javax.microedition.io.*;

import net.rim.device.api.browser.field.*;
import net.rim.device.api.io.http.HttpHeaders;
import net.rim.device.api.system.Application;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.*;

public class BrowserScreen extends MainScreen implements Runnable,
        RenderingApplication
{
    private RenderingSession renderSession;
    private LabelField status;
    private StatusUpdater updater;
    private String url;

    public BrowserScreen()
    {
        renderSession = RenderingSession.getNewInstance();
        status = new LabelField("Loading...");
        add(status);
        updater = new StatusUpdater(status);
        url = "http://www.google.com";
        (new Thread(this)).start();
    }

    private class BrowserFieldContainer extends VerticalFieldManager
    {
        public BrowserFieldContainer()
        {
            super(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR
                    | Manager.FIELD_HCENTER);
        }

        public void sublayout(int maxWidth, int maxHeight)
        {
            int width = BrowserScreen.this.getWidth();
            int height = BrowserScreen.this.getHeight();
            super.sublayout((int) (width * .9), height / 2);
        }
    }

    public void run()
    {
        HttpConnection conn = null;
        try
        {
            conn = (HttpConnection) Connector.open(url);
            updater.sendDelayedMessage("Connection opened");
            BrowserContent browserContent = renderSession.getBrowserContent(
                    conn, this, null);
            if (browserContent != null)
            {
                Field field = browserContent.getDisplayableContent();
                if (field != null)
                {
                    synchronized (Application.getEventLock())
                    {
                        deleteAll();
                        add(status);
                        add(new LabelField("Your search starts here."));
                        BrowserFieldContainer container = 
                            new BrowserFieldContainer();
                        container.add(field);
                        add(container);
                        add(new LabelField("Don't forget to tip the service!"));
                    }
                }
                browserContent.finishLoading();
            }
        }
        catch (Exception e)
        {
            updater.sendDelayedMessage(e.getMessage());
        }
        finally
        {
            try
            {
                if (conn != null)
                {
                    conn.close();
                }
            }
            catch (Exception e)
            {
            }
        }
    }

    public Object eventOccurred(Event event)
    {
        if (event.getUID() == Event.EVENT_URL_REQUESTED)
        {
            UrlRequestedEvent urlRequestedEvent = (UrlRequestedEvent) event;
            url = urlRequestedEvent.getURL();
            (new Thread(this)).start();
        }
        updater.sendDelayedMessage("Handle event " + event.getUID() + " for "
                + event.getSourceURL());
        return null;
    }

    public int getAvailableHeight(BrowserContent browserContent)
    {
        return getHeight() / 2;
    }

    public int getAvailableWidth(BrowserContent browserContent)
    {
        return (int) (getWidth() * .9);
    }

    public String getHTTPCookie(String url)
    {
        return null;
    }

    public int getHistoryPosition(BrowserContent browserContent)
    {
        return 0;
    }

protected HttpConnection getResourceConnection(String url,
        HttpHeaders requestHeaders)
{
    HttpConnection connection = null;
    try
    {
        connection = (HttpConnection) Connector.open(url);
        if (requestHeaders != null)
        {
            Hashtable headers = requestHeaders.toHashtable();
            if (headers != null)
            {
                Enumeration names = headers.keys();
                while (names.hasMoreElements())
                {
                    String name = (String) names.nextElement();
                    String value = (String) headers.get(name);
                    connection.setRequestProperty(name, value);
                }
            }
        }
    }
    catch (IOException ioe)
    {
        updater.sendDelayedMessage(ioe.getMessage());
    }
    return connection;
}

    public HttpConnection getResource(final RequestedResource resource,
            final BrowserContent referrer)
    {
        if (resource == null || resource.isCacheOnly())
        {
            return null;
        }

        String url = resource.getUrl();

        if (url == null)
        {
            return null;
        }

        if (referrer == null)
        {
            return getResourceConnection(resource.getUrl(), resource
                    .getRequestHeaders());
        }
        else
        {
            (new Thread()
            {
                public void run()
                {
                    HttpConnection connection = getResourceConnection(resource
                            .getUrl(), resource.getRequestHeaders());
                    resource.setHttpConnection(connection);
                    referrer.resourceReady(resource);
                }
            }).start();
        }
        return null;
    }

    public void invokeRunnable(Runnable runnable)
    {
        (new Thread(runnable)).start();
    }

}
