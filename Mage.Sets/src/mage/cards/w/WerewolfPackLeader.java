package mage.cards.w;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.keyword.PackTacticsAbility;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class WerewolfPackLeader extends CardImpl {

    public WerewolfPackLeader(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{G}{G}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.WEREWOLF);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Pack tactics — Whenever Werewolf Pack Leader attacks, if you attacked with creatures with total power 6 or greater this combat, draw a card.
        this.addAbility(new PackTacticsAbility(new DrawCardSourceControllerEffect(1)));

        // {3}{G}: Until end of turn, Werewolf Pack Leader has base power and toughness 5/3, gains trample, and isn't a Human.
        this.addAbility(new SimpleActivatedAbility(new WerewolfPackLeaderEffect(), new ManaCostsImpl<>("{3}{G}")));
    }

    private WerewolfPackLeader(final WerewolfPackLeader card) {
        super(card);
    }

    @Override
    public WerewolfPackLeader copy() {
        return new WerewolfPackLeader(this);
    }
}

class WerewolfPackLeaderEffect extends ContinuousEffectImpl {

    WerewolfPackLeaderEffect() {
        super(Duration.EndOfTurn, Outcome.Benefit);
        staticText = "until end of turn, {this} has base power and toughness 5/3, gains trample, and isn't a Human";
    }

    private WerewolfPackLeaderEffect(final WerewolfPackLeaderEffect effect) {
        super(effect);
    }

    @Override
    public WerewolfPackLeaderEffect copy() {
        return new WerewolfPackLeaderEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            switch (layer) {
                case TypeChangingEffects_4:
                    permanent.removeSubType(game, SubType.HUMAN);
                    break;
                case AbilityAddingRemovingEffects_6:
                    permanent.addAbility(TrampleAbility.getInstance(), source.getSourceId(), game);
                    break;
                case PTChangingEffects_7:
                    if (sublayer == SubLayer.SetPT_7b) {
                        permanent.getPower().setModifiedBaseValue(5);
                        permanent.getToughness().setModifiedBaseValue(3);
                    }
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (permanent != null) {
            affectedObjects.add(permanent);
            return true;
        }
        discard();
        return false;
    }

    @Override
    public boolean hasLayer(Layer layer) {
        switch (layer) {
            case TypeChangingEffects_4:
            case AbilityAddingRemovingEffects_6:
            case PTChangingEffects_7:
                return true;
        }
        return false;
    }

    @Override
    public boolean hasSubLayer(SubLayer sublayer) {
        return sublayer == SubLayer.SetPT_7b || sublayer == SubLayer.NA;
    }
}
