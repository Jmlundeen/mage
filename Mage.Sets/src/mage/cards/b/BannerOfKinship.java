package mage.cards.b;

import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.CountersSourceCount;
import mage.abilities.dynamicvalue.common.PermanentsOnBattlefieldCount;
import mage.abilities.effects.common.ChooseCreatureTypeEffect;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ContinuousAffected;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.counters.CounterType;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.mageobject.ChosenCreatureTypePredicate;

import java.util.UUID;

/**
 * @author PurpleCrowbar
 */
public final class BannerOfKinship extends CardImpl {
    private static final FilterControlledCreaturePermanent filter = new FilterControlledCreaturePermanent("creature you control of the chosen type");
    private static final DynamicValue xValue = new PermanentsOnBattlefieldCount(filter);
    private static final DynamicValue countersValue = new CountersSourceCount(CounterType.FELLOWSHIP);

    static {
        filter.add(ChosenCreatureTypePredicate.TRUE);
    }
    public BannerOfKinship(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{5}");

        // As this artifact enters, choose a creature type. This artifact enters with a fellowship counter on it for each creature you control of the chosen type.
        AsEntersBattlefieldAbility ability = new AsEntersBattlefieldAbility(new ChooseCreatureTypeEffect(Outcome.BoostCreature));
        ability.addEffect(new EntersWithCountersEffect(Duration.OneUse, ContinuousAffected.SOURCE, CounterType.FELLOWSHIP, xValue));
        this.addAbility(ability);

        // Creatures you control of the chosen type get +1/+1 for each fellowship counter on this artifact.
        this.addAbility(new SimpleStaticAbility(new ContinuousEffectBuilder(Outcome.BoostCreature, filter)
                .withAddPower(countersValue)
                .withAddToughness(countersValue)
                .setText("Creatures you control of the chosen type get +1/+1 for each fellowship counter on this artifact")
        ));
    }

    private BannerOfKinship(final BannerOfKinship card) {
        super(card);
    }

    @Override
    public BannerOfKinship copy() {
        return new BannerOfKinship(this);
    }
}
