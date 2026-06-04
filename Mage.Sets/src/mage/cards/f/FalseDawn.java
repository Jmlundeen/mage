
package mage.cards.f;

import mage.abilities.Ability;
import mage.abilities.effects.AsThoughEffectImpl;
import mage.abilities.effects.AsThoughManaEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.mana.ReplaceManaEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.players.ManaPoolItem;

import java.util.UUID;

/**
 *
 * @author notgreat
 */
public final class FalseDawn extends CardImpl {

    public FalseDawn(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.SORCERY},"{1}{W}");

        // Until end of turn, spells and abilities you control that would add colored mana instead add that much white mana. Until end of turn, you may spend white mana as though it were mana of any color. Draw a card.
        this.getSpellAbility().addEffect(ReplaceManaEffect.produced(Duration.EndOfTurn, Outcome.Neutral, ReplaceManaEffect.replaceAllWithColor(ManaType.WHITE))
                .setProducedMatcher(context -> context.game().getControllerId(context.eventSourceId()).equals(context.source().getControllerId()))
                .setText("Until end of turn, spells and abilities you control that would add colored mana instead add that much white mana."));
        this.getSpellAbility().addEffect(new FalseDawnManaSpendEffect());
        this.getSpellAbility().addEffect(new DrawCardSourceControllerEffect(1).concatBy("<br>"));
    }

    private FalseDawn(final FalseDawn card) {
        super(card);
    }

    @Override
    public FalseDawn copy() {
        return new FalseDawn(this);
    }
}

//Based on Celestial Dawn
class FalseDawnManaSpendEffect extends AsThoughEffectImpl implements AsThoughManaEffect {

    public FalseDawnManaSpendEffect() {
        super(AsThoughEffectType.SPEND_OTHER_MANA, Duration.EndOfTurn, Outcome.Benefit);
        staticText = "Until end of turn, you may spend white mana as though it were mana of any color.";
    }

    private FalseDawnManaSpendEffect(final FalseDawnManaSpendEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return true;
    }

    @Override
    public FalseDawnManaSpendEffect copy() {
        return new FalseDawnManaSpendEffect(this);
    }

    @Override
    public boolean applies(UUID objectId, Ability source, UUID affectedControllerId, Game game) {
        return source.isControlledBy(affectedControllerId);
    }

    @Override
    public ManaType getAsThoughManaType(ManaType manaType, ManaPoolItem mana, UUID affectedControllerId, Ability source, Game game) {
        if (mana.getWhite() > 0) {
            return ManaType.WHITE;
        }
        return null;
    }
}