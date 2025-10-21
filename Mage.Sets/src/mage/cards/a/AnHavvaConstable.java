
package mage.cards.a;

import mage.MageInt;
import mage.ObjectColor;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.IntPlusDynamicValue;
import mage.abilities.dynamicvalue.common.PermanentsOnBattlefieldCount;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.mageobject.ColorPredicate;

import java.util.UUID;

/**
 *
 * @author fireshoes
 */
public final class AnHavvaConstable extends CardImpl {

    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent("the number of green creatures on the battlefield");
    private static final DynamicValue creatureValue = new IntPlusDynamicValue(1, new PermanentsOnBattlefieldCount(filter));

    static {
        filter.add(new ColorPredicate(ObjectColor.GREEN));
    }

    public AnHavvaConstable(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{1}{G}{G}");
        this.subtype.add(SubType.HUMAN);
        this.power = new MageInt(2);
        this.toughness = new MageInt(1);

        // An-Havva Constable's toughness is equal to 1 plus the number of green creatures on the battlefield.
        this.addAbility(new SimpleStaticAbility(Zone.ALL, new ContinuousEffectBuilder(Duration.EndOfGame, Outcome.BoostCreature, ContinuousAffected.SOURCE)
                .withSetToughness(creatureValue)
                .setText("{this}'s toughness is equal to " + creatureValue.getMessage())
        ));
    }

    private AnHavvaConstable(final AnHavvaConstable card) {
        super(card);
    }

    @Override
    public AnHavvaConstable copy() {
        return new AnHavvaConstable(this);
    }
}
