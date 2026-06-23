package mage.cards.b;

import mage.MageInt;
import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.continuous.generic.GenericContinuousEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.abilities.mana.conditional.InvertedManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterSpell;
import mage.filter.StaticTypedFilters;
import mage.filter.predicate.mageobject.ManaValuePredicate;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class BatteryBearer extends CardImpl {

    private static final FilterSpell filter
            = new FilterSpell("an artifact spell with mana value 6 or greater");

    static {
        filter.add(CardType.ARTIFACT.getPredicate());
        filter.add(new ManaValuePredicate(ComparisonType.MORE_THAN, 5));
    }

    public BatteryBearer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}{U}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ARTIFICER);
        this.power = new MageInt(3);
        this.toughness = new MageInt(4);

        // Creatures you control have "{T}: Add {C}. This mana can't be spent to cast a nonartifact spell."
        Ability manaAbility = ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(Mana.ColorlessMana(1))
                .condition(new InvertedManaCondition(new FilteredSpellManaCondition(StaticTypedFilters.A_NON_ARTIFACT_SPELL)))
                .ruleText("Add {C}. This mana can't be spent to cast a nonartifact spell.")
                .build();
        this.addAbility(new SimpleStaticAbility(new GenericContinuousEffect(Duration.WhileOnBattlefield, Outcome.AddAbility, StaticTypedFilters.CREATURE_YOU_CONTROL, Zone.BATTLEFIELD)
                .withGainedAbilities(manaAbility)
                .setText("Creatures you control have \"{T}: Add {C}. This mana can't be spent to cast a nonartifact spell.\"")
        ));

        // Whenever you cast an artifact spell with mana value 6 or greater, draw a card.
        this.addAbility(new SpellCastControllerTriggeredAbility(
                new DrawCardSourceControllerEffect(1), filter, false
        ));
    }

    private BatteryBearer(final BatteryBearer card) {
        super(card);
    }

    @Override
    public BatteryBearer copy() {
        return new BatteryBearer(this);
    }
}
