package mage.cards.m;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.continuous.layers.L6_Abilities.GainAbilitiesOfEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.ability.type.ActivatedAbilityPredicate;
import mage.filter.predicate.typed.mageObject.object.SameNameAsSourcePredicate;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class MarvinMurderousMimic extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("activated abilities of creatures you control that don't have the same name as this creature")
            .addAll(
                    TargetController.YOU.getControllerPredicate(),
                    SameNameAsSourcePredicate.instance
            )
            .add(ActivatedAbilityPredicate.instance);

    public MarvinMurderousMimic(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{2}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.TOY);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Marvin, Murderous Mimic has all activated abilities of creatures you control that don't have the same name as this creature.
        this.addAbility(new SimpleStaticAbility(new GainAbilitiesOfEffect()
                .setAbilityFilter(filter, Zone.BATTLEFIELD)
                .setText("{this} has all activated abilities of creatures you control that don't have the same name as this creature")
        ));
    }

    private MarvinMurderousMimic(final MarvinMurderousMimic card) {
        super(card);
    }

    @Override
    public MarvinMurderousMimic copy() {
        return new MarvinMurderousMimic(this);
    }
}
