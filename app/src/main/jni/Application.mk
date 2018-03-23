T=arm64-v8a
#T=x86_64
APP_ABI :=  $T
APP_PLATFORM := android-24
APP_STL := stlport_shared
APP_CPPFLAGS += -fexceptions
APP_CPPFLAGS += -frtti

