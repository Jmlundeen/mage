#!/usr/bin/env perl
use strict;
use warnings;
use File::Find;
use File::Spec;

# Root search directory: adjust if needed
my $root = File::Spec->rel2abs('E:/mage');

# Data files similar to gen-card-test
my $dataFile = File::Spec->catfile('E:/mage', 'Utils', 'mtg-cards-data.txt');

# Collect results
my @results;

# Regex to detect assignment to secondSideCardClazz
# Matches lines like:
#   this.secondSideCardClazz = mage.cards.l.LilianaDefiantNecromancer.class;
# Allows optional whitespace and inner class notation ($)
my $assign_re = qr{\bthis\s*\.\s*secondSideCardClazz\s*=\s*mage\.cards\.[a-z]\.([A-Za-z0-9\$]+)\s*\.\s*class\b};

# Helpers similar to gen-card-test
sub toCamelCase {
    my ($string) = @_;
    $string =~ s/\b([\w']+)\b/ucfirst($1)/ge;
    $string =~ s/[-,\s':.!\/]//g;
    return $string;
}

# Load card data and build mapping from classNameLower -> card name
my %class_to_card;
if (-e $dataFile) {
    open(my $dfh, '<', $dataFile) or warn "can't open $dataFile : $!\n";
    if ($dfh) {
        while (my $line = <$dfh>) {
            chomp $line;
            next if $line eq '';
            my @data = split('\|', $line);
            # @data: [0]=cardName, [1]=setName, [2]=something (e.g., uuid), ...
            my $cardName = $data[0];
            my $classLower = lcfirst(toCamelCase($cardName));
            $class_to_card{$classLower} = $cardName;
        }
        close($dfh);
    }
}

# Fallback to prettify CamelCase to spaced words when card not found in data
sub camel_to_words {
    my ($camel) = @_;
    # Handle inner classes by taking last segment after $
    $camel =~ s/.*\$//;
    my @words = $camel =~ /[A-Z]?[a-z]+|[A-Z]+(?![a-z])|\d+/g;
    return join(' ', @words);
}

# Only consider files under a path segment 'mage/cards' and with .java extension
my $wanted = sub {
    return unless -f $_;
    return unless $_ =~ /\.java$/i;

    my $path = $File::Find::name;

    # Normalize path separators to '/'
    my $norm = $path;
    $norm =~ s/\\/\//g;

    # Require 'mage/cards' in the path to limit to the package
    return unless $norm =~ /\bmage\/cards\b/;

    # Extract source class name from file name (without extension)
    my ($vol, $dirs, $file) = File::Spec->splitpath($path);
    my $srcClass = $file;
    $srcClass =~ s/\.java$//i;

    # Read the file and search for the assignment
    open my $fh, '<', $path or do {
        warn "Could not open $path: $!\n";
        return;
    };
    my $line_no = 0;
    while (my $line = <$fh>) {
        $line_no++;
        if ($line =~ /$assign_re/) {
            my $assignedClass = $1; # captured assigned class name (back side)
            chomp $line;
            push @results, {
                path => $norm,
                line_no => $line_no,
                line => $line,
                srcClass => $srcClass,
                assignedClass => $assignedClass,
            };
        }
    }
    close $fh;
};

find($wanted, $root);

# Build unique set of front // back card name pairs
my %seen_pairs;
for my $r (@results) {
    my $srcClass = $r->{srcClass};
    my $assignedClass = $r->{assignedClass};

    # Resolve front card name
    my $frontLower = lcfirst($srcClass);
    my $frontName = $class_to_card{$frontLower};
    if (!defined $frontName || $frontName eq '') {
        $frontName = camel_to_words($srcClass);
    }

    # Resolve back card name
    my $backLower = lcfirst($assignedClass);
    my $backName = $class_to_card{$backLower};
    if (!defined $backName || $backName eq '') {
        my $outer = $assignedClass;
        $outer =~ s/\$.*$//;
        my $outerLower = lcfirst($outer);
        $backName = $class_to_card{$outerLower};
    }
    if (!defined $backName || $backName eq '') {
        $backName = camel_to_words($assignedClass);
    }

    my $line = "- [ ] -- $frontName // $backName";
    $seen_pairs{$line} = 1;
}

# Print in GitHub issue checklist format, for front // back pairs
if (!keys %seen_pairs) {
    print "No assignments to secondSideCardClazz found under mage.cards.\n";
    exit 0;
}

# Sort for stable output
for my $line (sort keys %seen_pairs) {
    print "$line\n";
}
