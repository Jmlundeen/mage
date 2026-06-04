package mage.cards.n;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.mana.ReplaceManaEffect;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.permanent.PermanentPredicate;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class NyxbloomAncient extends CardImpl {

    static final FilterTyped filter = new FilterTyped("a permanent you control")
            .addAll(
                    PermanentPredicate.instance,
                    TargetController.YOU.getControllerPredicate()
            );

    public NyxbloomAncient(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT, CardType.CREATURE}, "{4}{G}{G}{G}");

        this.subtype.add(SubType.ELEMENTAL);
        this.power = new MageInt(5);
        this.toughness = new MageInt(5);

        // Trample
        this.addAbility(TrampleAbility.getInstance());

        // If you tap a permanent for mana, it produces three times as much of that mana instead.
        this.addAbility(new SimpleStaticAbility(
                ReplaceManaEffect.produced(Duration.WhileOnBattlefield, Outcome.Benefit, ReplaceManaEffect.multiplyProducedMana(3))
                        .setProducedMatcher(filter)
                        .setText("If you tap a permanent for mana, it produces three times as much of that mana instead")
        ));
    }

    private NyxbloomAncient(final NyxbloomAncient card) {
        super(card);
    }

    @Override
    public NyxbloomAncient copy() {
        return new NyxbloomAncient(this);
    }
}
