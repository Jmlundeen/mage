package mage.cards.r;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.mana.ReplaceManaEffect;
import mage.abilities.keyword.CumulativeUpkeepAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.LogicalPredicate;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;

import java.util.Map;
import java.util.UUID;

/**
 * @author emerald000 & L_J
 */
public final class RealityTwist extends CardImpl {

    private static final ReplaceManaEffect.ProducedManaTransform manaTransform = ReplaceManaEffect.replaceBySubtypeMap(Map.of(
            SubType.PLAINS, ManaType.RED,
            SubType.SWAMP, ManaType.GREEN,
            SubType.MOUNTAIN, ManaType.WHITE,
            SubType.FOREST, ManaType.BLACK
    ));

    private static final FilterTyped filter = new FilterTyped("land with a basic land type")
            .add(
                    LogicalPredicate.or(
                            IMageObjectPredicate.getOSPPredicate(SubType.PLAINS.getPredicate()),
                            IMageObjectPredicate.getOSPPredicate(SubType.ISLAND.getPredicate()),
                            IMageObjectPredicate.getOSPPredicate(SubType.SWAMP.getPredicate()),
                            IMageObjectPredicate.getOSPPredicate(SubType.MOUNTAIN.getPredicate()),
                            IMageObjectPredicate.getOSPPredicate(SubType.FOREST.getPredicate())
                    )
            );

    public RealityTwist(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{U}{U}{U}");

        // Cumulative upkeep-Pay {1}{U}{U}.
        this.addAbility(new CumulativeUpkeepAbility(new ManaCostsImpl<>("{1}{U}{U}")));

        // If tapped for mana, Plains produce {R}, Swamps produce {G}, Mountains produce {W}, and Forests produce {B} instead of any other type.
        this.addAbility(new SimpleStaticAbility(ReplaceManaEffect.produced(Duration.WhileOnBattlefield, Outcome.Neutral, manaTransform)
                .setProducedMatcher(filter)
                .setText("If tapped for mana, Plains produce {R}, Swamps produce {G}, Mountains produce {W}, and Forests produce {B} instead of any other type")
        ));
    }

    private RealityTwist(final RealityTwist card) {
        super(card);
    }

    @Override
    public RealityTwist copy() {
        return new RealityTwist(this);
    }
}
