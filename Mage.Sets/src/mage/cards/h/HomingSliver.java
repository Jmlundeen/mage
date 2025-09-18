
package mage.cards.h;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.CyclingAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.game.Game;
import mage.players.Player;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author Luna Skyrise
 */

public final class HomingSliver extends CardImpl {

    private static final FilterCard filter = new FilterCard("Sliver card");

    static {
        filter.add(SubType.SLIVER.getPredicate());
    }

    public HomingSliver(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{2}{R}");
        this.subtype.add(SubType.SLIVER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Each Sliver card in each player's hand has slivercycling {3}.
        this.addAbility(new SimpleStaticAbility(new HomingSliverEffect()));
        
        // Slivercycling {3}
        this.addAbility(new CyclingAbility(new ManaCostsImpl<>("{3}"), filter, "Slivercycling"));
    }

    private HomingSliver(final HomingSliver card) {
        super(card);
    }

    @Override
    public HomingSliver copy() {
        return new HomingSliver(this);
    }
}

class HomingSliverEffect extends ContinuousEffectImpl {

    private static final FilterCard filter = new FilterCard("Sliver card");

    static {
        filter.add(SubType.SLIVER.getPredicate());
    }

    public HomingSliverEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        this.staticText = "each Sliver card in each player's hand has slivercycling {3}";
    }

    private HomingSliverEffect(final HomingSliverEffect effect) {
        super(effect);
    }

    @Override
    public HomingSliverEffect copy() {
        return new HomingSliverEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            game.getState().addOtherAbility((Card) object, new CyclingAbility(new GenericManaCost(3), filter, "Slivercycling") );
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        for (UUID playerId: game.getState().getPlayersInRange(controller.getId(), game)) {
            Player player = game.getPlayer(playerId);
            if (player != null) {
                affectedObjects.addAll(player.getHand().getCards(filter, game));
            }
        }
        return !affectedObjects.isEmpty();
    }
}
