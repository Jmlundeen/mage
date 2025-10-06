package mage.cards.h;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.ExileHandCost;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.CastSourceTriggeredAbility;
import mage.abilities.effects.common.DoIfCostPaid;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.keyword.EmergeAbility;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.stack.Spell;
import mage.players.Player;

import java.util.*;

/**
 * @author xenohedron
 */
public final class HerigastEruptingNullkite extends CardImpl {

    public HerigastEruptingNullkite(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{9}");
        
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ELDRAZI);
        this.subtype.add(SubType.DRAGON);
        this.power = new MageInt(6);
        this.toughness = new MageInt(6);

        // Emerge {6}{R}{R}
        this.addAbility(new EmergeAbility(this, "{6}{R}{R}"));

        // When you cast this spell, you may exile your hand. If you do, draw three cards.
        this.addAbility(new CastSourceTriggeredAbility(new DoIfCostPaid(
                new DrawCardSourceControllerEffect(3),
                new ExileHandCost()
        )));

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Each creature spell you cast has emerge. The emerge cost is equal to its mana cost.
        this.addAbility(new SimpleStaticAbility(new ContinuousEffectBuilder(Duration.WhileOnBattlefield, Outcome.AddAbility, TargetController.YOU)
                .withGainedAbility((card, source, game) -> new EmergeAbility(card, card.getManaCost().getText()))
                .setAffectedZones(Zone.HAND, Zone.GRAVEYARD, Zone.LIBRARY, Zone.EXILED, Zone.COMMAND, Zone.STACK)
                .setCardFilter(StaticFilters.FILTER_CARD_CREATURE)
                .setText("Each creature spell you cast has emerge. The emerge cost is equal to its mana cost.")
        ));
    }

    private HerigastEruptingNullkite(final HerigastEruptingNullkite card) {
        super(card);
    }

    @Override
    public HerigastEruptingNullkite copy() {
        return new HerigastEruptingNullkite(this);
    }
}
