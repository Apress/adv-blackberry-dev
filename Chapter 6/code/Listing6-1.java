import javax.microedition.pim.Contact;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMItem;

import net.rim.blackberry.api.pdap.BlackBerryContactList;
import net.rim.blackberry.api.pdap.PIMListListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

public class RemoveContactListener extends MainScreen implements PIMListListener
{
    
    LabelField instructions;
    StatusUpdater status;
    
    public RemoveContactListener()
    {
        instructions = new LabelField();
        instructions.setText("It's time to vote someone off the island!");
        add(instructions);
        status = new StatusUpdater(instructions);
        try
        {
            BlackBerryContactList contacts = (BlackBerryContactList)PIM.
                getInstance().openPIMList(PIM.CONTACT_LIST, PIM.READ_ONLY);
            contacts.addListener(this);
            contacts.close();
        }
        catch (Exception e)
        {
            System.err.println(e);
            e.printStackTrace();
        }
    }

    public void itemAdded(PIMItem added)
    {
        status.sendDelayedMessage("No!  You're supposed to get RID of people!");
    }

    public void itemRemoved(PIMItem removed)
    {
        if (removed instanceof Contact)
        {
            if (removed.countValues(Contact.NAME) > 0)
            {
                String[] name = removed.getStringArray(Contact.NAME, 0);
                String message = "Goodbye, " + name[Contact.NAME_GIVEN] + "!";
                status.sendDelayedMessage(message);
            }
        }
    }

    public void itemUpdated(PIMItem oldContent, PIMItem newContent)
    {
        status.sendDelayedMessage("Something changed, but they're still here.");
    }
    
}
