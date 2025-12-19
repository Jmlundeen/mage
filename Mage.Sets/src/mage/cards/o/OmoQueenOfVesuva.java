package mage.cards.o;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldOrAttacksSourceTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.abilities.mana.*;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterLandPermanent;
import mage.filter.common.FilterNonlandPermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCreaturePermanent;
import mage.target.common.TargetLandPermanent;
import mage.target.targetpointer.EachTargetPointer;

import java.util.List;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class OmoQueenOfVesuva extends CardImpl {

    private static final FilterPermanent landFilter = new FilterLandPermanent();
    private static final FilterPermanent creatureFilter = new FilterNonlandPermanent();

    static {
        landFilter.add(CounterType.EVERYTHING.getPredicate());
        creatureFilter.add(CardType.CREATURE.getPredicate());
        creatureFilter.add(CounterType.EVERYTHING.getPredicate());
    }

    public OmoQueenOfVesuva(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G/U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.SHAPESHIFTER);
        this.subtype.add(SubType.NOBLE);
        this.power = new MageInt(1);
        this.toughness = new MageInt(5);

        // Whenever Omo, Queen of Vesuva enters the battlefield or attacks, put an everything counter on each of up to one target land and up to one target creature.
        Ability ability = new EntersBattlefieldOrAttacksSourceTriggeredAbility(
                new AddCountersTargetEffect(CounterType.EVERYTHING.createInstance())
                        .setTargetPointer(new EachTargetPointer())
                        .setText("put an everything counter on each of up to one target land and up to one target creature")
        );
        ability.addTarget(new TargetLandPermanent(0, 1));
        ability.addTarget(new TargetCreaturePermanent(0, 1));
        this.addAbility(ability);

        // Each land with an everything counter on it is every land type in addition to its other types.
        this.addAbility(new SimpleStaticAbility(new ContinuousEffectBuilder(Outcome.Detriment, TargetController.EACH_PLAYER, landFilter)
                .withAddedSubTypes(false, SubType.PLAINS, SubType.ISLAND, SubType.SWAMP, SubType.MOUNTAIN, SubType.FOREST)
                .withIsEveryLandType()
                .setText("each land with an everything counter on it is every land type in addition to its other types")
        ));

        // Each nonland creature with an everything counter on it is every creature type.
        this.addAbility(new SimpleStaticAbility(new ContinuousEffectBuilder(Outcome.Detriment, TargetController.EACH_PLAYER, creatureFilter)
                .withIsEveryCreatureType()
                .setText("each nonland creature with an everything counter on it is every creature type")
        ));
    }

    private OmoQueenOfVesuva(final OmoQueenOfVesuva card) {
        super(card);
    }

    @Override
    public OmoQueenOfVesuva copy() {
        return new OmoQueenOfVesuva(this);
    }
}
