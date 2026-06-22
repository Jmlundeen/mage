package mage.cards.m;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.Condition;
import mage.abilities.condition.common.SourceHasCounterCondition;
import mage.abilities.costs.common.RemoveCountersSourceCost;
import mage.abilities.dynamicvalue.common.CountersSourceCount;
import mage.abilities.dynamicvalue.common.GetXValue;
import mage.abilities.effects.common.ReturnToHandSourceEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.mana.ActivatedManaAbilityImpl;
import mage.abilities.mana.AnyColorManaAbility;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ComparisonType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 * @author LevelX2
 */
public final class ManaBloom extends CardImpl {

    private static final Condition condition = new SourceHasCounterCondition(CounterType.CHARGE, ComparisonType.EQUAL_TO, 0);

    public ManaBloom(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{X}{G}");

        // Mana Bloom enters the battlefield with X charge counters on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.CHARGE, GetXValue.instance)));

        // Remove a charge counter from Mana Bloom: Add one mana of any color. Activate this ability only once each turn.
        ActivatedManaAbilityImpl ability = new AnyColorManaAbility(
                new RemoveCountersSourceCost(CounterType.CHARGE.createInstance()), new CountersSourceCount(CounterType.CHARGE)
        );
        ability.setMaxActivationsPerTurn(1);
        this.addAbility(ability);

        // At the beginning of your upkeep, if Mana Bloom has no charge counters on it, return it to its owner's hand.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(
                new ReturnToHandSourceEffect(true).setText("return it to its owner's hand")
        ).withInterveningIf(condition));
    }

    private ManaBloom(final ManaBloom card) {
        super(card);
    }

    @Override
    public ManaBloom copy() {
        return new ManaBloom(this);
    }
}
