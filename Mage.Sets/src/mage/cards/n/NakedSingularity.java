package mage.cards.n;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.mana.ReplaceManaEffect;
import mage.abilities.keyword.CumulativeUpkeepAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.LogicalPredicate;

import java.util.Map;
import java.util.UUID;

/**
 * @author emerald000
 */
public final class NakedSingularity extends CardImpl {

    private static final ReplaceManaEffect.ProducedManaTransform manaTransform = ReplaceManaEffect.replaceBySubtypeMap(Map.of(
            SubType.PLAINS, ManaType.RED,
            SubType.ISLAND, ManaType.GREEN,
            SubType.SWAMP, ManaType.WHITE,
            SubType.MOUNTAIN, ManaType.BLUE,
            SubType.FOREST, ManaType.BLACK
    ));

    private static final FilterTyped filter = new FilterTyped("land with a basic land type")
            .add(
                    LogicalPredicate.or(
                            SubType.PLAINS.getPredicate(),
                            SubType.ISLAND.getPredicate(),
                            SubType.SWAMP.getPredicate(),
                            SubType.MOUNTAIN.getPredicate(),
                            SubType.FOREST.getPredicate()
                    )
            );

    public NakedSingularity(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{5}");

        // Cumulative upkeep {3}
        this.addAbility(new CumulativeUpkeepAbility(new GenericManaCost(3)));

        // If tapped for mana, Plains produce {R}, Islands produce {G}, Swamps produce {W}, Mountains produce {U}, and Forests produce {B} instead of any other type.
        this.addAbility(new SimpleStaticAbility(
                ReplaceManaEffect.produced(Duration.WhileOnBattlefield, Outcome.Neutral, manaTransform)
                        .setProducedMatcher(filter)
                        .setText("If tapped for mana, Plains produce {R}, Islands produce {G}, Swamps produce {W}, Mountains produce {U}, and Forests produce {B} instead of any other type")
        ));
    }

    private NakedSingularity(final NakedSingularity card) {
        super(card);
    }

    @Override
    public NakedSingularity copy() {
        return new NakedSingularity(this);
    }
}
