package mage.cards.b;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.mana.AnyColorManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.stack.Spell;
import mage.target.targetpointer.FixedTarget;
import mage.watchers.Watcher;

import java.util.UUID;

/**
 * @author PurpleCrowbar
 */
public final class Biophagus extends CardImpl {

    public Biophagus(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}");
        this.subtype.add(SubType.HUMAN, SubType.TYRANID, SubType.WIZARD);
        this.power = new MageInt(1);
        this.toughness = new MageInt(3);

        // Genomic Enhancement — {T}: Add one mana of any color. If this mana is spent to cast a creature spell,
        // that creature enters the battlefield with an additional +1/+1 counter on it.
        Ability ability = new AnyColorManaAbility(new TapSourceCost(), true).withFlavorWord("Genomic Enhancement");
        ability.getEffects().get(0).setText("Add one mana of any color. If this mana is spent to cast a creature spell, " +
                "that creature enters the battlefield with an additional +1/+1 counter on it.");
        this.addAbility(ability, new BiophagusWatcher());
    }

    private Biophagus(final Biophagus card) {
        super(card);
    }

    @Override
    public Biophagus copy() {
        return new Biophagus(this);
    }
}

class BiophagusWatcher extends Watcher {
    BiophagusWatcher() {
        super(WatcherScope.CARD);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.MANA_PAID) {
            Spell target = game.getSpell(event.getTargetId());
            if (event.getSourceId() != null && event.getSourceId().equals(this.getSourceId())
                    && target != null && target.isCreature(game) && event.getFlag()) {
                ContinuousEffect effect = new EntersWithCountersEffect(Duration.EndOfTurn, ContinuousAffected.STATIC_OR_DYNAMIC, CounterType.P1P1.createInstance())
                        .setTargetPointer(new FixedTarget(target, game));
                game.addEffect(effect,target.getSpellAbility());
            }
        }
    }
}
