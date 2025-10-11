package mage.cards.m;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.continuous.layers.L6_Abilities.GainAbilitiesOfEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.FilterPermanent;
import mage.filter.StaticFilters;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.mageobject.SameNameAsSourcePredicate;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class MarvinMurderousMimic extends CardImpl {

    private static final FilterPermanent filter = new FilterCreaturePermanent("creatures you control that don't have the same name as this creature");

    static {
        filter.add(SameNameAsSourcePredicate.NOT);
    }
    public MarvinMurderousMimic(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{2}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.TOY);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Marvin, Murderous Mimic has all activated abilities of creatures you control that don't have the same name as this creature.
        this.addAbility(new SimpleStaticAbility(new GainAbilitiesOfEffect(StaticFilters.FILTER_ACTIVATED_ABILITY,
                "{this} has all activated abilities of creatures you control that don't have the same name as this creature")
                .fromPermanents(filter)
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
