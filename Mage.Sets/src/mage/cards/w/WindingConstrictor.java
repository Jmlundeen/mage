package mage.cards.w;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.replacement.ReplaceCounterEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.TargetController;
import mage.filter.StaticFilters;

import java.util.UUID;

/**
 * @author LevelX2
 */
public final class WindingConstrictor extends CardImpl {

    public WindingConstrictor(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{B}{G}");

        this.subtype.add(SubType.SNAKE);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // If one or more counters would be put on an artifact or creature you control, that many plus one of each of those kinds of counters are put on that permanent instead.
        this.addAbility(new SimpleStaticAbility(new ReplaceCounterEffect(ReplaceCounterEffect.ModificationType.ADD, 1)
                .setPermanentFilter(StaticFilters.FILTER_CONTROLLED_PERMANENT_ARTIFACT_OR_CREATURE)
                .setText("If one or more counters would be put on an artifact or creature you control, " +
                        "that many plus one of each of those kinds of counters are put on that permanent instead")
        ));

        // If you would get one or more counters, you get that many plus one of each of those kinds of counters instead.
        this.addAbility(new SimpleStaticAbility(new ReplaceCounterEffect(ReplaceCounterEffect.ModificationType.ADD, 1)
                .setValidPlayerTarget(TargetController.YOU)
                .setText("If you would get one or more counters, you get that many plus one of each of those kinds of counters instead")
        ));
    }

    private WindingConstrictor(final WindingConstrictor card) {
        super(card);
    }

    @Override
    public WindingConstrictor copy() {
        return new WindingConstrictor(this);
    }
}
