package mage.cards.g;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.CardsInControllerGraveyardCount;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.HasteAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AbilityWord;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.filter.StaticFilters;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class GolgariRaiders extends CardImpl {

    public GolgariRaiders(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{G}");

        this.subtype.add(SubType.ELF);
        this.subtype.add(SubType.WARRIOR);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // Haste
        this.addAbility(HasteAbility.getInstance());

        // Undergrowth — Golgari Raiders enters the battlefield with a +1/+1 counter on it for each creature card in your graveyard.
        this.addAbility(new SimpleStaticAbility(
                new EntersWithCountersEffect(CounterType.P1P1, new CardsInControllerGraveyardCount(StaticFilters.FILTER_CARD_CREATURE)))
                .setAbilityWord(AbilityWord.UNDERGROWTH)
        );
    }

    private GolgariRaiders(final GolgariRaiders card) {
        super(card);
    }

    @Override
    public GolgariRaiders copy() {
        return new GolgariRaiders(this);
    }
}
