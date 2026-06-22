package mage.cards.s;

import mage.abilities.Ability;
import mage.abilities.common.ActivateIfConditionActivatedAbility;
import mage.abilities.condition.Condition;
import mage.abilities.condition.common.PermanentsOnTheBattlefieldCondition;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.CreateTokenEffect;
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
import mage.game.permanent.token.SliverToken;

import java.util.UUID;

/**
 * @author emerald000
 */
public final class SliverHive extends CardImpl {

    private static final FilterTyped filterSpell = new FilterTyped("a Sliver spell")
            .addAll(
                    SpellPredicate.instance,
                    SubType.SLIVER.getPredicate()
            );

    private static final Condition condition = new PermanentsOnTheBattlefieldCondition(
            new FilterControlledPermanent(SubType.SLIVER, "you control a Sliver")
    );

    public SliverHive(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T}: Add one mana of any color. Spend this mana only to cast a Sliver spell.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addAnyColor(1)
                .condition(new FilteredSpellManaCondition(filterSpell))
                .ruleText("Add one mana of any color. Spend this mana only to cast a Sliver spell")
                .build()
        );

        // {5}, {T}: Create a 1/1 colorless Sliver creature token. Activate this ability only if you control a Sliver.
        Ability ability = new ActivateIfConditionActivatedAbility(
                new CreateTokenEffect(new SliverToken()), new GenericManaCost(5), condition
        );
        ability.addCost(new TapSourceCost());
        this.addAbility(ability);
    }

    private SliverHive(final SliverHive card) {
        super(card);
    }

    @Override
    public SliverHive copy() {
        return new SliverHive(this);
    }
}
