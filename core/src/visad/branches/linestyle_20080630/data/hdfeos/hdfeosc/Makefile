SLIB = hdfeos
SLIBDIR = .

# JAVADIR should be the top-level JDK1.2 directory
# JAVAINCS should list all the necessary include files under JAVADIR
#
JAVADIR = /opt/java
J_INC =  ${JAVADIR}/include
JAVAINCS = -I${J_INC} -I${J_INC}/${ARCH}
JH = javah

HDFINCS = -I${HDF_INC}
HDFEOSINCS = -I${HDFEOS_INC}

HDFLIBS = $(HDF_LIB)/libmfhdf.a $(HDF_LIB)/libdf.a $(HDF_LIB)/libjpeg.a $(HDF_LIB)/libz.a
HDFEOSLIBS = $(HDFEOS_LIB)/libhdfeos.a $(HDFEOS_LIB)/libGctp.a ./libhdfeos_ext.a

SHCFLAGS =

CC = cc
AR = ar -r

LD = $(CC)

OBJECTS = SWopenImp.o \
	SWattachImp.o SWinqswathImp.o SWinqdimsImp.o \
	SWinqdatafieldsImp.o SWinqgeofieldsImp.o SWinqmapsImp.o \
	SWnentriesImp.o SWfieldinfoImp.o SWfdimsImp.o \
	SWreadfieldImp.o GDattachImp.o GDnentriesImp.o \
	GDinqdimsImp.o GDinqfieldsImp.o GDfdimsImp.o \
	GDfieldinfoImp.o GDopenImp.o GDinqgridImp.o \
	GDprojinfoImp.o GDgridinfoImp.o GDinqattrsImp.o \
	EHchkfidImp.o EHcloseImp.o GDreadfieldImp.o \
	EHgetcalImp.o GetNumericAttrImp.o SDattrinfoImp.o

.SUFFIXES : .java .class

.java.class:
	$(JC) $(JFLAGS) $<

default: all

all:: HdfeosLib.class

all:: visad_data_hdfeos_hdfeosc_HdfeosLib.h

visad_data_hdfeos_hdfeosc_HdfeosLib.h:
	$(JH) -jni visad.data.hdfeos.hdfeosc.HdfeosLib

all::
	@UNAME=`uname -s`; \
	case "$$UNAME" in \
	IRIX*) make irix;; \
	SunOS*) make solaris;; \
	Linux*) make linux;; \
	*) echo "Unknown system type $$UNAME"; exit 1;; \
	esac

hdfeos_ext: SWfdims.o GDfdims.o
	$(AR) libhdfeos_ext.a SWfdims.o GDfdims.o

SWfdims.o: SWfdims.c
	$(CC) $(SHCFLAGS) -c SWfdims.c $(HDFINCS) $(HDFEOSINCS)

GDfdims.o: GDfdims.c
	$(CC) $(SHCFLAGS) -c GDfdims.c $(HDFINCS) $(HDFEOSINCS)

.c.o:
	$(CC) $(SHCFLAGS) -c $*.c $(JAVAINCS) $(HDFINCS) $(HDFEOSINCS)

hdfeosLib: $(OBJECTS) hdfeos_ext
	$(LD) $(LDOPT) $(OBJECTS) $(HDFEOSLIBS) $(HDFLIBS) $(SYSLIBS) -o $(SLIBDIR)/lib$(SLIB).so

clean:
	rm -f so_locations
	rm -f $(SLIBDIR)/lib$(SLIB).so
	rm -f libhdfeos_ext.a
	rm -f *.o
	rm -f *.class

required_macros:
	@if [ -z "$(HDF_INC)" ]; then \
	  echo ""; \
	  echo "  HDF_INC is undefined."; \
	  echo ""; \
	  echo "  (Don't worry, this is normal.  It just means the optional"; \
	  echo "   HDF-EOSC library won't be supported.)"; \
	  echo ""; \
	  echo "  This 'make' will self-destruct in 1 second"; \
	  echo ""; \
	  exit 1; \
	fi
	@if [ -z "$(HDF_LIB)" ]; then \
	  echo ""; \
	  echo "  HDF_LIB is undefined."; \
	  echo ""; \
	  echo "  (Don't worry, this is normal.  It just means the optional"; \
	  echo "   HDF-EOSC library won't be supported.)"; \
	  echo ""; \
	  echo "  This 'make' will self-destruct in 1 second"; \
	  echo ""; \
	  exit 1; \
	fi
	@if [ -z "$(HDFEOS_INC)" ]; then \
	  echo ""; \
	  echo "  HDFEOS_INC is undefined."; \
	  echo ""; \
	  echo "  (Don't worry, this is normal.  It just means the optional"; \
	  echo "   HDF-EOSC library won't be supported.)"; \
	  echo ""; \
	  echo "  This 'make' will self-destruct in 1 second"; \
	  echo ""; \
	  exit 1; \
	fi
	@if [ -z "$(HDFEOS_LIB)" ]; then \
	  echo ""; \
	  echo "  HDFEOS_LIB is undefined."; \
	  echo ""; \
	  echo "  (Don't worry, this is normal.  It just means the optional"; \
	  echo "   HDF-EOSC library won't be supported.)"; \
	  echo ""; \
	  echo "  This 'make' will self-destruct in 1 second"; \
	  echo ""; \
	  exit 1; \
	fi

solaris: required_macros
	@make ARCH=solaris SYSLIBS="-lnsl -lm" LDOPT="-G" hdfeosLib

irix: required_macros
	@make ARCH=irix SYSLIBS="-lm" LDOPT="-shared -Wl,-x" hdfeosLib

linux: required_macros
	@make ARCH=linux SYSLIBS="-lm" SHCFLAGS="-fPIC -DPIC" LDOPT="-shared" hdfeosLib

aix: required_macros
	@make ARCH=aix SYSLIBS="-lm -lc" LDOPT="-bE:shrsub.exp -bM:SRE -bnoentry" hdfeosLib
