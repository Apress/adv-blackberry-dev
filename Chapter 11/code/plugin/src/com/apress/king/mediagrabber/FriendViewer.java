package com.apress.king.mediagrabber;

import java.io.*;
import java.util.Enumeration;

import javax.microedition.io.HttpConnection;
import javax.microedition.pim.*;

import net.rim.blackberry.api.pdap.BlackBerryContact;
import net.rim.device.api.browser.field.*;
import net.rim.device.api.browser.plugin.*;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class FriendViewer extends BrowserContentProvider
{
    String[] MIME_TYPE = new String[]
    { "text/x-vcard-media" };

    public String[] getAccept(RenderingOptions context)
    {
        return MIME_TYPE;
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
        VerticalFieldManager manager = new VerticalFieldManager();
        RichTextField contentField = new RichTextField(
                RichTextField.USE_TEXT_WIDTH);
        manager.add(contentField);
        browserContent.setContent(manager);
        String email = "";
        try
        {
            HttpConnection conn = context.getHttpConnection();
            InputStream in = conn.openInputStream();
            // Remove network encoding by reading in to a memory stream.
            byte[] bytes = new byte[in.available()];
            in.read(bytes);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            PIM pim = PIM.getInstance();
            PIMItem[] items = pim.fromSerialFormat(bais, "UTF-8");
            if (items == null || items.length == 0)
            {
                contentField.setText("No contact found.");
            }
            else
            {
                Contact friend = (Contact) items[0];
                ContactList contacts = (ContactList) pim.openPIMList(
                        PIM.CONTACT_LIST, PIM.READ_ONLY);
                // See if we know this person, based on their email address.
                if (friend.countValues(Contact.EMAIL) == 0)
                {
                    contentField.setText("No email found.");
                }
                else
                {
                    email = friend.getString(Contact.EMAIL, 0);
                    Contact template = contacts.createContact();
                    template.addString(Contact.EMAIL, PIMItem.ATTR_NONE, email);
                    Enumeration matches = contacts.items(template);
                    if (!matches.hasMoreElements())
                    {
                        contentField.setText(email
                                + " isn't in your address book.");
                    }
                    else
                    {
                        Contact match = (Contact) matches.nextElement();
                        if (match.countValues(BlackBerryContact.USER4) == 0)
                        {
                            contentField.setText("You haven't sent " + email
                                    + " any media yet!");
                        }
                        else
                        {
                            String sentString = match.getString(
                                    BlackBerryContact.USER4, 0);
                            contentField.setText("You have sent " + email + " "
                                    + sentString + " media files so far.");
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw new RenderingException("Error: " + e.getMessage());
        }
        browserContent.setTitle(email);
        return browserContent;
    }

    public String[] getSupportedMimeTypes()
    {
        return MIME_TYPE;
    }
}
