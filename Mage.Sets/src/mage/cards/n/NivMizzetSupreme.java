package mage.cards.n;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.HexproofFromMonocoloredAbility;
import mage.abilities.keyword.JumpStartAbility;
import mage.cards.Card;
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
public final class NivMizzetSupreme extends CardImpl {

    public NivMizzetSupreme(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{W}{U}{B}{R}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.DRAGON);
        this.subtype.add(SubType.AVATAR);
        this.power = new MageInt(5);
        this.toughness = new MageInt(5);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Hexproof from monocolored
        this.addAbility(HexproofFromMonocoloredAbility.getInstance());

        // Each instant and sorcery card in your graveyard that's exactly two colors has jump-start.
        this.addAbility(new SimpleStaticAbility(new NivMizzetSupremeEffect()));
    }

    private NivMizzetSupreme(final NivMizzetSupreme card) {
        super(card);
    }

    @Override
    public NivMizzetSupreme copy() {
        return new NivMizzetSupreme(this);
    }
}

class NivMizzetSupremeEffect extends ContinuousEffectImpl {
    NivMizzetSupremeEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        staticText = "each instant and sorcery card in your graveyard that's exactly two colors has jump-start";
    }

    private NivMizzetSupremeEffect(final NivMizzetSupremeEffect effect) {
        super(effect);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Card card = (Card) object;
            game.getState().addOtherAbility(card, new JumpStartAbility(card));
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        for (Card card : controller.getGraveyard().getCards(game)) {
            if (card.isInstantOrSorcery(game) && card.getColor(game).getColorCount() == 2) {
                affectedObjects.add(card);
            }
        }
        return !affectedObjects.isEmpty();
    }

    @Override
    public NivMizzetSupremeEffect copy() {
        return new NivMizzetSupremeEffect(this);
    }
}
