package mage.cards.h;

import mage.Mana;
import mage.abilities.effects.common.CreateDelayedTriggeredAbilityEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.DelayedTriggeredManaAbility;
import mage.abilities.mana.providers.common.player.TargetPointerManaPlayerProvider;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.SetTargetPointer;
import mage.filter.StaticTypedFilters;

import java.util.UUID;

/**
 * @author Plopman
 */
public final class HighTide extends CardImpl {

    public HighTide(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{U}");

        // Until end of turn, whenever a player taps an Island for mana, that player adds {U}.
        this.getSpellAbility().addEffect(new CreateDelayedTriggeredAbilityEffect(
                new DelayedTriggeredManaAbility(
                        "Until end of turn, whenever a player taps an Island for mana, ",
                        StaticTypedFilters.AN_ISLAND,
                        ComposedManaAbilityBuilder.builder()
                                .addStatic(Mana.BlueMana(1))
                                .playerProvider(TargetPointerManaPlayerProvider.instance)
                                .ruleText("that player adds an additional {U}")
                                .buildEffect(),
                        Duration.EndOfTurn,
                        false
                )
                        .withSetTargetPointer(SetTargetPointer.TRIGGERED_CONTROLLER)
        ));
    }

    private HighTide(final HighTide card) {
        super(card);
    }

    @Override
    public HighTide copy() {
        return new HighTide(this);
    }
}
