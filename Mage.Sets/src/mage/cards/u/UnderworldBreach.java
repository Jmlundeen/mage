package mage.cards.u;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.SacrificeSourceEffect;
import mage.abilities.keyword.EscapeAbility;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.players.Player;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class UnderworldBreach extends CardImpl {

    public UnderworldBreach(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{1}{R}");

        // Each nonland card in your graveyard has escape. The escape cost is equal to the card's mana cost plus exile three other cards from your graveyard.
        this.addAbility(new SimpleStaticAbility(new UnderworldBreachEffect()));

        // At the beginning of the end step, sacrifice Underworld Breach.
        this.addAbility(new BeginningOfEndStepTriggeredAbility(
                TargetController.NEXT, new SacrificeSourceEffect(), false
        ));
    }

    private UnderworldBreach(final UnderworldBreach card) {
        super(card);
    }

    @Override
    public UnderworldBreach copy() {
        return new UnderworldBreach(this);
    }
}

class UnderworldBreachEffect extends ContinuousEffectImpl {

    UnderworldBreachEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        staticText = "Each nonland card in your graveyard has escape. " +
                "The escape cost is equal to the card's mana cost plus exile three other cards from your graveyard.";
    }

    private UnderworldBreachEffect(final UnderworldBreachEffect effect) {
        super(effect);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Card card = (Card) object;
            Ability ability = new EscapeAbility(card, card.getManaCost().getText(), 3);
            game.getState().addOtherAbility(card, ability);
        }
    }

    @Override
    public boolean queryAffectedObjects(Ability source, Game game, List<MageItem> affectedObjects) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        controller.getGraveyard()
                .getCards(game)
                .stream()
                .filter(Objects::nonNull)
                .filter(card -> !card.getManaCost().getText().isEmpty()) // card must have a mana cost
                .filter(card -> !card.isLand(game))
                .forEach(affectedObjects::add);
        return !affectedObjects.isEmpty();
    }

    @Override
    public UnderworldBreachEffect copy() {
        return new UnderworldBreachEffect(this);
    }
}
