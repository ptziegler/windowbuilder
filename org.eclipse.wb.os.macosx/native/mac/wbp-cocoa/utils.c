/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
#include "utils.h"

#if (defined(__LP64__) && (!defined(WBP_ARCH64))) || defined(_WIN64)
#define WBP_ARCH64
#endif /*__LP64__*/  

void* unwrap_pointer(JNIEnv *env, jobject jptr) {
	jclass clazz;
#ifdef WBP_ARCH64
	jlong result;
#else
	jint result;
#endif
	static jmethodID getterMethod = NULL;
	if (jptr == NULL) {
		return NULL;
	}
	clazz = (*env)->GetObjectClass(env, jptr);
#ifdef WBP_ARCH64
	if (getterMethod == NULL) {
		getterMethod = (*env)->GetMethodID(env, clazz, "longValue", "()J");
	}
	result = (*env)->CallLongMethod(env, jptr, getterMethod);
#else
	if (getterMethod == NULL) {
		getterMethod = (*env)->GetMethodID(env, clazz, "intValue", "()I");
	}
	result = (*env)->CallIntMethod(env, jptr, getterMethod);
#endif
	(*env)->DeleteLocalRef(env, clazz);
	return (void*)result;
}
jobject wrap_pointer(JNIEnv *env, const void* ptr) {
	jclass clazz;
	jobject newObject;
	static jmethodID ctor = NULL;
	if (ptr == NULL) {
		return NULL;
	}
#ifdef WBP_ARCH64
	clazz = (*env)->FindClass(env, "java/lang/Long");
	if (ctor == NULL) {
		ctor = (*env)->GetMethodID(env, clazz, "<init>", "(J)V");
	}
	newObject = (*env)->NewObject(env, clazz, ctor, (jlong)ptr);
#else
	clazz = (*env)->FindClass(env, "java/lang/Integer");
	if (ctor == NULL) {
		ctor = (*env)->GetMethodID(env, clazz, "<init>", "(I)V");
	}
	newObject = (*env)->NewObject(env, clazz, ctor, (jint)ptr);
#endif
	(*env)->DeleteLocalRef(env, clazz);
	return newObject;
}
