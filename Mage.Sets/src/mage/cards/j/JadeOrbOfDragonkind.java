package mage.cards.j;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.delayed.ManaSpentDelayedTriggeredAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.CreateDelayedTriggeredAbilityEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.HexproofAbility;
import mage.abilities.mana.BasicManaAbility;
import mage.abilities.mana.GreenManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ContinuousAffected;
import mage.constants.Duration;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.filter.FilterSpell;
import mage.filter.common.FilterCreatureSpell;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.stack.Spell;

import java.util.UUID;

/**
 * @author Zelane
 */
public final class JadeOrbOfDragonkind extends CardImpl {

    private static final FilterSpell filter = new FilterCreatureSpell("a Dragon creature spell");
    static {
        filter.add(SubType.DRAGON.getPredicate());
    }

    public JadeOrbOfDragonkind(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[] { CardType.ARTIFACT }, "{2}{G}");

        // {T}: Add {G}. When you spend this mana to cast a Dragon creature spell, it enters with an additional +1/+1 counter on it and gains hexproof until your next turn.
        BasicManaAbility ability = new GreenManaAbility();
        Effect additionalCounterEffect = new EntersWithCountersEffect(Duration.OneUse, ContinuousAffected.STATIC_OR_DYNAMIC, CounterType.P1P1.createInstance())
                .withEventCondition((event, source, game, effect) -> {
                    if (!(source instanceof ManaSpentDelayedTriggeredAbility)) {
                        return false;
                    }
                    GameEvent manaUsedEvent = ((ManaSpentDelayedTriggeredAbility) source).getTriggerEvent();
                    Spell spell = game.getStack().getSpell(manaUsedEvent.getTargetId());
                    if (spell == null) {
                        return false;
                    }
                    return event.getSourceId().equals(spell.getSourceId());
                });
        ManaSpentDelayedTriggeredAbility manaSpentAbility = new ManaSpentDelayedTriggeredAbility(additionalCounterEffect, filter);
        manaSpentAbility.addEffect(new JadeOrbGainHexproofEffect());
        ability.addEffect(new CreateDelayedTriggeredAbilityEffect(manaSpentAbility));
        ability.setUndoPossible(false);
        this.addAbility(ability);
    }

    private JadeOrbOfDragonkind(final JadeOrbOfDragonkind card) {
        super(card);
    }

    @Override
    public JadeOrbOfDragonkind copy() {
        return new JadeOrbOfDragonkind(this);
    }
}

class JadeOrbGainHexproofEffect extends GainAbilityTargetEffect {

    JadeOrbGainHexproofEffect() {
        super(HexproofAbility.getInstance(), Duration.UntilYourNextTurn, null, true);
        staticText = "and gains hexproof until your next turn";
    }

    private JadeOrbGainHexproofEffect(final JadeOrbGainHexproofEffect effect) {
        super(effect);
    }

    @Override
    public JadeOrbGainHexproofEffect copy() {
        return new JadeOrbGainHexproofEffect(this);
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);

        GameEvent event = ((ManaSpentDelayedTriggeredAbility) source).getTriggerEvent();
        if (event != null) {
            Spell spell = game.getStack().getSpell(event.getTargetId());
            if (spell != null) {
                affectedObjectList.add(new MageObjectReference(spell.getSourceId(), game));
            }
        }
    }
}
