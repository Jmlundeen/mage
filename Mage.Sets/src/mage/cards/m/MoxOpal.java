package mage.cards.m;

import mage.abilities.ActivatedAbilityImpl;
import mage.abilities.condition.common.MetalcraftCondition;
import mage.abilities.hint.common.MetalcraftHint;
import mage.abilities.mana.AnyColorManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AbilityWord;
import mage.constants.CardType;
import mage.constants.SuperType;

import java.util.UUID;

/**
 * @author BetaSteward_at_googlemail.com, Loki
 */
public final class MoxOpal extends CardImpl {

    public MoxOpal(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{0}");
        this.supertype.add(SuperType.LEGENDARY);

        // Metalcraft -- {T}: Add one mana of any color. Activate only if you control three or more artifacts.
        ActivatedAbilityImpl ability = new AnyColorManaAbility()
                .setCondition(MetalcraftCondition.instance);
        ability.setAbilityWord(AbilityWord.METALCRAFT);
        ability.addHint(MetalcraftHint.instance);
        ability.appendToRule(" Activate only if you control three or more artifacts.");
        this.addAbility(ability);
    }

    private MoxOpal(final MoxOpal card) {
        super(card);
    }

    @Override
    public MoxOpal copy() {
        return new MoxOpal(this);
    }

}
