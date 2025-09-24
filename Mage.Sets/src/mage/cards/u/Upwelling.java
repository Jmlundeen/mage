
package mage.cards.u;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.players.ManaPool;
import mage.players.Player;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class Upwelling extends CardImpl {

    public Upwelling(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{G}");

        // Mana pools don't empty as steps and phases end.
        this.addAbility(new SimpleStaticAbility(new UpwellingRuleEffect()));

    }

    private Upwelling(final Upwelling card) {
        super(card);
    }

    @Override
    public Upwelling copy() {
        return new Upwelling(this);
    }
}

class UpwellingRuleEffect extends ContinuousEffectImpl {

    UpwellingRuleEffect() {
        super(Duration.WhileOnBattlefield, Layer.RulesEffects, SubLayer.NA, Outcome.Detriment);
        staticText = "Players don't lose unspent mana as steps and phases end";
    }

    private UpwellingRuleEffect(final UpwellingRuleEffect effect) {
        super(effect);
    }

    @Override
    public UpwellingRuleEffect copy() {
        return new UpwellingRuleEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Player player = (Player) object;
            ManaPool pool = player.getManaPool();
            pool.addDoNotEmptyManaType(ManaType.WHITE);
            pool.addDoNotEmptyManaType(ManaType.GREEN);
            pool.addDoNotEmptyManaType(ManaType.BLUE);
            pool.addDoNotEmptyManaType(ManaType.RED);
            pool.addDoNotEmptyManaType(ManaType.BLACK);
            pool.addDoNotEmptyManaType(ManaType.COLORLESS);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        for (UUID playerId : game.getState().getPlayersInRange(controller.getId(), game)) {
            Player player = game.getPlayer(playerId);
            if (player != null) {
                affectedObjects.add(player);
            }
        }
        return !affectedObjects.isEmpty();
    }
}
