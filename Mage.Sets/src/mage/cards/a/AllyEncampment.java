package mage.cards.a;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.SacrificeSourceCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.ReturnToHandTargetEffect;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterTyped;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.predicate.typed.Spell.SpellPredicate;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;
import mage.target.common.TargetControlledPermanent;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class AllyEncampment extends CardImpl {

    private static final FilterTyped filterSpell = new FilterTyped("an Ally spell")
            .addAll(SpellPredicate.instance, IMageObjectPredicate.getOSPPredicate(SubType.ALLY.getPredicate()));
    private static final FilterControlledPermanent filterPermanent = new FilterControlledPermanent(SubType.ALLY, "Ally you control");

    public AllyEncampment(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.LAND},"");

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T} Add one mana of any color. Spend this mana only to cast an Ally spell.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addAnyColor(1)
                .condition(new FilteredSpellManaCondition(filterSpell))
                .ruleText("Add one mana of any color. Spend this mana only to cast an Ally spell.")
                .build()
        );

        // {1}, {T}, Sacrifice Ally Encampment: Return target Ally you control to its owner's hand.
        Ability ability = new SimpleActivatedAbility(new ReturnToHandTargetEffect(), new GenericManaCost(1));
        ability.addCost(new TapSourceCost());
        ability.addCost(new SacrificeSourceCost());
        ability.addTarget(new TargetControlledPermanent(filterPermanent));
        this.addAbility(ability);
    }

    private AllyEncampment(final AllyEncampment card) {
        super(card);
    }

    @Override
    public AllyEncampment copy() {
        return new AllyEncampment(this);
    }
}
