package mage.cards.x;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.replacement.ReplaceTokenEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.game.permanent.token.TreasureToken;

import java.util.UUID;

/**
 *
 * @author weirddan455
 */
public final class Xorn extends CardImpl {

    public Xorn(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{R}");

        this.subtype.add(SubType.ELEMENTAL);
        this.power = new MageInt(3);
        this.toughness = new MageInt(2);

        // If you would create one or more Treasure tokens, instead create those tokens plus an additional Treasure token.
        this.addAbility(new SimpleStaticAbility(new ReplaceTokenEffect(ReplaceTokenEffect.ModificationType.ADD, 1, new TreasureToken())
                .withTokenCondition((token, game) -> token.hasSubtype(SubType.TREASURE, game))
                .setText("If you would create one or more Treasure tokens, instead create those tokens plus an additional Treasure token.")
        ));
    }

    private Xorn(final Xorn card) {
        super(card);
    }

    @Override
    public Xorn copy() {
        return new Xorn(this);
    }
}