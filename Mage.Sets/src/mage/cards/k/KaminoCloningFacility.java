
package mage.cards.k;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;
import mage.game.permanent.token.TrooperToken;

import java.util.UUID;

/**
 *
 * @author Styxo
 */
public final class KaminoCloningFacility extends CardImpl {

    private static final FilterTyped FILTER = new FilterTyped("a Trooper spell")
            .add(IMageObjectPredicate.getOSPPredicate(SubType.TROOPER.getPredicate()));


    public KaminoCloningFacility(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.LAND},"");

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T} Add one mana of any color. Spend this mana only to cast a Trooper spell.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addAnyColor(1)
                .condition(new FilteredSpellManaCondition(FILTER))
                .ruleText("Add one mana of any color. Spend this mana only to cast a Trooper spell")
                .build()
        );

        // {5}, {T}: Create a 1/1 white Trooper creature tokens.
        Ability ability = new SimpleActivatedAbility(new CreateTokenEffect(new TrooperToken(), 1), new ManaCostsImpl<>("{5}"));
        ability.addCost(new TapSourceCost());
        this.addAbility(ability);
    }

    private KaminoCloningFacility(final KaminoCloningFacility card) {
        super(card);
    }

    @Override
    public KaminoCloningFacility copy() {
        return new KaminoCloningFacility(this);
    }
}
