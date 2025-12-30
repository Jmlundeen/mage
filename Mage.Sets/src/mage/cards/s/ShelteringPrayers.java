package mage.cards.s;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.ShroudAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterLandPermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author jeffwadsworth
 */
public final class ShelteringPrayers extends CardImpl {

    public ShelteringPrayers(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{W}");

        // Basic lands each player controls have shroud as long as that player controls three or fewer lands.
        this.addAbility(new SimpleStaticAbility(new ShelteringPrayersEffect()));

    }

    private ShelteringPrayers(final ShelteringPrayers card) {
        super(card);
    }

    @Override
    public ShelteringPrayers copy() {
        return new ShelteringPrayers(this);
    }
}

class ShelteringPrayersEffect extends ContinuousEffectImpl {

    ShelteringPrayersEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        staticText = "Basic lands each player controls have shroud as long as that player controls three or fewer lands.";

    }

    private ShelteringPrayersEffect(final ShelteringPrayersEffect effect) {
        super(effect);
    }

    @Override
    public ShelteringPrayersEffect copy() {
        return new ShelteringPrayersEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Permanent) object).addAbility(ShroudAbility.getInstance(), source.getSourceId(), game);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (UUID playerId : game.getState().getPlayersInRange(source.getControllerId(), game)) {
            Player player = game.getPlayer(playerId);
            if (player != null && game.getBattlefield().getAllActivePermanents(new FilterLandPermanent(), playerId, game).size() < 4) {
                for (Permanent land : game.getBattlefield().getAllActivePermanents(new FilterLandPermanent(), playerId, game)) {
                    if (land != null && land.isBasic(game)) {
                        affectedObjects.add(land);
                    }
                }
            }
        }
        return !affectedObjects.isEmpty();
    }
}
