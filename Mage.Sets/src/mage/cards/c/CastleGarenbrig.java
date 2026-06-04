package mage.cards.c;

import mage.Mana;
import mage.abilities.common.EntersBattlefieldTappedUnlessAbility;
import mage.abilities.condition.common.YouControlPermanentCondition;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.GreenManaAbility;
import mage.abilities.mana.conditional.SpendOrActivateManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterPermanent;
import mage.filter.StaticTypedFilters;
import mage.filter.common.FilterControlledPermanent;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class CastleGarenbrig extends CardImpl {

    private static final FilterPermanent filter = new FilterControlledPermanent(SubType.FOREST);
    private static final YouControlPermanentCondition condition = new YouControlPermanentCondition(filter);

    public CastleGarenbrig(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // Castle Garenbrig enters the battlefield tapped unless you control a Forest.
        this.addAbility(new EntersBattlefieldTappedUnlessAbility(condition).addHint(condition.getHint()));

        // {T}: Add {G}.
        this.addAbility(new GreenManaAbility());

        // {2}{G}{G}, {T}: Add six {G}. Spend this mana only to cast creature spells or activate abilities of creatures.
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new ManaCostsImpl<>("{2}{G}{G}"))
                .cost(new TapSourceCost())
                .addStatic(Mana.GreenMana(6))
                .condition(new SpendOrActivateManaCondition(StaticTypedFilters.A_CREATURE_CARD))
                .ruleText("Add six {G}. Spend this mana only to cast creature spells or activate abilities of creatures.")
                .build()
        );
    }

    private CastleGarenbrig(final CastleGarenbrig card) {
        super(card);
    }

    @Override
    public CastleGarenbrig copy() {
        return new CastleGarenbrig(this);
    }
}
