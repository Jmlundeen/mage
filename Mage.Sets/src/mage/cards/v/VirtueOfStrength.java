package mage.cards.v;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.ReturnFromGraveyardToHandTargetEffect;
import mage.abilities.effects.mana.ReplaceManaEffect;
import mage.cards.AdventureCard;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.filter.FilterCard;
import mage.filter.StaticTypedFilters;
import mage.filter.predicate.Predicates;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.UUID;

/**
 * @author Susucr
 */
public final class VirtueOfStrength extends AdventureCard {

    private static final FilterCard filter = new FilterCard("creature or land card from your graveyard");

    static {
        filter.add(Predicates.or(
                CardType.CREATURE.getPredicate(),
                CardType.LAND.getPredicate()
        ));
    }

    public VirtueOfStrength(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo,
                new CardType[]{CardType.ENCHANTMENT}, "{5}{G}{G}",
                "Garenbrig Growth",
                new CardType[]{CardType.SORCERY}, "{G}");

        // Virtue of Strength
        // If you tap a basic land for mana, it produces three times as much of that mana instead.
        this.getLeftHalfCard().addAbility(new SimpleStaticAbility(
                ReplaceManaEffect.produced(Duration.WhileOnBattlefield, Outcome.Benefit, ReplaceManaEffect.multiplyProducedMana(3))
                        .setProducedMatcher(StaticTypedFilters.BASIC_LAND_YOU_CONTROL)
                        .setText("if you tap a basic land for mana, it produces three times as much of that mana instead")
        ));

        // Garenbrig Growth
        // Return target creature or land card from your graveyard to your hand.
        this.getRightHalfCard().getSpellAbility().addEffect(new ReturnFromGraveyardToHandTargetEffect());
        this.getRightHalfCard().getSpellAbility().addTarget(new TargetCardInYourGraveyard(filter));

        finalizeCard();
    }

    private VirtueOfStrength(final VirtueOfStrength card) {
        super(card);
    }

    @Override
    public VirtueOfStrength copy() {
        return new VirtueOfStrength(this);
    }
}
