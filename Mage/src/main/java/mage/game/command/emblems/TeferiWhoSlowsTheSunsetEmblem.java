package mage.game.command.emblems;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.continuous.UntapAllDuringEachOtherPlayersUntapStepEffect;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.command.Emblem;
import mage.players.Player;

import java.util.List;

public class TeferiWhoSlowsTheSunsetEmblem extends Emblem {
    // You get an emblem with "Untap all permanents you control during each opponent's untap step" and "You draw a card during each opponent's draw step."
    public TeferiWhoSlowsTheSunsetEmblem() {
        super("Emblem Teferi");
        this.getAbilities().add(new SimpleStaticAbility(
                Zone.COMMAND, new UntapAllDuringEachOtherPlayersUntapStepEffect(StaticFilters.FILTER_CONTROLLED_PERMANENTS)
        ));
        this.getAbilities().add(new SimpleStaticAbility(new TeferiWhoSlowsTheSunsetEmblemEffect()));
    }

    private TeferiWhoSlowsTheSunsetEmblem(final TeferiWhoSlowsTheSunsetEmblem card) {
        super(card);
    }

    @Override
    public TeferiWhoSlowsTheSunsetEmblem copy() {
        return new TeferiWhoSlowsTheSunsetEmblem(this);
    }
}

class TeferiWhoSlowsTheSunsetEmblemEffect extends ContinuousEffectImpl {

    TeferiWhoSlowsTheSunsetEmblemEffect() {
        super(Duration.EndOfGame, Layer.RulesEffects, SubLayer.NA, Outcome.Benefit);
        staticText = "you draw a card during each opponent's draw step";
    }

    private TeferiWhoSlowsTheSunsetEmblemEffect(final TeferiWhoSlowsTheSunsetEmblemEffect effect) {
        super(effect);
    }

    @Override
    public TeferiWhoSlowsTheSunsetEmblemEffect copy() {
        return new TeferiWhoSlowsTheSunsetEmblemEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Player) object).setDrawsOnOpponentsTurn(true);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        } else {
            affectedObjects.add(controller);
            return true;
        }
    }
}
