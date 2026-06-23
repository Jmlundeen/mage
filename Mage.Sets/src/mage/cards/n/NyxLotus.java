package mage.cards.n;

import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTappedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.dynamicvalue.common.DevotionCount;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SuperType;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class NyxLotus extends CardImpl {

    public NyxLotus(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{4}");

        this.supertype.add(SuperType.LEGENDARY);

        // Nyx Lotus enters the battlefield tapped.
        this.addAbility(new EntersBattlefieldTappedAbility());

        // {T}: Choose a color. Add an amount of mana of that color equal to your devotion to that color.
        Ability ability = new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addDynamicChoice(NykthosManaAmounts.instance)
                .ruleText("Choose a color. Add an amount of mana of that color equal to your devotion to that color")
                .build();
        ability.addHint(DevotionCount.W.getHint());
        ability.addHint(DevotionCount.U.getHint());
        ability.addHint(DevotionCount.B.getHint());
        ability.addHint(DevotionCount.R.getHint());
        ability.addHint(DevotionCount.G.getHint());
        this.addAbility(ability);
    }

    private NyxLotus(final NyxLotus card) {
        super(card);
    }

    @Override
    public NyxLotus copy() {
        return new NyxLotus(this);
    }
}
