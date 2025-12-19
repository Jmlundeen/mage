package mage.cards.s;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.keyword.FlashAbility;
import mage.abilities.keyword.FlashbackAbility;
import mage.abilities.keyword.HarmonizeAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.List;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class SongcrafterMage extends CardImpl {

    public SongcrafterMage(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{G}{U}{R}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.BARD);
        this.power = new MageInt(3);
        this.toughness = new MageInt(2);

        // Flash
        this.addAbility(FlashAbility.getInstance());

        // When this creature enters, target instant or sorcery card in your graveyard gains harmonize until end of turn. Its harmonize cost is equal to its mana cost.
        Ability ability = new EntersBattlefieldTriggeredAbility(new ContinuousEffectBuilder(Duration.EndOfTurn, Outcome.AddAbility)
                .withGainedAbility((card, source, game) -> new HarmonizeAbility(card, card.getManaCost().getText()))
                .setText("target instant or sorcery card in your graveyard gains harmonize until end of turn. " +
                        "Its harmonize cost is equal to its mana cost"));
        ability.addTarget(new TargetCardInYourGraveyard(StaticFilters.FILTER_CARD_INSTANT_OR_SORCERY_FROM_YOUR_GRAVEYARD));
        this.addAbility(ability);
    }

    private SongcrafterMage(final SongcrafterMage card) {
        super(card);
    }

    @Override
    public SongcrafterMage copy() {
        return new SongcrafterMage(this);
    }
}
