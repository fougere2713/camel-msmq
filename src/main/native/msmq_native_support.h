/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#ifndef _MSMQ_NATIVE_SUPPORT_H_
#define _MSMQ_NATIVE_SUPPORT_H_

#ifdef SWIG

%javaconst(1);

%{
#include <MqOai.h>
#include <Mq.h>
#include <WTypes.h>
#include <map>
#include <string>
#include <stdexcept>
#include <iostream>
#include "msmq_native_support.h"
%}

%javaconst(0) MQMSG_DELIVERY_EXPRESS;      // From Mq.h
%javaconst(0) MQMSG_DELIVERY_RECOVERABLE;  // From Mq.h
%javaconst(0) PROPID_M_CORRELATIONID_SIZE; // From Mq.h
%javaconst(0) PROPID_M_MSGID_SIZE;         // From Mq.h
%javaconst(0) MQ_SEND_ACCESS;              // From Mq.h
%javaconst(0) MQ_RECEIVE_ACCESS;           // From Mq.h

%javaconst(0) INFINITE;                    // From WinBase.h

%javaconst(0) MAXMSGSIZE;

#define MQMSG_DELIVERY_EXPRESS               0 // From Mq.h
#define MQMSG_DELIVERY_RECOVERABLE           1 // From Mq.h
#define PROPID_M_CORRELATIONID_SIZE         20 // From Mq.h
#define PROPID_M_MSGID_SIZE                 20 // From Mq.h
#define MQ_RECEIVE_ACCESS           0x00000001 // From Mq.h
#define MQ_SEND_ACCESS              0x00000002 // From Mq.h

#define INFINITE                    0xFFFFFFFF // From WinBase.h

%include "arrays_java.i"
%include "carrays.i"
%array_class(signed char, ByteArray);

%include exception.i
%exception {
	try {
		$action
	} catch(const std::runtime_error& e) {
		SWIG_exception(SWIG_RuntimeError, e.what());
	} catch(...) {
		SWIG_exception(SWIG_RuntimeError,"Unknown exception");
	}
}

%pragma(java) jniclasscode=%{
	static {
		try {
			NativeLibraryLoader.loadLibrary("msmq_native_support");
		} catch (java.io.IOException e) {
			System.loadLibrary("msmq_native_support");
		}
	}
%}

%typemap(in) (void *buffer, long size) {
  /* %typemap(in) void * */
  $1 = jenv->GetDirectBufferAddress($input);
  $2 = (long)(jenv->GetDirectBufferCapacity($input));
}

/* These 3 typemaps tell SWIG what JNI and Java types to use */
%typemap(jni) (void *buffer, long size) "jobject"
%typemap(jtype) (void *buffer, long size) "java.nio.ByteBuffer"
%typemap(jstype) (void *buffer, long size) "java.nio.ByteBuffer"
%typemap(javain) (void *buffer, long size) "$javainput"
%typemap(javaout) (void *buffer, long size) {
    return $jnicall;
}
#else

#include <MqOai.h>
#include <Mq.h>
#include <WTypes.h>
#include <map>
#include <string>
#include <stdexcept>
#endif

#define MAXMSGSIZE 1024

class MsmqMessage {
	
	friend class MsmqQueue;
	
public:
	enum PropertyName {
		APPSPECIFIC,	
		ARRIVEDTIME,
		BODY,
		BODY_TYPE,
		BODY_SIZE,
		CORRELATIONID,
        DELIVERY,
		MSGID,
        PRIORITY,
        SENTTIME,
        TIME_TO_BE_RECEIVED,
		//Just to keep trak of the number of props
		NUMPROPS
	};

private:
	
	MQMSGPROPS  props;
	MSGPROPID   propIds[NUMPROPS];
	PROPVARIANT propVars[NUMPROPS];
	UCHAR       msgId[PROPID_M_MSGID_SIZE];
	UCHAR       corrId[PROPID_M_CORRELATIONID_SIZE];
	
public:

	MsmqMessage();
	virtual ~MsmqMessage();
	
	//PROPID_M_APPSPECIFIC
	void setAppSpecifc(unsigned int app);
	unsigned int getAppSpecific();
	
	//PROPID_M_ARRIVEDTIME
	unsigned int getArrivedTime();
	
	//PROPID_M_BODY
	void setMsgBody(signed char *body);
	void setMsgBodyWithByteBuffer(void *buffer, long size);
	
	//PROPID_M_BODY_TYPE
	unsigned int getBodyType();
	
	//PROPID_M_BODY_SIZE
	void setBodySize(unsigned int size);
	unsigned int getBodySize();
	
	//PROPID_M_CORRELATIONID
	void setCorrelationId(signed char corrid[PROPID_M_CORRELATIONID_SIZE]);
	void getCorrelationId(signed char corrid[PROPID_M_CORRELATIONID_SIZE]);
	
	//PROPID_M_DELIVERY
	void setDelivery(unsigned int delivery);
	unsigned int getDelivery();
	
	//PROPID_M_MSGID
	void getMsgId(signed char corrid[PROPID_M_MSGID_SIZE]);
	
	//PROPID_M_PRIORITY
	void setPriority(unsigned int priority);
	unsigned int getPriority();
	
	//PROPID_M_SENTTIME
	unsigned int getSentTime();
	
	//PROPID_M_TIME_TO_BE_RECEIVED
	void setTimeToBeReceived(unsigned int time);
	unsigned int getTimeToBeReceived();
	
};

class MsmqQueue {

	QUEUEHANDLE hQueue;

	bool m_isOpen;

public:

	MsmqQueue();

	~MsmqQueue();

	void open(const char* szMSMQQueuePath, int openmode);
	
	void sendMessage(const MsmqMessage& msg);

	bool receiveMessage(MsmqMessage msg, int timeout);

	void close(void);

	bool isOpen() const {
		return m_isOpen;
	}

	static void createQueue(char* pathName);
	
	static void deleteQueue(char* formatPathName);
};
#endif
