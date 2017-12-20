************************
Changelog
************************

Dec 25, 2016, lukeyj

- Re-added station info panel
- Only show good supplied or demanded
- Modernised build through maven
- Pulled out file paths into constants
- Apply patch  1557521 (simple cleanups)

Sep 3, 2006, Luke

- Give moving trains priority over stationary trains.
- Fixed bugs
- 1551106  Trains with 'auto consist' set don't pickup cargo

Sep 1, 2006, Luke

- Features implemented:
- 987520 Track Contention

Aug 31, 2006 Luke

- Brought back 'cheat' menu.
- Fixed bugs
- 1537413 Exception when building station.

Jul 24, 2006 Luke

- Fixed bug where list of saved games didn't get updated when files were added,
- removed, or renamed.

Jul 16, 2006 Luke

- Fixed bugs
- 1384250 Wait until full slows frame rate.
- 1384249 Unexpected Exception when removing last wagon.

Dec 5, 2005 Luke

- Fixed bugs
- 1313227 Remove last wagon doesn't work with more than 2 wagons
- 1313225 Harbour: Conversion makes no sense
- 1341365 Exception when calculating stock price after buying shares
- 1303162 Unexpected Exception in SquareTileBackgroundRenderer

Sept 21, 2005 Luke

- Improve exception reporting

Sept 19, 2005 Luke

- Fixed bugs
- 1289010 Net worth on broker screen is wrong
- 1289014 Can buy 100% of treasury stock
- 1289012 No limits on issuing/repaying bonds
- 1295855 Can buy stock when can't afford to
- 1289008 Stock price never changes

Sept 11, 2005 Luke

- Fixed bugs
- 1266577 Broker screen layout

Sept 10, 2005 Luke

- Fixed bugs
- 1269664 Can't tell other player has my stock
- 1266575 Stock holder equity not shown properly on the balance sheet.

Sept 9, 2005 Luke

- Fixed bugs
- 1269676 Stack traces often lost

Sept 8, 2005 Luke

- Fixed bugs
- 1266581 Underscores in map names
- 1223231 "waiting" message on launcher unclear
- 1266582  Progress bar
- 1269679  Train Info doesn't update

Aug- 31, 2005 Luke

- Fixed bugs
- 1266584 Can't select which saved game on launcher
- 1269683 Load game when no saved games.
- 1269688 Load game in full screen
- 1269689 Load game across network

Aug- 24, 2005 Luke

- RFEs implemented
- 1223234- Add show FPS to 'Display' menu.
- 1223235- Save launcher input
-
- Fixed bugs
- 1266695 Unexpected exception during network game
- 1266637 OutOfMemoryError
- 1110270 Reproducible Crash Bug
- 1223230 Incorrect station price on popup
- 1223228 NotSerializableException when saving game with trains

Jul 03, 2005 Luke

- More code cleanup.

Jul 03, 2005 Luke

- Reorganisation of existing code.

May 22, 2005 Luke

- Code cleanup
- Improve pathfinder - finding paths for track is now up to 20 times faster.
- More of the same

Apr 10, 2005 Luke

- More work on new train movement classes

Apr 04, 2005 Luke

- More work on new train movement classes

Apr 01, 2005 Luke

- More work on new train movement classes

Feb 20, 2005 Luke

- More work on new train movement classes

Feb 18, 2005 Luke

- Refactoring existing train movement classes in
- preparation to use new classes.

Feb 05, 2005 Luke

- Update website to use SSI
- Work on new train movement classes
- Added AI page to functional spec.

Feb 04, 2005 Luke

- Add new train movement classes.

Jan 27, 2005 Luke

- Added toString() to KEY classes.

Jan 27, 2005 Luke

- Added serialVersionUID field to serializable classes.

Jan 26, 2005 Luke

    Bugs Fixed:
    1105499- Word wrapping in Html components
- 1105494- Load game with wrong player
- 1105488- Attempting to join game in progress

Jan 25, 2005 Luke

- Work on bug 1105494- (Load game with wrong player).

Jan 24, 2005 Luke

 - Second attempt at fixing bug 1103632 (Sound on Linux)

Jan 17, 2005 Luke

- Note, some of theses changes occurred at earlier dates but were not
- entered into this change log.
- Bugs Fixed:
- 1103632- Sound on Linux
- 1103633- Build station mode
- 1103634- 'P' sets priority orders
- 1102801- keys on train orders
- 1102803- Blank schedule after adding stations
- 1102797- Pause 1st time track is built
- 1103154- Building track quickly with keyboard fails
- 1103150- Can build track in station placement mode
- 1102804- Cursor on map edges
- 1103155- Can't upgrade station with F8
- 1102800- Turbo game speed does nothing
- 1102806- Newspaper does nothing
- 1102798- Building track out of station too expensive
- 1102799- "Can't afford to remove station"
- 1087429- Same icon for info, no tunnels, no bridges
- 1096168- No tooltips on build tab
- 1087428- Wrong cursor message
- 1087431- Message "Illegal track config..-
- 1087373- Stations influence should not overlap
- 1087427- Terrain info dialogue close button
- 1087409- java.io.InvalidClassException
- 1087414- Upgrade track on Ocean -> ArrayIndexOutOfBoundsException
- 1087425- NullPointerException
- 1087426- Can see stations boxes for other players
- 1087433- Can't tell that train roster has focus
- 1087422- Pressing 'I' on other's station ->crash-
- 1005144- java.lang.IllegalArgumentException: Tried to add TrainPosition- - - - - -
-
- Features implemented:
- 927146- Display natural numbers for trains, stations, etc-
-
- Other changes:
- New track graphics

Jan 14, 2005 Luke

- Updated build.xml
- Minor javadoc updates

Jan 13, 2005 Luke

- Bugs fixed:
- 1098769 Blinking cursor
- 1098767 Can't remove bridges when 'no bridges' selected
- 1099095 Remove track not cancelled
- 1099093 Upgrade track starting at station fails
- 1099083 Remove train, then click train list-> Exception
- 1099091 Station placement cursor wrong colour-
- 1099092 Station influence remains after station removed

Jan 09, 2005 Luke

- Bugs fixed:
- 1087432- Can't remove or upgrade track using mouse

Jan 04, 2005 Luke

- Bugs fixed:
- 1087437- java properties window should word wrap.
- 1087434- Building track out of station
- -
- Other changes:
- Code cleanup

Dec 18, 2004 Luke

- RFEs Implemented:
- 1055501- Automatically build bridges & tunnels
- 931570- Improve Cursor
- 915941- Bridge types GUI
- 915940- Tunnels options GUI

Dec 15, 2004 Luke

- More on track build system.  Its almost complete.

Dec 14, 2004 Luke

- Work on track build system.  Appropriate track for the terrain
- is now automatically selected.  Still some bugs.

Dec 12, 2004 Luke

- Updated functional specification.

Nov 16, 2004 Luke

- Work on GUI to select track type and build mode.

Nov 15, 2004 Luke

- Started using java 1.5 language features
- Updated build.xml to use 1.5 and removed 'format' and 'ConstJava' ant targets.

Oct 27, 2004 Luke

- Bugs Fixed:
- 1054729- Can't build bridges using mouse

Oct 19, 2004 Luke

- Bugs Fixed:
- 1046399- No supply and demand at new stations

Oct 18, 2004 Luke

- RFEs Implemented:
-  1048913- Option to turn off sound
- Bugs:

- Work on 1046399- No supply and demand at new stations

Oct 17, 2004 Luke

- RFEs Implemented:
 - 972863- Launcher: progress bar should be on new page
- Bugs Fixed:
- 1047435- Can't rejoin game
- 1047445 Invalid port but next button enabled-
- 1047440 Progress bar not visible when starting network game
- 1047431- No server but no error message.
- 1047422- java.net.SocketException: Connection reset
- 1047412- 2 players, same name -> Exception

Oct 13, 2004 Luke

- Bugs Fixed:
- 1047428 "no players" message goes away
- 1047414 Connected players list should auto update
- 1047439 Shutting down remote client crashes server
- 1047425 2 servers, same port -> Exception
- 1046385 pressing Backspace causes IllegalStateException

Oct 12, 2004 Luke

- Made map scroll when mouse is dragged outside the view port
- when building track.

Sep 18, 2004 Luke

- RFEs Implemented:
- 931581 Build Industry.
- 931594 Show which player is winning.
- 915955 Automatic Schedules.
- 931597 Graph showing total profits over time.
- 915957 Build track by dragging mouse.-
- 932630 Change speed from network clients.

Aug 14, 2004 Luke

- Added ConstJava ant target
- Note, ConstJava adds the keyword 'const' to java.  It can
- be typed /*=const */ so that the files remain valid java files.
- Fixed some mutability problems that it identified.

Aug 10, 2004 Luke

- Implemented City growth
- Work on deadlock and unexpected exception bugs.

Jul 26, 2004 Luke

- Apply Jan Tozicka's patch for bug 997088  (IllegalArgumentException in OneTileMoveVector.getInstance)

Jul 21, 2004 Luke

- Remove some circular dependencies.

Jul 07, 2004 Luke

- Fixed problem with unit tests in freerails.controller.net

Jul 07, 2004 Luke

- Bugs fixed:-
- 972866 Build track by dragging - only when build track selected

Jul 06, 2004 Luke

- RFEs Implemented:
- 915943 Sounds!
- Bugs fixed:-
- 984510 freerails.world.player.player; local class incompatible

Jun 25, 2004 Luke

- Bugs fixed:-
 - 979831 Stack traces printed out when running unit tests

Jun 17, 2004 Luke

- Apply Vincenzo Di Massa's station distance patch.
- Fixed DisplayModesComboBoxModels.removeDisplayModesBelow(.) so
- that it does not remove display modes when displayMode.getBitDepth() returns DisplayMode.BIT_DEPTH_MULTI

Jun 15, 2004 Luke

- Bugs fixed:-
- 972869 Crash when track under train removed.
- 972867 Signal towers do nothing
- - I've removed them!
- 972864 Deselect place-station-mode when track selected

Jun 14, 2004 Luke

- Bugs fixed:
- 948668 Building Station on Curve - Cursor changes function -
- 948671 Map City Overlays incorrect
- 967675 No trains/stations but train & station menus selectable
- 972738 Crash when station removed
- 967662 Bottom of terrain info tab cut off in 640*480 res.
- 972869 Crash when track under train removed.

Jun 13, 2004 Luke

- Bugs fixed:
- 948651 IP Address input should be checked immediately.
- 948649 Dialogue Box Behavior
- 967668 No supply & demand at new station
- 948672 Large numbers of active trains slows performance -

Jun 12, 2004 Luke

- Bugs Fixed:
- 967667 Cannot close multiple dialogue boxes.
- 967664 Fullscreen res. below 640x480 16bit selectable.
- 967666 Selected fullscreen resolution ignored.
- 967713 FPS counter obscures build menu
- 967660 Debug text sent to console
- 948679 Delete/Rebuild single section of track doesn't cost anything

Jun 9, 2004 Luke

- Bugs Fixed:
- 967673 Crash when building track close to edge of map

Jun 6, 2004 Luke

- Bugs Fixed:
 - 967677 OutOfMemoryError after starting several new games

Jun 6, 2004 Luke

- RFE implemented:
- 915960 Logging

Jun 5, 2004 Luke

- Bugs Fixed:
- 967129 Main map white on 1.5.0 beta 2
- 941743 Build train dialog closes without building train.
- 967214 EchoGameServerTest hangs

May 31, 2004 Luke

- Bugs Fixed:
- 948653 Crash after loading a saved game when one is not available.-
- 948665 "Show Details" on Train List doesn't work if no train is selected.
- 948659 Dialogue Box Behavior not deterministic
- 948663 Extra Close Button on Station List tab
- 948661 No Formal Specification
-  see /src/docs/freerails_1_0_functional_specification.html
- 948656 Non Movable Dialogue Boxes
- - -made dialogue boxes movable
- - -added option to show/hide station names, spheres of influence, and cargo waiting.

May 30, 2004 Luke

- Bugs Fixed:
- 948666 Crash when Building Train with Money < 0 and only one station

May 28, 2004 Luke

- Bugs Fixed:
- 948655 Can't see consist when there are more than 6 wagons
- 948675 Can't upgrade station types
- 948680 No way to tell sphere of influence for a station type

May 27, 2004 PM Luke

- Bugs Fixed:
- 948676 Waiting list is cut off
- 948673 Cost of Building track/stations not shown
- 948670 Removing non-existent track
- 948654 Locomotive graphic backwards

May 24, 2004 PM Luke

- Bug fixes for freerails.world.top.WorldDifferences

May 24, 2004 PM Luke

- Added class freerails.world.top.WorldDifferences  - may be useful for RFE 915957!

May 10, 2004 11:09:17 PM Luke

- Applied Jan Tozicka's first patch for 915957 (Build track by dragging mouse)

May 5, 2004 5:57:26 PM Luke

- Fix bug in SimpleAStarPathFinder spotted by Jan Tozicka.

Apr 30, 2004 6:30:56 PM Luke

- Applied Jan Tozicka's patch
- Implements 927165 (Quick start option)

Apr 21, 2004 1:46:57 AM Luke

- Fix DialogueBoxTester
- Tweak build.xml

Apr 11, 2004 2:56:23 AM Luke

- Added some javadoc comments.
- Added hashcode methods to classes that override equals.
- Code cleanup
- Let track be built on terrain of category 'Industry' and 'Resource'

Apr 9, 2004 11:42:12 PM Luke

- Fixed bug 891452 (2 servers same port, no error message)
- Fixed bug 868555 (Undo move by pressing backspace doesn't work)
- Fix for bug 910132 (Too easy to make money!)
- More work on bug 910902 (Game speed not stored on world object)

Apr 8, 2004 10:52:32 PM Luke

- Added website to CVS
- Added website deployment targets to build.xml

Apr 7, 2004 8:18:36 PM Luke

- Implemented  930716 (Scale overview map) by
- incorporating code from Railz.

Apr 6, 2004 6:28:50 PM Luke

- Fix selection of track type and build mode that was broken by the game speed patch.

Apr 6, 2004 1:28:44 AM Luke

- Implemented 915945 (Stations should not overlap)
- Increased the quality of scaled images returned by ImageManagerImpl

Apr 5, 2004 10:42:20 PM Luke

- Implemented 915952 (Boxes showing cargo waiting at stations)

Apr 5, 2004 6:04:01 PM Luke

- Fixed 910134 Demand for mail and passengers
- Updated javadoc comments in freerails.server.parser.

Apr 4, 2004 4:16:21 PM Luke

- Implemented 927152 Show change station popup when add station is clicked

Apr 3, 2004 12:18:18 AM Luke

- Apply Jan Tozicka's 2nd patch for 910902

Apr 2, 2004 12:01:57 AM Luke

- Fixed bug 910130 (Placement of harbours)

Apr 1, 2004 8:16:48 PM Luke

- Made trains stop for a couple of seconds at stations.
- 915947 Implement wait until full.

Apr 1, 2004 12:38:47 AM Luke

- Implemented
- - 910138 After building a train display train orders
- - 910143 After building station show supply and demand
- Started rewriting freerails in C#!

Mar 30, 2004 6:39:20 PM Luke

- Implemented 915949 (Balance sheet)
- Fixed bug where an exception was thrown if you moved the cursor
- when 'View Mode' was selected on the build menu.

Mar 29, 2004 7:13:13 PM Luke

- Implemented 915948 (Income statement)

Mar 27, 2004 9:43:29 PM Luke

- Updated coding guidelines.

Mar 15, 2004 1:15:28 AM Luke

- Added 'Show java properties' to about menu.

Mar 14, 2004 11:47:50 PM Luke

- Implemented 910123 (Add/remove cargo to cities more frequently).

Mar 13, 2004 3:45:44 PM Luke

- Fixed various bugs where exceptions were getting thrown.
- Stopped the client window getting displayed before the world
- is loaded from the server.

Mar 13, 2004 3:50:51 AM Luke

- Implemented 910126 (Train list on RHS panel)
- Started 915303 (Icons for buttons and tabs) - the
- tabs on the RHS now have icons instead of titles.

Mar 12, 2004 8:37:12 PM Luke

- Apply Jan Tozicka's patch for 910902 (Game speed not stored on world object).

Mar 9, 2004 1:11:50 AM Luke

- Increase client performance.  93FPS to 111FPS on my machine.
- Note, I get much higher FPS when the client and server are in different JVMs.

Mar 8, 2004 11:39:01 PM Luke

- Readied 640x480 fixed size windows mode.  It is useful
- for taking screen shots and making sure the dialogue boxes
- work in 640x480 fullscreen mode.

Mar 6, 2004 12:39:54 PM Luke

- Added Scott Bennett's terrain randomisation patch.

Mar 6, 2004 12:10:49 AM Luke

- Remove 'never read' local variables.
- Fixed bug 910135 Trains jump when game un paused
- Fixed bug 891360 Trains don't get built while game is paused

Mar 5, 2004 8:16:50 PM Luke

- Applied Jan Tozicka's patch for bug 900039 (No clear indication game is paused).

Mar 4, 2004 7:38:53 PM Luke

- Minor changes to coding guidelines.
- Fixed stale serialVersionUID problem in freerails.world.player.Player
- Made ant script insert build id into README and about.htm

Mar 3, 2004 10:02:11 PM Luke

- Apply Scott Bennett's removal_of_Loading_text patch.

Mar 3, 2004 1:25:27 AM Luke

- Implemented Request 905446 Track should be continuous
- Implemented Request 905444 Multi player support: different track

Mar 2, 2004 5:21:23 PM Luke

- Implemented Request 905443 Multi player support: different trains

Mar 1, 2004 10:33:52 PM Luke

- Implemented Request 905441 Multi player support: different bank accounts
- Note, presently some of the dialogue boxes are not working.  This will
- be fixed as adding multi player support continues.

Feb 27, 2004 1:03:38 PM Luke

- Some fixes for DialogueBoxTester.

Feb 27, 2004 1:44:54 AM Luke

- Refactoring in preparation for multiplayer support.

Feb 26, 2004 9:48:04 PM Luke

- Applied Jan Tozicka's 'Shortcuts for gamespeed' (patch 904903).

Feb 21, 2004 2:14:47 AM Luke

- Fix 891359 - Javadoc package dependencies out of date
- Tidy up javadoc

Feb 20, 2004 6:34:03 PM Luke

- Fix 839371 - Goods & livestock wagons appear the same on train orders

Feb 20, 2004 12:31:15 PM Luke

- Fix bugs 867473 and 880450 (Intermittent deadlocks).

Feb 18, 2004 8:49:34 PM Luke

- Fix bug 839331 - set initial gamespeed to 'slow' instead of paused
- Fix bug 874416 (station icon hides after track-upgrade)
- Fix bug 839361 (Several industries of the same type in same city)
- Fix bug 891362 (Cancel button on select engine dialogue doesn't work )
- Fix bug 891431 No link between train list and train orders screens

Feb 18, 2004 1:12:22 AM Luke

- Removed unreachable code.
- Fix build.xml

Feb 17, 2004 1:22:31 AM Luke

- Apply move infrastructure patch.
- Apply OSX work around.

Feb 16, 2004 9:49:53 PM Luke

- Add new select station popup to train orders dialogue (fixes bug 891427).
- Add 'About' dialogue (fixes bug 891377)
- Add 'How to play' dialogue (fixes bug 891371)

Feb 6, 2004 12:23:44 AM Luke

- Apply Robert Tuck's patch to fix bug 880496 (User stuck after connection refused)

Feb 5, 2004 12:09:19 AM Luke

- Apply Robert Tuck's Mac OS X fixes.
- Uncomment out code in TrackMaintenanceMoveGenerator

Feb 4, 2004 4:07:21 PM Luke

- Add testDefensiveCopy() to WorldImplTest

Jan 19, 2004 7:21:04 PM Luke

- Applied Robert Tuck's launcher patch.

Dec 31, 2003 1:35:01 AM Luke

- Remove some unused code.
- Fix some things jlint moaned about - perhaps slightly pointless!

Dec 30, 2003 12:00:03 AM Luke

- Refactoring to change the threads in which moves are executed.
- - (i)  Moves are precommitted on the client's copy of the world
- - by the thread "AWT_EventQueue."
- - (ii) All moves are now executed on the server's copy of the world
- - in freerails.server.ServerGameEngine.update() by the thread "freerails server".
- - (iii) Moves received from the server are now executed on the clients copy of the
- - world in freerails.client.top.run() by the client thread by the  thread
- - "freerails client: ..."
- Moves are passed between threads using queues.
- Currently starting new games and loading games does not work.
- Removed most of the passing of mutexes between classes.

Dec 29, 2003 9:12:42 PM Luke

- Apply Robert Tuck's patch to BufferedTiledBackgroundRenderer.
- Make the client keep its own copy of the world object even when it is in the same VM as the server.

Dec 24, 2003 9:36:36 AM Luke

- Prepare for release.

Dec 23, 2003 23:15:58 PM Luke

- Refactoring to remove some cyclic dependencies.

Dec 20, 2003 1:53:19 AM Luke

- Apply part of Robert Tuck's performance patch.
- Update side on wagon graphics.
- Fix for bug 839355 (User not told why track cannot be built)

Dec 18, 2003 11:34:25 PM Luke

- Fix for bug 855729 (Game does not start on pre 1.4.2 VMs)

Dec 17, 2003 11:33:14 PM Luke

- Move UNITS_OF_CARGO_PER_WAGON constant to WagonType.

Dec 17, 2003 5:50:09 PM Luke

- Applied Robert Tuck's patch to fix apparent network lag.
- Tweaked 'format' ant target so that it does not format files that are up to date.

Dec 13, 2003 11:38:12 PM Luke

- Fix bug: stations on the trains schedule can now be changed again.

Dec 13, 2003 10:09:04 PM Luke

- Fixed bug: passengers are now demanded by cities and villages.
- Fixed bug: track maintenance cost is no longer equal to the build cost.
- Fixed bug 839366 (No feedback when trains arrive)

Dec 12, 2003 9:57:38 PM Luke

- Add Robert Tuck's new train graphics.

Dec 8, 2003 12:29:41 AM Luke

- Deprecate methods that take a mutex as a parameter.

Dec 6, 2003 12:20:24 AM Luke

- Apply source code formatting.

Dec 5, 2003 11:54:58 PM Luke

- Apply Robert Tucks move ahead patch.

Nov 30, 2003 2:27:01 PM Luke

- Fixed bug 839376 (Harbours are not painted properly)

Nov 30, 2003 1:24:28 AM Luke

- Fixed bug 839336 (Removing station train heading to causes Exception)

Nov 29, 2003 9:46:07 PM Luke

- Fixed bug 839392(After F8 to build station, position still follows mouse)
- Added jalopy 'format' target to build.xml

Nov 18, 2003 11:36:38 PM Luke

- Applied Robert Tuck's patch to fix the bug that occurred with 1 local client
- and 1 networked client in a 2nd VM.

Nov 10, 2003 3:25:23 PM Luke

- Made MoveExecuter non-static.
- Fixed bug 835337.
- Remove debug console output.

Nov 9, 2003 5:47:15 PM Luke

- Applied Robert Tuck's to fix bug 835241.

Nov 3, 2003 10:02:54 PM Luke

- Added Scott Bennett's enhanced city tile positioner.

03-Nov-2003 17:58:00 Luke

- Applied Robert Tuck's patches to update the launcher gui.
- Added Scott Bennett's extra Cities

18-Oct-2003 00:36:59 Luke

- Applied Robert Tuck's patch adding comments to ServerGameEngine.
- Other javadoc updates.

13-Oct-2003 21:59:01 Luke

- Applied Robert Tuck's network patch.

06-Oct-2003 23:45:23 Luke

- Fixed, I think, bug where trains went off the track.

04-Oct-2003 22:58:37 Luke

- Update CVS write permissions.

12-Sep-2003 21:00:00 Luke

- Add Robert Tuck's 'build' tab patch.

07-Sep-2003 22:00:00 Luke

- Added progress bar to show what is happening while
- the game is loading.

03-Sep-2003 21:50:00 Luke

- Added GUI to select display mode and number of clients.

28-Aug-2003 23:00:00 Luke

- Made train speed decrease with no of wagons.
- Made fare increase with distance travelled.
- Made CalcSupplyAtStations implement WorldListListener so
- that when a new station is added, its supply and demand is
- calculated by the server.

25-Aug-2003 23:00:00 Luke

- Added new Train orders dialogue.
- Made changes to train consist and schedule
- use Moves instead of changing the DB directly.
- Lots of other changes/fixes.

23-Aug-2003 15:45:00 Luke

- Removed cruft from the experimental package.
- Added a simple train list dialogue, accessible via the display menu.
- Made the engine images have transparent backgrounds and flipped them
- horizontally.

19-Aug-2003 00:59:00 Luke

- Applied Robert Tuck's patches that separated the
- client and server and allow you to start up two clients
- in the same JVM.
- Fixed painting bug that occurred when you started two
- clients.
- Major refactor to get the checkdep ant target working again.

11-Aug-2003 21:06:23 Luke

- You are now charged for track maintenance once per year.
- Cargo conversions occur when you deliver cargo to a station
- if an industry that converts the relevant cargo is within the
- station radius.

07-Aug-2003 23:26:02 Luke

- Applied Robert Tuck's patches to:
- (i) - Stop the Terrain Info panel from setting its preferred size to
- a fixed value.
- (ii) - Fix the issue with starting a new map and being unable to lay
- track.
- (iii) - Update remaining classes to use MoveExecuter.
- (iv) - Add the station info panel to the tab plane.
- (v)- - Add the train info/orders panel to the tab plane.

06-Aug-2003 20:54:47 Luke

- Applied Robert Tuck's patch to stop the splitpane divider
- getting focus when you press F8.
- Added the field 'constrained' to AddTransactionMove.  When
- this is set to true, the move will fail if you don't have enough
- cash.
- Made the building and upgrading track cash constrained.

04-Aug-2003 22:35:08 Luke

- Added 5 patches contributed by Robert Tuck
- (i)- - Changes to build.xml
- (ii)- Added 'View mode' to build menu.
- (iii)- Update to train schedule so that stations
- - - can be added and removed.
- (iv)- Changes to MoveChain and Addition of
- - - MoveExecutor.
- (v)- - Adding TabbedPane to the RHS with a tab
- - - to show terrain info.
- Made build xml copy the game controls html file.

02-Aug-2003 22:16:33 Luke

- Increased the number of resource tiles that are placed
- around cities.
- Fixed bug where cargo was added to trains before wagons
- were changed.

01-Aug-2003 20:57:10 Luke

- Fixed failure in DropOffAndPickupCargoMoveGeneratorTest.-

30-Jul-2003 21:51:56 Luke

- The player gets paid for delivering cargo, simply $1,000 per unit of cargo for now. See freerails.server.ProcessCargoAtStationMoveGenerator
- Fixed bug where 40 times too much cargo was being produced by changing figures in cargo_and_terrain.xml

27-Jul-2003 17:27:19 Luke

- Got DropOffAndPickupCargoMoveGeneratorTest running without failures.

21-Jul-2003 23:48:47 Luke

- The player now gets charged for:
 - building stations
 - building trains
 - upgrading track
- The text for the 'Game controls' dialogue box is now read in from a file rather than hard coded into the java.

08-Jul-2003 19:55:14 Luke

- Added initial balance of 1,000,000.
- Added prices to the track types defined in track_tiles.xml
- Updated the track XML parser to read in the track prices.
- Updated the build track moves that you get charged when you build track and get a small credit when you remove track.

07-Jul-2003 22:41:23 Luke

- Wrote 'Move' class to add financial transactions.
- Changed the class that adds cargo to stations so that- it adds 40 units per year if the station supplies one carload per year.

30-Jun-2003 17:29:00 Scott

- Cargo is now transferred correctly

28-Jun-2003 21:46:40 Luke

- Moved 'show game controls' menu item to the Help menu.
- Removed 'add cargo to stations' menu item from the game menu. Now cargo is added to stations at the start of each year.
- Set the initial game speed to 'moderate'.
- Added junit test for DropOffAndPickupCargoMoveGenerator

28-Jun-2003 13:18:04 Luke

- Moved classes to remove circular dependencies between- packages and updated the 'checkdep' ant target.

27-Jun-2003 23:46:15 Luke

- Added 'station of origin' field to CargoBatch and updated- the classes that use CargoBatch as appropriate.  It lets us
check whether a train has brought cargo back to the station- that it came from.

27-Jun-2003 23:25:54 Luke

- Added 'no change' option to train orders - it indicates that a train should keep whatever wagons it has when it stops
at a station.
- Made 'no change' the default order for new trains.

15-Jun_2003 23:17:00 Luke

- Improved the train orders dialogue to show- the current train consist and what cargo the train is carrying.

15-Jun_2003 23:17:00 Luke

- Fixed a load of problems with station building.
- stations can now only be built on the track
- building a station on a station now upgrades the station rather than adding a new one.
- building stations is now fully undoable in the same way as building track.

15-Jun_2003 20:55:00 Luke

- The map gets centered on the cursors when you press 'C';
- Pressing 'I' over a station brings up the station info dialogue box.
- Station radii are defined in track xml.
- The radius of the station type selected is shown on the map when the station types popup is visible.

14-Jun_2003 20:40:00 Luke

- Fixed bug where train went past station before turning around.

12-Jun_2003 20:40:00 Luke

- Improved javadoc comments.

11-Jun-2003 17:39:00 Luke

- Add change game speed submenu to game menu.

11-Jun-2003 17:26:00 Scott

- Implemented the Train/Station cargo dropoff and pickup feature, trains currently only pickup cargo. Its playable!

05-Jun-2003 21:57:45 Luke

- Added loadAndUnloadCargo(..) method to freerails.controller.pathfinder.TrainPathFinder

04-Jun-2003 23:04:47 Luke

- Updated freerails.world package overview.

01-Jun-2003 21:01:14 Luke

- The game times passes as real time passes.

01-Jun-2003 18:45:42 Luke

- Rewrote  ClientJFrame using Netbean's GUI editor.
- Added JLabels to show the date and available cash to ClientJFrame.

31-May-2003 23:58:04 Luke

- Pressing backspace now undoes building/removing track.

31-May-2003 21:28:30 Luke

- Make build track moves undoable.

31-May-2003 16:26:35 Luke

- Cargo gets added to stations based on what they supply, currently this is triggered by the 'Add cargo to stations' item
on the game menu.

19-May-2003 04:02:00 Scott

- Fixed the problem and deviation from the design ;-) of the station cargo calculations, there's now a temporary
menu item on the display menu. Use this to manually update the cargo supply rates.

18-May-2003 20:16:40 Luke

- Uses the new engine and wagon images on the select wagon, select engine, and train info dialogue boxes.

18-May-2003 00:52:00 Scott

- The cargo supplied to a station can now be viewed from the menu, although some more work is needed.

16-May-2003 22:51:07 Luke

- Now loads tile sized track images instead of grabbing- them from the big image.

12-May-2003 00:16:25 Luke

- Now prints out the time it takes to startup.

11-May-2003 20:24:44 Luke

- Track is shown on the overview map again.
- Rules about on what terrain track can be built have been added, this is driven by terrain category.

10-May-2003 00:02:13 Luke

- Rejig track and terrain graphics filenames following discussion on mailing list.
- Generated side-on and overhead train graphics.

05-May-2003 23:02:28 Luke

- Added station info dialogue.
- Fixed some bugs related to loading games and starting new games.

05-May-2003 18:00:53 Luke

- Changed map view classes to use a VolatileImage for a backbuffer.

05-May-2003 00:47:52 Luke

- Added terrain info dialogue.

03-May-2003 13:39:59 Luke

- Fixed river drawing bug.

02-May-2003 00:19:53 Luke

- The terrain graphics now get loaded correctly although there is a bug in the code that picks the right image
for rivers and other types that are drawn in the same way.

01-May-2003 00:39:49 Luke

- Split up track and terrain images.

28-Apr-2003 22:18:03 Luke

- Integrate new terrain and cargo xml into game. Temporarily lost terrain graphics.

19-Apr-2003 18:07:22 Luke

- More work on schedule GUI, you can set change the station that a train is going to.

19-Apr-2003 02:46:15 Luke


- Work on train schedule GUI.

16-Apr-2003 00:48:02 Luke

- Added NonNullElements WorldIterator which iterates over non-null elements
- Stations now get removed when you remove the track beneath them
- Station name renderer and train building and pathfinding classes updated to handle null values for stations gracefully.

10-Apr-2003 16:50:00 Scott

- Added City Names
- Added Random City Tile positioning.
- Cities are now no longer related to the image map. Positions are determined by the data in the south_america_cities.xml file.

04-Apr-2003 21:20:46 Luke

- Simple train schedules, set the 4 points on the track that trains will travel between by pressing F1 - F4- over the track.

04-Apr-2003 00:22:05 Luke

- Added package comments for javadoc.

22-Mar-2003 19:26:26 Luke

- Got the game running again!

19-Mar-2003 01:09:47 Luke

- Refactored to use the new world interface, does not run yet.

10-Mar-2003 17:52:45 Luke

- Fixed bug [ 684596 ] ant build failed

10-Mar-2003 17:22:16 Luke

- Added the MapViewJComponentMouseAdapter in MapViewJComponentConcrete.java contributed by Karl-Heinz Pennemann -
it scrolls the mainmap while pressing the second mouse button.

10-Mar-2003 17:20:43 Luke

- Added mnemonics contributed by Scott Bennett

24-Jan-2003 23:51:21 Luke

- Release refactorings.

12-Jan-2003 21:50:04 Luke

- Fixed javadoc errors.

12-Jan-2003 05:11:47 Luke

- Major refactoring
- added ant target, checkdep, to check that the dependencies between packages are in order.  What it does is copy the java files from
a package together with the java files from all the packages that it is allowed to depend on to a temporary directory.
It then compiles the java files from the package in question in the temporary director. If the- build succeeds, then the package dependencies are ok.

11-Jan-2003 02:44:16 Luke

- Refactoring and removing dead code.

10-Jan-2003 23:52:01 Luke

- Added package.html to freerails.moves
- refactoring to simplify the move classes.

22-Dec-2002 20:47:51 Luke Lindsay

- Added 'Newspaper' option to 'game' menu to test drawing on the glass panel.  The same technique can be used for dialogue boxes.

04-Dec-2002 21:36:42 Luke Lindsay

- The classes from the fastUtils library that are needed by freerails have been added to the freerails source tree, so
you no longer need fastUtils.jar on the classpath to compile and run freerails.

01-Dec-2002 15:53:02 Luke Lindsay

- Prepare for release.

01-Dec-2002 00:02:25 Luke Lindsay

- The trains no longer all move at the same speed.

30-Nov-2002 23:00:36 Luke Lindsay

- Load, save, and new game now work again.

30-Nov-2002 20:45:18 Luke Lindsay

- The path finder now controls train movement. Press t with the cursor over the track and all the trains will head for that point on the track.

27-Nov-2002 23:45:40 Luke Lindsay

- Wrote SimpleAStarPathFinder and a unit test for it. It seems to work. The next step is use it together with
NewFlatTrackExplorer to control train movement.

26-Nov-2002 21:32:20 Luke Lindsay

- More or less finished NewFlatTrackExplorer and incorporated it into the main game code.

26-Nov-2002 00:17:15 Luke Lindsay

- Wrote NewFlatTrackExplorer and NewFlatTrackExplorerTest, in preparation for writing a pathfinder.

24-Nov-2002 23:19:10 Luke Lindsay

- Rewrote PositionOnTrack and added PositionOnTrackTest. track positions can now be store as a single int.

24-Nov-2002 00:04:54 Luke Lindsay

- Organise imports.

09-Nov-2002 01:13:47 Luke Lindsay

- Changes to how the mainmap's buffer gets refreshed.vInstead of the refresh being driven by the cursor moving,
it is now driven by moves being received.  This means that it it will refresh even if the moves are generate by another
player.

08-Nov-2002 23:05:39 Luke Lindsay

- Stations can be built by pressing F8.
- The station types no longer appear with the track types on the build menu.

06-Nov-2002 20:24:10 Luke Lindsay

- Fixed 'jar_doc' task in build.xml

05-Nov-2002 22:53:11 Luke Lindsay

- Moving trains: the class ServerGameEngine has a list of TrainMover objects, which control the movement of individual trains.
  Movement is triggered by calls to ServerGameEngine.update() in the GameLoop's run() method.

03-Nov-2002 22:35:52 Luke Lindsay

- Improvements to TrainPosition and ChangeTrainPositionMove classes

28-Oct-2002 23:52:39 Luke Lindsay

- Fix javadoc warnings
- Add 'upload to sourceforge' task to build.xml
- Add world_javadoc task to build xml.

27-Oct-2002 21:54:46 Luke Lindsay

- Wrote ChangeTrainPositionMove and ChangeTrainPositionTest

27-Oct-2002 01:09:22 Luke Lindsay

- Wrote TrainPosition and TrainPositionTest to replace Snake class.

16-Oct-2002 22:58:03 Luke Lindsay

- Removed cyclic dependencies from the rest of the project.

16-Oct-2002 21:48:24 Luke Lindsay

- Refactored the freerails.world.* packages so that (1) freerails.world.* do not depend on any other freerails packages.
(2) there are no cyclic dependencies between any of the freerails.world.* packages.- hopefully this should make it easier to maintain.

13-Oct-2002 22:30:30 Luke Lindsay

- Added trains!  They don't move yet.  Hit F7 when the cursor is over the track to build one.

13-Oct-2002 00:24:18 Luke Lindsay :

- Add a task to build.xml that runs all junit tests.
- Change build.xml to work under Eclipse.

29-Sep-2002 20:24:18 Luke Lindsay :

- Reorganised package structure.
- Changed files that were incorrectly added to the cvs as binaries to text
- Small changes to build.xml so that the ChangeLog, TODO, and build.xml files are included in distributions.

- Changed DOMLoader so that it works correctly when reading files from a jar archive.

24-Sep-2002 23:49:14 Luke Lindsay :

- Updated TrainDemo, it now draws wagons rather than lines.

23-Sep-2002 23:35:30 Luke Lindsay :

- Wrote a simple demo, TrainDemo, to try out using FreerailsPathIterator and PathWalker to move trains along a track.
To see it in action, run: experimental.RunTrainDemo

22-Sep-2002 23:47:07 Luke Lindsay :

- wrote PathWalkerImpl and PathWalkerImplTest

19-Sep-2002 00:03:59 Luke Lindsay :

- wrote SimplePathIteratorImpl and SimplePathIteratorImplTest
- removed the method boolean canStepForward(int distance) from the interface PathWalker so that looking ahead is not required.

16-Sep-2002 21:36:50 Luke Lindsay :

- Updated and commented FreerailsPathIterator and PathWalker interfaces.
- build.xml written by JonLS added.  (Sorry, I - forgot to add it to the change log earlier.)

08-Sep-2002 22:11:24 Luke Lindsay :

- Wrote 'Snake' class that represents a train position.

26-Aug-2002 19:51:35 Luke Lindsay :

- Games can now be loaded and saved.-
- New games can be started.

18-Aug-2002 00:26:35 Luke Lindsay :

- More work on active rendering fixes for linux.-

28-Jul-2002 17:18:32 Luke Lindsay :

- Partially fixed active rendering under linux.-

04-Jul-2002 22:24:41 Luke Lindsay :

- Rotate method added to OneTileMoveVector

21 - Jun - 02 19 : 21 : 08 Luke Lindsay:

- Fullscreen mode
- GameLoop, freerails now uses active, rather than passive, rendering.
- Work on separating the model and view.
- Tilesets can be validated against rulesets - ViewLists.validate(Type t)  -
- FPS counter added.

04-Mar-2002 21:57:23 Luke Lindsay :

- Rearrange dependencies in freerails.world...

02-Mar-2002 19:02:48 Luke Lindsay :

- Reorganisation of package structure.

Sat Feb 16 22:48:00 2002 Luke Lindsay :

- Unrecoverable FreerailsExceptions replaced with standard unchecked exceptions.

Sat Feb 16 19:42:00 2002 Luke Lindsay :

- Changed CVS directory structure.

Sat Feb 16 15:00:00 2002 Luke Lindsay:

- This ChangeLog started!