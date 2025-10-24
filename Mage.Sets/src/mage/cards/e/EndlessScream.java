
package mage.cards.e;

import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.CountersSourceCount;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.EnchantAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.target.TargetPermanent;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 *
 * @author LoneFox
 */
public final class EndlessScream extends CardImpl {

    public EndlessScream(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ENCHANTMENT},"{X}{B}");
        this.subtype.add(SubType.AURA);

        // Enchant creature
        TargetPermanent auraTarget = new TargetCreaturePermanent();
        this.getSpellAbility().addTarget(auraTarget);
        this.getSpellAbility().addEffect(new AttachEffect(Outcome.BoostCreature));
        Ability ability = new EnchantAbility(auraTarget);
        this.addAbility(ability);

        // Endless Scream enters the battlefield with X scream counters on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.SCREAM.createInstance())));

        // Enchanted creature gets +1/+0 for each scream counter on Endless Scream.
        this.addAbility(new SimpleStaticAbility(new ContinuousEffectBuilder(Duration.WhileOnBattlefield, Outcome.BoostCreature, ContinuousAffected.ATTACHED_TO)
                .withAddPower(new CountersSourceCount(CounterType.SCREAM))
                .setText("Enchanted creature gets +1/+0 for each scream counter on {this}")
        ));
    }

    private EndlessScream(final EndlessScream card) {
        super(card);
    }

    @Override
    public EndlessScream copy() {
        return new EndlessScream(this);
    }
}
