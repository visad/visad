# makefile for visad/data/vis5d/Vis5DForm

# If DEC or Linux, (Little-endian), add -DLITTLE to CFLAGS
CFLAGS = -c -g

default: Vis5DForm.class libvis5d.so

# you may need to change these include directory paths
libvis5d.so: v5d_read.o v5d.o binio.o
	$(CC) -G v5d_read.o v5d.o binio.o -o libvis5d.so

Vis5DForm.class: Vis5DForm.java
	javac Vis5DForm.java

v5d_read.o: v5d_read.c
	$(CC) $(CFLAGS) -I/opt/java/include -I/opt/java/include/solaris v5d_read.c -o v5d_read.o

v5d.o:      v5d.c
	$(CC) $(CFLAGS) v5d.c -o v5d.o

binio.o:    binio.c
	$(CC) $(CFLAGS) binio.c -o binio.o

