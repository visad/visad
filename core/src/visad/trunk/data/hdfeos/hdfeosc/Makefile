SLIB = hdfeos
SLIBDIR = .
JAVAINCS = -I${J_INC} -I${J_INC}/${ARCH}

HDFINCS = -I${HDF_INC}
HDFEOSINCS = -I${HDFEOS_INC}

HDFLIBS = $(HDF_LIB)/libmfhdf.a $(HDF_LIB)/libdf.a $(HDF_LIB)/libjpeg.a $(HDF_LIB)/libz.a
HDFEOSLIBS = $(HDFEOS_LIB)/libhdfeos.a $(HDFEOS_LIB)/libGctp.a ./libhdfeos_ext.a

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
	EHchkfidImp.o EHcloseImp.o

default:
	@echo " use:  'make solaris' or 'make irix' "
	@exit 1

all: hdfeosLib

hdfeos_ext: SWfdims.o GDfdims.o
	$(AR) libhdfeos_ext.a SWfdims.o GDfdims.o

SWfdims.o: SWfdims.c
	$(CC) -c SWfdims.c $(HDFINCS) $(HDFEOSINCS)

GDfdims.o: GDfdims.c
	$(CC) -c GDfdims.c $(HDFINCS) $(HDFEOSINCS)

.c.o:
	$(CC) -c $*.c $(JAVAINCS) $(HDFINCS) $(HDFEOSINCS)

hdfeosLib: $(OBJECTS) hdfeos_ext
	$(LD) $(LDOPT) $(OBJECTS) $(HDFEOSLIBS) $(HDFLIBS) $(SYSLIBS) -o $(SLIBDIR)/lib$(SLIB).so
clean:
	rm -f so_locations
	rm -f $(SLIBDIR)/lib$(SLIB).so
	rm -f libhdfeos_ext.a
	rm -f *.o

solaris:
	@make ARCH=solaris SYSLIBS="-lnsl -lm" LDOPT="-G" all
irix:
	@make ARCH=irix SYSLIBS="-lm" LDOPT="-shared -Wl,-x" all
