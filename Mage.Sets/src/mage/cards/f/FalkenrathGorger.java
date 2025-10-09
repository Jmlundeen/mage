
package mage.cards.f;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.keyword.MadnessAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterCreatureCard;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class FalkenrathGorger extends CardImpl {

    private static final FilterCreatureCard filter = new FilterCreatureCard("Vampire creature card you own");

    static {
        filter.add(SubType.VAMPIRE.getPredicate());

    }

    public FalkenrathGorger(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{R}");
        this.subtype.add(SubType.VAMPIRE);
        this.subtype.add(SubType.BERSERKER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(1);
        /**
         * 4/8/2016 Falkenrath Gorger's ability only applies while it's on the
         * battlefield. If you discard it, it won't give itself madness.
         * 4/8/2016 If Falkenrath Gorger leaves the battlefield before the
         * madness trigger has resolved for a Vampire card that gained madness
         * with its ability, the madness ability will still let you cast that
         * Vampire card for the appropriate cost even though it no longer has
         * madness. 4/8/2016 If you discard a Vampire creature card that already
         * has a madness ability, you'll choose which madness ability exiles it.
         * You may choose either the one it normally has or the one it gains
         * from Falkenrath Gorger.
         */
        // Each Vampire creature card you own that isn't on the battlefield has madness. Its madness cost is equal to its mana cost.
        this.addAbility(new SimpleStaticAbility(new ContinuousEffectBuilder(Duration.WhileOnBattlefield, Outcome.AddAbility)
                .withGainedAbility((card, source, game) -> new MadnessAbility(card.getManaCost()))
                .setCardFilter(filter)
                .setAffectedZones(Zone.GRAVEYARD, Zone.HAND, Zone.LIBRARY, Zone.EXILED, Zone.COMMAND, Zone.STACK)
                .setText("Each Vampire creature card you own that isn't on the battlefield has madness. The madness cost is equal to its mana cost")
        ));
    }

    private FalkenrathGorger(final FalkenrathGorger card) {
        super(card);
    }

    @Override
    public FalkenrathGorger copy() {
        return new FalkenrathGorger(this);
    }
}
