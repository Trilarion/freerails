#ifndef __FREERAILSLOG_H__
#define __FREERAILSLOG_H__

#include <stdio.h>

class FreeRailsLog {
  
 public:
  FreeRailsLog();
  FreeRailsLog(char *, ...);
  ~FreeRailsLog();

  void close();

private:
  static FILE *fp;


};



#endif /* __FREERAILSLOG_H__ */
