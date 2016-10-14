package com.apress.king.mediagrabber;

import net.rim.blackberry.api.mail.*;
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
                    byte[] data)
    {
        this.contentType = contentType;
        this.filename = filename;
        this.message = message;
        this.data = data;
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

    private Message createMessage(String recipient, String type,
                    String filename, String message) throws MessagingException
    {
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
        text.setContent("Check this out!");
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
                updater.sendDelayedMessage("Problem sending: "
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
