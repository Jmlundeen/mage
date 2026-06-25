package mage.cards.t;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.continuous.layers.L6_Abilities.GainAbilitiesOfEffect;
import mage.abilities.keyword.DeathtouchAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.ability.type.ActivatedAbilityPredicate;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class TrazynTheInfinite extends CardImpl {

    static final FilterTyped filter = new FilterTyped("activated abilities of an artifact card")
            .addAll(
                    IMageObjectPredicate.getOSPPredicate(CardType.ARTIFACT.getPredicate()),
                    TargetController.YOU.getOwnerPredicate()
            )
            .add(ActivatedAbilityPredicate.instance);

    public TrazynTheInfinite(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{4}{B}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.NECRON);
        this.power = new MageInt(4);
        this.toughness = new MageInt(6);

        // Deathtouch
        this.addAbility(DeathtouchAbility.getInstance());

        // Prismatic Gallery -- As long as Trazyn the Infinite is on the battlefield, it has all activated abilities of all artifact cards in your graveyard.
        this.addAbility(new SimpleStaticAbility(new GainAbilitiesOfEffect()
                .setAbilityFilter(filter, Zone.GRAVEYARD)
                .setText("As long as {this} is on the battlefield, it has all activated abilities of all artifact cards in your graveyard")
        ).withFlavorWord("Prismatic Gallery"));
    }

    private TrazynTheInfinite(final TrazynTheInfinite card) {
        super(card);
    }

    @Override
    public TrazynTheInfinite copy() {
        return new TrazynTheInfinite(this);
    }
}
