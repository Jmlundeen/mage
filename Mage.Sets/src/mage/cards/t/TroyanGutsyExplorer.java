package mage.cards.t;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.costs.mana.VariableManaCost;
import mage.abilities.effects.common.DrawDiscardControllerEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.Spell.ISpellPredicate;

import java.util.UUID;

/**
 * @author Susucr
 */
public final class TroyanGutsyExplorer extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("spells with mana value 5 or greater or spells with {X} in their mana costs")
            .add((ISpellPredicate) (osp, game) -> osp.getObject().getManaValue() >= 5
                    || osp.getObject().getManaCost().stream().anyMatch(VariableManaCost.class::isInstance));

    public TroyanGutsyExplorer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.VEDALKEN);
        this.subtype.add(SubType.SCOUT);
        this.power = new MageInt(1);
        this.toughness = new MageInt(3);

        // {T}: Add {G}{U}. Spend this mana only to cast spells with mana value 5 or greater or spells with X in their mana costs.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(0, 1, 0, 0, 1, 0, 0)
                .condition(new FilteredSpellManaCondition(filter))
                .ruleText("Add {G}{U}. Spend this mana only to cast spells with mana value 5 or greater or spells with X in their mana costs")
                .build()
        );

        // {U}, {T}: Draw a card, then discard a card.
        Ability ability = new SimpleActivatedAbility(
                new DrawDiscardControllerEffect(),
                new ManaCostsImpl<>("{U}")
        );
        ability.addCost(new TapSourceCost());
        this.addAbility(ability);
    }

    private TroyanGutsyExplorer(final TroyanGutsyExplorer card) {
        super(card);
    }

    @Override
    public TroyanGutsyExplorer copy() {
        return new TroyanGutsyExplorer(this);
    }
}
