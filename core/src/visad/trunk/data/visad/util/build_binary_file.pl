#!/usr/local/bin/perl -w
#
# Build a BinaryFile.java file using data in the
# 'datas', 'flds', 'maths' and 'objs' files, which
# correspond to the DATA_*, FLD_*, MATH_* and OBJ_*
# constants.

use strict;

sub print_header {
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

  if (!open(FILE, $fileName)) {
    print STDERR "Couldn't open '$fileName'\n";
    return;
  }
  print "\n";

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

      print '  ',$comment,'byte ',$prefix,'_',$_,' = ',$num,";\n";
      $num++;
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
print_footer;

exit 0;
