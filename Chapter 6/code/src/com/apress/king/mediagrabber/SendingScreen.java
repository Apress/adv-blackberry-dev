package com.apress.king.mediagrabber;

import java.io.*;
import java.util.Enumeration;
import javax.microedition.pim.*;
import net.rim.blackberry.api.mail.*;
import net.rim.blackberry.api.pdap.*;
import net.rim.device.api.crypto.*;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.MainScreen;

public class SendingScreen extends MainScreen
{
    private static final int STATE_INPUT = 0;
    private static final int STATE_SENDING = 1;
    private static final int STATE_SENT = 2;

    private int state = STATE_INPUT;

    private String contentType;
    private String filename;
    private String message;
    private byte[] data;
    private boolean encrypt;
    private String iv;

    private BasicEditField receiver;
    private LabelField status;

    private StatusUpdater updater;

    private MenuItem sendItem = new MenuItem("Send", 0, 0)
    {
        public void run()
        {
            send();
        }
    };
    private MenuItem selectItem = new MenuItem("Select Recipient", 0, 0)
    {
        public void run()
        {
            selectRecipient();
        }
    };

    public SendingScreen(String contentType, String filename, String message,
            byte[] data, boolean encrypt)
    {
        this.contentType = contentType;
        this.filename = filename;
        this.message = message;
        this.data = data;
        this.encrypt = encrypt;
        status = new LabelField("Please enter an email address.");
        receiver = new BasicEditField("Recipient:", "", 100,
                BasicEditField.FILTER_EMAIL | Field.USE_ALL_WIDTH);
        add(status);
        add(receiver);
        updater = new StatusUpdater(status);
    }

    public void makeMenu(Menu menu, int instance)
    {
        if (instance == Menu.INSTANCE_DEFAULT)
        {
            if (state == STATE_INPUT)
            {
                menu.add(sendItem);
                menu.add(selectItem);
            }
        }
        super.makeMenu(menu, instance);
    }

    private byte[] bytesFromHexString(String input)
    {
        int length = input.length();
        // Each hex character represents 4 bits, so 1
        // byte is 2 characters.
        byte[] bytes = new byte[length / 2];
        for (int i = 0; i < length; i += 2)
        {
            bytes[i / 2] = (byte) Integer.parseInt(input.substring(i, i + 2),
                    16);
        }
        return bytes;
    }

    private String hexFromBytes(byte[] input)
    {
        int length = input.length;
        StringBuffer builder = new StringBuffer(length * 2);
        for (int i = 0; i < length; ++i)
        {
            byte value = (byte) input[i];
            String hex = Integer.toHexString(value);
            if (hex.length() == 8)
            {
                // Integer.toHexString assumes "negative" 4-byte inputs yield
                // an 8-character string, so trim off all but the last 2
                // characters.
                hex = hex.substring(6);
            }
            builder.append(hex);
        }
        return builder.toString();
    }

    private byte[] encryptData(byte[] in) throws CryptoException, IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String hexKey = "2BEAFABBABE4AFAD";
        byte[] binaryKey = bytesFromHexString(hexKey);
        DESKey key = new DESKey(binaryKey);
        DESEncryptorEngine encryptor = new DESEncryptorEngine(key);
        InitializationVector vector = new InitializationVector(8);
        byte[] ivValue = vector.getData();
        iv = hexFromBytes(ivValue);
        CFBEncryptor cfb = new CFBEncryptor(encryptor, vector, out, true);
        cfb.write(in);
        out.flush();
        return out.toByteArray();
    }

    private Message createMessage(String recipient, String type,
            String filename, String message) throws MessagingException,
            CryptoException, IOException
    {
        if (encrypt)
            data = encryptData(data);
        Store defaultStore = Session.getDefaultInstance().getStore();
        Folder sentFolder = defaultStore.getFolder(Folder.SENT);
        Message outgoing = new Message(sentFolder);
        Address friend = new Address(recipient, "");
        outgoing.addRecipient(Message.RecipientType.TO, friend);
        outgoing.setSubject(message);
        Multipart multipart = new Multipart();
        SupportedAttachmentPart file = new SupportedAttachmentPart(multipart,
                type, filename, data);
        multipart.addBodyPart(file);
        TextBodyPart text = new TextBodyPart(multipart);
        if (encrypt)
        {
            text.setContent("The attached file is encrypted, the vector is "
                    + iv);
        }
        else
        {
            text.setContent("Check this out!");
        }
        multipart.addBodyPart(text);
        outgoing.setContent(multipart);
        return outgoing;
    }

    private void send()
    {
        status.setText("Sending, please wait.");
        state = STATE_SENDING;
        receiver.setEditable(false);
        (new Thread(new MessageSender())).start();
    }

    private void selectRecipient()
    {
        BlackBerryContactList contacts = null;
        try
        {
            PIM pim = PIM.getInstance();
            contacts = (BlackBerryContactList) pim.openPIMList(
                    PIM.CONTACT_LIST, PIM.READ_ONLY);
            PIMItem item = contacts.choose();
            if (item == null || !(item instanceof Contact))
                return;
            Contact contact = (Contact) item;
            if (contact.countValues(Contact.EMAIL) > 0)
            {
                String email = contact.getString(Contact.EMAIL, 0);
                receiver.setText(email);
            }
        }
        catch (Throwable t)
        {
            updater.sendDelayedMessage(t.getMessage());
        }
        finally
        {
            if (contacts != null)
            {
                try
                {
                    contacts.close();
                }
                catch (PIMException pime)
                {
                    // Empty
                }
            }
        }
    }

    private void updateContact(String address)
    {
        BlackBerryContactList contacts = null;
        try
        {
            PIM pim = PIM.getInstance();
            contacts = (BlackBerryContactList) pim.openPIMList(
                    PIM.CONTACT_LIST, PIM.READ_WRITE);
            Contact template = contacts.createContact();
            template.addString(Contact.EMAIL, PIMItem.ATTR_NONE, address);
            Enumeration matches = contacts.items(template);
            while (matches.hasMoreElements())
            {
                Contact match = (Contact) matches.nextElement();
                if (match.countValues(BlackBerryContact.USER4) == 0)
                {
                    // First time sending to them.
                    match.addString(BlackBerryContact.USER4, PIMItem.ATTR_NONE,
                            "1");
                }
                else
                {
                    // Increment our counter.
                    String oldString = match.getString(BlackBerryContact.USER4,
                            0);
                    // If this isn't a number, will fall into the catch below.
                    int oldNumber = Integer.parseInt(oldString);
                    String newString = Integer.toString(oldNumber + 1);
                    match.setString(BlackBerryContact.USER4, 0,
                            PIMItem.ATTR_NONE, newString);
                }
                match.commit();
            }
        }
        catch (Throwable t)
        {
            updater.sendDelayedMessage(t.getMessage());
        }
        finally
        {
            if (contacts != null)
            {
                try
                {
                    contacts.close();
                }
                catch (PIMException pime)
                {
                    // Empty
                }
            }
        }
    }

    private class MessageSender implements Runnable
    {
        public void run()
        {
            String address = receiver.getText();
            try
            {
                Message outgoing = createMessage(address, contentType,
                        filename, message);
                Transport.send(outgoing);
                updateContact(address);
                updater.sendDelayedMessage("Message sent");
                state = STATE_SENT;
            }
            catch (Exception e)
            {
                updater
                        .sendDelayedMessage("Problem sending: "
                                + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public boolean onSavePrompt()
    {
        return true;
    }

}
