package mage.cards.l;

import mage.MageInt;
import mage.abilities.common.ChooseABackgroundAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.replacement.ReplaceCounterEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.TargetController;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.predicate.Predicates;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class LaezelVlaakithsChampion extends CardImpl {

    private static final FilterPermanent filter = new FilterControlledPermanent("creature or planeswalker you control");

    static {
        filter.add(Predicates.or(
                CardType.CREATURE.getPredicate(),
                CardType.PLANESWALKER.getPredicate()
        ));
    }

    public LaezelVlaakithsChampion(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.GITH);
        this.subtype.add(SubType.WARRIOR);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // If you would put one or more counters on a creature or planeswalker you control or on yourself, put that many plus one of each of those kinds of counters on that permanent or player instead.
        this.addAbility(new SimpleStaticAbility(new ReplaceCounterEffect(ReplaceCounterEffect.ModificationType.ADD, 1)
                .setEventController(TargetController.YOU)
                .setValidPlayerTarget(TargetController.YOU)
                .setPermanentFilter(filter)
                .setText("if you would put one or more counters on a creature or planeswalker you control or on yourself, " +
                        "put that many plus one of each of those kinds of counters on that permanent or player instead")
        ));

        // Choose a Background
        this.addAbility(ChooseABackgroundAbility.getInstance());
    }

    private LaezelVlaakithsChampion(final LaezelVlaakithsChampion card) {
        super(card);
    }

    @Override
    public LaezelVlaakithsChampion copy() {
        return new LaezelVlaakithsChampion(this);
    }
}
