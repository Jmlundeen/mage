package mage.cards.h;

import mage.abilities.Ability;
import mage.abilities.common.CantBlockAbility;
import mage.abilities.common.DiesSourceTriggeredAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BoostControlledEffect;
import mage.abilities.effects.common.continuous.BoostSourceEffect;
import mage.abilities.effects.common.continuous.GainAbilityControlledEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.Card;
import mage.cards.CardSetInfo;
import mage.cards.FlipCard;
import mage.constants.*;
import mage.filter.common.FilterCreaturePermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.UUID;

/**
 * @author LevelX2
 */
public final class HomuraHumanAscendant extends FlipCard {

    public HomuraHumanAscendant(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo,
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.CREATURE}, new SubType[]{SubType.HUMAN, SubType.MONK}, "{4}{R}{R}",
                "Homura's Essence",
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.ENCHANTMENT}, new SubType[]{});

        // Homura, Human Ascendant
        this.getLeftHalfCard().setPT(4, 4);

        // Homura, Human Ascendant can't block.
        this.getLeftHalfCard().addAbility(new CantBlockAbility());

        // When Homura dies, return it to the battlefield flipped.
        this.getLeftHalfCard().addAbility(new DiesSourceTriggeredAbility(new HomuraReturnFlippedSourceEffect()));

        // Homura's Essence
        // Creatures you control get +2/+2 and have flying and "{R}: This creature gets +1/+0 until end of turn."
        FilterCreaturePermanent filter = new FilterCreaturePermanent();
        Ability ability = new SimpleStaticAbility(new BoostControlledEffect(2, 2, Duration.WhileOnBattlefield, filter, false));
        Effect effect = new GainAbilityControlledEffect(FlyingAbility.getInstance(), Duration.WhileOnBattlefield, filter);
        effect.setText("and have flying");
        ability.addEffect(effect);
        Ability gainedAbility = new SimpleActivatedAbility(new BoostSourceEffect(1, 0, Duration.EndOfTurn), new ManaCostsImpl<>("{R}"));
        effect = new GainAbilityControlledEffect(gainedAbility, Duration.WhileOnBattlefield, filter);
        effect.setText("and \"{R}: This creature gets +1/+0 until end of turn.\"");
        ability.addEffect(effect);
        this.getRightHalfCard().addAbility(ability);
    }

    private HomuraHumanAscendant(final HomuraHumanAscendant card) {
        super(card);
    }

    @Override
    public HomuraHumanAscendant copy() {
        return new HomuraHumanAscendant(this);
    }
}

class HomuraReturnFlippedSourceEffect extends OneShotEffect {

    public HomuraReturnFlippedSourceEffect() {
        super(Outcome.BecomeCreature);
        staticText = "return it to the battlefield flipped";
    }

    private HomuraReturnFlippedSourceEffect(final HomuraReturnFlippedSourceEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Card sourceCard = game.getCard(source.getSourceId());
        Player controller = game.getPlayer(source.getControllerId());
        if (sourceCard != null && controller != null && game.getState().getZone(source.getSourceId()) == Zone.GRAVEYARD) {
            game.getState().setValue(FlipCard.VALUE_KEY_ENTER_FLIPPED + source.getSourceId(), Boolean.TRUE);
            controller.moveCards(sourceCard, Zone.BATTLEFIELD, source, game);
            Permanent permanent = game.getPermanent(source.getSourceId());
            if (permanent != null) {
                permanent.flip(game);  // not complete correct because it should enter the battlefield flipped
            }
            return true;
        }
        return false;
    }

    @Override
    public HomuraReturnFlippedSourceEffect copy() {
        return new HomuraReturnFlippedSourceEffect(this);
    }

}
