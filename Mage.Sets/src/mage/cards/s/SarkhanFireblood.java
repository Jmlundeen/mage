package mage.cards.s;

import mage.abilities.LoyaltyAbility;
import mage.abilities.costs.common.DiscardCardCost;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.DoIfCostPaid;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.Spell.SpellPredicate;
import mage.game.permanent.token.DragonToken2;

import java.util.UUID;

/**
 *
 * @author TheElk801
 */
public final class SarkhanFireblood extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("Dragon spells")
            .addAll(
                    SpellPredicate.instance,
                    SubType.DRAGON.getPredicate()
            );

    public SarkhanFireblood(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.PLANESWALKER}, "{1}{R}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.SARKHAN);
        this.setStartingLoyalty(3);

        // +1: You may discard a card. If you do, draw a card.
        this.addAbility(new LoyaltyAbility(new DoIfCostPaid(
                new DrawCardSourceControllerEffect(1),
                new DiscardCardCost()
        ), 1));

        // +1: Add two mana of any combination of colors. Spend this mana only to cast Dragon spells.
        this.addAbility(new LoyaltyAbility(
                ComposedManaAbilityBuilder.builder()
                        .addAnyCombination(2)
                        .condition(new FilteredSpellManaCondition(filter))
                        .ruleText("Add two mana of any combination of colors. Spend this mana only to cast Dragon spells.")
                        .buildEffect(),
                1
        ));

        // -7: Create four 5/5 red Dragon creature tokens with flying.
        this.addAbility(new LoyaltyAbility(new CreateTokenEffect(new DragonToken2(), 4), -7));
    }

    private SarkhanFireblood(final SarkhanFireblood card) {
        super(card);
    }

    @Override
    public SarkhanFireblood copy() {
        return new SarkhanFireblood(this);
    }
}
