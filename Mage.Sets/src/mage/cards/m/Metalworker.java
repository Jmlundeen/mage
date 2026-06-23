package mage.cards.m;

import mage.MageInt;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.MultipliedValue;
import mage.abilities.dynamicvalue.common.RevealedCardsAmount;
import mage.abilities.effects.common.reveal.RevealEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.SimpleManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ManaType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.filter.StaticFilters;

import java.util.UUID;

/**
 * @author anonymous
 */
public final class Metalworker extends CardImpl {

    private static final DynamicValue revealedCardsValue = new MultipliedValue(new RevealedCardsAmount(StaticFilters.FILTER_CARD_ARTIFACT), 2);

    public Metalworker(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{3}");
        this.subtype.add(SubType.CONSTRUCT);

        this.power = new MageInt(1);
        this.toughness = new MageInt(2);

        // {T}: Reveal any number of artifact cards in your hand. Add {C}{C} for each card revealed this way.
        SimpleManaAbility ability = new SimpleManaAbility(new RevealEffect(Outcome.Benefit)
                .setFilter(StaticFilters.FILTER_CARD_ARTIFACT)
                .setMinCardsToReveal(0)
                .rememberRevealed()
                .setText("Reveal any number of artifact cards in your hand"), new TapSourceCost()
        );
        ability.addEffect(new ComposedManaAbilityBuilder()
                .addDynamic(revealedCardsValue, ManaType.COLORLESS)
                .ruleText("Add {C}{C} for each card revealed this way")
                .buildEffect());
        ability.setUndoPossible(false);
        this.addAbility(ability);
    }

    private Metalworker(final Metalworker card) {
        super(card);
    }

    @Override
    public Metalworker copy() {
        return new Metalworker(this);
    }
}
