
package mage.cards.m;

import mage.MageInt;
import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.ChooseCreatureTypeEffect;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.effects.common.enterAttribute.EnterAttributeAddChosenSubtypeEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ContinuousAffected;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.mageobject.ChosenSubtypePredicate;

import java.util.UUID;

/**
 *
 * @author Styxo
 */
public final class MetallicMimic extends CardImpl {

    private static final FilterPermanent filter = new FilterControlledCreaturePermanent("other creature you control of the chosen type");

    static {
        filter.add(ChosenSubtypePredicate.TRUE);
    }

    public MetallicMimic(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{2}");

        this.subtype.add(SubType.SHAPESHIFTER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(1);

        // As Metallic Mimic enters the battlefield, choose a creature type.
        AsEntersBattlefieldAbility ability = new AsEntersBattlefieldAbility(new ChooseCreatureTypeEffect(Outcome.BoostCreature));

        // Metallic Mimic is the chosen type in addition to its other types.
        ability.addEffect(new EnterAttributeAddChosenSubtypeEffect());
        this.addAbility(ability);
        this.addAbility(new SimpleStaticAbility(new ContinuousEffectBuilder(Outcome.Benefit, ContinuousAffected.SOURCE)
                .withGainChosenCreatureType(false)
                .setText("{this} is the chosen type in addition to its other types")
        ));

        // Each other creature you control of the chosen type enters the battlefield with an additional +1/+1 counter on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(ContinuousAffected.STATIC_OR_DYNAMIC, CounterType.P1P1.createInstance())
                .setFilter(filter)
        ));
    }

    private MetallicMimic(final MetallicMimic card) {
        super(card);
    }

    @Override
    public MetallicMimic copy() {
        return new MetallicMimic(this);
    }
}
