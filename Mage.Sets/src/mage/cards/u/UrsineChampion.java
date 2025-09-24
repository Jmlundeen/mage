package mage.cards.u;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.LimitedTimesPerTurnActivatedAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.continuous.BoostSourceEffect;
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
public final class UrsineChampion extends CardImpl {

    public UrsineChampion(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.BERSERKER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // {5}{G}: Ursine Champion gets +3/+3 and becomes a Bear Berserker until end of turn. Activate this ability only once each turn.
        Ability ability = new LimitedTimesPerTurnActivatedAbility(
                Zone.BATTLEFIELD,
                new BoostSourceEffect(3, 3, Duration.EndOfTurn)
                        .setText("{this} gets +3/+3"),
                new ManaCostsImpl<>("{5}{G}")
        );
        ability.addEffect(new UrsineChampionEffect());
        this.addAbility(ability);
    }

    private UrsineChampion(final UrsineChampion card) {
        super(card);
    }

    @Override
    public UrsineChampion copy() {
        return new UrsineChampion(this);
    }


}

class UrsineChampionEffect extends ContinuousEffectImpl {

    UrsineChampionEffect() {
        super(Duration.EndOfTurn, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.BecomeCreature);
        staticText = "and becomes a Bear Berserker until end of turn";
    }

    private UrsineChampionEffect(final UrsineChampionEffect effect) {
        super(effect);
    }

    @Override
    public UrsineChampionEffect copy() {
        return new UrsineChampionEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            permanent.removeAllCreatureTypes(game);
            permanent.addSubType(game, SubType.BEAR, SubType.BERSERKER);
        }
    }

    @Override
    public boolean queryAffectedObjects(Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = game.getPermanent(source.getSourceId());
        if (permanent != null) {
            affectedObjects.add(permanent);
            return true;
        } else {
            return false;
        }
    }
}
