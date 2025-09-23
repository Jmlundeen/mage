package mage.cards.m;

import mage.abilities.effects.common.continuous.BecomesCreatureTargetEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.permanent.token.custom.CreatureToken;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class MordenkainensPolymorph extends CardImpl {

    public MordenkainensPolymorph(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{1}{U}");

        // Until end of turn, target creature becomes a Dragon with base power and toughness 4/4 and gains flying.
        this.getSpellAbility().addEffect(new BecomesCreatureTargetEffect(
                new CreatureToken(4, 4, "Dragon with base power and toughness 4/4 and gains flying")
                        .withSubType(SubType.DRAGON)
                        .withAbility(FlyingAbility.getInstance()),
                false, false, Duration.EndOfTurn)
                .withDurationRuleAtStart(true)
                .setRemoveSubtypes(true)
        );
        this.getSpellAbility().addTarget(new TargetCreaturePermanent());
    }

    private MordenkainensPolymorph(final MordenkainensPolymorph card) {
        super(card);
    }

    @Override
    public MordenkainensPolymorph copy() {
        return new MordenkainensPolymorph(this);
    }
}
