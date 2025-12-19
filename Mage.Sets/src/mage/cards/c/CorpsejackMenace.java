package mage.cards.c;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.replacement.ReplaceCounterEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.filter.StaticFilters;

import java.util.UUID;

/**
 * http://www.wizards.com/magic/magazine/article.aspx?x=mtg/faq/rtr
 * <p>
 * If a creature you control would enter the battlefield with a number of +1/+1
 * counters on it, it enters with twice that many instead.
 * <p>
 * If you control two Corpsejack Menaces, the number of +1/+1 counters placed is
 * four times the original number. Three Corpsejack Menaces multiplies the
 * original number by eight, and so on.
 *
 * @author LevelX2
 */
public final class CorpsejackMenace extends CardImpl {

    public CorpsejackMenace(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B}{G}");
        this.subtype.add(SubType.FUNGUS);

        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // If one or more +1/+1 counters would be put on a creature you control, twice that many +1/+1 counters are put on it instead.
        this.addAbility(new SimpleStaticAbility(new ReplaceCounterEffect(ReplaceCounterEffect.ModificationType.MULTIPLY, 2)
                .setPermanentFilter(StaticFilters.FILTER_CONTROLLED_CREATURE)
                .addValidCounterTypes(CounterType.P1P1)
                .setText("If one or more +1/+1 counters would be put on a creature you control, " +
                        "twice that many +1/+1 counters are put on it instead")
        ));

    }

    private CorpsejackMenace(final CorpsejackMenace card) {
        super(card);
    }

    @Override
    public CorpsejackMenace copy() {
        return new CorpsejackMenace(this);
    }
}
