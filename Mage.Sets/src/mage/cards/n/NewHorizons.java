
package mage.cards.n;

import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.effects.common.continuous.generic.GenericContinuousEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.abilities.keyword.EnchantAbility;
import mage.abilities.mana.AnyColorManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.target.TargetPermanent;
import mage.target.common.TargetControlledCreaturePermanent;
import mage.target.common.TargetLandPermanent;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class NewHorizons extends CardImpl {

    public NewHorizons(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{2}{G}");

        this.subtype.add(SubType.AURA);

        // Enchant land
        TargetPermanent auraTarget = new TargetLandPermanent();
        this.getSpellAbility().addTarget(auraTarget);
        this.getSpellAbility().addEffect(new AttachEffect(Outcome.BoostCreature));
        Ability ability = new EnchantAbility(auraTarget);
        this.addAbility(ability);

        // When New Horizons enters the battlefield, put a +1/+1 counter on target creature you control.
        ability = new EntersBattlefieldTriggeredAbility(new AddCountersTargetEffect(CounterType.P1P1.createInstance()));
        ability.addTarget(new TargetControlledCreaturePermanent());
        this.addAbility(ability);

        // Enchanted land has "{T}: Add two mana of any one color."
        Ability gainedAbility = new AnyColorManaAbility(2);
        Effect effect = new GenericContinuousEffect(Duration.WhileOnBattlefield, Outcome.AddAbility)
                .setAffected(ContinuousAffected.ATTACHED_TO)
                .withGainedAbilities(gainedAbility)
                .setText("Enchanted land has \"{T}: Add two mana of any one color.\"");
        this.addAbility(new SimpleStaticAbility(effect));
    }

    private NewHorizons(final NewHorizons card) {
        super(card);
    }

    @Override
    public NewHorizons copy() {
        return new NewHorizons(this);
    }
}
