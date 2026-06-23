package mage.cards.s;

import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.AsThoughEffectImpl;
import mage.abilities.effects.AsThoughManaEffect;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.effects.common.continuous.layers.L6_Abilities.GainAbilitiesOfEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterPermanent;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.LogicalPredicate;
import mage.filter.predicate.typed.ability.type.ActivatedAbilityPredicate;
import mage.filter.predicate.typed.ability.type.ManaAbilityPredicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.ManaPoolItem;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Susucr
 */
public final class SharkeyTyrantOfTheShire extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("non-mana activated abilities of a land your opponents control")
            .addAll(
                    TargetController.OPPONENT.getControllerPredicate(),
                    CardType.LAND.getPredicate()
            )
            .addAll(
                    LogicalPredicate.not(ManaAbilityPredicate.instance),
                    ActivatedAbilityPredicate.instance
            );

    public SharkeyTyrantOfTheShire(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.AVATAR);
        this.subtype.add(SubType.ROGUE);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // Activated abilities of lands your opponents control can't be activated unless they're mana abilities.
        this.addAbility(new SimpleStaticAbility(new SharkeyTyrantOfTheShireReplacementEffect()));

        // Sharkey, Tyrant of the Shire has all activated abilities of lands your opponents control except mana abilities.
        this.addAbility(new SimpleStaticAbility(new GainAbilitiesOfEffect()
                .setAbilityFilter(filter, Zone.BATTLEFIELD)
                .setText("{this} has all activated abilities of lands your opponents control except mana abilities.")
        ));

        // Mana of any type can be spent to activate Sharkey's abilities.
        this.addAbility(new SimpleStaticAbility(new SharkeyTyrantOfTheShireAsThoughEffect()));
    }

    private SharkeyTyrantOfTheShire(final SharkeyTyrantOfTheShire card) {
        super(card);
    }

    @Override
    public SharkeyTyrantOfTheShire copy() {
        return new SharkeyTyrantOfTheShire(this);
    }
}

class SharkeyTyrantOfTheShireReplacementEffect extends ReplacementEffectImpl {

    private static final FilterPermanent filter = new FilterPermanent("lands your opponents control");

    static {
        filter.add(CardType.LAND.getPredicate());
        filter.add(TargetController.OPPONENT.getControllerPredicate());
    }

    SharkeyTyrantOfTheShireReplacementEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Detriment);
        staticText = "Activated abilities of lands your opponents control can't be activated unless they're mana abilities";
    }

    private SharkeyTyrantOfTheShireReplacementEffect(final SharkeyTyrantOfTheShireReplacementEffect effect) {
        super(effect);
    }

    @Override
    public SharkeyTyrantOfTheShireReplacementEffect copy() {
        return new SharkeyTyrantOfTheShireReplacementEffect(this);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        return true;
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ACTIVATE_ABILITY;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        MageObject object = game.getObject(event.getSourceId());
        if (object instanceof Permanent && filter.match((Permanent) object, source.getControllerId(), source, game)) {
            Optional<Ability> ability = object.getAbilities().get(event.getTargetId());
            if (ability.isPresent() && !ability.get().isManaActivatedAbility()) {
                return true;
            }
        }
        return false;
    }
}

class SharkeyTyrantOfTheShireAsThoughEffect extends AsThoughEffectImpl implements AsThoughManaEffect {

    SharkeyTyrantOfTheShireAsThoughEffect() {
        super(AsThoughEffectType.SPEND_OTHER_MANA, Duration.WhileOnBattlefield, Outcome.Benefit);
        staticText = "Mana of any type can be spent to activate Sharkey's abilities";
    }

    private SharkeyTyrantOfTheShireAsThoughEffect(SharkeyTyrantOfTheShireAsThoughEffect effect) {
        super(effect);
    }

    @Override
    public SharkeyTyrantOfTheShireAsThoughEffect copy() {
        return new SharkeyTyrantOfTheShireAsThoughEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return true;
    }

    @Override
    public boolean applies(UUID objectId, Ability source, UUID affectedControllerId, Game game) {
        return objectId.equals(source.getSourceId());
    }

    @Override
    public ManaType getAsThoughManaType(ManaType manaType, ManaPoolItem mana, UUID affectedControllerId, Ability source, Game game) {
        return mana.getFirstAvailable();
    }
}
