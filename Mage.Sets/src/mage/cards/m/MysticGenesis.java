
package mage.cards.m;

import mage.abilities.dynamicvalue.common.manavalue.CounteredManaValue;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.countered.CounterEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.filter.StaticTypedFilters;
import mage.game.permanent.token.OozeToken;
import mage.target.TargetGeneric;

import java.util.Collections;
import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class MysticGenesis extends CardImpl {

    public MysticGenesis(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{2}{G}{U}{U}");

        // Counter target spell. Create an X/X green Ooze creature token, where X is that spell's converted mana cost.
        this.getSpellAbility().addEffect(new CounterEffect()
                .setText("counter target spell")
                .setRememberManaValue(true)
        );
        this.getSpellAbility().addEffect(new CreateTokenEffect((game, source, effect) -> {
                    int value = CounteredManaValue.instance.calculate(game, source, effect);
                    return Collections.singletonList(new OozeToken(value, value));
                })
                .setText("Create an X/X green Ooze creature token, where X is that spell's mana value"));
        this.getSpellAbility().addTarget(new TargetGeneric(StaticTypedFilters.SPELL));

    }

    private MysticGenesis(final MysticGenesis card) {
        super(card);
    }

    @Override
    public MysticGenesis copy() {
        return new MysticGenesis(this);
    }
}
