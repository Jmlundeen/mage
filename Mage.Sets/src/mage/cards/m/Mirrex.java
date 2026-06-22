package mage.cards.m;

import mage.abilities.Ability;
import mage.abilities.ActivatedAbilityImpl;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.condition.common.SourceEnteredThisTurnCondition;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.mana.AnyColorManaAbility;
import mage.abilities.mana.ColorlessManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.game.permanent.token.PhyrexianMiteToken;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class Mirrex extends CardImpl {

    public Mirrex(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        this.subtype.add(SubType.SPHERE);

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T}: Add one mana of any color. Activate only if Mirrex entered the battlefield this turn.
        ActivatedAbilityImpl manaAbility = new AnyColorManaAbility();
        manaAbility.setCondition(SourceEnteredThisTurnCondition.DID);
        manaAbility.appendToRule(" Activate only if {this} entered the battlefield this turn.");
        this.addAbility(manaAbility);

        // {3}, {T}: Create a 1/1 colorless Phyrexian Mite artifact creature token with toxic 1 and "This creature can't block."
        Ability ability = new SimpleActivatedAbility(
                new CreateTokenEffect(new PhyrexianMiteToken()), new GenericManaCost(3)
        );
        ability.addCost(new TapSourceCost());
        this.addAbility(ability);
    }

    private Mirrex(final Mirrex card) {
        super(card);
    }

    @Override
    public Mirrex copy() {
        return new Mirrex(this);
    }
}
