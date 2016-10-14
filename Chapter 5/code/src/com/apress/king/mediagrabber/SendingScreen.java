package com.apress.king.mediagrabber;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.Folder;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.MessagingException;
import net.rim.blackberry.api.mail.Multipart;
import net.rim.blackberry.api.mail.Session;
import net.rim.blackberry.api.mail.Store;
import net.rim.blackberry.api.mail.SupportedAttachmentPart;
import net.rim.blackberry.api.mail.TextBodyPart;
import net.rim.blackberry.api.mail.Transport;
import net.rim.device.api.crypto.CFBEncryptor;
import net.rim.device.api.crypto.CryptoException;
import net.rim.device.api.crypto.DESEncryptorEngine;
import net.rim.device.api.crypto.DESKey;
import net.rim.device.api.crypto.InitializationVector;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
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
