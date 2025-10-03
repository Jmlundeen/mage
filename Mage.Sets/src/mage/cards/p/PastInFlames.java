package mage.cards.p;

import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.keyword.FlashbackAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;

import java.util.UUID;

/**
 * @author BetaSteward
 */
public final class PastInFlames extends CardImpl {

    public PastInFlames(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{3}{R}");

        // Each instant and sorcery card in your graveyard gains flashback until end of turn. The flashback cost is equal to its mana cost.
        this.getSpellAbility().addEffect(new ContinuousEffectBuilder(Duration.EndOfTurn, Outcome.AddAbility, TargetController.YOU)
                .setAffectedZones(Zone.GRAVEYARD)
                .withGainedAbility((card, source, game) -> new FlashbackAbility(card, card.getManaCost()))
                .setText("Each instant and sorcery card in your graveyard gains flashback until end of turn. " +
                        "The flashback cost is equal to its mana cost")
        );

        // Flashback {4}{R}
        this.addAbility(new FlashbackAbility(this, new ManaCostsImpl<>("{4}{R}")));

    }

    private PastInFlames(final PastInFlames card) {
        super(card);
    }

    @Override
    public PastInFlames copy() {
        return new PastInFlames(this);
    }
}
