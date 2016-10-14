package com.apress.king;

import net.rim.device.api.system.*;

public class GoodbyeWorld
{

    public static void libMain(String[] args)
    {
        System.out.println("GoodbyeWorld launching");
        int handle = CodeModuleManager.getModuleHandle("HelloUniverse");
        ApplicationDescriptor[] descriptors = CodeModuleManager
                .getApplicationDescriptors(handle);
        if (descriptors.length > 0)
        {
            ApplicationDescriptor descriptor = descriptors[0];
            try
            {
                ApplicationManager manager = ApplicationManager
                        .getApplicationManager();
                while (manager.inStartup())
                {
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException ie)
                    {
                        // Ignore.
                    }
                }
                manager.runApplication(descriptor);
            }
            catch (ApplicationManagerException e)
            {
                System.out.println("I couldn't launch it!");
                e.printStackTrace();
            }
        }
        else
        {
            System.out.println("HelloUniverse is not installed.");
        }
        System.out.println("Goodbye, world!");
    }

}
