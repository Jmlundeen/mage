#!/usr/bin/perl -w

#author: Jmlundeen

use Text::Template;
use strict;
use utf8;
use open ':std', ':encoding(UTF-8)';

my $dataFile = 'mtg-cards-data.txt';
my $setsFile = 'mtg-sets-data.txt';
my $cardInfoTemplate = 'cardInfo.tmpl';

my %cards;
my %sets;

sub toCamelCase {
    my $string = $_[0];
    $string =~ s/\b([\w']+)\b/ucfirst($1)/ge;
    $string =~ s/[-,\s\':.!\/]//g;
    $string;
}

# Resolve a user-provided card name to the canonical card key in %cards.
# Tries:
# 1) exact key
# 2) case-insensitive exact match (ignoring punctuation)
# 3) case-insensitive substring match (ignoring punctuation, if single match => use it, if multiple => prompt user)
sub resolveCardName {
    my ($input) = @_;
    return undef unless defined $input;
    # trim whitespace
    $input =~ s/^\s+|\s+$//g;
    return $input if exists $cards{$input};

    my $lc_input = lc $input;
    # Remove punctuation for matching
    my $normalized_input = $lc_input;
    $normalized_input =~ s/[^\w\s]//g;  # Remove all non-alphanumeric except spaces

    # case-insensitive exact (ignoring punctuation)
    foreach my $k (keys %cards) {
        my $normalized_k = lc $k;
        $normalized_k =~ s/[^\w\s]//g;
        return $k if $normalized_k eq $normalized_input;
    }

    # substring (partial) matches (ignoring punctuation)
    my @matches = grep {
        my $normalized = lc $_;
        $normalized =~ s/[^\w\s]//g;
        index($normalized, $normalized_input) != -1
    } keys %cards;
    if (@matches == 1) {
        return $matches[0];
    } elsif (@matches > 1) {
        @matches = sort @matches;
        # If not interactive, don't block; print candidates and return undef
        unless (-t STDIN) {
            warn "Multiple matches found for '$input' (non-interactive):\n";
            foreach my $m (@matches) { warn "  $m\n"; }
            warn "Please be more specific.\n";
            return undef;
        }

        print "Multiple matches found for '$input':\n";
        my $i = 0;
        foreach my $m (@matches) {
            $i++;
            print "  $i) $m\n";
        }

        while (1) {
            print "Select a number (1-$i) or 0 to cancel: ";
            my $choice = <STDIN>;
            unless (defined $choice) { print "\nNo selection (EOF). Skipping.\n"; return undef; }
            chomp $choice;
            $choice =~ s/^\s+|\s+$//g;

            # numeric choice
            if ($choice =~ /^\d+$/) {
                my $num = int($choice);
                if ($num == 0) {
                    return undef;
                } elsif ($num >= 1 && $num <= $i) {
                    return $matches[$num - 1];
                }
            } else {
                # try exact name match among candidates (case-insensitive)
                foreach my $m (@matches) {
                    return $m if lc($m) eq lc($choice);
                }
            }

            print "Invalid selection, please try again.\n";
        }
    }

    return undef;
}

sub printCardInfo {
    my ($cardName, $infoTemplate) = @_;

    # attempt to resolve loosely if direct lookup fails
    if (!exists $cards{$cardName}) {
        my $resolved = resolveCardName($cardName);
        if (!defined $resolved) {
            print "Card name doesn't exist: $cardName (skipping)\n\n";
            return;
        }
        $cardName = $resolved;
    }

    my %vars;
    $vars{'classNameLower'} = lcfirst(toCamelCase($cardName));
    my @card;

    foreach my $setName (keys %{$cards{$cardName}}) {
        @card = @{(values(%{$cards{$cardName}{$setName}}))[0]};
        last; # Just get the first one
    }

    $vars{'cardName'} = $card[0];
    $vars{'manaCost'} = $card[4];
    $vars{'typeLine'} = $card[5];

    # Check if this is a planeswalker
    my $isPlaneswalker = $card[5] =~ /Planeswalker/i;

    my $cardAbilities;
    if ($isPlaneswalker) {
        # For planeswalkers: field 6 is loyalty, field 7 is abilities
        $vars{'loyalty'} = $card[6] if $card[6];  # loyalty
        $cardAbilities = $card[7];
    } else {
        # For non-planeswalkers: field 6/7 is power/toughness, field 8 is abilities
        if ($card[6]) {
            $vars{'powerToughness'} = "$card[6]/$card[7]";
        }
        $cardAbilities = $card[8];
    }

    my @abilities = split(/\$/, $cardAbilities);
    my $abilitiesFormatted = join("\n    ", @abilities);
    $vars{'abilities'} = $abilitiesFormatted;

    my $result = $infoTemplate->fill_in(HASH => \%vars);
    print "$result\n\n";
}

# Load data files
open(DATA, $dataFile) || die "can't open $dataFile : $!";
while (my $line = <DATA>) {
    my @data = split('\\|', $line);
    $cards{$data[0]}{$data[1]}{$data[2]} = \@data;
}
close(DATA);

open(DATA, $setsFile) || die "can't open $setsFile : $!";
while (my $line = <DATA>) {
    my @data = split('\\|', $line);
    $sets{$data[0]} = $data[1];
}
close(DATA);

# Get card names from arguments
my @cardNames = @ARGV;
if (@cardNames == 0) {
    print 'Enter card names (one per line, empty line to finish): ';
    while (my $input = <STDIN>) {
        chomp $input;
        last if $input eq '';
        push @cardNames, $input;
    }
}

if (@cardNames == 0) {
    die "No card names provided.\n";
}

# Load template
my $infoTemplate = Text::Template->new(TYPE => 'FILE', SOURCE => $cardInfoTemplate, DELIMITERS => [ '[=', '=]' ]);

# Print card info for each card
foreach my $cardName (@cardNames) {
    printCardInfo($cardName, $infoTemplate);
}