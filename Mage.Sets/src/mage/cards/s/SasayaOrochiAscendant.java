package mage.cards.s;

import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.TapForManaAllTriggeredManaAbility;
import mage.abilities.costs.common.RevealHandSourceControllerCost;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.FlipSourceEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.providers.ManaTypeProvider;
import mage.cards.CardSetInfo;
import mage.cards.FlipCard;
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

import java.util.EnumSet;
import java.util.Set;
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
                new ComposedManaAbilityBuilder()
                        .addDynamicCombination(SasayasEssenceOtherLandsCount.instance, SasayasEssenceProducedManaTypes.instance)
                        .ruleText("for each other land you control with the same name, add one mana of any type that land produced")
                        .buildEffect(),
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

enum SasayasEssenceOtherLandsCount implements DynamicValue {
    instance;

    @Override
    public int calculate(Game game, Ability source, Effect effect) {
        Player controller = game.getPlayer(source.getControllerId());
        Permanent permanent = effect.getTargetPointer().getFirstTargetPermanentOrLKI(game, source);
        if (controller == null || permanent == null) {
            return 0;
        }

        FilterPermanent filter = new FilterLandPermanent();
        filter.add(Predicates.not(new PermanentIdPredicate(permanent.getId())));
        filter.add(new NamePredicate(permanent.getName()));
        return game.getBattlefield().countAll(filter, controller.getId(), game);
    }

    @Override
    public SasayasEssenceOtherLandsCount copy() {
        return this;
    }

    @Override
    public String getMessage() {
        return "other lands you control with the same name";
    }

    @Override
    public String toString() {
        return "X";
    }
}

enum SasayasEssenceProducedManaTypes implements ManaTypeProvider {
    instance;

    @Override
    public Set<ManaType> getManaTypes(Game game, Ability source, Effect effect) {
        if (game == null) {
            return EnumSet.noneOf(ManaType.class);
        }
        Mana producedMana = (Mana) effect.getValue("mana");
        if (producedMana == null) {
            return EnumSet.noneOf(ManaType.class);
        }
        return ManaType.getManaTypesFromManaList(producedMana);
    }
}
