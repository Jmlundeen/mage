
package mage.cards.p;

import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.effects.common.CopyEffect;
import mage.abilities.keyword.DefenderAbility;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.choices.Choice;
import mage.choices.ChoiceImpl;
import mage.constants.*;
import mage.game.Game;
import mage.game.events.EntersTheBattlefieldEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.Token;
import mage.players.Player;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class PrimalPlasma extends CardImpl {

    public PrimalPlasma(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{U}");
        this.subtype.add(SubType.ELEMENTAL);
        this.subtype.add(SubType.SHAPESHIFTER);

        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // As Primal Plasma enters the battlefield, it becomes your choice of a 3/3 creature, a 2/2 creature with flying, or a 1/6 creature with defender.
        this.addAbility(new SimpleStaticAbility(Zone.ALL, new PrimalPlasmaReplacementEffect()));
    }

    private PrimalPlasma(final PrimalPlasma card) {
        super(card);
    }

    @Override
    public PrimalPlasma copy() {
        return new PrimalPlasma(this);
    }

    static class PrimalPlasmaReplacementEffect extends ReplacementEffectImpl {

        private static final String choice33 = "a 3/3 creature";
        private static final String choice22 = "a 2/2 creature with flying";
        private static final String choice16 = "a 1/6 creature with defender";

        public PrimalPlasmaReplacementEffect() {
            super(Duration.WhileOnBattlefield, Outcome.Benefit);
            staticText = "As {this} enters, it becomes your choice of a 3/3 creature, a 2/2 creature with flying, or a 1/6 creature with defender";
        }

        private PrimalPlasmaReplacementEffect(final PrimalPlasmaReplacementEffect effect) {
            super(effect);
        }

        @Override
        public boolean checksEventType(GameEvent event, Game game) {
            return event.getType() == GameEvent.EventType.ENTERS_THE_BATTLEFIELD;
        }

        @Override
        public boolean applies(GameEvent event, Ability source, Game game) {
            if (event.getTargetId().equals(source.getSourceId())) {
                Permanent sourcePermanent = ((EntersTheBattlefieldEvent) event).getTarget();
                return sourcePermanent != null && !sourcePermanent.isFaceDown();
            }
            return false;
        }

        @Override
        public boolean replaceEvent(GameEvent event, Ability source, Game game) {
            Permanent permanent = ((EntersTheBattlefieldEvent) event).getTarget();
            Player controller = game.getPlayer(source.getControllerId());
            if (permanent != null && controller != null) {
                MageObject referenceObject = getReferenceObject(permanent, game);
                Choice choice = new ChoiceImpl(true);
                choice.setMessage("Choose what " + permanent.getIdName() + " becomes to");
                choice.getChoices().add(choice33);
                choice.getChoices().add(choice22);
                choice.getChoices().add(choice16);
                if (!controller.choose(Outcome.Neutral, choice, game)) {
                    return false;
                }
                int power = 0;
                int toughness = 0;
                switch (choice.getChoice()) {
                    case choice33:
                        power = 3;
                        toughness = 3;
                        break;
                    case choice22:
                        power = 2;
                        toughness = 2;
                        addAbility(referenceObject, FlyingAbility.getInstance());
                        break;
                    case choice16:
                        power = 1;
                        toughness = 6;
                        addAbility(referenceObject, DefenderAbility.getInstance());
                        break;
                }
                if (permanent.isCopy()) {
                    if (referenceObject instanceof Token) {
                        ((Token) referenceObject).setPower(power);
                        ((Token) referenceObject).setToughness(toughness);
                    } else if (referenceObject instanceof Card) {
                        ((Card) referenceObject).setPT(power, toughness);
                    }
                } else {
                    permanent.setPT(power, toughness);
                }
            }
            return false;

        }

        private MageObject getReferenceObject(Permanent permanent, Game game) {
            if (permanent.isCopy()) {
                for (ContinuousEffect effect : game.getState().getContinuousEffects().getLayeredEffects(game)) {
                    if (!(effect instanceof CopyEffect)) {
                        continue;
                    }
                    CopyEffect copyEffect = (CopyEffect) effect;
                    if (copyEffect.getSourceId().equals(permanent.getId())) {
                        return copyEffect.getTarget();
                    }
                }
            }
            return permanent.getBasicMageObject();
        }

        private void addAbility(MageObject referenceObject, Ability ability) {
            if (referenceObject instanceof Permanent) {
                ((Permanent) referenceObject).addAbility(ability, null, null, false);
            } else if (referenceObject instanceof Card) {
                ((Card) referenceObject).addAbility(ability);
            } else if (referenceObject instanceof Token) {
                ((Token) referenceObject).addAbility(ability);
            }
        }

        @Override
        public PrimalPlasmaReplacementEffect copy() {
            return new PrimalPlasmaReplacementEffect(this);
        }

    }
}
