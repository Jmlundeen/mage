
package mage.cards.t;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.PermanentsOnBattlefieldCount;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.mageobject.AnotherPredicate;
import mage.filter.predicate.mageobject.NamePredicate;

import java.util.UUID;

/**
 *
 * @author jeffwadsworth
 */
public final class TimberpackWolf extends CardImpl {

    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent("other creature you control named Timberpack Wolf");

    static {
        filter.add(new NamePredicate("Timberpack Wolf"));
        filter.add(TargetController.YOU.getControllerPredicate());
        filter.add(AnotherPredicate.instance);
    }

    private static final DynamicValue xValue = new PermanentsOnBattlefieldCount(filter);
    public TimberpackWolf(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{1}{G}");
        this.subtype.add(SubType.WOLF);

        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Timberpack Wolf gets +1/+1 for each other creature you control named Timberpack Wolf.
        this.addAbility(new SimpleStaticAbility(new ContinuousEffectBuilder(Duration.WhileOnBattlefield, Outcome.BoostCreature, ContinuousAffected.SOURCE)
                .withAddPower(xValue)
                .withAddToughness(xValue)
                .setText("{this} gets +1/+1 for each other creature you control named Timberpack Wolf")
        ));
    }

    private TimberpackWolf(final TimberpackWolf card) {
        super(card);
    }

    @Override
    public TimberpackWolf copy() {
        return new TimberpackWolf(this);
    }
}
