package mage.cards.s;

import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.TapForManaAllTriggeredManaAbility;
import mage.abilities.costs.common.RevealHandSourceControllerCost;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.FlipSourceEffect;
import mage.abilities.effects.mana.ManaEffect;
import mage.cards.CardSetInfo;
import mage.cards.FlipCard;
import mage.choices.Choice;
import mage.choices.ChoiceColor;
import mage.constants.*;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterControlledLandPermanent;
import mage.filter.common.FilterLandCard;
import mage.filter.common.FilterLandPermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.NamePredicate;
import mage.filter.predicate.permanent.PermanentIdPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author LevelX2
 */
public final class SasayaOrochiAscendant extends FlipCard {

    public SasayaOrochiAscendant(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo,
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.CREATURE}, new SubType[]{SubType.SNAKE, SubType.MONK}, "{1}{G}{G}",
                "Sasaya's Essence",
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.ENCHANTMENT}, new SubType[]{});

        // Sasaya, Orochi Ascendant
        this.getLeftHalfCard().setPT(2, 3);

        // Reveal your hand: If you have seven or more land cards in your hand, flip Sasaya, Orochi Ascendant.
        Effect effect = new SasayaOrochiAscendantFlipEffect();
        effect.setOutcome(Outcome.AIDontUseIt);  // repetition issues need to be fixed for the AI to use this effectively
        this.getLeftHalfCard().addAbility(new SimpleActivatedAbility(effect, new RevealHandSourceControllerCost()));

        // Sasaya's Essence
        // Whenever a land you control is tapped for mana, for each other land you control with the same name, add one mana of any type that land produced.
        this.getRightHalfCard().addAbility(new TapForManaAllTriggeredManaAbility(
                new SasayasEssenceManaEffect(),
                new FilterControlledLandPermanent(), SetTargetPointer.PERMANENT));
    }

    private SasayaOrochiAscendant(final SasayaOrochiAscendant card) {
        super(card);
    }

    @Override
    public SasayaOrochiAscendant copy() {
        return new SasayaOrochiAscendant(this);
    }
}

class SasayaOrochiAscendantFlipEffect extends OneShotEffect {

    SasayaOrochiAscendantFlipEffect() {
        super(Outcome.Benefit);
        this.staticText = "If you have seven or more land cards in your hand, flip {this}";
    }

    private SasayaOrochiAscendantFlipEffect(final SasayaOrochiAscendantFlipEffect effect) {
        super(effect);
    }

    @Override
    public SasayaOrochiAscendantFlipEffect copy() {
        return new SasayaOrochiAscendantFlipEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller != null) {
            if (controller.getHand().count(new FilterLandCard(), game) > 6) {
                new FlipSourceEffect().apply(game, source);
            }
            return true;
        }
        return false;
    }
}

class SasayasEssenceManaEffect extends ManaEffect {

    SasayasEssenceManaEffect() {
        super();
        this.staticText = "for each other land you control with the same name, add one mana of any type that land produced";
    }

    private SasayasEssenceManaEffect(final SasayasEssenceManaEffect effect) {
        super(effect);
    }

    @Override
    public SasayasEssenceManaEffect copy() {
        return new SasayasEssenceManaEffect(this);
    }

    @Override
    public List<Mana> getNetMana(Game game, Ability source) {
        List<Mana> netMana = new ArrayList<>();
        Player controller = game.getPlayer(source.getControllerId());
        Mana producedMana = (Mana) this.getValue("mana");
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (controller != null && producedMana != null && permanent != null) {
            FilterPermanent filter = new FilterLandPermanent();
            filter.add(Predicates.not(new PermanentIdPredicate(permanent.getId())));
            filter.add(new NamePredicate(permanent.getName()));
            int count = game.getBattlefield().countAll(filter, controller.getId(), game);
            Mana mana = new Mana();
            if (count > 0) {
                if (producedMana.getBlack() > 0) {
                    mana.setBlack(count);
                }
                if (producedMana.getRed() > 0) {
                    mana.setRed(count);
                }
                if (producedMana.getBlue() > 0) {
                    mana.setBlue(count);
                }
                if (producedMana.getGreen() > 0) {
                    mana.setGreen(count);
                }
                if (producedMana.getWhite() > 0) {
                    mana.setWhite(count);
                }
                if (producedMana.getColorless() > 0) {
                    mana.setColorless(count);
                }
                mana.setAnyCombination(true);
                netMana.add(mana);
            }
        }
        return netMana;

    }

    /**
     * RULINGS 6/1/2005 If Sasaya’s Essence’s controller has four Forests and
     * taps one of them for Green, the Essence will add GreenGreenGreen to that
     * player’s mana pool for a total of GreenGreenGreenGreen.
     *
     * 6/1/2005 If Sasaya’s Essence’s controller has four Mossfire Valley and
     * taps one of them for RedGreen, the Essence will add three mana (one for
     * each other Mossfire Valley) of any combination of Red and/or Green to
     * that player’s mana pool.
     *
     * 6/1/2005 If Sasaya’s Essence’s controller has two Brushlands and taps one
     * of them for White, Sasaya’s Essence adds another White to that player’s
     * mana pool. It won’t produce Green or Colorless unless the land was tapped
     * for Green or Colorless instead.
     */
    @Override
    public Mana produceMana(Game game, Ability source) {
        Mana newMana = new Mana();
        if (game == null) {
            return newMana;
        }
        Player controller = game.getPlayer(source.getControllerId());
        Mana mana = (Mana) this.getValue("mana");
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (controller != null && mana != null && permanent != null) {
            FilterPermanent filter = new FilterLandPermanent();
            filter.add(Predicates.not(new PermanentIdPredicate(permanent.getId())));
            filter.add(new NamePredicate(permanent.getName()));
            int count = game.getBattlefield().countAll(filter, controller.getId(), game);
            if (count > 0) {
                Choice choice = new ChoiceColor(true);
                choice.getChoices().clear();
                choice.setMessage("Pick the type of mana to produce");
                if (mana.getBlack() > 0) {
                    choice.getChoices().add("Black");
                }
                if (mana.getRed() > 0) {
                    choice.getChoices().add("Red");
                }
                if (mana.getBlue() > 0) {
                    choice.getChoices().add("Blue");
                }
                if (mana.getGreen() > 0) {
                    choice.getChoices().add("Green");
                }
                if (mana.getWhite() > 0) {
                    choice.getChoices().add("White");
                }
                if (mana.getColorless() > 0) {
                    choice.getChoices().add("Colorless");
                }

                if (!choice.getChoices().isEmpty()) {

                    for (int i = 0; i < count; i++) {
                        choice.clearChoice();
                        String chosenColor;
                        if (choice.getChoices().size() == 1) {
                            chosenColor = choice.getChoices().iterator().next();
                        } else {
                            // workaround to skip choose dialog in check playable state
                            if (game.inCheckPlayableState()) {
                                chosenColor = "Any";
                            } else {
                                if (!controller.choose(Outcome.PutManaInPool, choice, game)) {
                                    return newMana;
                                }
                                chosenColor = choice.getChoice();
                            }
                        }
                        switch (chosenColor) {
                            case "Black":
                                newMana.increaseBlack();
                                break;
                            case "Blue":
                                newMana.increaseBlue();
                                break;
                            case "Red":
                                newMana.increaseRed();
                                break;
                            case "Green":
                                newMana.increaseGreen();
                                break;
                            case "White":
                                newMana.increaseWhite();
                                break;
                            case "Colorless":
                                newMana.increaseColorless();
                                break;
                            case "Any":
                                newMana.increaseAny();
                                break;
                        }
                    }
                }
            }
        }
        return newMana;
    }
}
