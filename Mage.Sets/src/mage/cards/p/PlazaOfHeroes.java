package mage.cards.p;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.ExileSourceCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.keyword.HexproofAbility;
import mage.abilities.keyword.IndestructibleAbility;
import mage.abilities.mana.AnyColorAmongManaAbility;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.SuperType;
import mage.constants.TargetController;
import mage.filter.FilterPermanent;
import mage.filter.FilterTyped;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.typed.Spell.SpellPredicate;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class PlazaOfHeroes extends CardImpl {

    private static final FilterPermanent legendaryCreatureFilter = new FilterCreaturePermanent("legendary creature");
    private static final FilterTyped legendaryPermFilter = new FilterTyped("legendary permanents you control")
            .add(TargetController.YOU.getControllerPredicate())
            .add(IMageObjectPredicate.getOSPPredicate(SuperType.LEGENDARY.getPredicate()));
    private static final FilterTyped spellFilter = new FilterTyped("legendary spell")
            .addAll(SpellPredicate.instance, IMageObjectPredicate.getOSPPredicate(SuperType.LEGENDARY.getPredicate()));

    static {
        legendaryCreatureFilter.add(SuperType.LEGENDARY.getPredicate());
    }

    public PlazaOfHeroes(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T}: Add one mana of any color. Spend this mana only to cast a legendary spell.
        this.addAbility(new ComposedManaAbilityBuilder()
                .addAnyColor(1)
                .condition(new FilteredSpellManaCondition(spellFilter))
                .ruleText("Add one mana of any color. Spend this mana only to cast a legendary spell.")
                .build()
        );

        // {T}: Add one mana of any color among legendary permanents you control.
        this.addAbility(AnyColorAmongManaAbility.builder(legendaryPermFilter)
                .onlyColors(true)
                .ruleText("Add one mana of any color among legendary permanents you control.")
                .build()
        );

        // {3}, {T}, Exile Plaza of Heroes: Target legendary creature gains hexproof and indestructible until end of turn.
        Ability ability = new SimpleActivatedAbility(new GainAbilityTargetEffect(
                HexproofAbility.getInstance(), Duration.EndOfTurn
        ).setText("target legendary creature gains hexproof"), new GenericManaCost(3));
        ability.addEffect(new GainAbilityTargetEffect(
                IndestructibleAbility.getInstance(), Duration.EndOfTurn
        ).setText("and indestructible until end of turn"));
        ability.addTarget(new TargetPermanent(legendaryCreatureFilter));
        ability.addCost(new TapSourceCost());
        ability.addCost(new ExileSourceCost());
        this.addAbility(ability);
    }

    private PlazaOfHeroes(final PlazaOfHeroes card) {
        super(card);
    }

    @Override
    public PlazaOfHeroes copy() {
        return new PlazaOfHeroes(this);
    }
}
