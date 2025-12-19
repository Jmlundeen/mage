
package mage.cards.k;

import mage.abilities.dynamicvalue.common.ObjectManaValue;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.filter.common.FilterArtifactPermanent;
import mage.filter.predicate.Predicates;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 *
 * @author TheElk801
 */
public final class KarnsTouch extends CardImpl {

    private static final FilterArtifactPermanent filter = new FilterArtifactPermanent("noncreature artifact");

    static {
        filter.add(Predicates.not(CardType.CREATURE.getPredicate()));
    }

    public KarnsTouch(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{U}{U}");

        // Target noncreature artifact becomes an artifact creature with power and toughness each equal to its converted mana cost until end of turn.
        this.getSpellAbility().addEffect(new ContinuousEffectBuilder(Duration.EndOfTurn, Outcome.BecomeCreature)
                .withAddedCardTypes(CardType.ARTIFACT, CardType.CREATURE)
                .withSetPower(ObjectManaValue.PERMANENT)
                .withSetToughness(ObjectManaValue.PERMANENT)
                .setText("Target noncreature artifact becomes an artifact creature with power and toughness each equal to its mana value until end of turn")
        );
        this.getSpellAbility().addTarget(new TargetPermanent(filter));
    }

    private KarnsTouch(final KarnsTouch card) {
        super(card);
    }

    @Override
    public KarnsTouch copy() {
        return new KarnsTouch(this);
    }
}
