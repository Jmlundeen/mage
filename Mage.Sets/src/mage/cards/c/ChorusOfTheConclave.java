package mage.cards.c;

import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.ForestwalkAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.players.Player;
import mage.target.targetpointer.FixedTarget;
import mage.util.ManaUtil;

import java.util.UUID;

/**
 * @author jeffwadsworth
 */
public final class ChorusOfTheConclave extends CardImpl {

    public ChorusOfTheConclave(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{G}{G}{W}{W}");
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.DRYAD);

        this.power = new MageInt(3);
        this.toughness = new MageInt(8);

        // Forestwalk
        this.addAbility(new ForestwalkAbility());

        // As an additional cost to cast creature spells, you may pay any amount of mana. If you do, that creature enters the battlefield with that many additional +1/+1 counters on it.
        this.addAbility(new SimpleStaticAbility(new ChorusOfTheConclaveReplacementEffect()));

    }

    private ChorusOfTheConclave(final ChorusOfTheConclave card) {
        super(card);
    }

    @Override
    public ChorusOfTheConclave copy() {
        return new ChorusOfTheConclave(this);
    }
}

class ChorusOfTheConclaveReplacementEffect extends ReplacementEffectImpl {

    ChorusOfTheConclaveReplacementEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Benefit);
        staticText = "As an additional cost to cast creature spells, you may pay any amount of mana. " +
                "If you do, that creature enters the battlefield with that many additional +1/+1 counters on it.";
    }

    private ChorusOfTheConclaveReplacementEffect(final ChorusOfTheConclaveReplacementEffect effect) {
        super(effect);
    }

    @Override
    public ChorusOfTheConclaveReplacementEffect copy() {
        return new ChorusOfTheConclaveReplacementEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.CAST_SPELL;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        if (event.getPlayerId().equals(source.getControllerId())) {
            MageObject spellObject = game.getObject(event.getSourceId());
            if (spellObject != null) {
                return spellObject.isCreature(game);
            }
        }
        return false;
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        int xCost = 0;
        Player controller = game.getPlayer(source.getControllerId());
        if (controller != null) {
            if (controller.chooseUse(Outcome.Benefit, "Pay the additonal cost to add +1/+1 counters to the creature you cast?", source, game)) {
                xCost += ManaUtil.playerPaysXGenericMana(false, "Chorus of the Conclave", controller, source, game);
                if (xCost <= 0) {
                    return false;
                }
                MageObject spellObject = game.getObject(event.getSourceId());
                if (spellObject instanceof Card) {
                    game.addEffect(new EntersWithCountersEffect(Duration.EndOfTurn, ContinuousAffected.STATIC_OR_DYNAMIC, CounterType.P1P1.createInstance(xCost))
                            .setTargetPointer(new FixedTarget(spellObject.getId(), game.getState().getZoneChangeCounter(spellObject.getId()) + 1)), source);
                }
            }
        }
        return false;
    }
}
