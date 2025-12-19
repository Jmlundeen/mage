
package mage.cards.s;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.UnearthAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.players.Player;

import java.util.List;
import java.util.UUID;

/**
 * @author Loki
 */
public final class SedrisTheTraitorKing extends CardImpl {

    public SedrisTheTraitorKing(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{3}{U}{B}{R}");
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ZOMBIE);
        this.subtype.add(SubType.WARRIOR);

        this.power = new MageInt(5);
        this.toughness = new MageInt(5);

        // Each creature card in your graveyard has unearth {2}{B}.
        this.addAbility(new SimpleStaticAbility(new SedrisTheTraitorKingEffect()));
    }

    private SedrisTheTraitorKing(final SedrisTheTraitorKing card) {
        super(card);
    }

    @Override
    public SedrisTheTraitorKing copy() {
        return new SedrisTheTraitorKing(this);
    }
}

class SedrisTheTraitorKingEffect extends ContinuousEffectImpl {
    SedrisTheTraitorKingEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        staticText = "Each creature card in your graveyard has unearth {2}{B}";
    }

    private SedrisTheTraitorKingEffect(final SedrisTheTraitorKingEffect effect) {
        super(effect);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Card card = (Card) object;
            UnearthAbility ability = new UnearthAbility(new ManaCostsImpl<>("{2}{B}"));
            ability.setSourceId(card.getId());
            ability.setControllerId(card.getOwnerId());
            game.getState().addOtherAbility(card, ability);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller != null) {
            for (UUID cardId : controller.getGraveyard()) {
                Card card = game.getCard(cardId);
                if (card != null && card.isCreature(game)) {
                    affectedObjects.add(card);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public SedrisTheTraitorKingEffect copy() {
        return new SedrisTheTraitorKingEffect(this);
    }
}
