package mage.cards.t;

import mage.abilities.Ability;
import mage.abilities.common.ActivateIfConditionActivatedAbility;
import mage.abilities.condition.common.CorruptedCondition;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.continuous.BoostTargetEffect;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterTyped;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.mageobject.PowerPredicate;
import mage.filter.predicate.mageobject.ToughnessPredicate;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 * @author AhmadYProjects
 */
public final class TheSeedcore extends CardImpl {

    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent("1/1 creature");
    private static final FilterTyped manaFilter = new FilterTyped("Phyrexian creature spell")
            .addAll(
                    IMageObjectPredicate.getOSPPredicate(SubType.PHYREXIAN.getPredicate()),
                    IMageObjectPredicate.getOSPPredicate(CardType.CREATURE.getPredicate())
            );

    static {
        filter.add(new PowerPredicate(ComparisonType.EQUAL_TO, 1));
        filter.add(new ToughnessPredicate(ComparisonType.EQUAL_TO, 1));
    }

    public TheSeedcore(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        this.subtype.add(SubType.SPHERE);

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T}: Add one mana of any color. Spend this mana only to cast Phyrexian creature spells.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addAnyColor(1)
                .condition(new FilteredSpellManaCondition(manaFilter))
                .ruleText("Add one mana of any color. Spend this mana only to cast Phyrexian creature spells")
                .build()
        );

        // Corrupted -- {T}: Target 1/1 creature gets +2/+1 until end of turn. Activate only if an opponent has three or more poison counters.
        Ability ability = new ActivateIfConditionActivatedAbility(
                new BoostTargetEffect(2, 1, Duration.EndOfTurn),
                new TapSourceCost(), CorruptedCondition.instance
        ).setAbilityWord(AbilityWord.CORRUPTED).addHint(CorruptedCondition.getHint());
        ability.addTarget(new TargetPermanent(filter));
        this.addAbility(ability);
    }

    private TheSeedcore(final TheSeedcore card) {
        super(card);
    }

    @Override
    public TheSeedcore copy() {
        return new TheSeedcore(this);
    }
}
