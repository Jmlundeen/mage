package mage.cards.a;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.dynamicvalue.common.CountersSourceCount;
import mage.abilities.dynamicvalue.common.GetXValue;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ManaType;
import mage.counters.CounterType;

import java.util.EnumSet;
import java.util.UUID;

/**
 * @author LevelX2
 */
public final class AstralCornucopia extends CardImpl {

    public AstralCornucopia(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{X}{X}{X}");

        // Astral Cornucopia enters the battlefield with X charge counters on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.CHARGE, GetXValue.instance)));

        // {T}: Choose a color. Add one mana of that color for each charge counter on Astral Cornucopia.
        this.addAbility(new ComposedManaAbilityBuilder()
                .addDynamicChoice(new CountersSourceCount(CounterType.CHARGE), EnumSet.of(
                        ManaType.WHITE, ManaType.BLUE, ManaType.BLACK, ManaType.RED, ManaType.GREEN
                ))
                .cost(new TapSourceCost())
                .ruleText("Choose a color. Add one mana of that color for each charge counter on {this}")
                .build());
    }

    private AstralCornucopia(final AstralCornucopia card) {
        super(card);
    }

    @Override
    public AstralCornucopia copy() {
        return new AstralCornucopia(this);
    }
}
