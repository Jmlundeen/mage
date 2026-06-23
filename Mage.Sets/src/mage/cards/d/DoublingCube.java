package mage.cards.d;

import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;

import java.util.UUID;

/**
 * @author jeffwadsworth
 */
public final class DoublingCube extends CardImpl {

    public DoublingCube(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{2}");

        // {3}, {T}: Double the amount of each type of mana in your mana pool.
        this.addAbility(new ComposedManaAbilityBuilder()
                .addCurrentManaPool()
                .cost(new ManaCostsImpl<>("{3}"))
                .cost(new TapSourceCost())
                .poolDependant()
                .ruleText("Double the amount of each type of unspent mana you have")
                .build()
        );
    }

    private DoublingCube(final DoublingCube card) {
        super(card);
    }

    @Override
    public DoublingCube copy() {
        return new DoublingCube(this);
    }
}

