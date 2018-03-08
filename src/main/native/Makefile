RM=del
MV=move
CP=copy
MKDIR=md
RMDIR=rd
CXX=cl
LINK=link

INCLUDES="/I$(JAVA_HOME)\include" "/I$(JAVA_HOME)\include\win32"
CXXFLAGS=/EHsc $(INCLUDES)

SOURCES=msmq_native_support.cxx msmq_native_support_wrap.cxx

OBJS=$(SOURCES:.cxx=.obj)

LIBS=mqrt.lib

all: msmq_native_support.dll 

msmq_native_support.dll: $(SOURCES) $(OBJS)
			if exist $@ $(RM) $@
			if exist ..\..\..\src\main\resources\$@ $(RM) ..\..\..\src\main\resources\$@
			$(LINK) /DLL /OUT:$@ $(OBJS) $(LIBS)
			$(CP) $@ ..\..\..\src\main\resources

cxx.obj:
	if exist $@ $(RM) $@
	$(CXX) -c $(CXXFLAGS) -Fo$@ $<          			
			
msmq_native_support_wrap.cxx: msmq_native_support.h
	swig -I. -java -package org.apache.camel.component.msmq.native_support -c++ -module msmq_native_support msmq_native_support.h
	$(MV) *.java ..\java\org\apache\camel\component\msmq\native_support
	
clean:
	$(RM) *.dll *.dps *.obj *.pdb *.ilk *.exp
	$(RM) ..\..\..\src\main\resources\*.dll
