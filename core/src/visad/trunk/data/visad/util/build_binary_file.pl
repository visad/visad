#!/usr/local/bin/perl -w
#
# Build a BinaryFile.java file using data in the
# 'datas', 'flds', 'maths', 'objs', and 'debugs' files, which
# correspond to the DATA_*, FLD_*, MATH_*, OBJ_*, and DEBUG_*
# constants.

use strict;

sub print_header {
  print "/*\n";
  print "VisAD system for interactive analysis and visualization of numerical\n";
  print "data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom\n";
  print "Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and\n";
  print "Tommy Jasmin.\n";
  print "\n";
  print "This library is free software; you can redistribute it and/or\n";
  print "modify it under the terms of the GNU Library General Public\n";
  print "License as published by the Free Software Foundation; either\n";
  print "version 2 of the License, or (at your option) any later version.\n";
  print "\n";
  print "This library is distributed in the hope that it will be useful,\n";
  print "but WITHOUT ANY WARRANTY; without even the implied warranty of\n";
  print "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU\n";
  print "Library General Public License for more details.\n";
  print "\n";
  print "You should have received a copy of the GNU Library General Public\n";
  print "License along with this library; if not, write to the Free\n";
  print "Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,\n";
  print "MA 02111-1307, USA\n";
  print "*/\n";
  print "\n";
  print "package visad.data.visad;\n";
  print "\n";
  print "/**\n";
  print " * Constant values used by both\n";
  print ' * {@link visad.data.visad.BinaryReader BinaryReader}',"\n";
  print " * and\n";
  print ' * {@link visad.data.visad.BinaryWriter BinaryWriter}<br>',"\n";
  print " * <br>\n";
  print " * <tt>MAGIC_STR</tt> and <tt>FORMAT_VERSION</tt> are used\n";
  print " * to mark the file as a VisAD binary file.<br>\n";
  print " * <tt>OBJ_</tt> constants indicate the type of the next\n";
  print " * object in the file.<br>\n";
  print " * <tt>FLD_</tt> constants indicate the type of the next\n";
  print " * field for the current object in the file.<br>\n";
  print " * <tt>MATH_</tt> constants indicate the type of <tt>FLD_MATH</tt>\n";
  print " * objects.<br>\n";
  print " * <tt>DATA_</tt> constants indicate the type of <tt>FLD_DATA</tt>\n";
  print " * objects.\n";
  print " */\n";
  print "public interface BinaryFile\n{\n";
  print "  String MAGIC_STR = \"VisADBin\";\n";
  print "  int FORMAT_VERSION = 1;\n";
}

sub print_file {
  my $fileName = shift;
  my $prefix = shift;
  my $type = shift;

  if (!open(FILE, $fileName)) {
    print STDERR "Couldn't open '$fileName'\n";
    return;
  }
  print "\n";

  if (!defined($type)) {
    $type = 'byte';
  }

  my $num = 1;
  while (<FILE>) {
    chomp;

    if ($_ eq '') {
      print "\n";

      my $low = $num % 10;
      if ($low != 0) {
	$num = ($num + 10) - $low;
      }
    } else {

      my $comment = '';
      $comment = '// ' if (s/^\/\/\s+//);

      my $val;
      if ($type eq 'boolean') {
        $val = 'false';
      } else {
        $val = $num;
        $num++;
      }

      print '  ',$comment,$type,' ',$prefix,'_',$_,' = ',$val,";\n";
    }
  }

  close(FILE);
}

sub print_footer {
  print "}\n";
}

print_header;
print_file('objs', 'OBJ');
print_file('flds', 'FLD');
print_file('maths', 'MATH');
print_file('datas', 'DATA');
print_file('debugs', 'DEBUG', 'boolean');
print_footer;

exit 0;
