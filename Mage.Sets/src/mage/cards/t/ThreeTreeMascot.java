package mage.cards.t;

import mage.MageInt;
import mage.abilities.ActivatedAbilityImpl;
import mage.abilities.keyword.ChangelingAbility;
import mage.abilities.mana.AnyColorManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class ThreeTreeMascot extends CardImpl {

    public ThreeTreeMascot(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{2}");

        this.subtype.add(SubType.SHAPESHIFTER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(1);

        // Changeling
        this.addAbility(new ChangelingAbility());

        // {1}: Add one mana of any color. Activate only once each turn.
        ActivatedAbilityImpl ability = new AnyColorManaAbility();
        ability.setMaxActivationsPerTurn(1);
        ability.appendToRule(" Activate only once each turn.");
        this.addAbility(ability);
    }

    private ThreeTreeMascot(final ThreeTreeMascot card) {
        super(card);
    }

    @Override
    public ThreeTreeMascot copy() {
        return new ThreeTreeMascot(this);
    }
}
