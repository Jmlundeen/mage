package mage.cards.f;

import mage.MageInt;
import mage.Mana;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.MyTurnCondition;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.effects.common.continuous.GainAbilitySourceEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredAbilityManaCondition;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.Filter;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.ability.type.EquipAbilityPredicate;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;

import java.util.UUID;

/**
 * @author Susucr
 */
public final class FreyaCrescent extends CardImpl {

    static final FilterTyped spellFilter = new FilterTyped("Equipment spell")
            .add(IMageObjectPredicate.getOSPPredicate(SubType.EQUIPMENT.getPredicate()));
    static final FilterTyped equipAbilityFilter = new FilterTyped("equip ability")
            .add(EquipAbilityPredicate.instance);

    public FreyaCrescent(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.RAT);
        this.subtype.add(SubType.KNIGHT);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // Jump -- During your turn, Freya Crescent has flying.
        this.addAbility(new SimpleStaticAbility(new ConditionalContinuousEffect(
                new GainAbilitySourceEffect(FlyingAbility.getInstance()),
                MyTurnCondition.instance, "during your turn, {this} has flying"
        )).withFlavorWord("Jump"));

        // {T}: Add {R}. Spend this mana only to cast an Equipment spell or activate an equip ability.
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addStatic(Mana.RedMana(1))
                .condition(new FilteredSpellManaCondition(spellFilter))
                .condition(new FilteredAbilityManaCondition(equipAbilityFilter))
                .comparisonScope(Filter.ComparisonScope.Any)
                .ruleText("Add {R}. Spend this mana only to cast an Equipment spell or activate an equip ability")
                .build()
        );
    }

    private FreyaCrescent(final FreyaCrescent card) {
        super(card);
    }

    @Override
    public FreyaCrescent copy() {
        return new FreyaCrescent(this);
    }
}
