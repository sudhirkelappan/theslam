//
// Created by sudhir on 1/3/18.
//

#ifndef RATSLAMLIB_FORLOG_H
#define RATSLAMLIB_FORLOG_H

#include <android/log.h>

#define  LOG_TAG    "RATSLAM_NATIVE"

#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#include <iostream>

class LOGOUT 
{
  public:
    LOGOUT () 
    {

    }
 

   
  LOGOUT& operator << (const char* fmtstr)
  {

    return *this;

  }



  private:
  
  
};

#endif //RATSLAMLIB_FORLOG_H
