#include "FreeRailsLog.h"

#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <iostream>

FILE *FreeRailsLog::fp=fopen("freerails.log", "w");

FreeRailsLog::FreeRailsLog(){
    
}


void FreeRailsLog::close(){
  fclose(fp);
}

FreeRailsLog::~FreeRailsLog(){
  
}

FreeRailsLog::FreeRailsLog(char *format, ...){

  char output[512];
  
  va_list ap;
  va_start(ap, format);

  if(fp){
    vfprintf(fp,format,ap);
    fputs("\n",fp);
    vsprintf(output,format,ap);
    std::cout << output << std::endl; 
  }

  va_end(ap);
    
}

