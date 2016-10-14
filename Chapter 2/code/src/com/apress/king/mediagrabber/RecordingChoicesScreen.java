package com.apress.king.mediagrabber;

import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.MainScreen;

public class RecordingChoicesScreen extends MainScreen 
{
	private BasicEditField location = new BasicEditField("Save location:",
			"file:///SDCard/BlackBerry", 100, Field.FIELD_VCENTER
					| BasicEditField.FILTER_FILENAME);
	private MenuItem audioItem = new MenuItem("Record Sound", 0, 0)
	{
		public void run()
		{
			launchRecorder(RecordingScreen.RECORD_AUDIO);
		}
	};
	private MenuItem pictureItem = new MenuItem("Take a Picture", 0, 0)
	{
		public void run()
		{
			launchRecorder(RecordingScreen.RECORD_PICTURE);
		}
	};
	private MenuItem videoItem = new MenuItem("Record Vide", 0, 0)
	{
		public void run()
		{
			launchRecorder(RecordingScreen.RECORD_VIDEO);
		}
	};

	public RecordingChoicesScreen()
	{
		setTitle("MediaGrabber");
		add(new LabelField(
				"Please enter a save location, then select a recording choice from the menu."));
		add(location);
	}

	public void close()
	{
		location.setDirty(false);
		super.close();
	}

	public void makeMenu(Menu menu, int instance)
	{
		if (instance == Menu.INSTANCE_DEFAULT)
		{
			String property = System.getProperty("supports.audio.capture");
			if (property != null && property.equals("true"))
			{
				menu.add(audioItem);
			}
			property = System.getProperty("video.snapshot.encodings");
			if (property != null && property.length() > 0)
			{
				menu.add(pictureItem);
			}
			property = System.getProperty("supports.video.capture");
			if (property != null && property.equals("true"))
			{
				menu.add(videoItem);
			}
		}

		super.makeMenu(menu, instance);
	}

	private void launchRecorder(int type)
	{
		String directory = location.getText();
		RecordingScreen screen = new RecordingScreen(type, directory);
		UiApplication.getUiApplication().pushScreen(screen);
	}

	public boolean onSavePrompt()
	{
		return true;
	}

}
