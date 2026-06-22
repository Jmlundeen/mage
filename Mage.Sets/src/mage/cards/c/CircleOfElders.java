
package mage.cards.c;

import mage.MageInt;
import mage.Mana;
import mage.abilities.condition.common.FormidableCondition;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.keyword.VigilanceAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AbilityWord;
import mage.constants.CardType;
import mage.constants.SubType;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class CircleOfElders extends CardImpl {

    public CircleOfElders(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{2}{G}{G}");
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.SHAMAN);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // Vigilance
        this.addAbility(VigilanceAbility.getInstance());

        // <i>Formidable</i> &mdash; {T}: Add {C}{C}{C}. Activate this only if creatures you control have total power 8 or greater.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(Mana.ColorlessMana(3))
                .activationCondition(FormidableCondition.instance)
                .ruleText("add {C}{C}{C}. Activate this only if creatures you control have total power 8 or greater")
                .build()
                .setAbilityWord(AbilityWord.FORMIDABLE)
        );
    }

    private CircleOfElders(final CircleOfElders card) {
        super(card);
    }

    @Override
    public CircleOfElders copy() {
        return new CircleOfElders(this);
    }
}
