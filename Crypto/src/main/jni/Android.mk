LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := aesgcm4
LOCAL_SRC_FILES := AESGCMJni4.c
#LOCAL_STATIC_LIBRARIES := boost_serialization_static
LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)

#$(call import-module,boost/1.57.0)
