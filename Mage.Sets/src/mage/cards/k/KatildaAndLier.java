package mage.cards.k;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.FlashbackAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.filter.FilterSpell;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.List;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class KatildaAndLier extends CardImpl {

    private static final FilterSpell filterSpell = new FilterSpell("a Human spell");
    private static final FilterCard filterCard = new FilterCard("instant or sorcery card in your graveyard");

    static {
        filterSpell.add(SubType.HUMAN.getPredicate());
        filterCard.add(Predicates.or(
                CardType.INSTANT.getPredicate(),
                CardType.SORCERY.getPredicate()));
    }

    public KatildaAndLier(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{G}{W}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Each instant and sorcery card in your graveyard has flashback. The flashback cost is equal to that card's mana cost.
        Ability ability = new SpellCastControllerTriggeredAbility(new KatildaAndLierEffect(), filterSpell, false);
        ability.addTarget(new TargetCardInYourGraveyard(filterCard));
        this.addAbility(ability);
    }

    private KatildaAndLier(final KatildaAndLier card) {
        super(card);
    }

    @Override
    public KatildaAndLier copy() {
        return new KatildaAndLier(this);
    }
}

class KatildaAndLierEffect extends ContinuousEffectImpl {

    KatildaAndLierEffect() {
        super(Duration.EndOfTurn, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        this.staticText = "target instant or sorcery card in your graveyard gains flashback until end of turn. The flashback cost is equal to its mana cost";
    }

    private KatildaAndLierEffect(final KatildaAndLierEffect effect) {
        super(effect);
    }

    @Override
    public KatildaAndLierEffect copy() {
        return new KatildaAndLierEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Card card = (Card) object;
            FlashbackAbility ability = new FlashbackAbility(card, card.getManaCost());
            ability.setSourceId(card.getId());
            ability.setControllerId(card.getOwnerId());
            game.getState().addOtherAbility(card, ability);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Card card = game.getCard(getTargetPointer().getFirst(game, source));
        if (card == null) {
            return false;
        }
        affectedObjects.add(card);
        return true;
    }
}
