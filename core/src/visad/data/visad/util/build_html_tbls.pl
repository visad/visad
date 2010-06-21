#!/usr/local/bin/perl -w
#
# Build tables for binary_file_format.html using data in the
# 'datas', 'flds', 'maths' and 'objs' files, which
# correspond to the DATA_*, FLD_*, MATH_* and OBJ_*
# constants.

use strict;

sub print_data {
  my $name = shift;
  my $value = shift;

  print '<td>',$name,'</td><td>',$value,'</td>';
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
  my %list = ();
  while (<FILE>) {
    chomp;

    if ($_ eq '') {
      my $low = $num % 10;
      if ($low != 0) {
	$num = ($num + 10) - $low;
      }
    } elsif (!/^\/\//) {
      $list{$_} = $num++;
    }
  }

  my @keys = sort {$list{$a} <=> $list{$b}} keys(%list);
  my $half = @keys / 2;

  for (my $i = 0; $i < $half; $i++) {
    print '      <tr>';
    print_data $prefix.'_'.$keys[$i],$list{$keys[$i]};
    print '<td></td>';
    print_data $prefix.'_'.$keys[$i+$half],$list{$keys[$i+$half]} if $half+$i < @keys;
    print "</tr>\n";
  }

  close(FILE);
}

print_file('objs', 'OBJ');
print_file('flds', 'FLD');
print_file('maths', 'MATH');
print_file('datas', 'DATA');

exit 0;
