package mage.cards.g;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.EnterUntappedAllEffect;
import mage.abilities.mana.AnyColorAmongManaAbility;
import mage.abilities.mana.ColorlessManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.TargetController;
import mage.filter.FilterPermanent;
import mage.filter.FilterTyped;
import mage.filter.common.FilterControlledPermanent;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class GondGate extends CardImpl {

    private static final FilterPermanent filter = new FilterControlledPermanent(SubType.GATE, "Gates you control");
    private static final FilterTyped filter2 = new FilterTyped("a Gate you control")
            .add(SubType.GATE.getPredicate())
            .add(TargetController.YOU.getControllerPredicate());

    public GondGate(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        this.subtype.add(SubType.GATE);

        // Gates you control enter the battlefield untapped.
        this.addAbility(new SimpleStaticAbility(new EnterUntappedAllEffect(filter)));

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T}: Add one mana of any color that a Gate you control could produce.
        this.addAbility(AnyColorAmongManaAbility.builder(filter2)
                .onlyColors(true)
                .onlyProducibleManaTypes(true)
                .ruleText("Add one mana of any color that a Gate you control could produce")
                .build()
        );
    }

    private GondGate(final GondGate card) {
        super(card);
    }

    @Override
    public GondGate copy() {
        return new GondGate(this);
    }
}
