
package mage.cards.c;

import mage.MageInt;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.common.replacement.ReplaceTokenEffect;
import mage.abilities.keyword.FlashAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class CraftyCutpurse extends CardImpl {

    public CraftyCutpurse(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{U}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.PIRATE);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Flash
        this.addAbility(FlashAbility.getInstance());

        // When Crafty Cutpurse enters the battlefield, each token that would be created under an opponent's control this turn is created under your control instead.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new ReplaceTokenEffect(Duration.EndOfTurn, Outcome.GainControl, ReplaceTokenEffect.ModificationType.CONTROLLER)
                .withGainControl()
                .setText("When {this} enters the , each token that would be created under an opponent's control this turn is created under your control instead"),
                false)
        );

    }

    private CraftyCutpurse(final CraftyCutpurse card) {
        super(card);
    }

    @Override
    public CraftyCutpurse copy() {
        return new CraftyCutpurse(this);
    }
}
