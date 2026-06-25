package mage.cards.m;

import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.ReturnFromGraveyardToHandTargetEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.SpendOrActivateManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterCard;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.UUID;

/**
 * @author nantuko
 */
public final class MyrReservoir extends CardImpl {

    private static final FilterCard myrCardFilter = new FilterCard("Myr card from your graveyard");
    private static final FilterTyped myrObjectFilter = new FilterTyped("Myr")
            .add(IMageObjectPredicate.getOSPPredicate(SubType.MYR.getPredicate()));

    static {
        myrCardFilter.add(IMageObjectPredicate.getOSPPredicate(SubType.MYR.getPredicate()));
    }

    public MyrReservoir(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{3}");

        // {T}: Add {C}{C}. Spend this mana only to cast Myr spells or activate abilities of Myr.
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addStatic(Mana.ColorlessMana(2))
                .condition(new SpendOrActivateManaCondition(myrObjectFilter))
                .ruleText("Add {C}{C}. Spend this mana only to cast Myr spells or activate abilities of Myr")
                .build()
        );

        // {3}, {T}: Return target Myr card from your graveyard to your hand.
        Ability ability = new SimpleActivatedAbility(new ReturnFromGraveyardToHandTargetEffect(), new GenericManaCost(3));
        ability.addCost(new TapSourceCost());
        ability.addTarget(new TargetCardInYourGraveyard(myrCardFilter));
        this.addAbility(ability);
    }

    private MyrReservoir(final MyrReservoir card) {
        super(card);
    }

    @Override
    public MyrReservoir copy() {
        return new MyrReservoir(this);
    }
}
