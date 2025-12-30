package mage.cards.s;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.PermanentsOnTheBattlefieldCondition;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.ReturnFromGraveyardToBattlefieldTargetEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.common.FilterEnchantmentPermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.AnotherPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author fireshoes
 */
public final class StarfieldOfNyx extends CardImpl {

    private static final String rule1 = "As long as you control five or more enchantments, "
            + "each other non-Aura enchantment you control is a creature in addition to its other types "
            + "and has base power and base toughness each equal to its mana value.";

    private static final FilterCard filterGraveyardEnchantment
            = new FilterCard("enchantment card from your graveyard");
    private static final FilterEnchantmentPermanent filterEnchantmentYouControl
            = new FilterEnchantmentPermanent("enchantment you control");

    static {
        filterEnchantmentYouControl.add(TargetController.YOU.getControllerPredicate());
    }

    static {
        filterGraveyardEnchantment.add(CardType.ENCHANTMENT.getPredicate());
    }

    public StarfieldOfNyx(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{4}{W}");

        // At the beginning of your upkeep, you may return target enchantment card
        // from your graveyard to the battlefield.
        Ability ability = new BeginningOfUpkeepTriggeredAbility(
                new ReturnFromGraveyardToBattlefieldTargetEffect(), true);
        ability.addTarget(new TargetCardInYourGraveyard(filterGraveyardEnchantment));
        this.addAbility(ability);

        // As long as you control five or more enchantments, each other non-Aura enchantment
        // you control is a creature in addition to its other types and has base power and
        // base toughness each equal to its converted mana cost.
        ConditionalContinuousEffect effect = new ConditionalContinuousEffect(
                new StarfieldOfNyxEffect(), new PermanentsOnTheBattlefieldCondition(
                        filterEnchantmentYouControl, ComparisonType.MORE_THAN, 4), rule1);
        this.addAbility(new SimpleStaticAbility(effect));
    }

    private StarfieldOfNyx(final StarfieldOfNyx card) {
        super(card);
    }

    @Override
    public StarfieldOfNyx copy() {
        return new StarfieldOfNyx(this);
    }
}

class StarfieldOfNyxEffect extends ContinuousEffectImpl {

    private static final FilterControlledPermanent filter
            = new FilterControlledPermanent("Each other non-Aura enchantment you control");

    static {
        filter.add(CardType.ENCHANTMENT.getPredicate());
        filter.add(Predicates.not(SubType.AURA.getPredicate()));
        filter.add(AnotherPredicate.instance);
    }

    StarfieldOfNyxEffect() {
        super(Duration.WhileOnBattlefield, Outcome.BecomeCreature);
        staticText = "Each other non-Aura enchantment you control is a creature "
                + "in addition to its other types and has base power and "
                + "toughness each equal to its mana value";

    }

    private StarfieldOfNyxEffect(final StarfieldOfNyxEffect effect) {
        super(effect);
    }

    @Override
    public StarfieldOfNyxEffect copy() {
        return new StarfieldOfNyxEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            switch (layer) {
                case TypeChangingEffects_4:
                    if (sublayer == SubLayer.NA) {
                        permanent.addCardType(game, CardType.CREATURE);
                    }
                    break;

                case PTChangingEffects_7:
                    if (sublayer == SubLayer.SetPT_7b) {
                        int manaCost = permanent.getManaValue();
                        permanent.getPower().setModifiedBaseValue(manaCost);
                        permanent.getToughness().setModifiedBaseValue(manaCost);
                    }
                    break;
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        if (!source.getAffectedObjects().isEmpty()) {
            affectedObjects.addAll(source.getAffectedObjects());
        } else {
            for (Permanent permanent : game.getBattlefield().getActivePermanents(filter,
                    source.getControllerId(), source, game)) {
                affectedObjects.add(permanent);
                source.getAffectedObjects().add(permanent);
            }
        }
        return !affectedObjects.isEmpty();
    }

    @Override
    public boolean hasLayer(Layer layer) {
        return layer == Layer.PTChangingEffects_7
                || layer == Layer.TypeChangingEffects_4;
    }

    @Override
    public boolean hasSubLayer(SubLayer sublayer) {
        return sublayer == SubLayer.NA
                || sublayer == SubLayer.SetPT_7b;
    }
}
