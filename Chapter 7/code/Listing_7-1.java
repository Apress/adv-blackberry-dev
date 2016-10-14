import java.io.EOFException;
import net.rim.blackberry.api.browser.*;
import net.rim.device.api.servicebook.*;
import net.rim.device.api.synchronization.ConverterUtilities;
import net.rim.device.api.util.DataBuffer;

public class BrowserLocator
{
    public static final int BROWSER_TYPE_WAP = 0;
    public static final int BROWSER_TYPE_BES = 1;
    public static final int BROWSER_TYPE_WIFI = 3;
    public static final int BROWSER_TYPE_BIS = 4;
    public static final int BROWSER_TYPE_WAP2 = 7;

    public static BrowserSession createBrowserSession(int browserType)
    {
        ServiceBook book = ServiceBook.getSB();
        ServiceRecord[] records = book.findRecordsByCid("BrowserConfig");
        int recordCount = records.length;
        for (int i = 0; i < recordCount; i++)
        {
            ServiceRecord record = records[i];
            if (record.isValid() && !record.isDisabled()
                    && getConfigurationType(record) == browserType)
            {
                return Browser.getSession(record.getUid());
            }
        }
        return null;
    }

    private static int getConfigurationType(ServiceRecord record)
    {
        try
        {
            byte[] appData = record.getApplicationData();
            if (appData != null)
            {
                DataBuffer buffer = new DataBuffer(appData, 0, appData.length,
                        true);
                // Skip past the first entry.
                buffer.readByte();
                // 12 is the magic field that holds the service
                // record's configuration type.
                if (ConverterUtilities.findType(buffer, 12))
                {
                    // Buffer is now pointing at the value.
                    return ConverterUtilities.readInt(buffer);
                }
            }
        }
        catch (EOFException eofe)
        {
        }
        return -1;
    }
}

