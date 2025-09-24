package mage.cards.t;

import mage.MageInt;
import mage.MageItem;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.common.LeavesBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ChooseCreatureEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.util.CardUtil;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author weirddan455
 */
public final class TyrannicalPitlord extends CardImpl {

    public TyrannicalPitlord(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{B}{B}");

        this.subtype.add(SubType.DEMON);
        this.power = new MageInt(6);
        this.toughness = new MageInt(6);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Trample
        this.addAbility(TrampleAbility.getInstance());

        // As Tyrannical Pitlord enters the battlefield, choose another creature you control.
        this.addAbility(new AsEntersBattlefieldAbility(new ChooseCreatureEffect()));

        // The chosen creature gets +3/+3 and has flying.
        Ability ability = new SimpleStaticAbility(new TyrannicalPitlordBoostEffect());
        this.addAbility(ability);

        // When Tyrannical Pitlord leaves the battlefield, sacrifice the chosen creature.
        this.addAbility(new LeavesBattlefieldTriggeredAbility(new TyrannicalPitlordSacrificeEffect(), false));
    }

    private TyrannicalPitlord(final TyrannicalPitlord card) {
        super(card);
    }

    @Override
    public TyrannicalPitlord copy() {
        return new TyrannicalPitlord(this);
    }
}

class TyrannicalPitlordBoostEffect extends ContinuousEffectImpl {

    TyrannicalPitlordBoostEffect() {
        super(Duration.WhileOnBattlefield, Outcome.BoostCreature);
        this.staticText = "the chosen creature gets +3/+3 and has flying";
    }

    private TyrannicalPitlordBoostEffect(final TyrannicalPitlordBoostEffect effect) {
        super(effect);
    }

    @Override
    public TyrannicalPitlordBoostEffect copy() {
        return new TyrannicalPitlordBoostEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            switch (layer) {
                case AbilityAddingRemovingEffects_6:
                    permanent.addAbility(FlyingAbility.getInstance(), source.getSourceId(), game);
                    break;
                case PTChangingEffects_7:
                    if (sublayer == SubLayer.ModifyPT_7c) {
                        permanent.addPower(3);
                        permanent.addToughness(3);
                    }
                    break;
            }
            ((Permanent) object).addPower(3);
            ((Permanent) object).addToughness(3);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Object chosenCreature = game.getState().getValue(CardUtil.getCardZoneString("chosenCreature", source.getSourceId(), game));
        if (!(chosenCreature instanceof MageObjectReference)) {
            return false;
        }
        Permanent permanent = ((MageObjectReference) chosenCreature).getPermanent(game);
        if (permanent == null) {
            return false;
        }
        affectedObjects.add(permanent);
        return true;
    }

    @Override
    public boolean hasLayer(Layer layer) {
        return layer == Layer.PTChangingEffects_7 || layer == Layer.AbilityAddingRemovingEffects_6;
    }

    @Override
    public boolean hasSubLayer(SubLayer sublayer) {
        return sublayer == SubLayer.ModifyPT_7c || sublayer == SubLayer.NA;
    }
}

class TyrannicalPitlordSacrificeEffect extends OneShotEffect {

    TyrannicalPitlordSacrificeEffect() {
        super(Outcome.Sacrifice);
        this.staticText = "sacrifice the chosen creature";
    }

    private TyrannicalPitlordSacrificeEffect(final TyrannicalPitlordSacrificeEffect effect) {
        super(effect);
    }

    @Override
    public TyrannicalPitlordSacrificeEffect copy() {
        return new TyrannicalPitlordSacrificeEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Object chosenCreature = game.getState().getValue(CardUtil.getCardZoneString("chosenCreature", source.getSourceId(), game, true));
        if (!(chosenCreature instanceof MageObjectReference)) {
            return false;
        }
        Permanent permanent = ((MageObjectReference) chosenCreature).getPermanent(game);
        if (permanent == null) {
            return false;
        }
        permanent.sacrifice(source, game);
        return true;
    }
}
