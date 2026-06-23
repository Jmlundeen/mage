package mage.cards.r;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.continuous.layers.L6_Abilities.GainAbilitiesOfEffect;
import mage.abilities.keyword.VigilanceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.ability.type.ActivatedAbilityPredicate;

import java.util.UUID;

/**
 * @author PurpleCrowbar
 */
public final class RobaranMercenaries extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("activated ability of a legendary creature you control")
            .addAll(
                    SuperType.LEGENDARY.getPredicate(),
                    TargetController.YOU.getControllerPredicate()
            )
            .add(ActivatedAbilityPredicate.instance);

    public RobaranMercenaries(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{W}");
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.MERCENARY);

        this.power = new MageInt(3);
        this.toughness = new MageInt(4);

        // Vigilance
        this.addAbility(VigilanceAbility.getInstance());

        // Robaran Mercenaries has all activated abilties of all legendary creatures you control.
        this.addAbility(new SimpleStaticAbility(new GainAbilitiesOfEffect()
                .setAbilityFilter(filter, Zone.BATTLEFIELD)
                .setText("{this} has all activated abilities of all legendary creatures you control")
        ));
    }

    private RobaranMercenaries(final RobaranMercenaries card) {
        super(card);
    }

    @Override
    public RobaranMercenaries copy() {
        return new RobaranMercenaries(this);
    }
}
