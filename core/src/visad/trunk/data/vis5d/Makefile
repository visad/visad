# makefile for visad/data/vis5d/Vis5DForm

default: Vis5DForm.class libvis5d.so

# you may need to change these include directory paths
libvis5d.so: v5d_read.o v5d.o binio.o
	$(CC) -G v5d_read.o v5d.o binio.o -o libvis5d.so

Vis5DForm.class: Vis5DForm.java
	javac Vis5DForm.java

.c.o:
	$(CC) -I/opt/java/include -I/opt/java/include/solaris -c $<

