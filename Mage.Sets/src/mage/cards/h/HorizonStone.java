package mage.cards.h;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.players.Player;

import java.util.List;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class HorizonStone extends CardImpl {

    public HorizonStone(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{5}");

        // If you would lose unspent mana, that mana becomes colorless instead.
        this.addAbility(new SimpleStaticAbility(new HorizonStoneEffect()));
    }

    private HorizonStone(final HorizonStone card) {
        super(card);
    }

    @Override
    public HorizonStone copy() {
        return new HorizonStone(this);
    }
}

class HorizonStoneEffect extends ContinuousEffectImpl {

    HorizonStoneEffect() {
        super(Duration.WhileOnBattlefield, Layer.RulesEffects, SubLayer.NA, Outcome.Benefit);
        staticText = "if you would lose unspent mana, that mana becomes colorless instead";
    }

    private HorizonStoneEffect(final HorizonStoneEffect effect) {
        super(effect);
    }

    @Override
    public HorizonStoneEffect copy() {
        return new HorizonStoneEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Player) object).getManaPool().setManaBecomesColorless(true);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Player player = game.getPlayer(source.getControllerId());
        if (player != null) {
            affectedObjects.add(player);
        }
        return !affectedObjects.isEmpty();
    }
}
