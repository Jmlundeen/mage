
package mage.cards.s;

import mage.abilities.common.delayed.AtTheBeginOfMainPhaseDelayedTriggeredAbility;
import mage.abilities.dynamicvalue.common.manavalue.CounteredManaValue;
import mage.abilities.effects.common.CreateDelayedTriggeredAbilityEffect;
import mage.abilities.effects.common.DoIfClashWonEffect;
import mage.abilities.effects.common.countered.CounterEffect;
import mage.abilities.effects.mana.ManaEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ManaType;
import mage.constants.TargetController;
import mage.target.TargetSpell;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class ScatteringStroke extends CardImpl {

    public ScatteringStroke(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.INSTANT},"{2}{U}{U}");

        // Counter target spell. Clash with an opponent. If you win, at the beginning of your next main phase, you may add an amount of {C} equal to that spell's mana value.
        this.getSpellAbility().addEffect(new CounterEffect()
                .setText("counter target spell")
                .setRememberManaValue(true)
        );
        ManaEffect effect = ComposedManaAbilityBuilder.builder()
                .addDynamic(CounteredManaValue.instance, ManaType.COLORLESS)
                .ruleText("add an amount of {C} equal to that spell's mana value")
                .buildEffect();
        this.getSpellAbility().addEffect(new DoIfClashWonEffect(new CreateDelayedTriggeredAbilityEffect(
                new AtTheBeginOfMainPhaseDelayedTriggeredAbility(effect, true, TargetController.YOU, AtTheBeginOfMainPhaseDelayedTriggeredAbility.PhaseSelection.NEXT_MAIN)
        )));
        this.getSpellAbility().addTarget(new TargetSpell());
    }

    private ScatteringStroke(final ScatteringStroke card) {
        super(card);
    }

    @Override
    public ScatteringStroke copy() {
        return new ScatteringStroke(this);
    }
}
