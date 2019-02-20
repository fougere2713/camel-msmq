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
#include "msmq_native_support.h"
#include<iostream>
#include<string>
#include<sstream>

MsmqMessage::MsmqMessage() {
	propIds[APPSPECIFIC] = PROPID_M_APPSPECIFIC;
	propIds[ARRIVEDTIME] = PROPID_M_ARRIVEDTIME;
	propIds[BODY] = PROPID_M_BODY;
	propIds[BODY_TYPE] = PROPID_M_BODY_TYPE;
	propIds[CORRELATIONID] = PROPID_M_CORRELATIONID;
	propIds[DELIVERY] = PROPID_M_DELIVERY;
	propIds[MSGID] = PROPID_M_MSGID;
	propIds[PRIORITY] = PROPID_M_PRIORITY;
	propIds[SENTTIME] = PROPID_M_SENTTIME;
	propIds[TIME_TO_BE_RECEIVED]= PROPID_M_TIME_TO_BE_RECEIVED;
	propIds[BODY_SIZE] = PROPID_M_BODY_SIZE;

	propVars[APPSPECIFIC].vt = VT_UI4;
	propVars[ARRIVEDTIME].vt = VT_UI4;
	propVars[BODY].vt = VT_VECTOR | VT_UI1;
	propVars[BODY_TYPE].vt = VT_UI4;
	propVars[CORRELATIONID].vt = VT_VECTOR | VT_UI1;
	propVars[DELIVERY].vt = VT_UI1;
	propVars[MSGID].vt = VT_VECTOR | VT_UI1;
	propVars[PRIORITY].vt = VT_UI1;
	propVars[SENTTIME].vt = VT_UI4;
	propVars[TIME_TO_BE_RECEIVED].vt = VT_UI4;
	propVars[BODY_SIZE].vt = VT_UI4;

	propVars[BODY].caub.pElems = NULL;
	propVars[BODY].caub.cElems = 0;
	propVars[BODY_TYPE].ulVal = VT_ARRAY | VT_UI1;
	propVars[BODY_SIZE].ulVal = MAXMSGSIZE;
	propVars[CORRELATIONID].caub.pElems = corrId;
	propVars[CORRELATIONID].caub.cElems = PROPID_M_CORRELATIONID_SIZE;
	propVars[MSGID].caub.pElems = msgId;
	propVars[MSGID].caub.cElems = PROPID_M_MSGID_SIZE;
	propVars[PRIORITY].bVal = 3;
	propVars[DELIVERY].bVal = MQMSG_DELIVERY_EXPRESS;
	propVars[TIME_TO_BE_RECEIVED].ulVal = INFINITE;

	props.cProp = NUMPROPS; // Number of message properties
	props.aPropID = propIds; // IDs of the message properties
	props.aPropVar = propVars; // Values of the message properties
	props.aStatus = 0;
}

MsmqMessage::~MsmqMessage() {
}

//PROPID_M_APPSPECIFIC
void MsmqMessage::setAppSpecifc(unsigned int app) {
	propVars[APPSPECIFIC].ulVal = app;
}

unsigned int MsmqMessage::getAppSpecific() {
	return propVars[APPSPECIFIC].ulVal;
}

//PROPID_M_ARRIVEDTIME
unsigned int MsmqMessage::getArrivedTime() {
	return propVars[ARRIVEDTIME].ulVal;
}

//PROPID_M_BODY
void MsmqMessage::setMsgBody(signed char *body) {
	propVars[BODY].caub.pElems = (UCHAR*) body;
}

void MsmqMessage::setMsgBodyWithByteBuffer(void *buffer, long size) {
	propVars[BODY].caub.pElems = (UCHAR*) buffer;
	propVars[BODY].caub.cElems = size;
	propVars[BODY_SIZE].ulVal = size;
}

//PROPID_M_BODY_TYPE
unsigned int MsmqMessage::getBodyType() {
	return propVars[BODY_TYPE].ulVal;
}

//PROPID_M_BODY_SIZE
void MsmqMessage::setBodySize(unsigned int size) {
	propVars[BODY].caub.cElems = size;
	propVars[BODY_SIZE].ulVal = size;
}

unsigned int MsmqMessage::getBodySize() {
	return propVars[BODY_SIZE].ulVal;
}

//PROPID_M_CORRELATIONID
void MsmqMessage::setCorrelationId(
		signed char corrid[PROPID_M_CORRELATIONID_SIZE]) {
	memcpy(propVars[CORRELATIONID].caub.pElems, corrid,
			PROPID_M_CORRELATIONID_SIZE);
}

void MsmqMessage::getCorrelationId(
		signed char corrid[PROPID_M_CORRELATIONID_SIZE]) {
	memcpy(corrid, propVars[CORRELATIONID].caub.pElems,
			PROPID_M_CORRELATIONID_SIZE);
}

//PROPID_M_DELIVERY
void MsmqMessage::setDelivery(unsigned int delivery) {
	propVars[DELIVERY].bVal = delivery;
}

unsigned int MsmqMessage::getDelivery() {
	return propVars[DELIVERY].bVal;
}

//PROPID_M_MSGID
void MsmqMessage::getMsgId(signed char msgid[PROPID_M_MSGID_SIZE]) {
	memcpy(msgid, propVars[MSGID].caub.pElems, PROPID_M_CORRELATIONID_SIZE);
}

//PROPID_M_PRIORITY
void MsmqMessage::setPriority(unsigned int priority) {
	propVars[PRIORITY].bVal = priority;
}

unsigned MsmqMessage::getPriority() {
	return propVars[PRIORITY].bVal;
}

//PROPID_M_SENTTIME
unsigned int MsmqMessage::getSentTime() {
	return propVars[SENTTIME].ulVal;
}

//PROPID_M_TIME_TO_BE_RECEIVED
void MsmqMessage::setTimeToBeReceived(unsigned int time) {
	propVars[TIME_TO_BE_RECEIVED].ulVal = time;
}

unsigned int MsmqMessage::getTimeToBeReceived() {
	return propVars[TIME_TO_BE_RECEIVED].ulVal;
}

MsmqQueue::MsmqQueue() :
	hQueue(NULL), m_isOpen(false) {
}

MsmqQueue::~MsmqQueue() {
}

void MsmqQueue::open(const char *szQueuePath, int openmode) {
	CHAR szDestFormatName[MQ_MAX_Q_NAME_LEN];
	WCHAR wszDestFormatName[2*MQ_MAX_Q_NAME_LEN];
	long accessmode = openmode; // bit field: MQ_{RECEIVE,SEND,PEEK,ADMIN}_ACCESS, 
	long sharemode = MQ_DENY_NONE;

	// Validate the input string.
	if (szQueuePath == NULL) {
		throw std::runtime_error("Error in opening the queue: invalid parameter");
	}

	strncpy(szDestFormatName, szQueuePath, sizeof(szDestFormatName)
			/sizeof(CHAR));

	// convert to wide characters;
	if (MultiByteToWideChar( (UINT) CP_ACP, (DWORD) 0,
			(LPCSTR) szDestFormatName, (int) sizeof(szDestFormatName),
			(LPWSTR) wszDestFormatName, (int) sizeof(wszDestFormatName) ) == 0) {
		throw std::runtime_error("Error in opening the queue: invalid parameter");
	}

	HRESULT hr = MQ_OK;

	hr = MQOpenQueue(wszDestFormatName, // Format name of the queue
			accessmode, // Access mode
			sharemode, // Share mode
			&hQueue // OUT: Handle to queue
			);

	// Retry to handle AD replication delays. 
	//
	if (hr == MQ_ERROR_QUEUE_NOT_FOUND) {
		int iCount = 0;
		while ((hr == MQ_ERROR_QUEUE_NOT_FOUND) && (iCount < 120)) {
			// Wait a bit.
			iCount++;
			Sleep(50);

			// Retry.
			hr = MQOpenQueue(wszDestFormatName, accessmode, sharemode, &hQueue);
		}
	}

	if (FAILED(hr)) {
		MQCloseQueue(hQueue);
		throw std::runtime_error("Error in opening the queue");
	} else
		m_isOpen = true;
}

bool MsmqQueue::receiveMessage(MsmqMessage msg, int timeout) {
	long iMessageBodySize = MAXMSGSIZE;

	DWORD i = 0;
	HRESULT hr = MQ_OK;

	hr = MQReceiveMessage(hQueue, // Handle to the destination queue
			timeout, // Time out interval
			MQ_ACTION_RECEIVE, // Peek?  or Dequeue.  Receive action
			&msg.props, // Pointer to the MQMSGPROPS structure
			NULL, NULL, // No OVERLAPPED structure etc.
			NULL, // Cursor
			MQ_NO_TRANSACTION // MQ_SINGLE_MESSAGE | MQ_MTS_TRANSACTION | MQ_XA_TRANSACTION   
			);

	if (hr == MQ_ERROR_BUFFER_OVERFLOW) {
		throw std::runtime_error("Message body too big");
	}

	if (hr == MQ_ERROR_IO_TIMEOUT) {
		return false;
	} else if (FAILED(hr) && hr != MQ_ERROR_IO_TIMEOUT) {
		throw std::runtime_error("Error in receiving");
	}
	return true;
}

void MsmqQueue::sendMessage(const MsmqMessage& msg) {
	HRESULT hr = MQ_OK;

	// Call MQSendMessage to put the message to the queue. 
	hr = MQSendMessage(hQueue, // Handle to the destination queue
			const_cast<MQMSGPROPS*>(&msg.props), // Pointer to the MQMSGPROPS structure
			MQ_NO_TRANSACTION);

	// transactionFlag:  MQ_NO_TRANSACTION, MQ_MTS_TRANSACTION, MQ_XA_TRANSACTION, or MQ_SINGLE_MESSAGE 
	// see mq.h for details...

	if (FAILED(hr)) {
	    std::stringstream ss;
        ss << "Error in sending the message, code " << hr;
    	throw std::runtime_error(ss.str());
	}
}

void MsmqQueue::close() {
	HRESULT hr = MQ_OK;
	if (hQueue)
		hr = MQCloseQueue(hQueue);

	if (FAILED(hr)) {
		throw std::runtime_error("Error in closing the queue");
	} else
		m_isOpen = false;
}

void MsmqQueue::createQueue(char* szQueuePath) {
	CHAR szDestFormatName[MQ_MAX_Q_NAME_LEN];
	WCHAR wszDestFormatName[2*MQ_MAX_Q_NAME_LEN];

	// Validate the input string.
	if (szQueuePath == NULL) {
		throw std::runtime_error("Error in opening the queue: invalid parameter");
	}

	strncpy(szDestFormatName, szQueuePath, sizeof(szDestFormatName)
			/sizeof(CHAR));

	// convert to wide characters;
	if (MultiByteToWideChar( (UINT) CP_ACP, (DWORD) 0,
			(LPCSTR) szDestFormatName, (int) sizeof(szDestFormatName),
			(LPWSTR) wszDestFormatName, (int) sizeof(wszDestFormatName) ) == 0) {
		throw std::runtime_error("Error in creating the queue: invalid parameter");
	}

	// Define the maximum number of queue properties.
	const int NUMBEROFPROPERTIES = 1;

	// Define a queue property structure and the structures needed to initialize it.
	MQQUEUEPROPS QueueProps;
	MQPROPVARIANT aQueuePropVar[NUMBEROFPROPERTIES];
	QUEUEPROPID aQueuePropId[NUMBEROFPROPERTIES];
	HRESULT aQueueStatus[NUMBEROFPROPERTIES];
	HRESULT hr = MQ_OK;

	// Set queue properties.
	DWORD cPropId = 0;
	aQueuePropId[cPropId] = PROPID_Q_PATHNAME;
	aQueuePropVar[cPropId].vt = VT_LPWSTR;
	aQueuePropVar[cPropId].pwszVal = wszDestFormatName;
	cPropId++;

	// Initialize the MQQUEUEPROPS structure.
	QueueProps.cProp = cPropId; // Number of properties
	QueueProps.aPropID = aQueuePropId; // IDs of the queue properties
	QueueProps.aPropVar = aQueuePropVar; // Values of the queue properties
	QueueProps.aStatus = aQueueStatus; // Pointer to the return status


	// Call MQCreateQueue to create the queue.
	WCHAR wszFormatNameBuffer[256];
	DWORD dwFormatNameBufferLength = sizeof(wszFormatNameBuffer)
			/sizeof(wszFormatNameBuffer[0]);
	hr = MQCreateQueue(NULL, // Security descriptor
  			           &QueueProps, // Address of queue property structure
			           wszFormatNameBuffer, // Pointer to format name buffer
		   	           &dwFormatNameBufferLength); // Pointer to receive the queue's format name length

	if(FAILED(hr))
		throw std::runtime_error("Error in creating the queue");
}

void MsmqQueue::deleteQueue(char *formatPathName)
{
	CHAR szDestFormatName[MQ_MAX_Q_NAME_LEN];
	WCHAR wszDestFormatName[2*MQ_MAX_Q_NAME_LEN];

	// Validate the input string.
	if (formatPathName == NULL) {
		throw std::runtime_error("Error in deleting the queue: invalid parameter");
	}

	strncpy(szDestFormatName, formatPathName, sizeof(szDestFormatName)
			/sizeof(CHAR));

	// convert to wide characters;
	if (MultiByteToWideChar( (UINT) CP_ACP, (DWORD) 0,
			(LPCSTR) szDestFormatName, (int) sizeof(szDestFormatName),
			(LPWSTR) wszDestFormatName, (int) sizeof(wszDestFormatName) ) == 0) {
		throw std::runtime_error("Error in opening the queue: invalid parameter");
	}

	HRESULT hr = MQ_OK;
	
	hr = MQDeleteQueue(wszDestFormatName);
	if(FAILED(hr))
		throw std::runtime_error("Error in deleting the queue");
}
