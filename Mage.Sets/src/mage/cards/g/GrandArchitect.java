package mage.cards.g;

import mage.MageInt;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapTargetCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.continuous.generic.GenericContinuousEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.SpendOrActivateManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterTyped;
import mage.filter.StaticFilters;
import mage.filter.StaticTypedFilters;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.predicate.mageobject.ColorPredicate;
import mage.filter.predicate.permanent.TappedPredicate;
import mage.target.TargetPermanent;

import java.util.Set;
import java.util.UUID;

/**
 * @author BetaSteward_at_googlemail.com, nantuko
 */
public final class GrandArchitect extends CardImpl {

    private static final FilterTyped boostFilter = new FilterTyped("blue creatures")
            .addAll(CardType.CREATURE.getPredicate(),
                    mage.filter.predicate.typed.mageObject.color.ColorPredicate.BLUE
            );
    private static final FilterControlledPermanent tapFilter = new FilterControlledCreaturePermanent("untapped blue creature you control");

    static {
        tapFilter.add(new ColorPredicate(ObjectColor.BLUE));
        tapFilter.add(TappedPredicate.UNTAPPED);
    }

    public GrandArchitect(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}{U}");
        this.subtype.add(SubType.VEDALKEN);
        this.subtype.add(SubType.ARTIFICER);

        this.power = new MageInt(1);
        this.toughness = new MageInt(3);

        // Other blue creatures you control get +1/+1.
        this.addAbility(new SimpleStaticAbility(new GenericContinuousEffect(Duration.WhileOnBattlefield, Outcome.BoostCreature, boostFilter, Zone.BATTLEFIELD)
                .setText("Other blue creatures you control get +1/+1")
                .withAddPower(1)
                .withAddToughness(1)
        ));

        // {U}: Target artifact creature becomes blue until end of turn.
        Ability ability = new SimpleActivatedAbility(new GenericContinuousEffect(Duration.EndOfTurn, Outcome.Detriment)
                .withAddedColor(true, ObjectColor.BLUE)
                .setText("target artifact becomes blue until end of turn"), new ManaCostsImpl<>("{U}"));
        ability.addTarget(new TargetPermanent(StaticFilters.FILTER_PERMANENT_ARTIFACT_CREATURE));
        this.addAbility(ability);

        // Tap an untapped blue creature you control: Add {C}{C}. Spend this mana only to cast artifact spells or activate abilities of artifacts.
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new TapTargetCost(tapFilter))
                .addStatic(Set.of(ManaType.COLORLESS), 2)
                .condition(new SpendOrActivateManaCondition(StaticTypedFilters.AN_ARTIFACT))
                .ruleText("Add {C}{C}. Spend this mana only to cast artifact spells or activate abilities of artifacts")
                .build()
        );
    }

    private GrandArchitect(final GrandArchitect card) {
        super(card);
    }

    @Override
    public GrandArchitect copy() {
        return new GrandArchitect(this);
    }
}
