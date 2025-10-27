
package mage.cards.s;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.CantBeCounteredSourceAbility;
import mage.abilities.effects.AsThoughEffectImpl;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.common.CantBeCounteredSourceEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.command.Commander;
import mage.game.events.GameEvent;
import mage.game.stack.Spell;
import mage.watchers.Watcher;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author LevelX2
 */
public final class SavageSummoning extends CardImpl {

    public SavageSummoning(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{G}");

        // Savage Summoning can't be countered.
        Ability ability = new CantBeCounteredSourceAbility();
        ability.setRuleAtTheTop(true);
        this.addAbility(ability);

        // The next creature card you cast this turn can be cast as though it had flash.
        // That spell can't be countered. That creature enters the battlefield with an additional +1/+1 counter on it.
        this.getSpellAbility().addEffect(new SavageSummoningAsThoughEffect());
        this.getSpellAbility().addWatcher(new SavageSummoningWatcher());

    }

    private SavageSummoning(final SavageSummoning card) {
        super(card);
    }

    @Override
    public SavageSummoning copy() {
        return new SavageSummoning(this);
    }
}

class SavageSummoningAsThoughEffect extends AsThoughEffectImpl {

    private SavageSummoningWatcher watcher;
    private int zoneChangeCounter;

    public SavageSummoningAsThoughEffect() {
        super(AsThoughEffectType.CAST_AS_INSTANT, Duration.EndOfTurn, Outcome.Benefit);
        staticText = "The next creature spell you cast this turn can be cast as though it had flash." +
                "That spell can't be countered. That creature enters with an additional +1/+1 counter on it.";
    }

    private SavageSummoningAsThoughEffect(final SavageSummoningAsThoughEffect effect) {
        super(effect);
        this.watcher = effect.watcher;
        this.zoneChangeCounter = effect.zoneChangeCounter;
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        watcher = game.getState().getWatcher(SavageSummoningWatcher.class, source.getControllerId());
        Card card = game.getCard(source.getSourceId());
        if (watcher != null && card != null) {
            watcher.setSavageSummoningSpellActive(card, game);
        } else {
            throw new IllegalArgumentException("Consume Savage watcher could not be found");
        }
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return true;
    }

    @Override
    public SavageSummoningAsThoughEffect copy() {
        return new SavageSummoningAsThoughEffect(this);
    }

    @Override
    public boolean applies(UUID objectId, Ability source, UUID affectedControllerId, Game game) {
        if (watcher.isSavageSummoningSpellActive()) {
            MageObject mageObject = game.getBaseObject(objectId);
            if (mageObject instanceof Commander) {
                Commander commander = (Commander) mageObject;
                return commander.isCreature(game) && commander.isControlledBy(source.getControllerId());
            } else if (mageObject instanceof Card) {
                Card card = (Card) mageObject;
                return card.isCreature(game) && card.isOwnedBy(source.getControllerId());
            }
        }
        return false;
    }

}

class SavageSummoningWatcher extends Watcher {

    private final Set<String> savageSummoningSpells = new HashSet<>();

    public SavageSummoningWatcher() {
        super(WatcherScope.PLAYER);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.SPELL_CAST) {
            if (isSavageSummoningSpellActive() && event.getPlayerId().equals(getControllerId())) {
                Spell spell = game.getStack().getSpell(event.getTargetId());
                if (spell != null && spell.isCreature(game)) {
                    ContinuousEffect counterEffect = new CantBeCounteredSourceEffect();
                    game.addEffect(counterEffect, spell.getSpellAbility());
                    ContinuousEffect counterETBEffect = new EntersWithCountersEffect(Duration.OneUse, ContinuousAffected.SOURCE, CounterType.P1P1.createInstance());
                    game.addEffect(counterETBEffect, spell.getSpellAbility());
                    savageSummoningSpells.clear();
                }
            }
        }
    }

    public void setSavageSummoningSpellActive(Card card, Game game) {
        String cardKey = card.getId().toString() + '_' + card.getZoneChangeCounter(game);
        savageSummoningSpells.add(cardKey);
    }

    public boolean isSavageSummoningSpellActive() {
        return !savageSummoningSpells.isEmpty();
    }

    @Override
    public void reset() {
        super.reset();
        savageSummoningSpells.clear();
    }

}
