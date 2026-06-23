package mage.cards.g;

import mage.MageInt;
import mage.Mana;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.PermanentsOnBattlefieldCount;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.VigilanceAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ContinuousAffected;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.filter.FilterTyped;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.mageobject.AnotherPredicate;
import mage.filter.predicate.typed.Spell.SpellPredicate;

import java.util.UUID;

/**
 * @author woshikie
 */
public final class GiadaFontOfHope extends CardImpl {
    private static final FilterTyped ANGEL_SPELL_FILTER = new FilterTyped("an Angel spell")
            .addAll(SpellPredicate.instance, SubType.ANGEL.getPredicate());
    private static final FilterPermanent angelFilter = new FilterControlledCreaturePermanent(SubType.ANGEL, "other Angel you control");
    private static final DynamicValue angelCount = new PermanentsOnBattlefieldCount(new FilterControlledCreaturePermanent(SubType.ANGEL));

    static {
        angelFilter.add(AnotherPredicate.instance);
    }

    public GiadaFontOfHope(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ANGEL);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Vigilance
        this.addAbility(VigilanceAbility.getInstance());

        // Each other Angel you control enters the battlefield with an additional +1/+1 counter on it for each Angel you already control.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(ContinuousAffected.STATIC_OR_DYNAMIC, CounterType.P1P1, angelCount)
                .setFilter(angelFilter)
                .setText("each other Angel you control enters with an additional +1/+1 counter on it for each Angel you already control")
        ));

        // {T}: Add {W}. Spend this mana only to cast an Angel spell.
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addStatic(Mana.WhiteMana(1))
                .condition(new FilteredSpellManaCondition(ANGEL_SPELL_FILTER))
                .ruleText("Add {W}. Spend this mana only to cast an Angel spell.")
                .build()
        );
    }

    private GiadaFontOfHope(final GiadaFontOfHope card) {
        super(card);
    }

    @Override
    public GiadaFontOfHope copy() {
        return new GiadaFontOfHope(this);
    }
}
