package mage.cards.s;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterPlaneswalkerPermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class SparkRupture extends CardImpl {

    public SparkRupture(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{2}{W}");

        // When Spark Rupture enters the battlefield, draw a card.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new DrawCardSourceControllerEffect(1)));

        // Each planeswalker with one or more loyalty counters on it loses all abilities and is a creature with power and toughness each equal to the number of loyalty counters on it.
        this.addAbility(new SimpleStaticAbility(new SparkRuptureEffect()));
    }

    private SparkRupture(final SparkRupture card) {
        super(card);
    }

    @Override
    public SparkRupture copy() {
        return new SparkRupture(this);
    }
}

class SparkRuptureEffect extends ContinuousEffectImpl {

    private static final FilterPermanent filter = new FilterPlaneswalkerPermanent();

    static {
        filter.add(CounterType.LOYALTY.getPredicate());
    }

    SparkRuptureEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Detriment);
        staticText = "each planeswalker with one or more loyalty counters on it loses all abilities " +
                "and is a creature with power and toughness each equal to the number of loyalty counters on it";
    }

    private SparkRuptureEffect(final SparkRuptureEffect effect) {
        super(effect);
    }

    @Override
    public SparkRuptureEffect copy() {
        return new SparkRuptureEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            switch (layer) {
                case TypeChangingEffects_4:
                    permanent.removeAllCardTypes(game);
                    permanent.addCardType(game, CardType.CREATURE);
                    break;
                case AbilityAddingRemovingEffects_6:
                    permanent.removeAllAbilities(source.getSourceId(), game);
                    break;
                case PTChangingEffects_7:
                    if (sublayer != SubLayer.SetPT_7b) {
                        break;
                    }
                    int counters = permanent.getCounters(game).getCount(CounterType.LOYALTY);
                    permanent.getPower().setModifiedBaseValue(counters);
                    permanent.getToughness().setModifiedBaseValue(counters);
                    break;
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        if (!source.getAffectedObjects().isEmpty()) {
            affectedObjects.addAll(source.getAffectedObjects());
        } else {
            for (Permanent permanent : game.getBattlefield().getActivePermanents(
                    filter, source.getControllerId(), source, game
            )) {
                affectedObjects.add(permanent);
                source.getAffectedObjects().add(permanent);
            }
        }
        return !affectedObjects.isEmpty();
    }

    @Override
    public boolean hasLayer(Layer layer) {
        switch (layer) {
            case AbilityAddingRemovingEffects_6:
            case TypeChangingEffects_4:
            case PTChangingEffects_7:
                return true;
        }
        return false;
    }

    @Override
    public boolean hasSubLayer(SubLayer sublayer) {
        return sublayer == SubLayer.NA || sublayer == SubLayer.SetPT_7b;
    }
}
