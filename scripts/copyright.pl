#!/usr/bin/perl -w
#
# This script will automatically add a copyright to any source files
# which don't have one, and will update any files whose copyrights
# are non-standard.
#
# To use:
#
#       cd ~/src/visad
#       copyright.pl

use strict;

my @VISAD_COPYRIGHT =
  (
   'VisAD system for interactive analysis and visualization of numerical',
  'data.  Copyright (C) 1996 - <YEAR> Bill Hibbard, Curtis Rueden, Tom',
   'Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and',
   'Tommy Jasmin.',
);

my @VISAD_LICENSE =
  (
   'This library is free software; you can redistribute it and/or',
   'modify it under the terms of the GNU Library General Public',
   'License as published by the Free Software Foundation; either',
   'version 2 of the License, or (at your option) any later version.',
   '',
   'This library is distributed in the hope that it will be useful,',
   'but WITHOUT ANY WARRANTY; without even the implied warranty of',
   'MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU',
   'Library General Public License for more details.',
   '',
   'You should have received a copy of the GNU Library General Public',
   'License along with this library; if not, write to the Free',
   'Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,',
   'MA 02111-1307, USA',
);

# set this to non-zero for verbose output
#
my $VERBOSE = 1;
my $EXTRA_VERBOSE = 0;
my $REALLY_FIX = 1;

# definitions for various copyrights
#
my $NO_COPYRIGHT = 0;
my $VISAD_COPYRIGHT_UNKNOWN = 200;
my $VISAD_COPYRIGHT_ANCIENT = 201;
my $VISAD_COPYRIGHT_EMMERSON = 202;
my $VISAD_RASMUSSEN_UNKNOWN = 210;
my $VISAD_RASMUSSEN_ANCIENT = 211;
my $VISAD_RASMUSSEN_OLDYEAR = 212;
my $VISAD_RASMUSSEN_GPL = 218;
my $VISAD_RASMUSSEN_LGPL = 219;
my $VISAD_WHITTAKER_UNKNOWN = 220;
my $VISAD_WHITTAKER_ANCIENT = 221;
my $VISAD_WHITTAKER_OLDYEAR = 222;
my $VISAD_WHITTAKER_GPL = 228;
my $VISAD_WHITTAKER_LGPL = 229;
my $VISAD_COPYRIGHT_GPL = 290;
my $VISAD_COPYRIGHT_LGPL = 299;
my $UCAR_COPYRIGHT = 900;
my $LGPL_COPYRIGHT = 901;
my $GPL_COPYRIGHT = 902;
my $BOM_COPYRIGHT = 903;
my $HUGHES_COPYRIGHT = 904;
my $UILL_COPYRIGHT = 905;
my $FSU_JENA_COPYRIGHT = 906;
my $MACHINE_GENERATED = 997;
my $MYSTERIOUS_PROPRIETARY = 998;
my $MYSTERIOUS_COPYRIGHT = 999;

# first year that various copyrights were dated
#
my $VISAD_COPYRIGHT_YEAR_OFFSET = 10000;
my $VISAD_COPYRIGHT_FIRST_YEAR = $VISAD_COPYRIGHT_YEAR_OFFSET + 1996;
my $EMMERSON_COPYRIGHT_YEAR_OFFSET = 15000;
my $EMMERSON_COPYRIGHT_FIRST_YEAR = $EMMERSON_COPYRIGHT_YEAR_OFFSET + 1996;

# definitions for various filetypes
#
my $STYLE_UNKNOWN = 1;
my $STYLE_C = 2;
my $STYLE_FORTRAN = 3;
my $STYLE_SHELL = 5;
my $STYLE_TCL = 6;
my $STYLE_JAVA = 7;

############################################################################

# cached text for current copyright
#
my @latestCopyright = ();

# current year
#
my $currentYear = &findCurrentYear;

############################################################################

# find the current year
#
sub findCurrentYear {
  my ($x,$year);
  ($x,$x,$x,$x,$x,$year) = localtime(time);
  return $year + 1900;
}

# return description of copyright type
#
sub copyrightName {
  my $type = shift;

  if ($type == $VISAD_COPYRIGHT_UNKNOWN) {
    return "unknown VisAD copyright";
  }

  if ($type == $VISAD_COPYRIGHT_ANCIENT) {
    return "ancient VisAD copyright";
  }

  if ($type == $VISAD_COPYRIGHT_EMMERSON) {
    return "Emmerson's VisAD copyright";
  }

  if ($type > $EMMERSON_COPYRIGHT_FIRST_YEAR &&
      $type < $EMMERSON_COPYRIGHT_YEAR_OFFSET + $currentYear)
    {
    return ($type - $EMMERSON_COPYRIGHT_YEAR_OFFSET) . " Emmerson VisAD copyright";
  }

  if ($type == $VISAD_RASMUSSEN_UNKNOWN) {
    return "unknown Rasmussen copyright";
  }

  if ($type == $VISAD_RASMUSSEN_ANCIENT) {
    return "ancient Rasmussen copyright";
  }

  if ($type == $VISAD_RASMUSSEN_OLDYEAR) {
    return "outdated Rasmussen copyright";
  }

  if ($type == $VISAD_RASMUSSEN_GPL) {
    return "GPL'd Rasmussen";
  }

  if ($type == $VISAD_RASMUSSEN_LGPL) {
    return "current Rasmussen copyright";
  }

  if ($type == $VISAD_WHITTAKER_UNKNOWN) {
    return "unknown Whittaker copyright";
  }

  if ($type == $VISAD_WHITTAKER_ANCIENT) {
    return "ancient Whittaker copyright";
  }

  if ($type == $VISAD_WHITTAKER_OLDYEAR) {
    return "outdated Whittaker copyright";
  }

  if ($type == $VISAD_WHITTAKER_GPL) {
    return "GPL'd Whittaker";
  }

  if ($type == $VISAD_WHITTAKER_LGPL) {
    return "current Whittaker copyright";
  }

  if ($type == $VISAD_COPYRIGHT_GPL) {
    return "GPL'd VisAD";
  }

  if ($type == $VISAD_COPYRIGHT_LGPL) {
    return "current VisAD copyright";
  }

  if ($type > $VISAD_COPYRIGHT_FIRST_YEAR &&
      $type < $VISAD_COPYRIGHT_YEAR_OFFSET + $currentYear)
    {
    return ($type - $VISAD_COPYRIGHT_YEAR_OFFSET) . " VisAD copyright";
  }

  if ($type == $UCAR_COPYRIGHT) {
    return "UCAR copyright";
  }

  if ($type == $LGPL_COPYRIGHT) {
    return "GNU Library copyright";
  }

  if ($type == $GPL_COPYRIGHT) {
    return "GNU General copyright";
  }

  if ($type == $BOM_COPYRIGHT) {
    return "Australian BoM copyright";
  }

  if ($type == $HUGHES_COPYRIGHT) {
    return "Hughes copyright";
  }

  if ($type == $UILL_COPYRIGHT) {
    return "Univ. Illinois copyright";
  }

  if ($type == $FSU_JENA_COPYRIGHT) {
    return "FSU Jena copyright";
  }

  if ($type == $MACHINE_GENERATED) {
    return "machine-generated data";
  }

  if ($type == $MYSTERIOUS_PROPRIETARY) {
    return "mysterious \"proprietary\" reference";
  }

  if ($type == $MYSTERIOUS_COPYRIGHT) {
    return "mysterious copyright";
  }

  return "no notice/copyright";
}

# determine the copyright type in the file
#
sub copyrightType
{
  my $path = shift;

  if (!open(FILE, $path)) {
    print STDERR "Couldn't open \"$path\"\n";
    return undef;
  }

  # paw through the file, looking for any sort of copyright line
  #
  my $type = $NO_COPYRIGHT;
  while (<FILE>) {
    # check for VisAD copyrights
    #
    if (/VisAD system for interactive analysis and/) {

      $type = $VISAD_COPYRIGHT_LGPL;

      # check the second line of the VisAD copyright
      #
      $_ = <FILE>;
      if (!/\.\s+Copyright \(C\) 1996 - (\d+) Bill H.*\s+(Rueden.*)\s*$/) {
        $type = $VISAD_COPYRIGHT_UNKNOWN;
        last;
      }

      # save fields from second line
      #
      my ($annum,$names) = ($1,$2);

      # analyze copyright year
      #
      $annum += 0;        # force to a number
      if ($annum != $currentYear) {
        if ($annum >= ($VISAD_COPYRIGHT_FIRST_YEAR -
                       $VISAD_COPYRIGHT_YEAR_OFFSET) &&
            $annum < $currentYear)
        {
          $type = $annum + $VISAD_COPYRIGHT_YEAR_OFFSET;
        } else {
          print STDERR "Unrecognized VisAD year \"$annum\" in $path\n";
          $type = $VISAD_COPYRIGHT_UNKNOWN;
          last;
        }
      }

      # analyze end of line
      #
      if ($names =~ /Rueden and Tom$/) {
        $type = $VISAD_COPYRIGHT_ANCIENT;
        last;
      }
      if ($names !~ /Rueden, Tom$/) {
        $type = $VISAD_COPYRIGHT_UNKNOWN;
        last;
      }

      # match the third line of the VisAD copyright
      #
      $_ = <FILE>;
      if (/Rink, Dave Glowacki, and Steve Emmerson.\s*/) {
        if ($type >= $VISAD_COPYRIGHT_YEAR_OFFSET) {
          $type += $EMMERSON_COPYRIGHT_YEAR_OFFSET -
            $VISAD_COPYRIGHT_YEAR_OFFSET;
        } else {
          $type = $VISAD_COPYRIGHT_EMMERSON;
        }
        last;
      }
      if (/Rink, \S+ G\S+i, \S+ Emmerson, \S+ Whittaker, \S+ Murray, and/) {
        $_ = <FILE>;
        if (!/Tommy Jasmin./) {
          $type = $VISAD_COPYRIGHT_UNKNOWN;
          last;
        }
      } elsif (!/Rink and Dave Glowacki.\s*/) {
        $type = $VISAD_COPYRIGHT_UNKNOWN;
        last;
      }

      # ignore the first line after the VisAD copyright
      #
      $_ = <FILE>;

      # match the second line after the VisAD copyright
      #
      $_ = <FILE>;
      if (!/This .* is free software.* can redistribute it and\/or/) {
        $type = $VISAD_COPYRIGHT_UNKNOWN;
        last;
      }

      # match the third line after the VisAD copyright
      #
      $_ = <FILE>;
      if (/it under the terms of the GNU General Public License as/) {
        $type = $VISAD_COPYRIGHT_GPL;
        last;
      } elsif (!/it under the terms of the GNU Library General Public/) {
        $type = $VISAD_COPYRIGHT_UNKNOWN;
        last;
      }

      last;

    # check for Nick Rasmussen's VisAD copyright
    #
    } elsif (/^\s*VisAD Utility Library: Widgets for use in building.*/) {

      $type = $VISAD_RASMUSSEN_LGPL;

      # check the second line of Rasmussen's VisAD copyright
      #
      $_ = <FILE>;
      if (!/^the VisAD interactive analysis and visualization library\s*$/) {
        $type = $VISAD_RASMUSSEN_UNKNOWN;
        last;
      }

      # match the third line of Rasmussen's VisAD copyright
      #
      $_ = <FILE>;
      if (!/^Copyright \(C\) 1998 Nick Rasmussen\s*$/) {
        $type = $VISAD_RASMUSSEN_UNKNOWN;
        last;
      }

      # check the fourth line of Rasmussen's VisAD copyright
      #
      $_ = <FILE>;
      if (!/Copyright \(C\) 1996 - (\d+) Bill H.*\s+(Rueden.*)\s*$/) {
        $type = $VISAD_RASMUSSEN_UNKNOWN;
        last;
      }

      # save fields from fourth line
      #
      my ($annum,$names) = ($1,$2);

      # analyze copyright year
      #
      $annum += 0;        # force to a number
      if ($annum != $currentYear) {
        if ($annum >= ($VISAD_COPYRIGHT_FIRST_YEAR -
                       $VISAD_COPYRIGHT_YEAR_OFFSET) &&
            $annum <  $currentYear)
        {
          $type = $VISAD_RASMUSSEN_OLDYEAR;
          last;
        } else {
          print STDERR "Unrecognized Rasmussen year \"$annum\" in $path\n";
          $type = $VISAD_RASMUSSEN_UNKNOWN;
          last;
        }
      }

      # analyze end of line
      #
      if ($names =~ /Rueden and Tom$/) {
        $type = $VISAD_RASMUSSEN_UNKNOWN;
        last;
      }
      if ($names !~ /Rueden, Tom$/) {
        $type = $VISAD_RASMUSSEN_UNKNOWN;
        last;
      }

      # match the fifth line of Rasmussen's VisAD copyright
      #
      $_ = <FILE>;
      if (!/^\s*Rink and Dave Glowacki.\s*/) {
        $type = $VISAD_RASMUSSEN_UNKNOWN;
        last;
      }

      # match the sixth line of Rasmussen's VisAD copyright
      #
      $_ = <FILE>;
      if (!/^\s*$/) {
        $type = $VISAD_RASMUSSEN_UNKNOWN;
        last;
      }

      # match the seventh line of Rasmussen's VisAD copyright
      #
      $_ = <FILE>;
      if (!/^This program is free software.*or modify$/) {
        $type = $VISAD_RASMUSSEN_UNKNOWN;
        last;
      }

      # match the eighth line of Rasmussen's VisAD copyright
      #
      $_ = <FILE>;
      if (/^it under the terms of the GNU General Public License as/) {
        $type = $VISAD_RASMUSSEN_GPL;
        last;
      } elsif (!/it under the terms of the GNU Library General Public/) {
        $type = $VISAD_RASMUSSEN_UNKNOWN;
        last;
      }

      last;

    # check for Tom Whittaker's VisAD copyright
    #
    } elsif (/is Copyright\(C\) (\d+) by Tom Whittaker\.\s*/) {

      $type = $VISAD_WHITTAKER_LGPL;

      # analyze copyright year
      #
      my $annum = $1 + 0;        # force to a number
      if ($annum != $currentYear) {
        if ($annum >= ($VISAD_COPYRIGHT_FIRST_YEAR -
                       $VISAD_COPYRIGHT_YEAR_OFFSET) &&
            $annum <  $currentYear)
        {
          $type = $VISAD_WHITTAKER_OLDYEAR;
          last;
        } else {
          print STDERR "Unrecognized Whittake year \"$annum\" in $path\n";
          $type = $VISAD_WHITTAKER_UNKNOWN;
          last;
        }
      }

      # check the second line of Whittaker's VisAD copyright
      #
      $_ = <FILE>;
      if (!/^It is designed to be used with the VisAD system for .*/) {
        $type = $VISAD_WHITTAKER_UNKNOWN;
        last;
      }

      # match the third line of Whittaker's VisAD copyright
      #
      $_ = <FILE>;
      if (!/^analysis and visualization of numerical data.\s*$/) {
        $type = $VISAD_WHITTAKER_UNKNOWN;
        last;
      }

      # match the fourth line of Whittaker's VisAD copyright
      #
      $_ = <FILE>;
      if (!/^\s*$/) {
        $type = $VISAD_WHITTAKER_UNKNOWN;
        last;
      }

      # match the fifth line of Whittaker's VisAD copyright
      #
      $_ = <FILE>;
      if (!/^This program is free software.*or modify$/) {
        $type = $VISAD_WHITTAKER_UNKNOWN;
        last;
      }

      # match the sixth line of Whittaker's VisAD copyright
      #
      $_ = <FILE>;
      if (/^it under the terms of the GNU General Public License as/) {
        $type = $VISAD_WHITTAKER_GPL;
        last;
      } elsif (!/it under the terms of the GNU Library General Public/) {
        $type = $VISAD_WHITTAKER_UNKNOWN;
        last;
      }

      last;

    # maybe this is a UCAR file...
    #
    } elsif (/Copyright .* University Corporation for Atmospheric Research/) {
      $type = $UCAR_COPYRIGHT;
      last;

    # maybe this is an LGPL file...
    #
    } elsif (/the\s+terms\s+of\s+the\s+GNU\s+Library\s+General\s+Public/) {
      $type = $LGPL_COPYRIGHT;
      last;

    # maybe this is a GPL file...
    #
    } elsif (/the\s+terms\s+of\s+the\s+GNU\s+General\s+Public/) {
      $type = $GPL_COPYRIGHT;
      last;

    # maybe this is a BoM file...
    #
    } elsif (/Bureau\s+of\s+Meteorology/) {
      $type = $BOM_COPYRIGHT;
      last;

    # maybe this is a Hughes file...
    #
    } elsif (/Hughes\s+and\s+Applied\s+Research\s+Corporation/) {
      $type = $HUGHES_COPYRIGHT;
      last;

    # maybe this is a U-Illinois file...
    #
    } elsif (/the\s+Board\s+of\s+Trustees\s+of\s+the\s+University\s+of\s+Illinois/) {
      $type = $UILL_COPYRIGHT;
      last;

    # maybe this is an FSU Jena file...
    #
    } elsif (/FSU\s+Jena,\s+Dept\.\s+of\s+Geoinformatics/) {
      $type = $FSU_JENA_COPYRIGHT;
      last;

    # maybe this is a generated JNI header file
    #
    } elsif (/DO NOT EDIT THIS FILE - it is machine generated/) {
      $type = $MACHINE_GENERATED;
      last;

    # maybe this is a Together/J-generated file
    #
    } elsif (/Generated by Together\s*$/) {
      $type = $MACHINE_GENERATED;
      last;

    # maybe this is copied from Sun's API specs
    #
    } elsif (/copied from Sun\'s Java 3D API Specification/) {
      $type = $MACHINE_GENERATED;
      last;

    # match any other copyrights
    #
    } elsif (/copyright/i) {
      $type = $MYSTERIOUS_COPYRIGHT;

    # match any proprietary notices
    #
    } elsif (/proprietary/i) {
      if ($type == $NO_COPYRIGHT) {
        $type = $MYSTERIOUS_PROPRIETARY
      }
    }
  }
  close(FILE);

  # return copyright type
  #
  return $type;
}

# figure out style used to add copyright to this file
#
sub styleType {
  my $path = shift;

  # get the file's suffix
  #
  my $suffix = undef;
  if ($path =~ /\.([^\.]+)$/) {
    $suffix = $1;
  }

  # use suffix to determine file style
  #
  my $style;
  if (!defined($suffix)) {
    $style = undef;
  } elsif ($suffix eq 'c' || $suffix eq 'h') {
    $style = $STYLE_C;
  } elsif ($suffix eq 'f' || $suffix eq 'inc' || $suffix eq 'dlm') {
    $style = $STYLE_FORTRAN;
  } elsif ($suffix eq 'shk' || $suffix eq 'sh') {
    $style = $STYLE_SHELL;
  } elsif ($suffix eq 'tcl' || $suffix eq 'gui' || $suffix eq 'gtk') {
    $style = $STYLE_TCL;
  } elsif ($suffix eq 'java') {
    $style = $STYLE_JAVA;
  } else {
    return undef;
  }

  return $style;
}

# write lines with appropriate comment character(s) before & after
#
sub addCLines {
  my $fh = shift;
  my $pre = shift;
  my $post = shift;
  my $license = shift;

  my $l;
  foreach $l (@_) {
    if ($license && $l =~ /<YOUR LICENSE HERE>/) {
      foreach $l (@VISAD_LICENSE) {
        print $fh $pre,$l,$post,"\n";
      }
    } else {
      print $fh $pre,$l,$post,"\n";
    }
  }

  if (!$license) {
    print $fh $pre,$post,"\n";
    foreach $l (@VISAD_LICENSE) {
      print $fh $pre,$l,$post,"\n";
    }
  }
}

# build the standard copyright message
#
sub buildStandardCopyright {
  # add the current year to the standard copyright message
  #
  my $i;
  for ($i = 0; $i < @_; $i++) {
    $_[$i] =~ s/<YEAR>/$currentYear/;
  };

  return @_;
}

# write the supplied copyright to the file
# using the appropriate style
#
sub addCopyright {
  my $fh = shift;
  my $style = shift;
  my $fixing = shift;
  my $license = shift;

  if ($style == $STYLE_C) {
    print $fh '/*',"\n" if !$fixing;
    addCLines $fh, ' * ', '', $license, @_;
    print $fh " */\n\n";
  } elsif ($style == $STYLE_FORTRAN) {
    addCLines $fh, 'C ', '', $license, @_;
    print $fh "\n";
  } elsif ($style == $STYLE_SHELL) {
    addCLines $fh, '# ', '', $license, @_;
    print $fh "\n";
  } elsif ($style == $STYLE_TCL) {
    addCLines $fh, '# ', '', $license, @_;
    print $fh "\n";
  } elsif ($style == $STYLE_JAVA) {
    print $fh '/*',"\n" if !$fixing;
    addCLines $fh, '', '', $license, @_;
    print $fh "*/\n\n";
  }
}

# remove all blank or empty comment lines from the top of the supplied list
#
sub skipBlankLines {
  my $list = shift;
  my $style = shift;

  # match empty comment lines for the specified language
  #
  my $blankpat = '^\s*$';
  if ($style == $STYLE_C) {
    $blankpat .= '|^\s*\/\*\s*\*\/\s*$|^\s*\*\s*$';
  } elsif ($style == $STYLE_FORTRAN) {
    $blankpat .= '|^\S\s*$';
  } elsif ($style == $STYLE_SHELL) {
    $blankpat .= '|^\s*\#\s*$';
  } elsif ($style == $STYLE_TCL) {
    $blankpat .= '|^\s*\#\s*$';
  }

  # toss out empty/comment lines
  #
  my $keep_skipping = 1;
  while (@$list && $$list[0] =~ /$blankpat/) {
    $keep_skipping = 0;
    shift(@$list);
  }
  $keep_skipping = 1 if (!@$list);

  # return zero if one or more lines were skipped
  #
  return $keep_skipping;
}

# add a proper copyright to this file
#
sub fixFile
{
  my $path = shift;
  my $type = shift;

  if (!$REALLY_FIX) {
    print "\n";
    return;
  }

  # give up if this isn't a file
  #
  if (! -f $path) {
    print "\n";                 # caller expects this routine to end the line
    print STDERR "Not fixing \"$path\": Not a file\n";
    return;
  }

  # determine file commenting style
  #
  my $style = styleType $path;
  if (!defined($style)) {
    print ", but is an unknown filetype ... NOT CHANGED\n";
    return;
  }

  # try read the file
  #
  if (!open(FILE, $path)) {
    print "\n";                 # caller expects this routine to end the line
    print STDERR "Not fixing \"$path\": Couldn't open for reading\n";
    return;
  }

  my @preamble = ();
  my @postamble = ();
  my @comment = ();
  my @oldCopyright = ();

  my $found_copyright = 0;
  my $magic_line = undef;
  my $skipped_license = undef;

  # read the file until we either find a copyright line or we reach EOF
  #
  while (<FILE>) {
    if (/Copyright\s*\([Cc]\)\s+/) {
      $found_copyright = 1;
      last;
    }

    # save magic lines
    #
    if (($style == $STYLE_SHELL || $style == $STYLE_TCL) && /^\#!/) {
      chomp($magic_line = $_);
      print STDERR "Found \"$magic_line\" at line $. in $path\n" if ($. != 1);

    # enable comment cache if we're in a C comment
    #
    } elsif (($style == $STYLE_C || $style == $STYLE_JAVA) &&
             (/^\s*\/\*/ && !/\*\//))
    {
      push(@comment, $_);

    # keep caching C comments until we reach a */
    #
    } elsif (@comment > 0) {
      if (!/\*\//) {
        push(@comment, $_);
      } else {
        push(@preamble, @comment, $_);
        @comment = ();
      }

    # save remaining lines
    #
    } else {
      push(@preamble, $_);
    }
  }

  # save copyright lines, look for start of GPL/LGPL
  if ($found_copyright) {

    if (@comment > 0) {
      push(@oldCopyright, @comment);
    }

    while (!/This program is free software; you can redistribute it/) {

      my $fixed = undef;
      if (/^(.*Copyright\s*\(C\)\s*)(\d+\s*)(-*\s*)(\d*\s*)(.*)\s*$/) {
        if ($2 != $currentYear) {
          if (!defined($4) || $4 eq '') {
            $fixed = $1 . $2 . '- ' . $currentYear . ' ' . $5;
          } elsif ($4 != $currentYear) {
            $fixed = $1 . $2 . $3 . $currentYear . ' ' . $5;
          }
        }
      }

      if (!defined($fixed)) {
        chomp;
        $fixed = $_
      }

      if ($fixed =~ /^[ \*]*Rink and Dave Glowacki/ ||
          $fixed =~ /^[ \*]*Rink, Dave Glowacki,* and Steve Emmerson./)
      {
        my $cfound = 0;
        my $c;
        foreach $c (@VISAD_COPYRIGHT) {
          if ($cfound) {
            push(@oldCopyright, $c);
          } elsif ($c =~ /Dave Glowacki/) {
            $cfound = 1;
            push(@oldCopyright, $c);
          }
        }

        while (!/MA\s+02\d+[-\d]+, USA\.*\s*$/) {
          last if eof(FILE);
          $_ = <FILE>;
        }

        push(@oldCopyright, '');
        push(@oldCopyright, '<YOUR LICENSE HERE>');
        $skipped_license = 1;

        $_ = <FILE> if !eof(FILE);

        last;
      }

      push(@oldCopyright, $fixed);

      # quit on end of GPL/LGPL
      #
      if (/MA\s+02\d+[-\d]+, USA\.*\s*$/) {
        $_ = <FILE> if (!eof(FILE));
        $skipped_license = 1;
        last;
      }

      last if eof(FILE);
      $_ = <FILE>;
    }

    # skip past end of C/Java copyright
    #
    if ($style == $STYLE_C || $style == $STYLE_JAVA) {
      while (!/\*\/\s*$/) {
        last if eof(FILE);
        push(@oldCopyright, $_);
        $_ = <FILE>;
      }
    }

    # remove comment character from copyright
    #
    my $i;
    for ($i = 0; $i < @oldCopyright; $i++) {
      $oldCopyright[$i] =~ s/^ *\* ?|^C |^\" |^\# //;
      chomp($oldCopyright[$i]);
    }

    if (eof(FILE)) {
      print "\n";               # caller expects this routine to end the line
      print STDERR "Couldn't find end of (L)GPL in $path\n";
      close(FILE);
      return;
    }
  }

  while (<FILE>) {
    if (/Copyright\s*\([Cc]\)\s+/) {
      print STDERR "Multiple copyright lines found in \"$path\"!\n";
    }

    push(@postamble, $_);
  }

#  print ' ### ',scalar(@preamble),'/',scalar(@oldCopyright),'/',scalar(@postamble),"\n"; return;

  if (!rename $path, $path . '.bak') {
    print "\n";                 # caller expects this routine to end the line
    print STDERR "Couldn't rename $path to $path.bak\n";
    close(FILE);
    return;
  }

  if (!open(NEW, '>' . $path)) {
    print "\n";                 # caller expects this routine to end the line
    if (!rename $path . '.bak', $path) {
      print STDERR "Couldn't create new $path file or recover $path.bak file\n";
    } else {
      print STDERR "Couldn't create new $path file\n";
    }
    close(FILE);
    return;
  }

  if (defined($magic_line)) {
    print NEW $magic_line,"\n\n";
  }

  if ($found_copyright) {
    skipBlankLines \@preamble, $style;

    my $l;
    foreach $l (@preamble) {
      print NEW $l;
    }
    @preamble = ();
  }

  # add old or new copyright line
  #
  if (@oldCopyright > 0) {
    addCopyright \*NEW, $style, $found_copyright, $skipped_license, @oldCopyright;
  } else {
    addCopyright \*NEW, $style, $found_copyright, $skipped_license, @latestCopyright;
  }

  my $keep_skipping = 1;

  $keep_skipping = skipBlankLines \@preamble, $style;

  my $l;
  foreach $l (@preamble) {
    print NEW $l;
  }

  if ($keep_skipping) {
    $keep_skipping = skipBlankLines \@postamble, $style;
  }

  foreach $l (@postamble) {
    print NEW $l;
  }

  close(NEW);
  close(FILE);

  print " ... fixed\n" if $VERBOSE;
}

# fix this file if necessary
#
sub checkFile
  {
    my $path = shift;

    my $type = copyrightType $path;
    if (!defined($type)) {
      # don't do anything if this file couldn't be read
    }

    elsif ($type == $VISAD_COPYRIGHT_UNKNOWN ||
        $type == $VISAD_COPYRIGHT_ANCIENT ||
        $type == $VISAD_COPYRIGHT_GPL)
    {
      print "$path has ",&copyrightName($type) if $VERBOSE;
      fixFile $path, $type;
    }

    elsif ($type == $VISAD_COPYRIGHT_EMMERSON) {
      print "$path already has ",copyrightName($type)," ... NOT CHANGED\n" if $EXTRA_VERBOSE;
      fixFile $path, $type;
    }

    elsif ($type >= $EMMERSON_COPYRIGHT_FIRST_YEAR &&
           $type < $currentYear + $EMMERSON_COPYRIGHT_YEAR_OFFSET)
    {
      print "$path has ",copyrightName($type) if $VERBOSE;
      fixFile $path, $type;
    }

    elsif ($type == $VISAD_COPYRIGHT_LGPL) {
      print "$path already has ",copyrightName($type)," ... NOT CHANGED\n" if $EXTRA_VERBOSE;
    }

    elsif ($type == $VISAD_RASMUSSEN_UNKNOWN ||
        $type == $VISAD_RASMUSSEN_ANCIENT ||
        $type == $VISAD_RASMUSSEN_OLDYEAR ||
        $type == $VISAD_RASMUSSEN_GPL)
    {
      print "$path has ",&copyrightName($type)," ... NOT CHANGED\n" if $EXTRA_VERBOSE;
    }

    elsif ($type == $VISAD_RASMUSSEN_LGPL) {
      print "$path already has ",copyrightName($type)," ... NOT CHANGED\n" if $EXTRA_VERBOSE;
    }

    elsif ($type == $VISAD_WHITTAKER_UNKNOWN ||
        $type == $VISAD_WHITTAKER_ANCIENT ||
        $type == $VISAD_WHITTAKER_OLDYEAR ||
        $type == $VISAD_WHITTAKER_GPL)
    {
      print "$path has ",&copyrightName($type)," ... NOT CHANGED\n" if $EXTRA_VERBOSE;
    }

    elsif ($type == $VISAD_WHITTAKER_LGPL) {
      print "$path already has ",copyrightName($type)," ... NOT CHANGED\n" if $EXTRA_VERBOSE;
    }

    elsif ($type >= $VISAD_COPYRIGHT_FIRST_YEAR &&
           $type < $currentYear + $VISAD_COPYRIGHT_YEAR_OFFSET)
    {
      print "$path has ",copyrightName($type) if $VERBOSE;
      fixFile $path, $type;
    }

    elsif ($type == $UCAR_COPYRIGHT || $type == $LGPL_COPYRIGHT ||
           $type == $GPL_COPYRIGHT || $type == $BOM_COPYRIGHT ||
           $type == $HUGHES_COPYRIGHT || $type == $UILL_COPYRIGHT ||
           $type == $FSU_JENA_COPYRIGHT ||
           $type == $MYSTERIOUS_COPYRIGHT ||
           $type == $MYSTERIOUS_PROPRIETARY ||
           $type == $MACHINE_GENERATED)
    {
      print "$path has ",copyrightName($type)," ... NOT CHANGED!\n" if $VERBOSE;
    }

    else {
      if ($VERBOSE) {
        print "$path has no copyright";
        if ($type != 0) {
          print " (",copyrightName($type),")";
        }
      }
      fixFile $path, $type;
    }
  }

# check this entire directory
#
sub checkDir
  {
    my $dir = shift;

    if (!opendir(DIR, $dir)) {
      print STDERR "Couldn't open directory \"$dir\"\n";
      return;
    }

    my $f;
    foreach $f (readdir DIR) {

      # ignore special directories
      #
      next if ($f eq '.' || $f eq '..' || $f eq 'CVS' || $f eq 'RCS' ||
               $f =~ /^obj_/);

      my $p = $dir . '/' . $f;

      if (-d $p) {

        # ignore specific directories
        #
        next if ($f eq 'paoloa' || $f eq 'aune' || $f eq 'benjamin' ||
                 $f eq 'rabin');

        checkDir($p);
      } else {

        # ignore specific files
        #
        next if ($f eq '.cvsignore' || $f =~ /Makefile/ ||
                 $f =~ /README/ || $f eq 'depend' || $f eq 'rmic_script' ||
                 $f eq 'NOTEBOOK' || $f eq 'StandardQuantityDB.save' ||
                 $f =~ /~$/ || $f =~ /^AREA\d+$/ || $f =~ /^OUTL[A-Z]+$/);

        # check suffix
        #
        if ($f =~ /\.([^.]+)$/) {
          my $suffix = $1;

          next if ($suffix eq 'o' || $suffix eq 'a' ||
                   $suffix eq 'class' || $suffix eq 'html' ||
                   $suffix eq 'gif' || $suffix eq 'nc' ||
                   $suffix eq 'so' || $suffix eq 'txt' ||
                   $suffix eq 'make' || $suffix eq 'py' ||
                   $suffix eq 'fits' || $suffix eq 'fit' ||
                   $suffix eq 'v5d' || $suffix eq 'vad' ||
                   $suffix eq 'bak' || $suffix eq 'jpg' ||
                   $suffix eq 'jhf' || $suffix eq 'jar' ||
                   $suffix eq 'cdf' || $suffix eq 'png' ||
                   $suffix eq 'csv' || $suffix eq 'PIC');
        }

        checkFile $p;
      }
    }
    closedir(DIR);
  }

############################################################################

# build the standard copyright message
#
@latestCopyright = buildStandardCopyright @VISAD_COPYRIGHT;

if (@ARGV == 0) {
  push(@ARGV, '.');
}

my $a;
foreach $a (@ARGV) {
  if ( -d $a) {
    checkDir $a;
  } else {
    checkFile $a;
  }
}

exit 0;
