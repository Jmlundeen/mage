package mage.cards.g;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.ActivatedAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.ExileSourceFromGraveCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.ExileTargetEffect;
import mage.abilities.mana.AnyColorManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.target.common.TargetCardInGraveyard;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class GravestoneStrider extends CardImpl {

    public GravestoneStrider(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{2}");

        this.subtype.add(SubType.GOLEM);
        this.power = new MageInt(1);
        this.toughness = new MageInt(3);

        // {1}: Add one mana of any color. Activate only once each turn.
        ActivatedAbility manaAbility = new AnyColorManaAbility();
        manaAbility.setMaxActivationsPerTurn(1);
        this.addAbility(manaAbility);

        // {2}, Exile Gravestone Strider from your graveyard: Exile target card from a graveyard.
        Ability ability = new SimpleActivatedAbility(Zone.GRAVEYARD, new ExileTargetEffect(), new GenericManaCost(2));
        ability.addCost(new ExileSourceFromGraveCost());
        ability.addTarget(new TargetCardInGraveyard());
        this.addAbility(ability);
    }

    private GravestoneStrider(final GravestoneStrider card) {
        super(card);
    }

    @Override
    public GravestoneStrider copy() {
        return new GravestoneStrider(this);
    }
}
