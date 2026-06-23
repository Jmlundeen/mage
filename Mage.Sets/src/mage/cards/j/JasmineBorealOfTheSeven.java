package mage.cards.j;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.combat.CantBeBlockedByCreaturesAllEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterTyped;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.NoAbilityPredicate;
import mage.filter.predicate.typed.Spell.SpellPredicate;

import java.util.UUID;

public final class JasmineBorealOfTheSeven extends CardImpl {

    private static final FilterTyped spell_filter = new FilterTyped("creature spells with no abilities")
            .addAll(SpellPredicate.instance,
                    mage.filter.predicate.typed.mageObject.ability.NoAbilityPredicate.instance,
                    CardType.CREATURE.getPredicate()
            );
    private static final FilterCreaturePermanent your_creatures_filter
            = new FilterCreaturePermanent("creatures you control with no abilities");
    private static final FilterCreaturePermanent with_abilities_filter
            = new FilterCreaturePermanent("creatures with abilities");

    static {
        your_creatures_filter.add(NoAbilityPredicate.instance);
        your_creatures_filter.add(TargetController.YOU.getControllerPredicate());
        with_abilities_filter.add(Predicates.not(NoAbilityPredicate.instance));
    }

    public JasmineBorealOfTheSeven(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.DRUID);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // {T}: Add {G}{W}. Spend this mana only to cast creature spells with no abilities.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(1, 0, 0, 0, 1, 0, 0)
                .condition(new FilteredSpellManaCondition(spell_filter))
                .ruleText("Add {G}{W}. Spend this mana only to cast creature spells with no abilities")
                .build()
        );

        // Creatures you control with no abilities can’t be blocked by creatures with abilities.
        this.addAbility(new SimpleStaticAbility(new CantBeBlockedByCreaturesAllEffect(
                your_creatures_filter, with_abilities_filter, Duration.WhileOnBattlefield
        )));
    }

    private JasmineBorealOfTheSeven(final JasmineBorealOfTheSeven card) {
        super(card);
    }

    @Override
    public JasmineBorealOfTheSeven copy() {
        return new JasmineBorealOfTheSeven(this);
    }
}