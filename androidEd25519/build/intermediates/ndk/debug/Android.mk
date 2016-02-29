LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := ed25519_android
LOCAL_LDFLAGS := -Wl,--build-id
LOCAL_SRC_FILES := \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\Android.mk \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\Application.mk \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\com_github_dazoe_android_Ed25519.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\java_sha512.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\crypto_verify_32.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\fe_0.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\fe_1.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\fe_add.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\fe_cmov.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\fe_copy.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\fe_frombytes.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\fe_invert.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\fe_isnegative.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\fe_isnonzero.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\fe_mul.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\fe_neg.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\fe_pow22523.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\fe_sq.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\fe_sq2.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\fe_sub.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\fe_tobytes.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\ge_add.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\ge_double_scalarmult.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\ge_frombytes.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\ge_madd.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\ge_msub.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\ge_p1p1_to_p2.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\ge_p1p1_to_p3.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\ge_p2_0.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\ge_p2_dbl.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\ge_p3_0.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\ge_p3_dbl.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\ge_p3_tobytes.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\ge_p3_to_cached.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\ge_p3_to_p2.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\ge_precomp_0.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\ge_scalarmult_base.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\ge_sub.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\ge_tobytes.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\keypair.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\open.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\sc_muladd.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\sc_reduce.c \
	C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni\ed25519\sign.c \

LOCAL_C_INCLUDES += C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\main\jni
LOCAL_C_INCLUDES += C:\Users\in805\OneDrive\Documents\GitHub\Android-Sqrl2\androidEd25519\src\debug\jni

include $(BUILD_SHARED_LIBRARY)
