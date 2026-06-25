package mage.cards.p;

import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.condition.common.YouControlTwoOrMoreGatesCondition;
import mage.abilities.effects.common.GainLifeEffect;
import mage.abilities.hint.common.GatesYouControlHint;
import mage.abilities.mana.AnyColorAmongManaAbility;
import mage.abilities.mana.ColorlessManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.TargetController;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class PlazaOfHarmony extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("a Gate you control")
            .add(IMageObjectPredicate.getOSPPredicate(SubType.GATE.getPredicate()))
            .add(TargetController.YOU.getControllerPredicate());

    public PlazaOfHarmony(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // When Plaza of Harmony enters the battlefield, if you control two or more Gates, you gain 3 life.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new GainLifeEffect(3))
                .withInterveningIf(YouControlTwoOrMoreGatesCondition.instance).addHint(GatesYouControlHint.instance));

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T}: Add one mana of any type a Gate you control could produce.
        this.addAbility(AnyColorAmongManaAbility.builder(filter)
                .onlyProducibleManaTypes(true)
                .ruleText("Add one mana of any type that a Gate you control could produce")
                .build()
        );
    }

    private PlazaOfHarmony(final PlazaOfHarmony card) {
        super(card);
    }

    @Override
    public PlazaOfHarmony copy() {
        return new PlazaOfHarmony(this);
    }
}
