set JDE_PATH=C:\dev\ide\eclipse_34\plugins\net.rim.eide.componentpack4.7.0_4.7.0.46\components\
set FILES=MediaGrabber.java ChoicesScreen.java PlayingScreen.java RecordingScreen.java SendingScreen.java StatusUpdater.java MediaGrabber.rrh MediaGrabber.rrc MediaGrabber_cs.rrc MediaGrabber_en.rrc MediaGrabber_en_GB.rrc MediaGrabber_en_US.rrc
set SOURCEPATH=src\com\apress\king\mediagrabber
set PASSWORD=runes2342
set STARTDIR="%CD%"
cd %SOURCEPATH%
%JDE_PATH%\bin\rapc.exe -import="%JDE_PATH%\lib\net_rim_api.jar" %FILES%
%JDE_PATH%\bin\SignatureTool.jar -a -p %PASSWORD% -c MediaGrabber.cod
copy MediaGrabber.cod %STARTDIR%
cd %STARTDIR%
