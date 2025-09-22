
package mage.cards.g;

import mage.abilities.common.AttacksAllTriggeredAbility;
import mage.abilities.effects.common.continuous.LoseAbilityTargetEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.mageobject.AbilityPredicate;

import java.util.UUID;

/**
 *
 * @author jeffwadsworth
 */
public final class GravityWell extends CardImpl {

    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent("creature with flying");

    static {
        filter.add(new AbilityPredicate(FlyingAbility.class));
    }

    public GravityWell(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ENCHANTMENT},"{1}{G}{G}");

        // Whenever a creature with flying attacks, it loses flying until end of turn.
        this.addAbility(new AttacksAllTriggeredAbility(
                new LoseAbilityTargetEffect(FlyingAbility.getInstance(), Duration.EndOfTurn),
                false, filter, SetTargetPointer.PERMANENT, false
        ));
    }

    private GravityWell(final GravityWell card) {
        super(card);
    }

    @Override
    public GravityWell copy() {
        return new GravityWell(this);
    }
}
