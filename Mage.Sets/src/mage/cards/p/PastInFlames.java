package mage.cards.p;

import mage.MageItem;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.FlashbackAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.players.Player;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * @author BetaSteward
 */
public final class PastInFlames extends CardImpl {

    public PastInFlames(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{3}{R}");

        // Each instant and sorcery card in your graveyard gains flashback until end of turn. The flashback cost is equal to its mana cost.
        this.getSpellAbility().addEffect(new PastInFlamesEffect());

        // Flashback {4}{R}
        this.addAbility(new FlashbackAbility(this, new ManaCostsImpl<>("{4}{R}")));

    }

    private PastInFlames(final PastInFlames card) {
        super(card);
    }

    @Override
    public PastInFlames copy() {
        return new PastInFlames(this);
    }
}

class PastInFlamesEffect extends ContinuousEffectImpl {

    PastInFlamesEffect() {
        super(Duration.EndOfTurn, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        this.staticText = "Each instant and sorcery card in your graveyard gains flashback until end of turn. The flashback cost is equal to its mana cost";
    }

    private PastInFlamesEffect(final PastInFlamesEffect effect) {
        super(effect);
    }

    @Override
    public PastInFlamesEffect copy() {
        return new PastInFlamesEffect(this);
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        if (getAffectedObjectsSet()) {
            Player player = game.getPlayer(source.getControllerId());
            if (player != null) {
                player.getGraveyard().stream().map(game::getCard).filter(card1 -> card1.isInstantOrSorcery(game)).forEachOrdered((card) -> {
                    affectedObjectList.add(new MageObjectReference(card, game));
                });
            }
        }
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Card card = (Card) object;
            FlashbackAbility ability = new FlashbackAbility(card, card.getManaCost());
            ability.setSourceId(card.getId());
            ability.setControllerId(card.getOwnerId());
            game.getState().addOtherAbility(card, ability);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return false;
        }
        for (Iterator<MageObjectReference> it = affectedObjectList.iterator(); it.hasNext(); ) {
            MageObjectReference mor = it.next();
            Card card = mor.getCard(game);
            if (card == null) {
                it.remove();
                continue;
            }
            affectedObjects.add(card);
        }
        return !affectedObjects.isEmpty();
    }
}
