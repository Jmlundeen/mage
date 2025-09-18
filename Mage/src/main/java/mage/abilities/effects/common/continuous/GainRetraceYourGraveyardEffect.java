package mage.abilities.effects.common.continuous;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.RetraceAbility;
import mage.cards.*;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.filter.FilterCard;
import mage.game.Game;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.List;
import java.util.UUID;

/**
 * "[filter] cards in your graveyard have retrace."
 *
 * @author Susucr
 */
public class GainRetraceYourGraveyardEffect extends ContinuousEffectImpl {

    private final FilterCard filter;

    public GainRetraceYourGraveyardEffect(FilterCard filter) {
        this(Duration.WhileOnBattlefield, filter);
    }

    public GainRetraceYourGraveyardEffect(Duration duration, FilterCard filter) {
        super(duration, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        this.filter = filter;
        staticText = filter.getMessage() + " in your graveyard have retrace.";
    }

    private GainRetraceYourGraveyardEffect(final GainRetraceYourGraveyardEffect effect) {
        super(effect);
        this.filter = effect.filter;
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Card card = (Card) object;
            Ability ability = new RetraceAbility(card);
            ability.setSourceId(card.getId());
            ability.setControllerId(card.getOwnerId());
            game.getState().addOtherAbility(card, ability);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        for (UUID cardId : controller.getGraveyard()) {
            Card card = game.getCard(cardId);
            if (card != null) {
                affectedObjects.addAll(CardUtil.getCastableComponents(card, filter, source, controller, game, null, false));
            }
        }
        return !affectedObjects.isEmpty();
    }

    @Override
    public GainRetraceYourGraveyardEffect copy() {
        return new GainRetraceYourGraveyardEffect(this);
    }
}
