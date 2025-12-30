package mage.cards.k;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.common.continuous.BecomesCreatureAllEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.SubType;
import mage.filter.common.FilterLandPermanent;
import mage.game.permanent.token.custom.CreatureToken;

import java.util.UUID;

/**
 *
 * @author KholdFuzion
 *
 */
public final class KormusBell extends CardImpl {

    private static final FilterLandPermanent filter = new FilterLandPermanent(SubType.SWAMP, "All Swamps");

    public KormusBell(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{4}");

        // All Swamps are 1/1 black creatures that are still lands.
        ContinuousEffect effect = new BecomesCreatureAllEffect(
                new CreatureToken(1, 1, "1/1 black creatures").withColor("B"),
                "lands", filter,
                Duration.WhileOnBattlefield, true);
        this.addAbility(new SimpleStaticAbility(effect));
    }

    private KormusBell(final KormusBell card) {
        super(card);
    }

    @Override
    public KormusBell copy() {
        return new KormusBell(this);
    }
}
