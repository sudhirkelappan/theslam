MY_LOCAL_PATH := $(call my-dir)

LOCAL_PATH := $(MY_LOCAL_PATH)

T=x86_64
#T=arm64-v8a



######################   Build The Native jni interface lib  #########################
include $(CLEAR_VARS)

LOCAL_MODULE := MyNativeLib

TARGET_ARCH_ABI := $T
#TARGET_ARCH_ABI := arm64-v8a
TARGET_PLATFORM := android-24

LOCAL_CPP_FEATURES := rtti exceptions


LOCAL_C_INCLUDES := $(MY_LOCAL_PATH)/ratslam  $(MY_LOCAL_PATH)/gri /home/sudhir/boost_1_61_0 /home/sudhir/boost_1_61_0/boost /home/sudhir/Android/Sdk/ndk-bundle/sources/cxx-stl/stlport/stlport

LOCAL_DISABLE_FATAL_LINKER_WARNINGS := true

LOCAL_LDLIBS := -llog


LOCAL_SRC_FILES := \
                   ratslam_jni.cpp \

LOCAL_SHARED_LIBRARIES := ratslam RgbLib


include $(BUILD_SHARED_LIBRARY)



######################   Build The RGB Conversion library  #########################
include $(CLEAR_VARS)

LOCAL_MODULE := RgbLib

TARGET_ARCH_ABI := $T
 
TARGET_PLATFORM := android-24

LOCAL_CPP_FEATURES := rtti exceptions


LOCAL_C_INCLUDES := $(MY_LOCAL_PATH)/ratslam  $(MY_LOCAL_PATH)/gri /home/sudhir/boost_1_61_0 /home/sudhir/boost_1_61_0/boost /home/sudhir/Android/Sdk/ndk-bundle/sources/cxx-stl/stlport/stlport


LOCAL_SRC_FILES := \
                   yuv2rgb/yuv420rgb888c.c \
                   yuv2rgb/yuv422rgb888c.c \


include $(BUILD_SHARED_LIBRARY)





######################   Build The RatSlam library  #########################
include $(CLEAR_VARS)

LOCAL_MODULE := ratslam

TARGET_ARCH_ABI := $T
 
TARGET_PLATFORM := android-24

LOCAL_CPP_FEATURES := rtti exceptions


LOCAL_C_INCLUDES := $(MY_LOCAL_PATH)/ratslam  $(MY_LOCAL_PATH)/gri /home/sudhir/boost_1_61_0 /home/sudhir/boost_1_61_0/boost /home/sudhir/Android/Sdk/ndk-bundle/sources/cxx-stl/stlport/stlport

LOCAL_LDLIBS := -llog

LOCAL_SRC_FILES := ratslam/Ratslam.cpp \
                   ratslam/Experience_Map.cpp \
                   ratslam/Pose_Cell_Network.cpp \
                   ratslam/Visual_Template_Match.cpp \


include $(BUILD_SHARED_LIBRARY)


