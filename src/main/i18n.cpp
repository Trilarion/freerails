/*
 * $Id$
 * Internalization support
 */

#include "i18n.h"

// Initializes NLS (Native Language Support)
// It enables to translate FreeRails to many languages :-) - rivo
void i18n_init()
{
  // We localize only messages currently
  setlocale(LC_MESSAGES, "");
  bindtextdomain("freerails", LOCALEDIR);
  textdomain("freerails");
}
