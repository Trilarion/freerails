/*
 * $Id$
 * Internationalization support
 */

#ifndef __I18N_H__
#define __I18N_H__

#define LOCALEDIR "/usr/local/share/locale"
void i18n_init();

// !!! If you don't want to enable NLS or if your system does not have
//    locale.h file, then comment next line
#define ENABLE_NLS 1

#ifdef ENABLE_NLS
#include <libintl.h>
#include <locale.h>

#define _(string) gettext(string)

#else

#define _(string) (string)
#define bindtextdomain(package, dir)
#define textdomain(domain)

#endif // ENABLE_NLS

#endif // __I18N_H__
