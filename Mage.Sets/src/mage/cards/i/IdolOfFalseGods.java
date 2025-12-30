package mage.cards.i;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.DiesCreatureTriggeredAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.keyword.AnnihilatorAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.predicate.mageobject.AnotherPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.EldraziSpawnToken;

import java.util.List;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class IdolOfFalseGods extends CardImpl {

    private static final FilterPermanent filter
            = new FilterControlledPermanent(SubType.ELDRAZI, "another Eldrazi you control");

    static {
        filter.add(AnotherPredicate.instance);
    }

    public IdolOfFalseGods(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.KINDRED, CardType.ARTIFACT}, "{2}");

        this.subtype.add(SubType.ELDRAZI);

        // {1}{C}, {T}: Create a 0/1 colorless Eldrazi Spawn creature token with "Sacrifice this creature: Add {C}."
        Ability ability = new SimpleActivatedAbility(
                new CreateTokenEffect(new EldraziSpawnToken()), new ManaCostsImpl<>("{1}{C}")
        );
        ability.addCost(new TapSourceCost());
        this.addAbility(ability);

        // Whenever another Eldrazi you control dies, put a +1/+1 counter on Idol of False Gods.
        this.addAbility(new DiesCreatureTriggeredAbility(
                new AddCountersSourceEffect(CounterType.P1P1.createInstance()), false, filter
        ));

        // As long as Idol of False Gods has eight or more +1/+1 counters on it, it's a 0/0 creature in addition to its other types and it has annihilator 2.
        this.addAbility(new SimpleStaticAbility(new IdolOfFalseGodsEffect()));
    }

    private IdolOfFalseGods(final IdolOfFalseGods card) {
        super(card);
    }

    @Override
    public IdolOfFalseGods copy() {
        return new IdolOfFalseGods(this);
    }
}

class IdolOfFalseGodsEffect extends ContinuousEffectImpl {

    IdolOfFalseGodsEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Benefit);
        staticText = "as long as {this} has eight or more +1/+1 counters on it, " +
                "it's a 0/0 creature in addition to its other types and it has annihilator 2";
    }

    private IdolOfFalseGodsEffect(final IdolOfFalseGodsEffect effect) {
        super(effect);
    }

    @Override
    public IdolOfFalseGodsEffect copy() {
        return new IdolOfFalseGodsEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            switch (layer) {
                case TypeChangingEffects_4:
                    permanent.addCardType(game, CardType.CREATURE);
                    break;
                case AbilityAddingRemovingEffects_6:
                    permanent.addAbility(new AnnihilatorAbility(2), source.getSourceId(), game);
                    break;
                case PTChangingEffects_7:
                    if (sublayer == SubLayer.SetPT_7b) {
                        permanent.getPower().setModifiedBaseValue(0);
                        permanent.getToughness().setModifiedBaseValue(0);
                        break;
                    }
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (permanent == null || permanent.getCounters(game).getCount(CounterType.P1P1) < 8) {
            return false;
        }
        if (!source.getAffectedObjects().isEmpty()) {
            affectedObjects.addAll(source.getAffectedObjects());
        } else {
            affectedObjects.add(permanent);
            source.getAffectedObjects().add(permanent);
        }
        return true;
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
