/*
 * VisAD system for interactive analysis and visualization of numerical
 * data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
 * Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
 * Tommy Jasmin.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA
 */

/*
 * Start a Java virtual machine, run the specified test, and clean up.
 *
 * On Solaris, this gets compiled with something like:
 *
 *   cc -I/usr/java/include -I/usr/java/include/solaris \
 *      -o display_test display_test.c \
 *      -L/usr/java/jre/lib/sparc -R/usr/java/jre/lib/sparc -ljvm
 *
 * (You will probably need to change all the "/usr/java" paths
 *  to whatever is appropriate on your machine.)
 *
 * On Linux, substitute 'linux' for 'solaris' and 'i386' (or the
 * appropriate architecture specification) for 'sparc'.
 *
 * Some implementations store libjvm in a subdirectory of jre/lib/<arch>,
 * so you may also need to change '-L/usr/java/jre/lib/sparc' to
 * something like '-L/usr/java/jre/lib/i386/hotspot'.
 */

#include <stdlib.h>
#include <stdio.h>

#include <jni.h>

#define DEFAULT_CLASSPATH        "."

/* Concatenate two strings */
static const char *
concat(const char *str1, const char *str2)
{
  size_t len1, len2;
  char *tmpbuf;

  /* catch null pointers */
  if (str1 == NULL) {
    return str2;
  } else if (str2 == NULL) {
    return str1;
  }

  /* catch empty strings */
  if (*str1 == 0) {
    return str2;
  } else if (*str2 == 0) {
    return str1;
  }

  /* get string lengths */
  len1 = strlen(str1);
  len2 = strlen(str2);

  /* allocate enough space to concatenated string */
  tmpbuf = (char *)malloc(len1 + len2 + 1);
  if (tmpbuf == NULL) {
    return NULL;
  }

  /* copy strings */
  strcpy(tmpbuf, str1);
  strcpy(tmpbuf+len1, str2);

  /* be extra-anal */
  tmpbuf[len1+len2] = 0;

  return tmpbuf;
}

/* Start a Java virtual machine.
 *
 * If no CLASSPATH environment variable is found in the user's environment,
 * the DEFAULT_CLASSPATH is used.
 */
static int
startJVM(JavaVM **jvmPtr, JNIEnv **envPtr)
{
  const char *classPath;
  JavaVMInitArgs jvm_args;
  JavaVMOption options[1];
  int nOptions = 0;

  classPath = getenv("CLASSPATH");
  if (classPath == NULL) {
    classPath = DEFAULT_CLASSPATH;
  }

  options[nOptions++].optionString = concat("-Djava.class.path=", classPath);

  /* print JNI-related errors */
  /* options[nOptions++].optionString = "-verbose:jni"; */

  jvm_args.version = JNI_VERSION_1_2;
  jvm_args.options = options;
  jvm_args.nOptions = nOptions;
  jvm_args.ignoreUnrecognized = 1;

  return JNI_CreateJavaVM(jvmPtr, (void **)envPtr, (void *)&jvm_args);
}

static jclass
getTestClass(JNIEnv *env, int num)
{
  char caseName[8];

  sprintf(caseName, "Test%02d", num);

  return (*env)->FindClass(env, caseName);
}

static jobject
toString(JNIEnv *env, jclass class, jobject obj)
{
  jmethodID toStringMethod;

  toStringMethod = (*env)->GetMethodID(env, class, "toString",
				 "()Ljava/lang/String;");
  if (toStringMethod == NULL) {
    return NULL;
  }

  return (*env)->CallObjectMethod(env, obj, toStringMethod);
}

static int
runTest(JNIEnv *env, int caseNum, jclass testClass, int argc, char *argv[])
{
  jobject testObj;
  jmethodID processArgs, startThreads;
  jclass string;
  jobject emptyString;
  jobjectArray argArray;
  int i;
  jobject classStr;
  const char *strChars;

  testObj = (*env)->AllocObject(env, testClass);
  if (testObj == NULL) {
    fprintf(stderr, "Couldn't get Test%02d object\n", caseNum);
    return 1;
  }

  /* Get the ID for the class' processArgs() method */
  processArgs = (*env)->GetMethodID(env, testClass, "processArgs",
                                         "([Ljava/lang/String;)Z");
  if (processArgs == NULL) {
    fprintf(stderr,
	    "Couldn't find \"Test%02d.processArgs(String[])\" method\n",
	    caseNum);
    return 2;
  }

  /* get the String class */
  string = (*env)->FindClass(env, "java/lang/String");
  if (string == NULL) {
    fprintf(stderr, "Couldn't find \"String\" class\n");
    return 3;
  }

  /* build an empty Java String */
  emptyString = (*env)->NewStringUTF(env, "");
  if (emptyString == NULL) {
    fprintf(stderr, "Couldn't create empty Java String\n");
    return 4;
  }

  /* build an array to hold the remaining arguments */
  argArray = (*env)->NewObjectArray(env, argc - 2, string, emptyString);
  if (argArray == NULL) {
    fprintf(stderr,
	    "Couldn't create args[] array for \"Test%02d.processArgs()\"\n",
            caseNum);
    return 5;
  }

  /* add the remaining arguments to the array */
  for (i = 2; i < argc; i++) {
    jobject argString = (*env)->NewStringUTF(env, argv[i]);

    (*env)->SetObjectArrayElement(env, argArray, i - 2, argString);
  }

  /* call the specified method */
  (*env)->CallBooleanMethod(env, testObj, processArgs, argArray);


  classStr = toString(env, testClass, testObj);
  if (classStr == NULL) {
    fprintf(stderr, "Couldn't execute Test%02d toString() method\n", caseNum);
    return 6;
  }

  strChars = (*env)->GetStringUTFChars(env, classStr, 0);

  printf(" %d%s\n", caseNum, strChars);

  (*env)->ReleaseStringUTFChars(env, classStr, strChars);

  /* Get the ID for the class' startThreads() method */
  startThreads = (*env)->GetMethodID(env, testClass, "startThreads", "()V");
  if (startThreads == NULL) {
    fprintf(stderr, "Couldn't find \"Test%02d.startThreads()\" method\n",
	    caseNum);
    return 7;
  }

  (*env)->CallVoidMethod(env, testObj, startThreads);

  /* done! */
  return 0;
}

static void
describeTests(JNIEnv *env)
{
  int n;
  jclass testClass;

  printf("To test VisAD's displays, run\n");
  printf("  java DisplayTest N, where N =\n");

  for (n = 0; 1; n++) {
    jobject testObj, classStr;
    const char *strChars;

    testClass = getTestClass(env, n);
    if (testClass == NULL) {
      break;
    }

    testObj = (*env)->AllocObject(env, testClass);
    if (testObj == NULL) {
      fprintf(stderr, "Couldn't get Test%02d object\n", n);
      continue;
    }

    classStr = toString(env, testClass, testObj);
    if (classStr == NULL) {
      fprintf(stderr, "Couldn't execute Test%02d toString() method\n", n);
      continue;
    }

    strChars = (*env)->GetStringUTFChars(env, classStr, 0);

    printf(" %d%s\n", n, strChars);

    (*env)->ReleaseStringUTFChars(env, classStr, strChars);
  }
}

/* Get rid of the Java virtual machine */
static void
destroyJVM(JavaVM *jvm)
{
  (*jvm)->DestroyJavaVM(jvm);
}

int
main(int argc, char *argv[])
{
  JavaVM *jvm;
  JNIEnv *env;
  int caseNum = -1;
  jclass skelClass = NULL;

  startJVM(&jvm, &env);

  if (argc > 1) {
    char *endPtr;

    caseNum = (int )strtol(argv[1], &endPtr, 10);
    if (endPtr == NULL || *endPtr != 0) {
      fprintf(stderr, "Bad display_test \"%s\"\n", argv[1]);
      caseNum = -1;
    }

    if (caseNum != -1) {
      skelClass = getTestClass(env, caseNum);
    }
  }

  if (skelClass != NULL) {
    runTest(env, caseNum, skelClass, argc, argv);
  } else {
    describeTests(env);
  }
   
  destroyJVM(jvm);

  return 0;
}
