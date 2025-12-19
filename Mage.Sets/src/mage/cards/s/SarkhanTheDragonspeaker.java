
package mage.cards.s;

import mage.abilities.LoyaltyAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.DamageTargetEffect;
import mage.abilities.effects.common.GetEmblemEffect;
import mage.abilities.effects.common.continuous.BecomesCreatureSourceEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.HasteAbility;
import mage.abilities.keyword.IndestructibleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.command.emblems.SarkhanTheDragonspeakerEmblem;
import mage.game.permanent.token.custom.CreatureToken;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 * import mage.game.command.emblems.SarkhanTheDragonspeakerEmblem;
 *
 * @author emerald000
 */
public final class SarkhanTheDragonspeaker extends CardImpl {

    public SarkhanTheDragonspeaker(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.PLANESWALKER}, "{3}{R}{R}");
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.SARKHAN);

        this.setStartingLoyalty(4);

        // +1: Until end of turn, Sarkhan, the Dragonspeaker becomes a legendary 4/4 red Dragon creature with flying, indestructible, and haste.
        this.addAbility(new LoyaltyAbility(new BecomesCreatureSourceEffect(
                new CreatureToken(4, 4, "legendary 4/4 red Dragon creature with flying, indestructible, and haste")
                        .withColor("R")
                        .withSubType(SubType.DRAGON)
                        .withSuperType(SuperType.LEGENDARY)
                        .withAbility(FlyingAbility.getInstance())
                        .withAbility(IndestructibleAbility.getInstance())
                        .withAbility(HasteAbility.getInstance()),
                null, Duration.EndOfTurn), 1));

        // -3: Sarkhan, the Dragonspeaker deals 4 damage to target creature.
        LoyaltyAbility ability = new LoyaltyAbility(new DamageTargetEffect(4), -3);
        ability.addTarget(new TargetCreaturePermanent());
        this.addAbility(ability);

        // -6: You get an emblem with "At the beginning of your draw step, draw two additional cards" and "At the beginning of your end step, discard your hand."
        Effect effect = new GetEmblemEffect(new SarkhanTheDragonspeakerEmblem());
        effect.setText("You get an emblem with \"At the beginning of your draw step, draw two additional cards\" and \"At the beginning of your end step, discard your hand.\"");
        this.addAbility(new LoyaltyAbility(effect, -6));
    }

    private SarkhanTheDragonspeaker(final SarkhanTheDragonspeaker card) {
        super(card);
    }

    @Override
    public SarkhanTheDragonspeaker copy() {
        return new SarkhanTheDragonspeaker(this);
    }
}
