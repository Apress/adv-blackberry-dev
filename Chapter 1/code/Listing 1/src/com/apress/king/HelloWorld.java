package com.apress.king;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

public class HelloWorld extends MIDlet implements CommandListener
{

    protected void startApp() throws MIDletStateChangeException
    {
        Form form = new Form("Welcome!");
        StringItem text = new StringItem(null, "Hello, World!");
        form.insert(0, text);
        Command quitCommand = new Command("Quit", Command.EXIT, 0);
        form.addCommand(quitCommand);
        form.setCommandListener(this);
        Display.getDisplay(this).setCurrent(form);
    }

    public void commandAction(Command c, Displayable d)
    {
        if (c.getCommandType() == Command.EXIT)
        {
            try
            {
                destroyApp(true);
                notifyDestroyed();
            }
            catch (MIDletStateChangeException e)
            {
                e.printStackTrace();
            }
        }
    }

    protected void destroyApp(boolean arg0) throws MIDletStateChangeException
    {
        notifyDestroyed();
    }

    protected void pauseApp()
    {
        // This method intentionally left blank.
    }

}
