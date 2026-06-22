
package mage.cards.g;

import mage.MageInt;
import mage.abilities.costs.common.ReturnToHandFromBattlefieldSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.TimingRule;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class GrinningIgnus extends CardImpl {

    public GrinningIgnus(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{2}{R}");
        this.subtype.add(SubType.ELEMENTAL);

        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // {R}, Return Grinning Ignus to its owner's hand: Add {C}{C}{R}. Activate this ability only any time you could cast a sorcery.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new ManaCostsImpl<>("{R}"))
                .cost(new ReturnToHandFromBattlefieldSourceCost())
                .addStatic(0, 0, 0, 1, 0, 2, 0)
                .ruleText("add {C}{C}{R}. Activate this ability only any time you could cast a sorcery")
                .build()
                .setTiming(TimingRule.SORCERY)
        );
    }

    private GrinningIgnus(final GrinningIgnus card) {
        super(card);
    }

    @Override
    public GrinningIgnus copy() {
        return new GrinningIgnus(this);
    }
}
