import java.io.*;
	
import javax.microedition.io.HttpConnection;

import net.rim.device.api.browser.field.*;
import net.rim.device.api.browser.plugin.*;
import net.rim.device.api.ui.component.RichTextField;

public class JavaViewer extends BrowserContentProvider implements
        BrowserPageContext
{
    String[] MIME_TYPES = new String[]
    { "text/x-java", "text/x-java-source" };

    public String[] getAccept(RenderingOptions context)
    {
        return MIME_TYPES;
    }

    public BrowserContent getBrowserContent(
            BrowserContentProviderContext context) throws RenderingException
    {
        if (context == null)
            throw new RenderingException("No context");
        BrowserContentBaseImpl browserContent = new BrowserContentBaseImpl(
                context.getHttpConnection().getURL(), null, context
                        .getRenderingApplication(), context
                        .getRenderingSession().getRenderingOptions(), context
                        .getFlags());
        RichTextField contentField = new RichTextField();
        String fileName = "";
        try
        {
            HttpConnection conn = context.getHttpConnection();
            InputStream in = conn.openInputStream();
            fileName = conn.getFile();
            int numBytes = in.available();
            StringBuffer builder = new StringBuffer(numBytes);
            int depth = 0;
            int read = 0;
            do
            {
                read = in.read();
                if (read != -1)
                {
                    if (read == '}')
                        --depth;
                    if (depth < 2)
                        builder.append((char) read);
                    if (read == '{')
                        ++depth;
                }
            } while (read != -1);
            String compressed = builder.toString();
            contentField.setText(compressed);
        }
        catch (IOException ioe)
        {
            throw new RenderingException("I/O Error: " + ioe.getMessage());
        }
        browserContent.setContent(contentField);
        browserContent.setTitle(fileName);
        browserContent.setBrowserPageContext(this);
        return browserContent;
    }

    public String[] getSupportedMimeTypes()
    {
        return MIME_TYPES;
    }

    public boolean getPropertyWithBooleanValue(int id, boolean defaultValue)
    {
        return defaultValue;
    }

    public int getPropertyWithIntValue(int id, int defaultValue)
    {
        if (id == BrowserPageContext.DISPLAY_STYLE)
            return BrowserPageContext.STYLE_VERTICAL_SCROLL_ON_LEFT
                    | BrowserPageContext.STYLE_SHOW_IN_FULL_SCREEN;
        return defaultValue;
    }

    public Object getPropertyWithObjectValue(int id, Object defaultValue)
    {
        return defaultValue;
    }

    public String getPropertyWithStringValue(int id, String defaultValue)
    {
        return defaultValue;
    }

}
