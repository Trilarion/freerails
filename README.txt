Railz
=====

Railz is a GPL'd Railroad Tycoon - style railway strategy management game.
The source initially originated from the JFreerails project, built upon for the Railz project and now
developed separately. The Railz website can be found at:

https://sourceforge.net/projects/railz2/

The source tree and release files are hosted on sourceforge:

https://sourceforge.net/projects/railz2/

Downloading:
============

Chances are you've already downloaded it if you're reading this :)

It's available from public CVS at berlios.de:

At the command line -
$ cvs -d:pserver:anonymous@cvs.railz.berlios.de:/cvsroot/railz co railz

More detailed instructions are at
http://developer.berlios.de/cvs/?group_id=1483

Requirements:
=============

JDK 1.4.2 or later, which can be downloaded from:
http://java.sun.com/j2se/

Compilation:
============

To compile, either 
(i) go to the directory src, and type:
 javac -source 1.4 org/railz/launcher/Launcher.java org/railz/server/scripting/*.java
then type
java org/railz/launcher/Launcher
to start. 

or	

(ii) use the build.xml file and Ant version 1.5.

N.B. The source package contains some unit tests.  To compile them you will
need to add junit.jar to your classpath, which can be downloaded from 
www.junit.org.

Running It:
===========

To run, type:
java -jar railz.jar
				
Mac OS X users:
---------------

There appears to be a bug in Mac OS X Java 1.4.2 which causes problems with map
redraws. If this affects you then try running
java -DOSXWorkaround=true -jar railz.jar

Contacts:
=========

Railz2 is currently developed on sourceforge, contact us through there:

https://sourceforge.net/projects/railz2/

Bug reports and Feature Requests
--------------------------------
If you have bugs, or suggestions for enhancement, please submit them to the bug
report tool at
https://sourceforge.net/projects/railz2/

Please check to see whether your bug has already been filed by someone else in
the bug database first before filing it.

Mailing list
------------
There is no mailing list at present but I'll be looking to develop one soon.
Those interested in Railz2, keep your eyes open.

Help Wanted!
============
I am currently looking for a front end Swing developer. Developing the front 
end and UI would massively help the success of the game and those skills would 
be very useful to have on board.

I am also looking for a "stable" branch developer. The stable branch will
concentrate mostly on GUI enhancements/features and bug fixes.

Feedback on game play is essential. Any comments or suggestions we will be 
delighted to hear so we can continue to improve it for the benefit of the community.

Patches are always welcome.

Credits
=======
Thanks to Robert Tuck of the original Railz project and also those who worked on
JFreerails:
http://sourceforge.net/projects/freerails

