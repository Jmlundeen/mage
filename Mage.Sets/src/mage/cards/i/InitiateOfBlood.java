package mage.cards.i;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.delayed.WhenTargetDiesDelayedTriggeredAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.CreateDelayedTriggeredAbilityEffect;
import mage.abilities.effects.common.DamageTargetEffect;
import mage.abilities.effects.common.FlipSourceEffect;
import mage.cards.CardSetInfo;
import mage.cards.FlipCard;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.StaticFilters;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 * @author awjackson
 */
public final class InitiateOfBlood extends FlipCard {

    public InitiateOfBlood(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo,
                new CardType[]{CardType.CREATURE}, new SubType[]{SubType.OGRE, SubType.SHAMAN}, "{3}{R}",
                "Goka the Unjust",
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.CREATURE}, new SubType[]{SubType.OGRE, SubType.SHAMAN});

        // Initiate of Blood
        this.getLeftHalfCard().setPT(2, 2);

        // {T}: Initiate of Blood deals 1 damage to target creature that was dealt damage this turn. 
        // When that creature dies this turn, flip Initiate of Blood.
        Ability ability = new SimpleActivatedAbility(new DamageTargetEffect(1), new TapSourceCost());
        ability.addEffect(new CreateDelayedTriggeredAbilityEffect(new WhenTargetDiesDelayedTriggeredAbility(
                new FlipSourceEffect().setText("flip {this}")
        )));
        ability.addTarget(new TargetPermanent(StaticFilters.FILTER_CREATURE_DAMAGED_THIS_TURN));
        this.getLeftHalfCard().addAbility(ability);

        // Goka the Unjust
        this.getRightHalfCard().setPT(4, 4);

        // {T}: Goka the Unjust deals 4 damage to target creature that was dealt damage this turn.
        Ability ability2 = new SimpleActivatedAbility(new DamageTargetEffect(4), new TapSourceCost());
        ability2.addTarget(new TargetPermanent(StaticFilters.FILTER_CREATURE_DAMAGED_THIS_TURN));
        this.getRightHalfCard().addAbility(ability2);
    }

    private InitiateOfBlood(final InitiateOfBlood card) {
        super(card);
    }

    @Override
    public InitiateOfBlood copy() {
        return new InitiateOfBlood(this);
    }
}
