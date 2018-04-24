***********
Development
***********

.. figure:: /images/design_client_server_overview.png
   :width: 14 cm
   :align: center

   Client/Server design overview.

Brief overview of the architecture
----------------------------------

Outdated!

The model and the view are separated. The classes that make up the model, referred to as the game world, are in
the freerails.world.* packages. The view classes are in the freerails.client.* packages.
The state of game world is stored by an instance of a class implementing the interface World.
This class is mutable - one can think of it as a specialised hashmap. All the objects that represent entities in the
world - values in the hashmap - are immutable. It follows that changes to the game world involve adding, removing or
replacing objects representing entities rather than changing their properties.

The client and server are separate. They communicate by sending objects to each other. This is done either by placing
objects on a queue or by sending serialized objects over a network connection. All objects passed between the client
and server are immutable and are instances of one of the following:

- Message2Server
- Message2Client
- MessageStatus
- PreMove
- PreMoveStatus
- Move
- MoveStatus

When a new game starts or a game is loaded, the server sends the client a copy of the World object (using an instance
of SetWorldMessage2Client) All changes to the game world that occur after the game has started, referred to as moves,
are done using the classes in the package freerails.move. Moves are either obtained from a PreMove object or constructed directly.

Coding Guidelines
-----------------

General suggestions
+++++++++++++++++++

- Package dependencies.
- Avoid circular dependencies between packages. I.e. if any classes in package A import classes from package B, then
  classes in package B should not import classes from package A.
- (ii) Follow the package dependency rules.
- Some of the GUI classes have been created using the NetBeans GUI editor. The .java files for these classes have corresponding .form files.
  Within the java files are protected sections of code, if you edit these files, please do not alter these sections by hand.
- Generally follow the sun java coding converions(http://java.sun.com/docs/codeconv/).
- Run all unit tests after making changes to see whether anything has broken. You can do this using the test Ant target.
- Check the javadoc Ant target runs without errors or warnings.
- Javadoc comments. Add a couple of sentences saying what the class does and the reason for its addition with a date. Also use the @author tag if you add a new class or make a significant change to an existing one.
- Use Code Formatters with care. Avoid reformatting whole files with different formatting schemes when you have only changed a few lines.
- Consider writing junit tests.
- Consider using assertions.
- Add //FIXME ..and //TODO comments if you spot possible problems in existing code.
- Use logging instead of System.out or System.err for debug messages. Each class should have its own logger, e.g.
  private static final Logger logger = Logger.getLogger(SomeClass.class.getName());

Submitting Patches
++++++++++++++++++

- Small patches that fix one thing, add one feature, or clean up one aspect of the code are generally the easiest to manage.
- Check list:

    (A) Is there patch against the lastest code in CVS? I.e. if you do a fresh CVS checkout of the freerails module, does the patch apply correctly.

    (B) If you have create new files, are they included in the patch? Look at the -N option.

    (C) Are binary files excluded from the diff file? Any new or modified binary files, \*.png, \*.wav etc, should be included separately.

    (D) Do the following Ant targets work without warnings or errors when the patch is applied against a fresh CVS checkout?

    (E) Does the patch have a meaningful filename? E.g. bug910902_patch1.diff.

- Submit patches using the patch or bug tracker on the freerails sourceforge page.

Recommended reading
+++++++++++++++++++

- Effective Java (http://java.sun.com/docs/books/effective/) (sample chapters online)
- Java Platform Performance (http://java.sun.com/docs/books/performance/) (available online)
- User Interface Design for Programmers (http://www.joelonsoftware.com/uibook/chapters/fog0000000057.html) (available online)



