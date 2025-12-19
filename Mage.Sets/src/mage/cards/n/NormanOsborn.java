package mage.cards.n;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.ActivateAsSorceryActivatedAbility;
import mage.abilities.common.DealsCombatDamageToAPlayerTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.TransformSourceEffect;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.cost.SpellsCostReductionControllerEffect;
import mage.abilities.effects.keyword.ConniveSourceEffect;
import mage.abilities.keyword.CantBeBlockedSourceAbility;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.MayhemAbility;
import mage.abilities.keyword.MenaceAbility;
import mage.cards.Card;
import mage.cards.CardSetInfo;
import mage.cards.ModalDoubleFacedCard;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.filter.StaticFilters;
import mage.filter.predicate.card.CastFromZonePredicate;
import mage.game.Game;
import mage.players.Player;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author Jmlundeen
 */
public final class NormanOsborn extends ModalDoubleFacedCard {

    private static final FilterCard filter = new FilterCard("spells you cast from your graveyard");

    static {
        filter.add(new CastFromZonePredicate(Zone.GRAVEYARD));
    }

    public NormanOsborn(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo,
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.CREATURE}, new SubType[]{SubType.HUMAN, SubType.SCIENTIST, SubType.VILLAIN}, "{1}{U}",
                "Green Goblin",
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.CREATURE}, new SubType[]{SubType.GOBLIN, SubType.HUMAN, SubType.VILLAIN}, "{1}{U}{B}{R}");

        this.getLeftHalfCard().setPT(1, 1);
        this.getRightHalfCard().setPT(3, 3);

        // Norman Osborn can't be blocked.
        this.getLeftHalfCard().addAbility(new CantBeBlockedSourceAbility());

        // Whenever Norman Osborn deals combat damage to a player, he connives.
        this.getLeftHalfCard().addAbility(new DealsCombatDamageToAPlayerTriggeredAbility(new ConniveSourceEffect("he")));

        // {1}{U}{B}{R}: Transform Norman Osborn. Activate only as a sorcery.
        this.getLeftHalfCard().addAbility(new ActivateAsSorceryActivatedAbility(
                new TransformSourceEffect(), new ManaCostsImpl<>("{1}{U}{B}{R}")
        ));

        // Green Goblin
        // Flying
        this.getRightHalfCard().addAbility(FlyingAbility.getInstance());

        // Menace
        this.getRightHalfCard().addAbility(new MenaceAbility());

        // Spells you cast from your graveyard cost {2} less to cast.
        this.getRightHalfCard().addAbility(new SimpleStaticAbility(new SpellsCostReductionControllerEffect(filter, 2)));

        // Goblin Formula -- Each nonland card in your graveyard has mayhem. The mayhem cost is equal to its mana cost.
        this.getRightHalfCard().addAbility(new SimpleStaticAbility(new ContinuousEffectBuilder(Duration.WhileOnBattlefield, Outcome.AddAbility)
                .setAffectedZones(Zone.GRAVEYARD)
                .setCardFilter(StaticFilters.FILTER_CARD_A_NON_LAND)
                .withGainedAbility((card, source, game) -> new MayhemAbility(card, card.getManaCost().getText()))
                .setText("Each nonland card in your graveyard has mayhem. The mayhem cost is equal to the card's mana cost.")
        ));
    }

    private NormanOsborn(final NormanOsborn card) {
        super(card);
    }

    @Override
    public NormanOsborn copy() {
        return new NormanOsborn(this);
    }
}
