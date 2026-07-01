
package mage.cards.m;

import mage.abilities.common.delayed.AtTheBeginOfMainPhaseDelayedTriggeredAbility;
import mage.abilities.common.delayed.AtTheBeginOfMainPhaseDelayedTriggeredAbility.PhaseSelection;
import mage.abilities.dynamicvalue.common.manavalue.CounteredManaValue;
import mage.abilities.effects.common.CreateDelayedTriggeredAbilityEffect;
import mage.abilities.effects.common.countered.CounterEffect;
import mage.abilities.effects.mana.ManaEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ManaType;
import mage.constants.TargetController;
import mage.filter.StaticTypedFilters;
import mage.target.TargetGeneric;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class ManaDrain extends CardImpl {

    public ManaDrain(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.INSTANT},"{U}{U}");

        // Counter target spell. At the beginning of your next main phase, add an amount of {C} equal to that spell's mana value.
        this.getSpellAbility().addTarget(new TargetGeneric(StaticTypedFilters.SPELL));
        this.getSpellAbility().addEffect(new CounterEffect()
                .setText("Counter target spell")
                .setRememberManaValue(true));
        ManaEffect effect = ComposedManaAbilityBuilder.builder()
                .addDynamic(CounteredManaValue.instance, ManaType.COLORLESS)
                .ruleText("add an amount of {C} equal to that spell's mana value")
                .buildEffect();
        this.getSpellAbility().addEffect(new CreateDelayedTriggeredAbilityEffect(
                new AtTheBeginOfMainPhaseDelayedTriggeredAbility(effect, false, TargetController.YOU, PhaseSelection.NEXT_MAIN),
                false
        ));
    }

    private ManaDrain(final ManaDrain card) {
        super(card);
    }

    @Override
    public ManaDrain copy() {
        return new ManaDrain(this);
    }
}
