package mage.cards.n;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.continuous.layers.L6_Abilities.GainAbilitiesOfEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.ability.type.ActivatedAbilityPredicate;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;

import java.util.UUID;

/**
 * @author BetaSteward_at_googlemail.com
 */
public final class NecroticOoze extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("activated abilities of a creature card")
            .add(ActivatedAbilityPredicate.instance)
            .add(IMageObjectPredicate.getOSPPredicate(CardType.CREATURE.getPredicate()));

    public NecroticOoze(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B}{B}");
        this.subtype.add(SubType.OOZE);

        this.power = new MageInt(4);
        this.toughness = new MageInt(3);

        // As long as Necrotic Ooze is on the battlefield, it has all 
        // activated abilities of all creature cards in all graveyards
        this.addAbility(new SimpleStaticAbility(new GainAbilitiesOfEffect()
                .setAbilityFilter(filter, Zone.GRAVEYARD)
                .setText("As long as {this} is on the battlefield, it has all activated abilities of all creature cards in all graveyards")
        ));
    }

    private NecroticOoze(final NecroticOoze card) {
        super(card);
    }

    @Override
    public NecroticOoze copy() {
        return new NecroticOoze(this);
    }
}
