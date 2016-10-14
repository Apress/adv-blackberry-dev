package com.apress.king.mediagrabber;

import net.rim.device.api.browser.plugin.BrowserContentProviderRegistry;

public class FriendTrackerLoader
{
    public static void libMain(String[] args)
    {
        BrowserContentProviderRegistry registry = BrowserContentProviderRegistry
                .getInstance();
        registry.register(new FriendViewer());
    }
}
