package mage.cards.s;

import mage.abilities.dynamicvalue.common.manavalue.CounteredManaValue;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.countered.CounterEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.game.permanent.token.TreasureToken;
import mage.target.TargetSpell;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class SpellSwindle extends CardImpl {

    public SpellSwindle(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{3}{U}{U}");

        // Counter target spell. Create X colorless Treasure artifact tokens, where X is that spell's converted mana cost. They have "T, Sacrifice this artifact: Add one mana of any color."
        this.getSpellAbility().addEffect(new CounterEffect()
                .setText("counter target spell")
                .setRememberManaValue(true)
        );
        this.getSpellAbility().addEffect(new CreateTokenEffect(new TreasureToken(), CounteredManaValue.instance)
                .setText("Create X Treasure tokens, where X is that spell's mana value"));
        this.getSpellAbility().addTarget(new TargetSpell());
    }

    private SpellSwindle(final SpellSwindle card) {
        super(card);
    }

    @Override
    public SpellSwindle copy() {
        return new SpellSwindle(this);
    }
}
