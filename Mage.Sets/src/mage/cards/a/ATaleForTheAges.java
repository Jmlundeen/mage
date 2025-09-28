package mage.cards.a;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.permanent.EnchantedPredicate;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class ATaleForTheAges extends CardImpl {

    private static final FilterControlledCreaturePermanent filter = new FilterControlledCreaturePermanent("enchanted creatures you control");

    static {
        filter.add(EnchantedPredicate.instance);
    }

    public ATaleForTheAges(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{1}{W}");

        // Enchanted creatures you control get +2/+2.
        this.addAbility(new SimpleStaticAbility(
                new ContinuousEffectBuilder(Outcome.Benefit, filter)
                        .withAddPower(2)
                        .withAddToughness(2)
                        .setText("{permFilter} get {ptMod}"))
        );
    }

    private ATaleForTheAges(final ATaleForTheAges card) {
        super(card);
    }

    @Override
    public ATaleForTheAges copy() {
        return new ATaleForTheAges(this);
    }
}
