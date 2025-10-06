package mage.cards.s;

import mage.MageItem;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.continuous.BoostSourceEffect;
import mage.abilities.effects.common.continuous.GainAbilityControlledEffect;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class ShadesBreath extends CardImpl {

    public ShadesBreath(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{1}{B}");

        // Until end of turn, each creature you control becomes a black Shade and gains "{B}: This creature gets +1/+1 until end of turn."
        ContinuousEffect effect = new ContinuousEffectBuilder(Duration.EndOfTurn, Outcome.BoostCreature, ContinuousAffected.SOURCE)
                .withAddPower(1)
                .withAddToughness(1)
                .setText("{this} gets +1/+1 until end of turn");
        Ability gainedAbility = new SimpleActivatedAbility(
                Zone.BATTLEFIELD,
                effect,
                new ManaCostsImpl<>("{B}")
        );
        this.getSpellAbility().addEffect(new ContinuousEffectBuilder(Duration.EndOfTurn, Outcome.BecomeCreature, TargetController.YOU, StaticFilters.FILTER_PERMANENT_CREATURE)
                .withAddedColor(true, ObjectColor.BLACK)
                .withAddedSubTypes(true, SubType.SHADE)
                .withGainedAbilities(gainedAbility)
                .setText("Until end of turn, each creature you control becomes a black Shade and gains \"{B}: This creature gets +1/+1 until end of turn.\"")
        );
    }

    private ShadesBreath(final ShadesBreath card) {
        super(card);
    }

    @Override
    public ShadesBreath copy() {
        return new ShadesBreath(this);
    }
}
