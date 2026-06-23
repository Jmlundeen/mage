package mage.cards.c;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.DiesSourceTriggeredAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.ReturnFromGraveyardToHandTargetEffect;
import mage.abilities.keyword.HasteAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.StaticFilters;
import mage.filter.StaticTypedFilters;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class CormelaGlamourThief extends CardImpl {

    public CormelaGlamourThief(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}{B}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.VAMPIRE);
        this.subtype.add(SubType.ROGUE);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // Haste
        this.addAbility(HasteAbility.getInstance());

        // {1}, {T}: Add {U}{B}{R}. Spend this mana only to cast instant and/or sorcery spells.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new GenericManaCost(1))
                .cost(new TapSourceCost())
                .addStatic(0, 1, 1, 1, 0, 0, 0)
                .condition(new FilteredSpellManaCondition(StaticTypedFilters.AN_INSTANT_OR_SORCERY_SPELL))
                .ruleText("Add {U}{B}{R}. Spend this mana only to cast instant and/or sorcery spells")
                .build()
        );

        // When Cormela, Glamour Thief dies, return up to one target instant or sorcery card from your graveyard to your hand.
        Ability ability = new DiesSourceTriggeredAbility(new ReturnFromGraveyardToHandTargetEffect(), false);
        ability.addTarget(new TargetCardInYourGraveyard(
                0, 1, StaticFilters.FILTER_CARD_INSTANT_OR_SORCERY_FROM_YOUR_GRAVEYARD
        ));
        this.addAbility(ability);
    }

    private CormelaGlamourThief(final CormelaGlamourThief card) {
        super(card);
    }

    @Override
    public CormelaGlamourThief copy() {
        return new CormelaGlamourThief(this);
    }
}
