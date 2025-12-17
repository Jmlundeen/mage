#!/usr/bin/perl -w

#author: Jmlundeen

use Text::Template;
use strict;
use utf8;
use open ':std', ':encoding(UTF-8)';

my $dataFile = 'mtg-cards-data.txt';
my $setsFile = 'mtg-sets-data.txt';
my $cardInfoTemplate = Text::Template->new(TYPE => 'FILE', SOURCE => 'cardInfo.tmpl', DELIMITERS => [ '[=', '=]' ]);
my $cardWithPartsInfoTemplate = Text::Template->new(TYPE => 'FILE', SOURCE => 'cardWithPartsInfo.tmpl', DELIMITERS => [ '[=', '=]' ]);

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
# 3) case-insensitive substring match (ignoring punctuation)
# 4) for split cards (containing //), also match on individual card names
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

        # Check if input matches the full name
        if (index($normalized, $normalized_input) != -1) {
            1;
        } else {
            # For split cards (containing //), also check each individual name
            if ($_ =~ /\/\//) {
                my @parts = split(/\s*\/\/\s*/, $_);
                foreach my $part (@parts) {
                    my $normalized_part = lc $part;
                    $normalized_part =~ s/[^\w\s]//g;
                    # Match if input matches a part exactly or is contained in a part
                    if ($normalized_part eq $normalized_input || index($normalized_part, $normalized_input) != -1) {
                        1;
                    }
                }
            }
        }


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
    my ($cardName) = @_;
    my $infoTemplate = $cardInfoTemplate;
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
    $vars{'cardOneClassNameLower'} = lcfirst(toCamelCase($cardName));
    $vars{'cardOneClassName'} = toCamelCase($cardName);
    $vars{'cardOneFirstLetter'} = lc(substr($cardName, 0, 1));
    my @card;

    foreach my $setName (keys %{$cards{$cardName}}) {
        @card = @{(values(%{$cards{$cardName}{$setName}}))[0]};
        last; # Just get the first one
    }
    # clean up em dash
    $card[10] =~ s/—/--/g;
    $card[5] =~ s/—/--/g;
    # clean up minus sign
    $card[8] =~ s/−/-/g;
    $card[13] =~ s/−/-/g;
    if ($card[0] =~ /\/\//) {
        # Split card: use first part of name for class name
        my @parts = split(/\s*\/\/\s*/, $card[0]);
        $vars{'cardOneClassNameLower'} = lcfirst(toCamelCase($parts[0]));
        $vars{'cardOneClassName'} = toCamelCase($parts[0]);
        $vars{'cardTwoClassNameLower'} = lcfirst(toCamelCase($parts[1]));
        $vars{'cardTwoClassName'} = toCamelCase($parts[1]);
        $vars{'cardTwoFirstLetter'} = lc(substr($parts[1], 0, 1));
        $vars{'cardOneName'} = $parts[0];
        $vars{'cardTwoName'} = $parts[1];
        $vars{'cardTwoCost'} = $card[9]; # mana cost of second part
        $vars{'cardTwoType'} = $card[10]; # type line of second part
        if ($card[10] =~ /Planeswalker/i) {
            $vars{'cardTwoLoyalty'} = $card[11];
        } else {
            $vars{'cardTwoPT'} = "$card[11]/$card[12]" if exists $card[11] && exists $card[12]; # power/toughness of second part
        }
        $vars{'cardTwoAbilities'} = join("\n    * ", split(/\$/, $card[13])); # abilities of second part
        $infoTemplate = $cardWithPartsInfoTemplate;
    } else {
        $vars{'cardOneName'} = $card[0];
    }
    $vars{'cardOneCost'} = $card[4];
    $vars{'cardOneType'} = $card[5];
    if ($card[5] =~ /Planeswalker/i) {
        $vars{'cardOneLoyalty'} = $card[6];
    } else {
        if (exists $card[6] && exists $card[7]) {
            $vars{'cardOnePT'} = "$card[6]/$card[7]";
        }
    }
    $vars{'cardOneAbilities'} = join("\n    ", split(/\$/, $card[8]));


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

# Print card info for each card
foreach my $cardName (@cardNames) {
    printCardInfo($cardName);
}