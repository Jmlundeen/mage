package mage.cards.p;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.continuous.BecomesCreatureSourceEffect;
import mage.abilities.keyword.FirstStrikeAbility;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.LifelinkAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.permanent.token.custom.CreatureToken;

import java.util.UUID;

/**
 * @author jeffwadsworth
 */
public final class ParagonOfTheAmesha extends CardImpl {

    public ParagonOfTheAmesha(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{W}");
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.KNIGHT);

        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // First strike
        this.addAbility(FirstStrikeAbility.getInstance());

        // {W}{U}{B}{R}{G}: Until end of turn, Paragon of the Amesha becomes an Angel, gets +3/+3, and gains flying and lifelink.
        BecomesCreatureSourceEffect effect = new BecomesCreatureSourceEffect(
                new CreatureToken(3, 3, "Angel, gets +3/+3, and gains flying and lifelink")
                        .withSubType(SubType.ANGEL)
                        .withAbility(FlyingAbility.getInstance())
                        .withAbility(LifelinkAbility.getInstance()),
                null, Duration.EndOfTurn);
        Ability ability = new SimpleActivatedAbility(effect, new ManaCostsImpl<>("{W}{U}{B}{R}{G}"));
        this.addAbility(ability);
    }

    private ParagonOfTheAmesha(final ParagonOfTheAmesha card) {
        super(card);
    }

    @Override
    public ParagonOfTheAmesha copy() {
        return new ParagonOfTheAmesha(this);
    }
}
