package mage.cards.o;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.SacrificeSourceCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ColoredManaCost;
import mage.abilities.effects.common.LookLibraryAndPickControllerEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.SpendOrActivateManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ColoredManaSymbol;
import mage.constants.PutCards;
import mage.constants.SubType;
import mage.filter.FilterCard;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;

import java.util.UUID;

/**
 * @author weirddan455
 */
public final class OrbOfDragonkind extends CardImpl {

    private static final FilterCard filter = new FilterCard("a Dragon card");
    private static final FilterTyped manaFilter = new FilterTyped("Dragon spells or abilities of Dragons");

    static {
        filter.add(IMageObjectPredicate.getOSPPredicate(SubType.DRAGON.getPredicate()));
    }

    public OrbOfDragonkind(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{1}{R}");

        // {1}, {T}: Add two mana in any combination of colors. Spend this mana only to cast Dragon spells or to activate abilities of Dragons.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addAnyCombination(2)
                .condition(new SpendOrActivateManaCondition(manaFilter))
                .ruleText("Add two mana in any combination of colors. Spend this mana only to cast Dragon spells or to activate abilities of Dragons")
                .build()
        );

        // {R}, {T}, Sacrifice Orb of Dragonkind: Look at the top seven cards of your library.
        // You may reveal a Dragon card from among them and put it into your hand. Put the rest on the bottom of your library in a random order.
        Ability ability = new SimpleActivatedAbility(
                new LookLibraryAndPickControllerEffect(7, 1, filter, PutCards.HAND, PutCards.BOTTOM_RANDOM),
                new ColoredManaCost(ColoredManaSymbol.R));
        ability.addCost(new TapSourceCost());
        ability.addCost(new SacrificeSourceCost());
        this.addAbility(ability);
    }

    private OrbOfDragonkind(final OrbOfDragonkind card) {
        super(card);
    }

    @Override
    public OrbOfDragonkind copy() {
        return new OrbOfDragonkind(this);
    }
}
