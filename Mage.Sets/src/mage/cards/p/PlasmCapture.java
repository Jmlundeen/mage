
package mage.cards.p;

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
import mage.constants.TargetController;
import mage.filter.StaticTypedFilters;
import mage.target.TargetGeneric;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class PlasmCapture extends CardImpl {

    public PlasmCapture(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{G}{G}{U}{U}");

        // Counter target spell. At the beginning of your next precombat main phase, add X mana in any combination of colors, where X is that spell's converted mana cost.
        this.getSpellAbility().addTarget(new TargetGeneric(StaticTypedFilters.SPELL));
        this.getSpellAbility().addEffect(new CounterEffect()
                .setText("counter target spell")
                .setRememberManaValue(true)
        );
        ManaEffect effect = ComposedManaAbilityBuilder.builder()
                .addDynamicAnyCombination(CounteredManaValue.instance)
                .ruleText("Add X mana in any combination of colors, where X is that spell's mana value")
                .buildEffect();
        this.getSpellAbility().addEffect(new CreateDelayedTriggeredAbilityEffect(
                new AtTheBeginOfMainPhaseDelayedTriggeredAbility(effect, false, TargetController.YOU, PhaseSelection.NEXT_PRECOMBAT_MAIN)
        ));
    }

    private PlasmCapture(final PlasmCapture card) {
        super(card);
    }

    @Override
    public PlasmCapture copy() {
        return new PlasmCapture(this);
    }
}
