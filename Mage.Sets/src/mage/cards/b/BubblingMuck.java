package mage.cards.b;

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
 * @author spjspj
 */
public final class BubblingMuck extends CardImpl {

    public BubblingMuck(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{B}");

        // Until end of turn, whenever a player taps a Swamp for mana, that player adds {B}.
        this.getSpellAbility().addEffect(new CreateDelayedTriggeredAbilityEffect(
                new DelayedTriggeredManaAbility(
                        "Until end of turn, whenever a player taps a Swamp for mana, ",
                        StaticTypedFilters.A_SWAMP,
                        ComposedManaAbilityBuilder.builder()
                                .addStatic(Mana.BlackMana(1))
                                .playerProvider(TargetPointerManaPlayerProvider.instance)
                                .ruleText("that player adds an additional {B}")
                                .buildEffect(),
                        Duration.EndOfTurn,
                        false
                )
                        .withSetTargetPointer(SetTargetPointer.TRIGGERED_CONTROLLER)
        ));
    }

    private BubblingMuck(final BubblingMuck card) {
        super(card);
    }

    @Override
    public BubblingMuck copy() {
        return new BubblingMuck(this);
    }
}
