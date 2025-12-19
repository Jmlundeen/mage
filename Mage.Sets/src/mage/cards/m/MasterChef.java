package mage.cards.m;

import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.StaticFilters;

import java.util.UUID;

public class MasterChef extends CardImpl {

    public MasterChef(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{2}{G}");
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.BACKGROUND);

        // Commander creatures you own have "This creature enters the battlefield with an additional +1/+1 counter on it"
        // and "Other creatures you control enter the battlefield with an additional +1/+1 counter on them."
        Ability selfCounterAbility = new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.P1P1.createInstance())
                .setText("{this} enters with an additional +1/+1 counter on it")
        );
        Ability otherCounterAbility = new SimpleStaticAbility(
            new EntersWithCountersEffect(ContinuousAffected.STATIC_OR_DYNAMIC, CounterType.P1P1.createInstance())
                    .setFilter(StaticFilters.FILTER_OTHER_CONTROLLED_CREATURES)
                    .setText("other creatures you control enter with an additional +1/+1 counter on them")
        );
        this.addAbility(new SimpleStaticAbility(new ContinuousEffectBuilder(Outcome.AddAbility, StaticFilters.FILTER_CREATURES_OWNED_COMMANDER)
                .withGainedAbilities(selfCounterAbility, otherCounterAbility)
                .setText("commander creatures you own have \"This creature enters with an additional +1/+1 counter on it\" and" +
                        " \"Other creatures you control enter with an additional +1/+1 counter on them.\"")
        ));

    }

    private MasterChef(final MasterChef card) {
        super(card);
    }

    @Override
    public MasterChef copy() {
        return new MasterChef(this);
    }
    
}
