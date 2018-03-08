# camel-msmq

Camel component for sending and receiving messages with Microsoft Message Queuing.

Was originally created by FUSE but is no longer maintained.

## Building

`mvn package` will build the jar, but not the native jni wrapper, nor run unit tests

Activating the `msmq` profile (ex:`mvn -P msmq package`) will also rebuild the jni wrapper and run unit tests. 
For that you will need: 

1) MQ installed and running
2) Visual C++ 2008 (works with the Express version too)
3) Command line with a functional nmake (execute vcvarsall.bat if needed) and properly defined JAVA_HOME