package mage.cards.k;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
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

        // Whenever you cast a Human spell, target instant or sorcery card in your graveyard gains flashback until end of turn. The flashback cost is equal to its mana cost.
        ContinuousEffect effect = new ContinuousEffectBuilder(Duration.EndOfTurn, Outcome.AddAbility)
                .withGainedAbility((card, source, game) -> new FlashbackAbility(card, card.getManaCost()))
                .setText("target instant or sorcery card in your graveyard gains flashback until end of turn. The flashback cost is equal to its mana cost");
        Ability ability = new SpellCastControllerTriggeredAbility(effect, filterSpell, false);
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
