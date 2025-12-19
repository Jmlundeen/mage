package mage.cards.d;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.replacement.ReplaceTokenEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.PartnerVariantType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.game.permanent.token.MutagenToken;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class DonatelloTheBrains extends CardImpl {

    public DonatelloTheBrains(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.MUTANT);
        this.subtype.add(SubType.NINJA);
        this.subtype.add(SubType.TURTLE);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // If one or more tokens would be created under your control, those tokens plus a Mutagen token are created instead.
        this.addAbility(new SimpleStaticAbility(new ReplaceTokenEffect(ReplaceTokenEffect.ModificationType.ADD, 1, new MutagenToken())
                .setText("if one or more tokens would be created under your control, " +
                        "those tokens plus a Mutagen token are created instead")
        ));

        // Partner--Character select
        this.addAbility(PartnerVariantType.CHARACTER_SELECT.makeAbility());
    }

    private DonatelloTheBrains(final DonatelloTheBrains card) {
        super(card);
    }

    @Override
    public DonatelloTheBrains copy() {
        return new DonatelloTheBrains(this);
    }
}
