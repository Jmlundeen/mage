
package mage.cards.m;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.ObjectManaValue;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterArtifactPermanent;
import mage.filter.predicate.Predicates;

import java.util.UUID;

/**
 *
 * @author Plopman
 */
public final class MarchOfTheMachines extends CardImpl {

    private static final FilterArtifactPermanent filter = new FilterArtifactPermanent();

    static {
        filter.add(Predicates.not(CardType.CREATURE.getPredicate()));
    }

    public MarchOfTheMachines(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{U}");

        // Each noncreature artifact is an artifact creature with power and toughness each equal to its converted mana cost.
        this.addAbility(new SimpleStaticAbility(new ContinuousEffectBuilder(Outcome.BecomeCreature, filter)
                .withSetPowerAndToughness(ObjectManaValue.instance, ObjectManaValue.instance)
                .withAddedCardTypes(false, CardType.CREATURE)
                .setText("Each noncreature artifact is an artifact creature with power and toughness each equal to " + ObjectManaValue.instance.getMessage())
        ));
    }

    private MarchOfTheMachines(final MarchOfTheMachines card) {
        super(card);
    }

    @Override
    public MarchOfTheMachines copy() {
        return new MarchOfTheMachines(this);
    }
}