
package mage.cards.s;

import mage.MageInt;
import mage.MageItem;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.PermanentsOnBattlefieldCount;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.ChooseOpponentEffect;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterLandPermanent;
import mage.filter.predicate.permanent.ControllerIdPredicate;
import mage.game.Game;
import mage.players.Player;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author TheElk801
 */
public final class SkyshroudWarBeast extends CardImpl {

    public SkyshroudWarBeast(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}");

        this.subtype.add(SubType.BEAST);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // Trample
        this.addAbility(TrampleAbility.getInstance());

        // As Skyshroud War Beast enters the battlefield, choose an opponent.
        this.addAbility(new AsEntersBattlefieldAbility(new ChooseOpponentEffect(Outcome.BoostCreature)));

        // Skyshroud War Beast's power and toughness are each equal to the number of nonbasic lands the chosen player controls.
        this.addAbility(new SimpleStaticAbility(Zone.ALL, new SkyshroudWarBeastEffect()));
    }

    private SkyshroudWarBeast(final SkyshroudWarBeast card) {
        super(card);
    }

    @Override
    public SkyshroudWarBeast copy() {
        return new SkyshroudWarBeast(this);
    }
}

class SkyshroudWarBeastEffect extends ContinuousEffectImpl {

    SkyshroudWarBeastEffect() {
        super(Duration.EndOfGame, Layer.PTChangingEffects_7, SubLayer.CharacteristicDefining_7a, Outcome.BoostCreature);
        staticText = "{this}'s power and toughness are each equal to the number of nonbasic lands the chosen player controls";
    }

    private SkyshroudWarBeastEffect(final SkyshroudWarBeastEffect effect) {
        super(effect);
    }

    @Override
    public SkyshroudWarBeastEffect copy() {
        return new SkyshroudWarBeastEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        UUID playerId = (UUID) game.getState().getValue(source.getSourceId().toString() + ChooseOpponentEffect.VALUE_KEY);
        FilterLandPermanent filter = FilterLandPermanent.nonbasicLand();
        filter.add(new ControllerIdPredicate(playerId));

        int number = new PermanentsOnBattlefieldCount(filter).calculate(game, source, this);
        for (MageItem object : affectedObjects) {
            MageObject target = (MageObject) object;
            target.getPower().setModifiedBaseValue(number);
            target.getToughness().setModifiedBaseValue(number);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Player controller = game.getPlayer(source.getControllerId());
        MageObject sourceCard = game.getObject(source);
        if (controller == null || sourceCard == null) {
            return false;
        }
        affectedObjects.add(sourceCard);
        return true;
    }
}
