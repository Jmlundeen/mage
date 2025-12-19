package mage.cards.q;

import mage.MageInt;
import mage.MageObject;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.PermanentsOnBattlefieldCount;
import mage.abilities.effects.common.continuous.SetBasePowerToughnessSourceEffect;
import mage.abilities.effects.common.replacement.ReplaceTokenEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.permanent.token.SoldierToken;

import java.util.UUID;

/**
 *
 * @author weirddan455
 */
public final class QueenAllenalOfRuadach extends CardImpl {

    private static final PermanentsOnBattlefieldCount count
            = new PermanentsOnBattlefieldCount(StaticFilters.FILTER_CONTROLLED_CREATURES);

    public QueenAllenalOfRuadach(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{G}{W}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ELF);
        this.subtype.add(SubType.NOBLE);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // Queen Allenal of Ruadach's power and toughness are each equal to the number of creatures you control.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new SetBasePowerToughnessSourceEffect(count)
        ));

        // If one or more creature tokens would be created under your control, those tokens plus a 1/1 white Soldier creature token are created instead.
        this.addAbility(new SimpleStaticAbility(new ReplaceTokenEffect(ReplaceTokenEffect.ModificationType.ADD, 1, new SoldierToken())
                .withTokenCondition(MageObject::isCreature)
                .setText("If one or more creature tokens would be created under your control, those tokens plus a 1/1 white Soldier creature token are created instead")
        ));
    }

    private QueenAllenalOfRuadach(final QueenAllenalOfRuadach card) {
        super(card);
    }

    @Override
    public QueenAllenalOfRuadach copy() {
        return new QueenAllenalOfRuadach(this);
    }
}
