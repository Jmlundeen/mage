
package mage.cards.t;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.ExileFromGraveCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.counter.AddCountersAllEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.FilterCard;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterCreaturePermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCardInASingleGraveyard;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author emerald000
 */
public final class ThelonOfHavenwood extends CardImpl {

    private static final FilterCard filterCard = new FilterCard("a Fungus card from a graveyard");
    private static final FilterPermanent filterPermanent = new FilterPermanent("Fungus on the battlefield");
    static {
        filterCard.add(SubType.FUNGUS.getPredicate());
        filterPermanent.add(SubType.FUNGUS.getPredicate());
    }

    public ThelonOfHavenwood(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{G}{G}");
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ELF);
        this.subtype.add(SubType.DRUID);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Each Fungus creature gets +1/+1 for each spore counter on it.
        this.addAbility(new SimpleStaticAbility(new ThelonOfHavenwoodBoostEffect()));

        // {B}{G}, Exile a Fungus card from a graveyard: Put a spore counter on each Fungus on the battlefield.
        Ability ability = new SimpleActivatedAbility(new AddCountersAllEffect(CounterType.SPORE.createInstance(), filterPermanent), new ManaCostsImpl<>("{B}{G}"));
        ability.addCost(new ExileFromGraveCost(new TargetCardInASingleGraveyard(1, 1, filterCard), "exile a Fungus card from a graveyard"));
        this.addAbility(ability);
    }

    private ThelonOfHavenwood(final ThelonOfHavenwood card) {
        super(card);
    }

    @Override
    public ThelonOfHavenwood copy() {
        return new ThelonOfHavenwood(this);
    }
}

class ThelonOfHavenwoodBoostEffect extends ContinuousEffectImpl {

    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent("Fungus creature");
    static {
        filter.add(SubType.FUNGUS.getPredicate());
    }

    ThelonOfHavenwoodBoostEffect() {
        super(Duration.WhileOnBattlefield, Layer.PTChangingEffects_7, SubLayer.ModifyPT_7c, Outcome.BoostCreature);
        staticText = "Each Fungus creature gets +1/+1 for each spore counter on it";
    }

    private ThelonOfHavenwoodBoostEffect(final ThelonOfHavenwoodBoostEffect effect) {
        super(effect);
    }

    @Override
    public ThelonOfHavenwoodBoostEffect copy() {
        return new ThelonOfHavenwoodBoostEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent creature = (Permanent) object;
            int numCounters = creature.getCounters(game).getCount(CounterType.SPORE);
            creature.addPower(numCounters);
            creature.addToughness(numCounters);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (Permanent creature : game.getBattlefield().getActivePermanents(filter, source.getControllerId(), source, game)) {
            if (creature.getCounters(game).getCount(CounterType.SPORE) > 0) {
                affectedObjects.add(creature);
            }
        }
        return !affectedObjects.isEmpty();
    }
}
