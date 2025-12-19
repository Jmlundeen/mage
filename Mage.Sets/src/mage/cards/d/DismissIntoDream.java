
package mage.cards.d;

import mage.abilities.Ability;
import mage.abilities.common.BecomesTargetSourceTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.SacrificeSourceEffect;
import mage.abilities.effects.common.continuous.CreaturesBecomeOtherTypeEffect;
import mage.abilities.effects.common.continuous.GainAbilityAllEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterCreaturePermanent;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class DismissIntoDream extends CardImpl {

    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent("Each creature your opponents control");
    static {
        filter.add(TargetController.OPPONENT.getControllerPredicate());
    }

    public DismissIntoDream(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ENCHANTMENT},"{6}{U}");

        // Each creature your opponents control is an Illusion in addition to its other types 
        // and has "When this creature becomes the target of a spell or ability, sacrifice it."
        Ability ability = new SimpleStaticAbility(new CreaturesBecomeOtherTypeEffect(filter, SubType.ILLUSION, Duration.WhileOnBattlefield));
        ability.addEffect(new GainAbilityAllEffect(
                new BecomesTargetSourceTriggeredAbility(new SacrificeSourceEffect()),
                Duration.WhileOnBattlefield, filter, "and has \"When this creature becomes the target of a spell or ability, sacrifice it.\""
        ));
        this.addAbility(ability);
    }

    private DismissIntoDream(final DismissIntoDream card) {
        super(card);
    }

    @Override
    public DismissIntoDream copy() {
        return new DismissIntoDream(this);
    }
}
