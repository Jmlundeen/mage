package mage.cards.o;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.costs.common.ExertSourceCost;
import mage.abilities.mana.AnyColorManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class OasisRitualist extends CardImpl {

    public OasisRitualist(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{G}");

        this.subtype.add(SubType.SNAKE);
        this.subtype.add(SubType.DRUID);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // {T}: Add one mana of any color.
        this.addAbility(new AnyColorManaAbility());

        // {T}, Exert Oasis Ritualist: Add two mana of any one color to your manna pool.
        Ability ability = new AnyColorManaAbility(2);
        ability.addCost(new ExertSourceCost());
        this.addAbility(ability);
    }

    private OasisRitualist(final OasisRitualist card) {
        super(card);
    }

    @Override
    public OasisRitualist copy() {
        return new OasisRitualist(this);
    }
}
