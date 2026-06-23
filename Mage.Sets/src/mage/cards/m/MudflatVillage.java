package mage.cards.m;

import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.SacrificeSourceCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ReturnFromGraveyardToHandTargetEffect;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterCard;
import mage.filter.StaticTypedFilters;
import mage.filter.predicate.Predicates;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class MudflatVillage extends CardImpl {

    private static final FilterCard filter = new FilterCard("Bat, Lizard, Rat, or Squirrel card from your graveyard");

    static {
        filter.add(Predicates.or(
                SubType.BAT.getPredicate(),
                SubType.LIZARD.getPredicate(),
                SubType.RAT.getPredicate(),
                SubType.SQUIRREL.getPredicate()
        ));
    }

    public MudflatVillage(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T}: Add {B}. Spend this mana only to cast a creature spell.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(Mana.BlackMana(1))
                .condition(new FilteredSpellManaCondition(StaticTypedFilters.A_CREATURE_SPELL))
                .ruleText("Add {B}. Spend this mana only to cast a creature spell")
                .build()
        );
        // {1}{B}, {T}, Sacrifice Mudflat Village: Return target Bat, Lizard, Rat, or Squirrel card from your graveyard to your hand.
        Ability ability = new SimpleActivatedAbility(new ReturnFromGraveyardToHandTargetEffect(), new ManaCostsImpl<>("{1}{B}"));
        ability.addCost(new TapSourceCost());
        ability.addCost(new SacrificeSourceCost());
        ability.addTarget(new TargetCardInYourGraveyard(filter));
        this.addAbility(ability);
    }

    private MudflatVillage(final MudflatVillage card) {
        super(card);
    }

    @Override
    public MudflatVillage copy() {
        return new MudflatVillage(this);
    }
}
