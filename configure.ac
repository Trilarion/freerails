dnl without this order in this file, autoconf will not work!
dnl the argument is a source file out of your sources. But
dnl acinclude.m4 makes the job for all programs ;-)
dnl Process this file with autoconf to produce a configure script.

AC_PREREQ(2.54)
AC_INIT([freerails],[0.0.7],[bugs])

AC_CONFIG_SRCDIR([acinclude.m4]) dnl a source file from your sub dir

dnl This is so we can use kde-common
AC_CONFIG_AUX_DIR(admin)

dnl This ksh/zsh feature conflicts with `cd blah ; pwd`
dnl unset CDPATH

dnl Checking host/target/build systems, for make, install etc.
AC_CANONICAL_TARGET([]) 

AM_INIT_AUTOMAKE

dnl Perform program name transformation
AC_CHECK_COMPILERS
AC_PROG_RANLIB
AC_PROG_LIBTOOL

AC_PREFIX_DEFAULT(/usr/local)
if test "x$prefix" = "xNONE"; then
  prefix=$ac_default_prefix
  ac_configure_args="$ac_configure_args --prefix $prefix"
fi

dnl generate the config header
AC_CONFIG_HEADERS(config.h:config.h.in) dnl at the distribution this done

dnl create only shared libtool-libraries
AC_ENABLE_SHARED(yes)

dnl set the following to yes, if you want to create static
dnl libtool-libraries
AC_ENABLE_STATIC(no)

dnl for NLS support. Call them in this order!
dnl WITH_NLS is for the po files
AM_KDE_WITH_NLS
KDE_CHECK_FINAL

all_libraries="$all_libraries $USER_LDFLAGS"
all_includes="$all_includes $USER_INCLUDES"
AC_SUBST(all_includes)
AC_SUBST(all_libraries)

AC_SUBST(AUTODIRS)

dnl in this space add everything you want to check for
dnl examples are specific headers, libraries, etc.

dnl --------------------------------------------------------
dnl FreeRails tests - beginning
dnl --------------------------------------------------------

dnl Check whether to compile Qt or ParaGUI client
dnl Which client should we prefer ??? Qt ?
AC_ARG_WITH(qt-client,
    [  --with-qt-client        Use Qt version of client],
    [ qt_client=yes ],
    [ qt_client=check ])
AC_ARG_WITH(paragui-client,
    [  --with-paragui-client   Use ParaGUI version of client],
    [ paragui_client=yes ],
    [ paragui_client=check ])

if test "$qt_client" = "yes" && test "$paragui_client" = "yes"; then
  AC_MSG_ERROR([You specified both --with-qt-client and --with-paragui-client!
You must specify only one type of client to build!])
fi

if test "$qt_client" = "check" && test "$paragui_client" = "check"; then
  dnl No client specified, check
  AC_MSG_CHECKING([which client to compile:])
  dnl I prefer Qt :-)
  AC_PATH_QT
  if test "$found_qt" = "no"; then
    dnl No Qt, try ParaGUI
    AM_FREERAILS_PARAGUI
    dnl If ParaGUI isn't found we're quitted by now
    WRAPPER=paragui
    AC_SUBST(WRAPPER)
  else
    dnl Qt was found
    WRAPPER=qt
    AC_SUBST(WRAPPER)
  fi
elif test "$qt_client" = "yes"; then
  AC_PATH_QT("yes")
  dnl AC_PATH_QT exits itself if 1st argument is yes
  WRAPPER=qt
  AC_SUBST(WRAPPER)
elif test "$paragui_client" = "yes"; then
  AM_FREERAILS_PARAGUI
  WRAPPER=paragui
  AC_SUBST(WRAPPER)
fi

AM_CONDITIONAL(WRAPPER_QT, test $WRAPPER = qt)
AM_CONDITIONAL(WRAPPER_PARAGUI, test $WRAPPER = paragui)

dnl --------------------------------------------------------
dnl FreeRails tests - end
dnl --------------------------------------------------------

dnl add here all your Makefiles. These will be created by configure
AC_CONFIG_FILES([Makefile \
data/Makefile \
data/graphics/Makefile \
data/graphics/tilesets/Makefile \
data/graphics/tilesets/default/Makefile \
data/graphics/ui/Makefile \
data/graphics/ui/buttons/Makefile \
data/graphics/ui/cursors/Makefile \
src/Makefile \
src/controllers/Makefile \
src/elements/Makefile \
src/map/Makefile \
src/main/Makefile \
src/network/Makefile \
src/wrapper/Makefile \
src/wrapper/paragui/Makefile \
src/wrapper/qt/Makefile \
src/game/Makefile \
])

AC_OUTPUT
