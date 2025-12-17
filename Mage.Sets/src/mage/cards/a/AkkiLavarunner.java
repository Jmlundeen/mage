package mage.cards.a;

import mage.MageObject;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.effects.common.FlipSourceEffect;
import mage.abilities.keyword.HasteAbility;
import mage.abilities.keyword.ProtectionAbility;
import mage.cards.CardSetInfo;
import mage.cards.FlipCard;
import mage.constants.*;
import mage.game.Game;
import mage.game.events.DamagedPlayerEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;

import java.util.UUID;

/**
 * @author Loki
 */
public final class AkkiLavarunner extends FlipCard {

    public AkkiLavarunner(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo,
                new CardType[]{CardType.CREATURE}, new SubType[]{SubType.GOBLIN, SubType.WARRIOR}, "{3}{R}",
                "Tok-Tok, Volcano Born",
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.CREATURE}, new SubType[]{SubType.GOBLIN, SubType.SHAMAN});

        // Akki Lavarunner
        this.getLeftHalfCard().setPT(1, 1);

        // Haste
        this.getLeftHalfCard().addAbility(HasteAbility.getInstance());

        // Whenever Akki Lavarunner deals damage to an opponent, flip it.
        this.getLeftHalfCard().addAbility(new AkkiLavarunnerAbility());

        // Tok-Tok, Volcano Born
        this.getRightHalfCard().setPT(2, 2);

        // Protection from red
        this.getRightHalfCard().addAbility(ProtectionAbility.from(ObjectColor.RED));

        // If a red source would deal damage to a player, it deals that much damage plus 1 to that player instead.
        this.getRightHalfCard().addAbility(new SimpleStaticAbility(new TokTokVolcanoBornEffect()));
    }

    private AkkiLavarunner(final AkkiLavarunner card) {
        super(card);
    }

    @Override
    public AkkiLavarunner copy() {
        return new AkkiLavarunner(this);
    }
}

class AkkiLavarunnerAbility extends TriggeredAbilityImpl {

    public AkkiLavarunnerAbility() {
        super(Zone.BATTLEFIELD, new FlipSourceEffect());
    }

    private AkkiLavarunnerAbility(final AkkiLavarunnerAbility ability) {
        super(ability);
    }

    @Override
    public AkkiLavarunnerAbility copy() {
        return new AkkiLavarunnerAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DAMAGED_PLAYER;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        DamagedPlayerEvent damageEvent = (DamagedPlayerEvent) event;
        return damageEvent.isCombatDamage() && this.sourceId.equals(event.getSourceId());
    }

    @Override
    public String getRule() {
        return "Whenever {this} deals damage to an opponent, flip it.";
    }
}

class TokTokVolcanoBornEffect extends ReplacementEffectImpl {

    TokTokVolcanoBornEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Benefit);
        staticText = "If a red source would deal damage to a player, it deals that much damage plus 1 to that player instead";
    }

    private TokTokVolcanoBornEffect(final TokTokVolcanoBornEffect effect) {
        super(effect);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DAMAGE_PLAYER;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        MageObject sourceObject;
        Permanent sourcePermanent = game.getPermanentOrLKIBattlefield(event.getSourceId());
        if (sourcePermanent == null) {
            sourceObject = game.getObject(event.getSourceId());
        } else {
            sourceObject = sourcePermanent;
        }

        if (sourceObject != null && sourceObject.getColor(game).isRed()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        event.setAmount(event.getAmount() + 1);
        return false;
    }

    @Override
    public TokTokVolcanoBornEffect copy() {
        return new TokTokVolcanoBornEffect(this);
    }
}
