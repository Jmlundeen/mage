package mage.cards.m;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.keyword.ForetellAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.game.events.EntersTheBattlefieldEvent;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeGroupEvent;
import mage.game.permanent.Permanent;
import mage.target.TargetPermanent;
import mage.watchers.Watcher;

import java.util.*;

/**
 * @author TheElk801
 */
public final class MysticReflection extends CardImpl {

    private static final FilterPermanent filter = new FilterCreaturePermanent("nonlegendary creature");

    static {
        filter.add(Predicates.not(SuperType.LEGENDARY.getPredicate()));
    }

    public MysticReflection(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{1}{U}");

        // Choose target nonlegendary creature. The next time one or more creatures or planeswalkers enter the battlefield this turn, they enter as copies of the chosen creature instead.
        this.getSpellAbility().addEffect(new MysticReflectionEffect());
        this.getSpellAbility().addTarget(new TargetPermanent(filter));
        this.getSpellAbility().addWatcher(new MysticReflectionWatcher());

        // Foretell {U}
        this.addAbility(new ForetellAbility(this, "{U}"));
    }

    private MysticReflection(final MysticReflection card) {
        super(card);
    }

    @Override
    public MysticReflection copy() {
        return new MysticReflection(this);
    }
}

class MysticReflectionEffect extends OneShotEffect {

    MysticReflectionEffect() {
        super(Outcome.Benefit);
        staticText = "Choose target nonlegendary creature. The next time one or more creatures or planeswalkers "
                + "enter the battlefield this turn, they enter as copies of the chosen creature.";
    }

    private MysticReflectionEffect(final MysticReflectionEffect effect) {
        super(effect);
    }

    @Override
    public MysticReflectionEffect copy() {
        return new MysticReflectionEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent targetedPermanent = game.getPermanent(source.getFirstTarget());
        MysticReflectionWatcher watcher = game.getState().getWatcher(MysticReflectionWatcher.class);
        game.addEffect(new MysticReflectionReplacementEffect(watcher.getEnteredThisTurn(), targetedPermanent), source);
        return true;
    }
}

class MysticReflectionReplacementEffect extends ReplacementEffectImpl {

    private final int enteredThisTurn;
    private final Permanent copiedPermanent;

    MysticReflectionReplacementEffect(int enteredThisTurn, Permanent copiedPermanent) {
        super(Duration.EndOfTurn, Outcome.Copy, false);
        this.enteredThisTurn = enteredThisTurn;
        this.copiedPermanent = copiedPermanent;
        staticText = "The next time one or more creatures or planeswalkers "
                + "enter the battlefield this turn, they enter as copies of {this}";
    }

    private MysticReflectionReplacementEffect(final MysticReflectionReplacementEffect effect) {
        super(effect);
        this.enteredThisTurn = effect.enteredThisTurn;
        this.copiedPermanent = effect.copiedPermanent;
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ENTERS_THE_BATTLEFIELD;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        MysticReflectionWatcher watcher = game.getState().getWatcher(MysticReflectionWatcher.class);
        if (watcher != null) {
            if (watcher.getEnteredThisTurn() > this.enteredThisTurn) {
                discard();
                return false;
            }
        }
        Permanent permanentEnteringTheBattlefield = ((EntersTheBattlefieldEvent) event).getTarget();
        return permanentEnteringTheBattlefield != null
                && copiedPermanent != null
                && (permanentEnteringTheBattlefield.isCreature(game)
                || permanentEnteringTheBattlefield.isPlaneswalker(game));
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        game.copyPermanent(copiedPermanent, event.getTargetId(), source, null);
        return false;
    }

    @Override
    public MysticReflectionReplacementEffect copy() {
        return new MysticReflectionReplacementEffect(this);
    }
}

class MysticReflectionWatcher extends Watcher {

    private int enteredThisTurn = 0;

    MysticReflectionWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() != GameEvent.EventType.ZONE_CHANGE_GROUP) {
            return;
        }
        ZoneChangeGroupEvent zEvent = (ZoneChangeGroupEvent) event;
        if (zEvent.getToZone() != Zone.BATTLEFIELD) {
            return;
        }
        Set<Card> cards = new HashSet<>();
        cards.addAll(zEvent.getCards());
        cards.addAll(zEvent.getTokens());
        if (cards.stream()
                .filter(Objects::nonNull)
                .map(MageItem::getId)
                .map(game::getPermanent)
                .filter(Objects::nonNull)
                .anyMatch(p -> p.isPlaneswalker(game) || p.isCreature(game))) {
            enteredThisTurn++;
        }
    }

    @Override
    public void reset() {
        super.reset();
        enteredThisTurn = 0;
    }

    public int getEnteredThisTurn() {
        return enteredThisTurn;
    }
}
