package mage.cards.h;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.HalfValue;
import mage.abilities.dynamicvalue.common.SourceXCostValue;
import mage.abilities.effects.common.CastSourceTriggeredAbility;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.GainLifeEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class HydroidKrasis extends CardImpl {

    private static final DynamicValue halfX = new HalfValue(SourceXCostValue.instance, false);

    public HydroidKrasis(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{X}{G}{U}");

        this.subtype.add(SubType.JELLYFISH);
        this.subtype.add(SubType.HYDRA);
        this.subtype.add(SubType.BEAST);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // When you cast this spell, you gain half X life and draw half X cards. Round down each time.
        Ability castTrigger = new CastSourceTriggeredAbility(new GainLifeEffect(halfX)
                .setText("you gain half X life"), false);
        castTrigger.addEffect(new DrawCardSourceControllerEffect(SourceXCostValue.instance)
                .concatBy("and")
                .setText("draw half X cards. Round down each time"));
        this.addAbility(castTrigger);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Trample
        this.addAbility(TrampleAbility.getInstance());

        // Hydroid Krasis enters the battlefield with X +1/+1 counters on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.P1P1, SourceXCostValue.instance)));
    }

    private HydroidKrasis(final HydroidKrasis card) {
        super(card);
    }

    @Override
    public HydroidKrasis copy() {
        return new HydroidKrasis(this);
    }
}
