package mage.cards.t;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldFromGraveyardTriggeredAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.UndyingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetOpponent;

import java.util.List;
import java.util.UUID;

/**
 * @author noxx
 */
public final class TreacherousPitDweller extends CardImpl {

    public TreacherousPitDweller(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{B}{B}");
        this.subtype.add(SubType.DEMON);

        this.power = new MageInt(4);
        this.toughness = new MageInt(3);

        // Undying
        this.addAbility(new UndyingAbility());

        // When Treacherous Pit-Dweller enters the battlefield from a graveyard, target opponent gains control of it.
        Ability ability = new EntersBattlefieldFromGraveyardTriggeredAbility(new TreacherousPitDwellerEffect());
        ability.addTarget(new TargetOpponent());
        this.addAbility(ability);
    }

    private TreacherousPitDweller(final TreacherousPitDweller card) {
        super(card);
    }

    @Override
    public TreacherousPitDweller copy() {
        return new TreacherousPitDweller(this);
    }
}

class TreacherousPitDwellerEffect extends ContinuousEffectImpl {

    TreacherousPitDwellerEffect() {
        super(Duration.Custom, Layer.ControlChangingEffects_2, SubLayer.NA, Outcome.GainControl);
        staticText = "target opponent gains control of {this}";
    }

    private TreacherousPitDwellerEffect(final TreacherousPitDwellerEffect effect) {
        super(effect);
    }

    @Override
    public TreacherousPitDwellerEffect copy() {
        return new TreacherousPitDwellerEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Permanent) object).changeControllerId(source.getFirstTarget(), game, source);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        Player targetOpponent = game.getPlayer(source.getFirstTarget());
        if (permanent != null && targetOpponent != null) {
            affectedObjects.add(permanent);
            return true;
        }
        discard();
        return false;
    }

}
