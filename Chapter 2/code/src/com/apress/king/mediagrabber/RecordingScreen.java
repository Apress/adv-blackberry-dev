package com.apress.king.mediagrabber;

import java.io.*;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.media.*;
import javax.microedition.media.control.*;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.MainScreen;

public class RecordingScreen extends MainScreen implements PlayerListener
{
	public static final int RECORD_AUDIO = 1;
	public static final int RECORD_PICTURE = 2;
	public static final int RECORD_VIDEO = 3;

	public static final int STATE_WAITING = 1;
	public static final int STATE_READY = 2;
	public static final int STATE_RECORDING = 3;

	private volatile int state = STATE_WAITING;

	private int type;
	private String location;

	private Player player;
	private RecordControl recorder;
	private VideoControl video;
	private Field cameraView;

	private LabelField status;

	private ByteArrayOutputStream dataOut;

	private MenuItem goItem = new MenuItem("Go", 0, 0)
	{
		public void run()
		{
			go();
		}
	};
	private MenuItem stopItem = new MenuItem("Stop", 0, 0)
	{
		public void run()
		{
			stop();
		}
	};
	private MenuItem doneItem = new MenuItem("Return", 0, 0)
	{
		public void run()
		{
			close();
		}
	};

	public RecordingScreen(int type, String location)
	{
		this.type = type;
		this.location = location;
		status = new LabelField("Waiting");
		add(status);
		dataOut = new ByteArrayOutputStream();
		initMedia();
	}

	public void initMedia()
	{
		try
		{
			switch (type)
			{
			case RECORD_AUDIO:
				player = Manager.createPlayer("capture://audio");
				player.start();
				break;
			case RECORD_PICTURE:
			case RECORD_VIDEO:
				player = Manager.createPlayer("capture://video");
				player.start();
				video = (VideoControl) player.getControl("VideoControl");
				cameraView = (Field) video.initDisplayMode(
						VideoControl.USE_GUI_PRIMITIVE,
						"net.rim.device.api.ui.Field");
				add(cameraView);
				break;
			}
			player.addPlayerListener(this);
			state = STATE_READY;
			status.setText("Ready");
		}
		catch (MediaException me)
		{
			status.setText(me.getMessage());
		}
		catch (IOException ioe)
		{
			status.setText(ioe.getMessage());
		}
	}

	public void makeMenu(Menu menu, int instance)
	{
		if (instance == Menu.INSTANCE_DEFAULT)
		{
			if (state == STATE_READY)
			{
				menu.add(goItem);
			}
			else if (state == STATE_RECORDING)
			{
				menu.add(stopItem);
			}
			menu.add(doneItem);
		}
		super.makeMenu(menu, instance);
	}

	private void go()
	{
		if (type == RECORD_PICTURE)
		{
			takeSnapShot();
		}
		else
		{
			recorder = (RecordControl) player.getControl("RecordControl");
			if (recorder != null)
			{
				recorder.setRecordStream(dataOut);
				recorder.startRecord();
				state = STATE_RECORDING;
				status.setText("Recording");
			}
		}
	}

	private void takeSnapShot()
	{
		try
		{
			byte[] imageData = video
					.getSnapshot("encoding=jpeg&width=640&height=480&quality=normal");
			if (imageData != null)
			{
				writeToFile(imageData, location + "/image.jpg");
				status.setText("Image taken");
			}
			else
			{
				status.setText("Please try again later.");
			}
		}
		catch (IOException ioe)
		{
			status.setText(ioe.getMessage());
		}
		catch (MediaException me)
		{
			status.setText(me.getMessage());
		}
	}

	private void writeToFile(byte[] data, String fileName) throws IOException
	{
		FileConnection file = null;
		OutputStream output = null;
		try
		{
			file = (FileConnection) Connector.open(fileName,
					Connector.READ_WRITE);
			if (file.exists())
			{
				file.delete();
			}
			file.create();
			output = file.openOutputStream();
			output.write(data);
		}
		finally
		{
			if (output != null)
			{
				output.close();
			}
			if (file != null)
			{
				file.close();
			}
		}
	}

	private void stop()
	{
		try
		{
			if (type == RECORD_AUDIO || type == RECORD_VIDEO)
			{
				recorder.commit();
				if (type == RECORD_AUDIO)
				{
					writeToFile(dataOut.toByteArray(), location + "/audio.amr");
				}
				else
				{
					writeToFile(dataOut.toByteArray(), location + "/video.3gp");
				}
				status.setText("Data saved");
				state = STATE_READY;
			}
		}
		catch (IOException ioe)
		{
			status.setText(ioe.getMessage());
		}
	}

	public void playerUpdate(Player arg0, String arg1, Object arg2)
	{
		System.out.println("playerUpdate: " + arg1);
	}

}
