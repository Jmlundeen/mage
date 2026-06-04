package mage.cards.p;

import mage.Mana;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.mana.ReplaceManaEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.filter.StaticTypedFilters;

import java.util.UUID;

/**
 * @author L_J
 */
public final class PulseOfLlanowar extends CardImpl {

    public PulseOfLlanowar(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{G}");

        // If a basic land you control is tapped for mana, it produces mana of a color of your choice instead of any other type.
        this.addAbility(new SimpleStaticAbility(ReplaceManaEffect.produced(Duration.WhileOnBattlefield, Outcome.Neutral, ReplaceManaEffect.replaceAllProducedMana(Mana.AnyMana(1)))
                .setProducedMatcher(StaticTypedFilters.BASIC_LAND_YOU_CONTROL)
                .setText("If a basic land you control is tapped for mana, it produces mana of a color of your choice instead of any other type")
        ));
    }

    private PulseOfLlanowar(final PulseOfLlanowar card) {
        super(card);
    }

    @Override
    public PulseOfLlanowar copy() {
        return new PulseOfLlanowar(this);
    }
}
