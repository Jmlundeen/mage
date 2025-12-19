package mage.cards.a;

import mage.abilities.LoyaltyAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ContinuousAffected;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterControlledPermanent;
import mage.game.permanent.token.WolfToken;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class ArlinnVoiceOfThePack extends CardImpl {

    private static final FilterPermanent filter = new FilterControlledPermanent("creature you control that's a Wolf or a Werewolf");

    static {
        filter.add(SubType.WOLF.getPredicate());
        filter.add(SubType.WEREWOLF.getPredicate());
    }

    public ArlinnVoiceOfThePack(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.PLANESWALKER}, "{4}{G}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ARLINN);
        this.setStartingLoyalty(7);

        // Each creature you control that's a Wolf or Werewolf enters the battlefield with an additional +1/+1 counter on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(ContinuousAffected.STATIC_OR_DYNAMIC, CounterType.P1P1.createInstance())
                .setFilter(filter)
        ));

        // -2: Create a 2/2 green Wolf creature token.
        this.addAbility(new LoyaltyAbility(new CreateTokenEffect(new WolfToken()), -2));
    }

    private ArlinnVoiceOfThePack(final ArlinnVoiceOfThePack card) {
        super(card);
    }

    @Override
    public ArlinnVoiceOfThePack copy() {
        return new ArlinnVoiceOfThePack(this);
    }
}
