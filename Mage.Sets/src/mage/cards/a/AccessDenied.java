package mage.cards.a;

import mage.abilities.dynamicvalue.common.manavalue.CounteredManaValue;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.countered.CounterEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.filter.StaticTypedFilters;
import mage.game.permanent.token.ThopterColorlessToken;
import mage.target.TargetGeneric;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class AccessDenied extends CardImpl {

    public AccessDenied(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{3}{U}{U}");

        // Counter target spell. Create X 1/1 colorless Thopter artifact creature tokens with flying, where X is that spell's mana value.
        this.getSpellAbility().addEffect(new CounterEffect()
                .setText("counter target spell")
                .setRememberManaValue(true)
        );
        this.getSpellAbility().addEffect(new CreateTokenEffect(new ThopterColorlessToken(), CounteredManaValue.instance)
                .setText("create X 1/1 colorless Thopter artifact creature tokens with flying, where X is that spell's mana value")
        );
        this.getSpellAbility().addTarget(new TargetGeneric(StaticTypedFilters.SPELL));
    }

    private AccessDenied(final AccessDenied card) {
        super(card);
    }

    @Override
    public AccessDenied copy() {
        return new AccessDenied(this);
    }
}
