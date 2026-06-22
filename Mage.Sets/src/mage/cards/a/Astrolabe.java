
package mage.cards.a;

import mage.abilities.common.delayed.AtTheBeginOfNextUpkeepDelayedTriggeredAbility;
import mage.abilities.costs.common.SacrificeSourceCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.CreateDelayedTriggeredAbilityEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.mana.ComposedManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;

import java.util.UUID;

/**
 *
 * @author fireshoes
 */
public final class Astrolabe extends CardImpl {

    public Astrolabe(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ARTIFACT},"{3}");

        // {1}, {tap}, Sacrifice Astrolabe: Add two mana of any one color. Draw a card at the beginning of the next turn's upkeep.
        ComposedManaAbility manaAbility = ComposedManaAbilityBuilder.builder()
                .cost(new ManaCostsImpl<>("{1}"))
                .cost(new TapSourceCost())
                .cost(new SacrificeSourceCost())
                .addAnyColor(2)
                .ruleText("Add two mana of any one color")
                .build();
        manaAbility.addEffect(new CreateDelayedTriggeredAbilityEffect(
                new AtTheBeginOfNextUpkeepDelayedTriggeredAbility(new DrawCardSourceControllerEffect(1), Duration.OneUse), false));
        manaAbility.setUndoPossible(false);
        this.addAbility(manaAbility);
    }

    private Astrolabe(final Astrolabe card) {
        super(card);
    }

    @Override
    public Astrolabe copy() {
        return new Astrolabe(this);
    }
}
