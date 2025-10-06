package mage.cards.l;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.ContinuousRuleModifyingEffectImpl;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.keyword.FlashbackAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.players.Player;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class LierDiscipleOfTheDrowned extends CardImpl {

    public LierDiscipleOfTheDrowned(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{U}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.WIZARD);
        this.power = new MageInt(3);
        this.toughness = new MageInt(4);

        // Spells can't be countered.
        this.addAbility(new SimpleStaticAbility(new LierDiscipleOfTheDrownedCounteredEffect()));

        // Each instant and sorcery card in your graveyard has flashback. The flashback cost is equal to that card's mana cost.
        this.addAbility(new SimpleStaticAbility(new ContinuousEffectBuilder(Duration.WhileOnBattlefield, Outcome.AddAbility, TargetController.YOU)
                .setAffectedZones(Zone.GRAVEYARD)
                .setCardFilter(StaticFilters.FILTER_CARD_INSTANT_OR_SORCERY)
                .withGainedAbility((card, source, game) -> new FlashbackAbility(card, card.getManaCost()))
                .setText("Each instant and sorcery card in your graveyard has flashback. " +
                        "The flashback cost is equal to that card's mana cost")
        ));
    }

    private LierDiscipleOfTheDrowned(final LierDiscipleOfTheDrowned card) {
        super(card);
    }

    @Override
    public LierDiscipleOfTheDrowned copy() {
        return new LierDiscipleOfTheDrowned(this);
    }
}

class LierDiscipleOfTheDrownedCounteredEffect extends ContinuousRuleModifyingEffectImpl {

    LierDiscipleOfTheDrownedCounteredEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Benefit);
        staticText = "spells can't be countered";
    }

    private LierDiscipleOfTheDrownedCounteredEffect(final LierDiscipleOfTheDrownedCounteredEffect effect) {
        super(effect);
    }

    @Override
    public LierDiscipleOfTheDrownedCounteredEffect copy() {
        return new LierDiscipleOfTheDrownedCounteredEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.COUNTER;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        return game.getSpell(event.getTargetId()) != null;
    }
}
