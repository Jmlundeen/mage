package mage.cards.a;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.ChooseCreatureTypeEffect;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author emerald000
 */
public final class AshesOfTheFallen extends CardImpl {

    public AshesOfTheFallen(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{2}");

        // As Ashes of the Fallen enters the battlefield, choose a creature type.
        this.addAbility(new AsEntersBattlefieldAbility(new ChooseCreatureTypeEffect(Outcome.Benefit)));

        // Each creature card in your graveyard has the chosen creature type in addition to its other types.
        this.addAbility(new SimpleStaticAbility(new ContinuousEffectBuilder(Duration.WhileOnBattlefield, Outcome.AddAbility)
                .setAffectedZones(Zone.GRAVEYARD)
                .withGainChosenCreatureType(false)
                .setText("Each creature card in your graveyard has the chosen creature type in addition to its other types")
                ));
    }

    private AshesOfTheFallen(final AshesOfTheFallen card) {
        super(card);
    }

    @Override
    public AshesOfTheFallen copy() {
        return new AshesOfTheFallen(this);
    }
}
