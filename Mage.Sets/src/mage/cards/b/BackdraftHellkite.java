package mage.cards.b;

import mage.MageInt;
import mage.abilities.common.AttacksTriggeredAbility;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.keyword.FlashbackAbility;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class BackdraftHellkite extends CardImpl {

    public BackdraftHellkite(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{R}{R}");

        this.subtype.add(SubType.DRAGON);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Whenever Backdraft Hellkite attacks, each instant and sorcery card in your graveyard gains flashback until end of turn. The flashback cost is equal to its mana cost.
        this.addAbility(new AttacksTriggeredAbility(new ContinuousEffectBuilder(Duration.EndOfTurn, Outcome.AddAbility)
                .setAffectedZones(Zone.GRAVEYARD)
                .withGainedAbility((card, source, game) -> new FlashbackAbility(card, card.getManaCost()))
                .setText("Each instant and sorcery card in your graveyard gains flashback until end of turn. " +
                        "The flashback cost is equal to its mana cost"), false));
    }

    private BackdraftHellkite(final BackdraftHellkite card) {
        super(card);
    }

    @Override
    public BackdraftHellkite copy() {
        return new BackdraftHellkite(this);
    }
}

