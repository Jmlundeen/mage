package mage.cards.l;

import mage.MageInt;
import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.PayLifeCost;
import mage.abilities.costs.common.SacrificeTargetCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.MillCardsTargetEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.TrampleAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterTyped;
import mage.filter.StaticFilters;
import mage.filter.predicate.typed.Spell.SpellCastFromZonePredicate;
import mage.filter.predicate.typed.Spell.SpellPredicate;
import mage.target.TargetPlayer;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class LordOfTheForsaken extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("a spell from your graveyard")
            .addAll(
                    SpellPredicate.instance,
                    SpellCastFromZonePredicate.GRAVEYARD
            );

    public LordOfTheForsaken(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{B}{B}");

        this.subtype.add(SubType.DEMON);
        this.power = new MageInt(6);
        this.toughness = new MageInt(6);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Trample
        this.addAbility(TrampleAbility.getInstance());

        // {B}, Sacrifice another creature: Target player mills three cards.
        Ability ability = new SimpleActivatedAbility(
                new MillCardsTargetEffect(3), new ManaCostsImpl<>("{B}")
        );
        ability.addCost(new SacrificeTargetCost(StaticFilters.FILTER_CONTROLLED_ANOTHER_CREATURE));
        ability.addTarget(new TargetPlayer());
        this.addAbility(ability);

        // Pay 1 life: Add {C}. Spend this mana only to cast a spell from your graveyard.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new PayLifeCost(1))
                .addStatic(Mana.ColorlessMana(1))
                .condition(new FilteredSpellManaCondition(filter))
                .ruleText("Add {C}. Spend this mana only to cast a spell from your graveyard")
                .build()
        );
    }

    private LordOfTheForsaken(final LordOfTheForsaken card) {
        super(card);
    }

    @Override
    public LordOfTheForsaken copy() {
        return new LordOfTheForsaken(this);
    }
}
