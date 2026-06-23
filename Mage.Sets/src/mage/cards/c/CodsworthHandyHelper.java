package mage.cards.c;

import mage.MageInt;
import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.common.ActivateAsSorceryActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.AttachTargetToTargetEffect;
import mage.abilities.effects.common.continuous.generic.GenericContinuousEffect;
import mage.abilities.keyword.WardAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.FilterTyped;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.typed.LogicalPredicate;
import mage.filter.predicate.typed.Spell.SpellPredicate;
import mage.target.TargetPermanent;
import mage.target.common.TargetControlledCreaturePermanent;

import java.util.UUID;

/**
 * @author PurpleCrowbar
 */
public final class CodsworthHandyHelper extends CardImpl {

    private static final FilterTyped commanderFilter = new FilterTyped("commander you control")
            .add(mage.filter.predicate.typed.mageObject.object.CommanderPredicate.instance);
    private static final FilterControlledPermanent filter2 = new FilterControlledPermanent("Aura or Equipment you control");
    private static final FilterTyped auraOrEquipmentFilter = new FilterTyped("Aura or Equipment spell")
            .addAll(SpellPredicate.instance,
                    LogicalPredicate.or(
                    SubType.AURA.getPredicate(),
                    SubType.EQUIPMENT.getPredicate()
            ));

    static {
        filter2.add(Predicates.or(SubType.AURA.getPredicate(), SubType.EQUIPMENT.getPredicate()));
    }

    public CodsworthHandyHelper(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{2}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ROBOT);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // Commanders you control have ward {2}.
        this.addAbility(new SimpleStaticAbility(new GenericContinuousEffect(Outcome.AddAbility, commanderFilter)
                .withGainedAbilities(new WardAbility(new GenericManaCost(2)))
                .setText("Commanders you control have ward {2}")
        ));

        // {T}: Add {W}{W}. Spend this mana only to cast Aura and/or Equipment spells.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(Mana.WhiteMana(2))
                .condition(new FilteredSpellManaCondition(auraOrEquipmentFilter))
                .ruleText("Add {W}{W}. Spend this mana only to cast Aura and/or Equipment spells")
                .build()
        );

        // {T}: Attach target Aura or Equipment you control to target creature you control. Activate only as a sorcery.
        Ability ability = new ActivateAsSorceryActivatedAbility(new AttachTargetToTargetEffect(), new TapSourceCost());
        ability.addTarget(new TargetPermanent(filter2));
        ability.addTarget(new TargetControlledCreaturePermanent());
        this.addAbility(ability);
    }

    private CodsworthHandyHelper(final CodsworthHandyHelper card) {
        super(card);
    }

    @Override
    public CodsworthHandyHelper copy() {
        return new CodsworthHandyHelper(this);
    }
}
