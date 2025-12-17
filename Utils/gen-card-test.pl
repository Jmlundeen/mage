#!/usr/bin/perl -w

#author: North/Jmlundeen
=begin comment

To use this script you can call it with perl ./gen-card-test.pl "Storm Crow" "Lightning Bolt"
The first argument (Storm Crow) is the main card to generate a test class for.
The cards after (Lighting Bolt) will place card info and create a variable inside the test class.
You can add as many additional cards as you like.

You can also call the script without arguments and it will prompt you for card names
=cut

use Text::Template;
use strict;
use File::Path qw(make_path);

my $authorFile = 'author.txt';
my $dataFile = 'mtg-cards-data.txt';
my $setsFile = 'mtg-sets-data.txt';
my $knownSetsFile = 'known-sets.txt';
my $keywordsFile = 'keywords.txt';
my $cardInfoTemplate = Text::Template->new(TYPE => 'FILE', SOURCE => 'cardInfo.tmpl', DELIMITERS => [ '[=', '=]' ]);
my $cardWithPartsInfoTemplate = Text::Template->new(TYPE => 'FILE', SOURCE => 'cardWithPartsInfo.tmpl', DELIMITERS => [ '[=', '=]' ]);

my %cards;
my %sets;
my %knownSets;
my %keywords;

sub toCamelCase {
    my $string = $_[0];
    $string =~ s/\b([\w']+)\b/ucfirst($1)/ge;
    $string =~ s/[-,\s\':.!\/]//g;
    $string;
}

sub fixCost {
    my $string = $_[0];
    $string =~ s/{([2BUGRW])([2BUGRW])}/{$1\/$2}/g;
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


sub generateCardInfo {
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
    $vars{'cardOneAbilities'} = join("\n    * ", split(/\$/, $card[8]));

    return $infoTemplate->fill_in(HASH => \%vars);
}


my $author;
if (-e $authorFile) {
    open(DATA, $authorFile) || die "can't open $authorFile : $!";
    $author = <DATA>;
    chomp $author;
    close(DATA);
} else {
    $author = 'anonymous';
}

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

open(DATA, $knownSetsFile) || die "can't open $knownSetsFile : $!";
while (my $line = <DATA>) {
    my @data = split('\\|', $line);
    $knownSets{$data[0]} = $data[1];
}
close(DATA);

open(DATA, $keywordsFile) || die "can't open $keywordsFile : $!";
while (my $line = <DATA>) {
    my @data = split('\\|', $line);
    $keywords{toCamelCase($data[0])} = $data[1];
}
close(DATA);

# Get card names from arguments or prompt
my @cardNames = @ARGV;
if (@cardNames == 0) {
    print 'Enter a card name: ';
    my $input = <STDIN>;
    chomp $input;
    push @cardNames, $input;

    # Prompt for additional cards
    print 'Enter additional card names (one per line, empty line to finish): ';
    while (my $additionalCard = <STDIN>) {
        chomp $additionalCard;
        last if $additionalCard eq '';  # Empty line ends input
        push @cardNames, $additionalCard;
    }
}

# Trim whitespace for all inputs
foreach my $i (0..$#cardNames) {
    $cardNames[$i] =~ s/^\s+|\s+$//g if defined $cardNames[$i];
}

# Main card is the first one
my $mainCardNameInput = $cardNames[0];

# Resolve main card with loose matching
my $resolvedMain = resolveCardName($mainCardNameInput);
if (!defined $resolvedMain) {
    die "Card name doesn't exist or is ambiguous: $mainCardNameInput\n";
}
my $mainCardName = $resolvedMain;

my @additionalCardsInput = ();
if (@cardNames > 1) {
    @additionalCardsInput = @cardNames[1..$#cardNames];
}

if (!exists $cards{$mainCardName}) {
    die "Card name doesn't exist: $mainCardName\n";
}

my $cardTemplate = 'cardTest.tmpl';
my $originalName = $mainCardName;
my $setCode;

# Generate lines to corresponding sets
my %vars;
$vars{'className'} = toCamelCase($mainCardName);
$vars{'classNameLower'} = lcfirst(toCamelCase($mainCardName));
$vars{'cardNameFirstLetter'} = lc substr($mainCardName, 0, 1);

foreach my $setName (keys %{$cards{$originalName}}) {
    if (exists $sets{$setName}) {
        $setCode = lc($sets{$setName});
        last;  # Use the first valid set found
    }
}

# Fallback if no valid set code was found
unless (defined $setCode) {
    warn "Warning: No valid set code found for card '$mainCardName'. Using 'unk' as fallback.\n";
    warn "Available sets for this card: " . join(", ", keys %{$cards{$originalName}}) . "\n";
    $setCode = 'unk';
}

# Check if card is already implemented
my $fileName = "../Mage.Tests/src/test/java/org/mage/test/cards/single/" . $setCode . "/" . toCamelCase($mainCardName) . "Test.java";
if (-e $fileName) {
    die "$mainCardName is already implemented.\n$fileName\n";
}

# Create directory if it doesn't exist
my $dir = "../Mage.Tests/src/test/java/org/mage/test/cards/single/" . $setCode;
make_path($dir) unless -d $dir;

# Generate the card templates
my $result;
my $template = Text::Template->new(TYPE => 'FILE', SOURCE => $cardTemplate, DELIMITERS => [ '[=', '=]' ]);
my $infoTemplate = Text::Template->new(TYPE => 'FILE', SOURCE => $cardInfoTemplate, DELIMITERS => [ '[=', '=]' ]);

$vars{'author'} = $author;
$vars{'setCode'} = $setCode;

# Generate main card info
my $allCardInfo = generateCardInfo($mainCardName);

# Generate additional card info templates (resolve each loosely)
foreach my $additionalCardInput (@additionalCardsInput) {
    my $resolved = resolveCardName($additionalCardInput);
    if (!defined $resolved) {
        warn "Skipping additional card (not found or ambiguous): $additionalCardInput\n";
        next;
    }
    my $additionalInfo = generateCardInfo($resolved);
    if (defined $additionalInfo && $additionalInfo ne '') {
        $allCardInfo .= "\n\n" . $additionalInfo;
    }
}

$vars{'cardInfo'} = $allCardInfo;
$result = $template->fill_in(HASH => \%vars);

open CARD, "> $fileName";
print CARD $result;
close CARD;

print "$fileName\n";
if (@additionalCardsInput > 0) {
    print "Additional cards included: " . join(", ", map { resolveCardName($_) // $_ } @additionalCardsInput) . "\n";
}