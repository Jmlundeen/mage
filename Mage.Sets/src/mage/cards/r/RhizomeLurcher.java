package mage.cards.r;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.CardsInControllerGraveyardCount;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AbilityWord;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.filter.StaticFilters;

import java.util.UUID;

/**
 *
 * @author TheElk801
 */
public final class RhizomeLurcher extends CardImpl {

    public RhizomeLurcher(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B}{G}");

        this.subtype.add(SubType.FUNGUS);
        this.subtype.add(SubType.ZOMBIE);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Undergrowth — Rhizome Lurcher enters the battlefield with a number of +1/+1 counters on it equal to the number of creature cards in your graveyard.
        Ability ability = new SimpleStaticAbility(new EntersWithCountersEffect(
                CounterType.P1P1,
                new CardsInControllerGraveyardCount(StaticFilters.FILTER_CARD_CREATURE))
                .setText("{this} enters with a number of +1/+1 counters on it equal to the number of creature cards in your graveyard")
        );
        ability.setAbilityWord(AbilityWord.UNDERGROWTH);
        this.addAbility(ability);
    }

    private RhizomeLurcher(final RhizomeLurcher card) {
        super(card);
    }

    @Override
    public RhizomeLurcher copy() {
        return new RhizomeLurcher(this);
    }
}
