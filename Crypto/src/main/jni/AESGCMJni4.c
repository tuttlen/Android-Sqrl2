#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#include "aes.c"
#include "gcm.c"

#include "detect_platform.h"
#include "AESGCMJni4.h"
#ifndef DEBUG
	#define DEBUG 1
#endif
#define _base(x) ((x >= '0' && x <= '9') ? '0' : \
         (x >= 'a' && x <= 'f') ? 'a' - 10 : \
         (x >= 'A' && x <= 'F') ? 'A' - 10 : \
            '\255')
#define HEXOF(x) (x - _base(x))

void printArray(uchar *ptr, size_t length)          
{         
    //for statment to print values using array             
    size_t i = 0;

    for( ; ptr[i] && i<length; ++i )
    {    	
		printf("%02x", ptr[i]);
    }
    printf("\n");
}


void hexArray(uchar *hexString,uchar *ptr, size_t length)          
{   
    size_t i = 0;    
 
	hexString[0] ='\0';
    for( ; i<length; ++i )
    {	
		sprintf(hexString,"%s%02x",hexString, ptr[i]);	
    }
}

void simpleJsonObject(uchar *outSTring, uchar *ptr[], uchar *names[], size_t lengths[])
{	
	int i = 0;
	int range =(sizeof(lengths) / sizeof(size_t))+1;
	uchar outInter[256];
	outInter[0] ='\0';
	outSTring[0] ='\0';
	sprintf(outSTring,"{");
	
	for(; i < range; ++i)
	{
		hexArray((uchar *)&outInter,ptr[i],lengths[i]);
		sprintf(outSTring,"%s\"%s\":\"%s\"",outSTring,names[i],outInter);
		if(i != range-1)
		{
			sprintf(outSTring,"%s,",outSTring);
		}
	}
	sprintf(outSTring,"%s}",outSTring);
}

int hexdec2(unsigned char *output, unsigned char *hex, size_t length) {
     uchar *p;
     int i=0;

    for (p = hex;i < length; p+=2 ) {
	if( p && *p)
	{
           output[(p - hex) >> 1] = ((HEXOF(*p)) << 4) + HEXOF(*(p+1));

	} else
	{
	  output[i] = 0;
	}        
		i++;
    }
    return 0;
}

/*******************************************************************************
 *
 *  VERIFY_GCM_ENCRYPTION
 *
 *  Handles block type 0:  This is the first of the three routines, called by
 *  VERIFY_GCM, which reads the "gcm_test_vectors.bin" file block by block.
 *  It invokes the AES-GCM library "gcm_crypt_and_tag" function to encrypt
 *  the provided plaintext, then verifies the returned ciphertext and auth
 *  tag against the correct test vector data provided by the NIST file.
 */
int verify_gcm_encryption(
        const uchar *key,       // pointer to the cipher key
        size_t key_len,         // byte length of the key
        const uchar *iv,        // pointer to the initialization vector
        size_t iv_len,          // byte length of the initialization vector
        const uchar *aad,       // pointer to the non-ciphered additional data
        size_t aad_len,         // byte length of the additional AEAD data
        const uchar *pt,        // pointer to the plaintext SOURCE data
        const uchar *ct,        // pointer to the CORRECT cipher data
        size_t ct_len,          // byte length of the cipher data
        const uchar *tag,       // pointer to the CORRECT tag to be generated
        size_t tag_len,          // byte length of the tag to be generated
        uchar *ct_buf,
        uchar *tag_buf)
{
    int ret = 0;                // our return value
    gcm_context ctx;            // includes the AES context structure
    //uchar ct_buf[256];          // cipher text results for comparison
    //uchar tag_buf[16];          // tag result buffer for comparison

    gcm_setkey( &ctx, key, (const uint)key_len );   // setup our AES-GCM key

    // encrypt the NIST-provided plaintext into the local ct_buf and
    // tag_buf ciphertext and authentication tag buffers respectively.
    ret = gcm_crypt_and_tag( &ctx, ENCRYPT, iv, iv_len, aad, aad_len,
                             pt, ct_buf, ct_len, tag_buf, tag_len);
    ret |= memcmp( ct_buf, ct, ct_len );    // verify correct ciphertext
    ret |= memcmp( tag_buf, tag, tag_len ); // verify correct authentication tag

    gcm_zero_ctx( &ctx );       // not really necessary here, but good to do

    return ( ret );             // return any error 'OR' generated above
}


/*******************************************************************************
 *
 *  VERIFY_GCM_DECRYPTION
 *
 *  Handles block type 1:  This is the second of the three routines, called by
 *  VERIFY_GCM, which reads the "gcm_test_vectors.bin" file block by block.
 *  It invokes the AES-GCM library "gcm_auth_decrypt" function to decrypt the
 *  provided ciphertext, then verifies the returned plaintext and auth tag
 *  against the correct test vector data provided by the NIST file.
 */
int verify_gcm_decryption(
        const uchar *key,       // pointer to the cipher key
        size_t key_len,         // byte length of the key
        const uchar *iv,        // pointer to the initialization vector
        size_t iv_len,          // byte length of the initialization vector
        const uchar *aad,       // pointer to the non-ciphered additional data
        size_t aad_len,         // byte length of the additional AEAD data
        const uchar *pt,        // pointer to the plaintext SOURCE data
        const uchar *ct,        // pointer to the CORRECT cipher data
        size_t ct_len,          // byte length of the cipher data
        const uchar *tag,       // pointer to the CORRECT tag to be generated
        size_t tag_len,         // byte length of the tag to be generated
        uchar *pt_buf)
{
    int ret = 0;                // our return value
    gcm_context ctx;            // includes the AES context structure
    //uchar pt_buf[256];          // plaintext results for comparison

    gcm_setkey( &ctx, key, (const uint)key_len );   // setup our AES-GCM key

    // decrypt the NIST-provided ciphertext and auth tag into the local pt_buf 
    ret = gcm_auth_decrypt( &ctx, iv, iv_len, aad, aad_len,
                             ct, pt_buf, ct_len, tag, tag_len);
    ret |= memcmp( pt_buf, pt, ct_len );

    gcm_zero_ctx( &ctx );

    return ( ret );             // return any error 'OR' generated above
}


/*
 * Class:     AESGCMJni3
 * Method:    doEncryption
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_tuttlen_aesgcm_1android_AESGCMJni4_doDecryption
  (JNIEnv *env, jobject thisobject, jbyteArray inKey, jbyteArray inIV, jbyteArray inaad, jbyteArray intag, jbyteArray ciphertext)
{
    	int ret = 0;
    	uchar RecordType;

	jsize key_len = (*env)->GetArrayLength(env, inKey);
	jsize iv_len = (*env)->GetArrayLength(env, inIV);
	jsize aad_len = (*env)->GetArrayLength(env, inaad);
	jsize tag_len = (*env)->GetArrayLength(env, intag);
	jsize ct_len = (*env)->GetArrayLength(env, ciphertext);
	jsize pt_len =ct_len;
	uchar *key, *iv, *aad, *pt, *ct, *tag;
	
	key = (*env)->GetByteArrayElements(env, inKey, 0);
	iv = (*env)->GetByteArrayElements(env, inIV, 0);
	aad = (*env)->GetByteArrayElements(env, inaad, 0);
	ct = (*env)->GetByteArrayElements(env, ciphertext, 0);
	tag =(*env)->GetByteArrayElements(env, intag, 0);
	
	gcm_initialize(); 

 	uchar ct_buf[256];          // cipher text results for comparison
	uchar tag_buf[16];          // tag result buffer for comparison
	uchar pt_buf[256];

	gcm_context ctx;            // includes the AES context structure
	gcm_setkey( &ctx, key, (const uint)key_len );   // setup our AES-GCM key

    	// encrypt the NIST-provided plaintext into the local ct_buf and
    	// tag_buf ciphertext and authentication tag buffers respectively.
   
    ret = gcm_auth_decrypt( &ctx, iv, iv_len, aad, aad_len, ct,pt_buf,ct_len,tag,tag_len);
	if( !ret) 
	{
		printf("Authenticate!");
	} else
	{
		printf("Fail");
	}
	//2 bytes for hexstring
	uchar outCStr[pt_len*2];
	//printf("%d %d ",ct_len,pt_len);
	hexArray((uchar *)&outCStr,pt_buf,pt_len);

	(*env)->ReleaseByteArrayElements(env,inKey,key,JNI_ABORT); 
	(*env)->ReleaseByteArrayElements(env,inIV,iv,JNI_ABORT); 
	(*env)->ReleaseByteArrayElements(env,inaad,aad,JNI_ABORT);	
	(*env)->ReleaseByteArrayElements(env,intag,tag,JNI_ABORT);
	(*env)->ReleaseByteArrayElements(env,ciphertext,ct,JNI_ABORT);		
	
	return (*env)->NewStringUTF(env, outCStr);
}


/*
 * Class:     AESGCMJni3
 * Method:    doEncryption
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_tuttlen_aesgcm_1android_AESGCMJni4_doEncryption
  (JNIEnv *env, jobject thisObj, jbyteArray inKey, jbyteArray inIV, jbyteArray inaad, jbyteArray intag, jbyteArray plaintext)  
{
    	int ret = 0;
    	uchar RecordType;

	jsize key_len = (*env)->GetArrayLength(env, inKey);
	jsize iv_len = (*env)->GetArrayLength(env, inIV);
	jsize aad_len = (*env)->GetArrayLength(env, inaad);
	jsize tag_len = (*env)->GetArrayLength(env, intag);
	jsize pt_len = (*env)->GetArrayLength(env, plaintext);
	jsize ct_len =pt_len;
	uchar *key, *iv, *aad, *pt, *ct, *tag;
	
	key = (*env)->GetByteArrayElements(env, inKey, 0);
	iv = (*env)->GetByteArrayElements(env, inIV, 0);
	aad = (*env)->GetByteArrayElements(env, inaad, 0);
	pt = (*env)->GetByteArrayElements(env, plaintext, 0);
	tag =(*env)->GetByteArrayElements(env, intag, 0);
	
	gcm_initialize(); 

 	uchar ct_buf[256];          // cipher text results for comparison
	uchar tag_buf[16];          // tag result buffer for comparison
	uchar pt_buf[256];

	gcm_context ctx;            // includes the AES context structure
	gcm_setkey( &ctx, key, (const uint)key_len );   // setup our AES-GCM key

    // encrypt the NIST-provided plaintext into the local ct_buf and
    // tag_buf ciphertext and authentication tag buffers respectively.
    ret = gcm_crypt_and_tag( &ctx, ENCRYPT, iv, iv_len, aad, aad_len,  pt, ct_buf, ct_len, tag_buf, tag_len);
	
	//2 bytes for hexstring
	uchar* names[] ={"CipherText","Tag"};
	//todo statically adding the size of the name array
	uchar outCStr[ct_len*2+pt_len*2+13];
	uchar *ptr[] ={(uchar *)&ct_buf,(uchar *)&tag_buf};
	
	size_t lengths[] = {ct_len,tag_len};
	
	simpleJsonObject((uchar *)&outCStr, ptr,names, lengths);

	(*env)->ReleaseByteArrayElements(env,inKey,key,JNI_ABORT); 
	(*env)->ReleaseByteArrayElements(env,inIV,iv,JNI_ABORT); 
	(*env)->ReleaseByteArrayElements(env,inaad,aad,JNI_ABORT);
	(*env)->ReleaseByteArrayElements(env,plaintext,pt,JNI_ABORT);	
	(*env)->ReleaseByteArrayElements(env,intag,tag,JNI_ABORT);	
	
	return (*env)->NewStringUTF(env, outCStr);
}

