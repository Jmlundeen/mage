package mage.cards.m;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.mana.ReplaceManaEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;

import java.util.UUID;

/**
 * @author jeffwadsworth
 */
public final class ManaReflection extends CardImpl {

    public ManaReflection(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{4}{G}{G}");

        // If you tap a permanent for mana, it produces twice as much of that mana instead.
        this.addAbility(new SimpleStaticAbility(
                ReplaceManaEffect.produced(Duration.WhileOnBattlefield, Outcome.Benefit, ReplaceManaEffect.multiplyProducedMana(2))
                        .setProducedMatcher(context -> context.source().isControlledBy(context.recipientId()))
                        .setText("If you tap a permanent for mana, it produces twice as much of that mana instead")
        ));
    }

    private ManaReflection(final ManaReflection card) {
        super(card);
    }

    @Override
    public ManaReflection copy() {
        return new ManaReflection(this);
    }
}
