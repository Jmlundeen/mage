package mage.cards.m;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.DealsDamageToACreatureTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;
import java.util.UUID;

/**
 * @author jeffwadsworth
 */
public final class MephidrossVampire extends CardImpl {

    public MephidrossVampire(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{B}{B}");
        this.subtype.add(SubType.VAMPIRE);

        this.power = new MageInt(3);
        this.toughness = new MageInt(4);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Each creature you control is a Vampire in addition to its other creature types and has "Whenever this creature deals damage to a creature, put a +1/+1 counter on this creature."
        this.addAbility(new SimpleStaticAbility(new MephidrossVampireEffect()));

    }

    private MephidrossVampire(final MephidrossVampire card) {
        super(card);
    }

    @Override
    public MephidrossVampire copy() {
        return new MephidrossVampire(this);
    }
}

class MephidrossVampireEffect extends ContinuousEffectImpl {

    MephidrossVampireEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Detriment);
        this.staticText = "Each creature you control is a Vampire in addition to its other creature types and has \"Whenever this creature deals damage to a creature, put a +1/+1 counter on this creature.\"";
    }

    private MephidrossVampireEffect(final MephidrossVampireEffect effect) {
        super(effect);
    }

    @Override
    public MephidrossVampireEffect copy() {
        return new MephidrossVampireEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent creature = (Permanent) object;
            switch (layer) {
                case AbilityAddingRemovingEffects_6:
                    creature.addAbility(new DealsDamageToACreatureTriggeredAbility(new AddCountersSourceEffect(CounterType.P1P1.createInstance()), false, false, false), source.getSourceId(), game);
                    break;
                case TypeChangingEffects_4:
                    creature.addSubType(game, SubType.VAMPIRE);
                    break;
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        if (!source.getAffectedObjects().isEmpty()) {
            affectedObjects.addAll(source.getAffectedObjects());
        } else {
            for (Permanent creature : game.getBattlefield().getActivePermanents(StaticFilters.FILTER_CONTROLLED_CREATURES, source.getControllerId(), game)) {
                affectedObjects.add(creature);
                source.getAffectedObjects().add(creature);
            }
        }
        return !affectedObjects.isEmpty();
    }

    @Override
    public boolean hasLayer(Layer layer) {
        return layer == Layer.AbilityAddingRemovingEffects_6 || layer == Layer.TypeChangingEffects_4;
    }
}
