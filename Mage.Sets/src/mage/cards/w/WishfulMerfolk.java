package mage.cards.w;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.DefenderAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;
import java.util.UUID;

/**
 * @author jmharmon
 */

public final class WishfulMerfolk extends CardImpl {

    public WishfulMerfolk(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}");
        this.subtype.add(SubType.MERFOLK);

        this.power = new MageInt(3);
        this.toughness = new MageInt(2);

        // Defender
        this.addAbility(DefenderAbility.getInstance());

        // {1}{U}: Wishful Merfolk loses defender and becomes a Human until end of turn.
        this.addAbility(new SimpleActivatedAbility(new WishfulMerfolkEffect(), new ManaCostsImpl<>("{1}{U}")));
    }

    private WishfulMerfolk(final WishfulMerfolk card) {
        super(card);
    }

    @Override
    public WishfulMerfolk copy() {
        return new WishfulMerfolk(this);
    }
}

class WishfulMerfolkEffect extends ContinuousEffectImpl {

    WishfulMerfolkEffect() {
        super(Duration.EndOfTurn, Outcome.AddAbility);
        staticText = "{this} loses defender and becomes a Human until end of turn";
    }

    private WishfulMerfolkEffect(final WishfulMerfolkEffect effect) {
        super(effect);
    }

    @Override
    public WishfulMerfolkEffect copy() {
        return new WishfulMerfolkEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            switch (layer) {
                case AbilityAddingRemovingEffects_6:
                    permanent.removeAbility(DefenderAbility.getInstance(), source.getSourceId(), game);
                    break;
                case TypeChangingEffects_4:
                    permanent.removeAllCreatureTypes(game);
                    permanent.addSubType(game, SubType.HUMAN);
                    break;
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = game.getPermanent(source.getSourceId());
        if (permanent == null) {
            return false;
        }
        affectedObjects.add(permanent);
        return true;
    }

    @Override
    public boolean hasLayer(Layer layer) {
        return layer == Layer.AbilityAddingRemovingEffects_6
                || layer == Layer.TypeChangingEffects_4;
    }
}
