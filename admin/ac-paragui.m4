# Configure paths for ParaGUI
# Alexander Pipelka 17.05.2000
# stolen from Sam Lantinga 
# stolen from Manish Singh
# stolen back from Frank Belew
# stolen from Manish Singh
# Shamelessly stolen from Owen Taylor

# Stolen for FreeRails by Rivo Laks from people above

dnl AM_PATH_PARAGUI([MINIMUM-VERSION, [ACTION-IF-FOUND [, ACTION-IF-NOT-FOUND]]])
dnl Test for PARAGUI, and define PARAGUI_CFLAGS and PARAGUI_LIBS
dnl
AC_DEFUN(AM_PATH_PARAGUI,
[dnl 
dnl Get the cflags and libraries from the paragui-config script
dnl
AC_ARG_WITH(paragui-prefix,[  --with-paragui-prefix=PFX   Prefix where PARAGUI is installed (optional)],
            paragui_prefix="$withval", paragui_prefix="")
AC_ARG_WITH(paragui-exec-prefix,[  --with-paragui-exec-prefix=PFX Exec prefix where PARAGUI is installed (optional)],
            paragui_exec_prefix="$withval", paragui_exec_prefix="")
AC_ARG_ENABLE(paraguitest, [  --disable-paraguitest       Do not try to compile and run a test PARAGUI program],
		    , enable_paraguitest=yes)

  if test x$paragui_exec_prefix != x ; then
     paragui_args="$paragui_args --exec-prefix=$paragui_exec_prefix"
     if test x${PARAGUI_CONFIG+set} != xset ; then
        PARAGUI_CONFIG=$paragui_exec_prefix/bin/paragui-config
     fi
  fi
  if test x$paragui_prefix != x ; then
     paragui_args="$paragui_args --prefix=$paragui_prefix"
     if test x${PARAGUI_CONFIG+set} != xset ; then
        PARAGUI_CONFIG=$paragui_prefix/bin/paragui-config
     fi
  fi

  AC_PATH_PROG(PARAGUI_CONFIG, paragui-config, no)
  min_paragui_version=ifelse([$1], ,0.11.0,$1)
  AC_MSG_CHECKING(for PARAGUI - version >= $min_paragui_version)
  no_paragui=""
  if test "$PARAGUI_CONFIG" = "no" ; then
    no_paragui=yes
  else
    PARAGUI_CFLAGS=`$PARAGUI_CONFIG $paraguiconf_args --cflags`
    PARAGUI_LIBS=`$PARAGUI_CONFIG $paraguiconf_args --libs`

    paragui_major_version=`$PARAGUI_CONFIG $paragui_args --version | \
           sed 's/\([[0-9]]*\).\([[0-9]]*\).\([[0-9]]*\)/\1/'`
    paragui_minor_version=`$PARAGUI_CONFIG $paragui_args --version | \
           sed 's/\([[0-9]]*\).\([[0-9]]*\).\([[0-9]]*\)/\2/'`
    paragui_micro_version=`$PARAGUI_CONFIG $paragui_config_args --version | \
           sed 's/\([[0-9]]*\).\([[0-9]]*\).\([[0-9]]*\)/\3/'`
    if test "x$enable_paraguitest" = "xyes" ; then
      ac_save_CFLAGS="$CFLAGS"
      ac_save_LIBS="$LIBS"
      CXXFLAGS="$CFLAGS $PARAGUI_CFLAGS"
      LIBS="$LIBS $PARAGUI_LIBS"
dnl
dnl Now check if the installed PARAGUI is sufficiently new. (Also sanity
dnl checks the results of paragui-config to some extent
dnl
      rm -f conf.paraguitest
      AC_LANG([C++])
      AC_TRY_RUN([
#include "paragui.h"
#include "pgapplication.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

char*
my_strdup (char *str)
{
  char *new_str;
  
  if (str)
    {
      new_str = (char *)malloc ((strlen (str) + 1) * sizeof(char));
      strcpy (new_str, str);
    }
  else
    new_str = NULL;
  
  return new_str;
}

int main (int argc, char *argv[])
{
  int major, minor, micro;
  char *tmp_version;

  PG_Application app;
  
  /* This hangs on some systems (?)
  system ("touch conf.paraguitest");
  */
  { FILE *fp = fopen("conf.paraguitest", "a"); if ( fp ) fclose(fp); }

  /* HP/UX 9 (%@#!) writes to sscanf strings */
  tmp_version = my_strdup("$min_paragui_version");
  if (sscanf(tmp_version, "%d.%d.%d", &major, &minor, &micro) != 3) {
     printf("%s, bad version string\n", "$min_paragui_version");
     exit(1);
   }

   if (($paragui_major_version > major) ||
      (($paragui_major_version == major) && ($paragui_minor_version > minor)) ||
      (($paragui_major_version == major) && ($paragui_minor_version == minor) && ($paragui_micro_version >= micro)))
    {
      return 0;
    }
  else
    {
      printf("\n*** 'paragui-config --version' returned %d.%d.%d, but the minimum version\n", $paragui_major_version, $paragui_minor_version, $paragui_micro_version);
      printf("*** of PARAGUI required is %d.%d.%d. If paragui-config is correct, then it is\n", major, minor, micro);
      printf("*** best to upgrade to the required version.\n");
      printf("*** If paragui-config was wrong, set the environment variable PARAGUI_CONFIG\n");
      printf("*** to point to the correct copy of paragui-config, and remove the file\n");
      printf("*** config.cache before re-running configure\n");
      return 1;
    }
}

],, no_paragui=yes,[echo $ac_n "cross compiling; assumed OK... $ac_c"])
       CFLAGS="$ac_save_CFLAGS"
       LIBS="$ac_save_LIBS"
     fi
  fi
  if test "x$no_paragui" = x ; then
     AC_MSG_RESULT(yes)
     ifelse([$2], , :, [$2])     
  else
     AC_MSG_RESULT(no)
     if test "$PARAGUI_CONFIG" = "no" ; then
       echo "*** The paragui-config script installed by PARAGUI could not be found"
       echo "*** If PARAGUI was installed in PREFIX, make sure PREFIX/bin is in"
       echo "*** your path, or set the PARAGUI_CONFIG environment variable to the"
       echo "*** full path to paragui-config."
     else
       if test -f conf.paraguitest ; then
        :
       else
          echo "*** Could not run PARAGUI test program, checking why..."
          CFLAGS="$CFLAGS $PARAGUI_CFLAGS"
          LIBS="$LIBS $PARAGUI_LIBS"
          AC_TRY_LINK([
#include "paragui.h"
#include <stdio.h>
],      [ return 0; ],
        [ echo "*** The test program compiled, but did not run. This usually means"
          echo "*** that the run-time linker is not finding PARAGUI or finding the wrong"
          echo "*** version of PARAGUI. If it is not finding PARAGUI, you'll need to set your"
          echo "*** LD_LIBRARY_PATH environment variable, or edit /etc/ld.so.conf to point"
          echo "*** to the installed location  Also, make sure you have run ldconfig if that"
          echo "*** is required on your system"
	  echo "***"
          echo "*** If you have an old version installed, it is best to remove it, although"
          echo "*** you may also be able to get things to work by modifying LD_LIBRARY_PATH"],
        [ echo "*** The test program failed to compile or link. See the file config.log for the"
          echo "*** exact error that occured. This usually means PARAGUI was incorrectly installed"
          echo "*** or that you have moved PARAGUI since it was installed. In the latter case, you"
          echo "*** may want to edit the paragui-config script: $PARAGUI_CONFIG" ])
          CFLAGS="$ac_save_CFLAGS"
          LIBS="$ac_save_LIBS"
       fi
     fi
     PARAGUI_CFLAGS=""
     PARAGUI_LIBS=""
     ifelse([$3], , :, [$3])
  fi
  AC_SUBST(PARAGUI_CFLAGS)
  AC_SUBST(PARAGUI_LIBS)
  rm -f conf.paraguitest
])

dnl SDL + SDL add-ons + ParaGUI checker
AC_DEFUN(AM_FREERAILS_PARAGUI,
[
  dnl I assume, that if SDL/ParaGUI isn't found, then user can't build FreeRails
  dnl (ParaGUI is last chance)
  SDL_MINVERSION=1.2.0
  PARAGUI_MINVERSION=1.0.0
  
  dnl Check for SDL
  AM_PATH_SDL($SDL_MINVERSION, sdl_found=yes, sdl_found=no)
  if test $sdl_found = "no"; then
    dnl No SDL, quit
    AC_MSG_ERROR([SDL version $SDL_MINVERSION not found!])
  fi

  dnl Get SDL prefix
  SDL_PREFIX=`sdl-config --prefix`
  AC_SUBST(SDL_PREFIX)

  dnl Check for SDL_ttf
  have_SDL_ttf_inc=no
  have_SDL_ttf_lib=no
  AC_CHECK_LIB(SDL_ttf, TTF_Init, have_SDL_ttf_lib=yes)
  if test $have_SDL_ttf_lib = "no"; then
    AC_MSG_ERROR([SDL_ttf library not found!])
  fi
  AC_CHECK_HEADER($SDL_PREFIX/include/SDL/SDL_ttf.h, have_SDL_ttf_inc=yes)
  if test $have_SDL_ttf_inc = "no"; then
    AC_MSG_ERROR([SDL_ttf headers not found!])
  fi

  dnl Check for SDL_image
  have_SDL_image_inc=no
  have_SDL_image_lib=no
  AC_CHECK_LIB(SDL_image, IMG_Load, have_SDL_image_lib=yes)
  if test $have_SDL_image_lib = "no"; then
    AC_MSG_ERROR([SDL_image library not found!])
  fi
  AC_CHECK_HEADER($SDL_PREFIX/include/SDL/SDL_image.h, have_SDL_image_inc=yes)
  if test $have_SDL_image_inc = "no"; then
    AC_MSG_ERROR([SDL_image headers not found!])
  fi

  dnl Check for FreeType
  have_freetype=no
  AC_CHECK_LIB(ttf, TT_Init_FreeType, have_freetype=yes)
  if test $have_freetype = "no"; then
    AC_MSG_ERROR([FreeType library not found])
  fi

  dnl Finally, check for ParaGUI
  AM_PATH_PARAGUI($PARAGUI_MINVERSION, paragui_found=yes, paragui_found=no)
  if test $paragui_found = "no"; then
    AC_MSG_ERROR([ParaGUI $PARAGUI_MINVERSION not found!])
  fi

  dnl If we've made it to this far, user has everything we need!
  dnl Variables should be set by AM_PATH_SDL and AM_PATH_PARAGUI
])
