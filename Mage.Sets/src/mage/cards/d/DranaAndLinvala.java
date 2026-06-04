package mage.cards.d;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.AsThoughEffectImpl;
import mage.abilities.effects.AsThoughManaEffect;
import mage.abilities.effects.RestrictionEffect;
import mage.abilities.effects.common.continuous.layers.L6_Abilities.GainAbilitiesOfEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.VigilanceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.ability.ActivatedAbilityPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.ManaPoolItem;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class DranaAndLinvala extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("activated abilities of creatures your opponents control")
            .addAll(
                    CardType.CREATURE.getPredicate(),
                    TargetController.OPPONENT.getControllerPredicate()
            )
            .add(ActivatedAbilityPredicate.instance);

    public DranaAndLinvala(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{W}{W}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.VAMPIRE);
        this.subtype.add(SubType.ANGEL);
        this.power = new MageInt(3);
        this.toughness = new MageInt(4);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Vigilance
        this.addAbility(VigilanceAbility.getInstance());

        // Activated abilities of creatures your opponents control can't be activated.
        this.addAbility(new SimpleStaticAbility(new DranaAndLinvalaCantActivateEffect()));

        // Drana and Linvala has all activated abilities of all creatures your opponents control. You may spend mana as though it were mana of any color to activate those abilities.
        Ability ability = new SimpleStaticAbility(new GainAbilitiesOfEffect()
                .setAbilityFilter(filter, Zone.BATTLEFIELD)
                .modifyAbilities((newAbility) -> newAbility.getEffects().setValue("dranaLinvalaFlag", true))
                .setText("{this} has all activated abilities of all creatures your opponents control")
        );
        ability.addEffect(new DranaAndLinvalaManaEffect());
        this.addAbility(ability);
    }

    private DranaAndLinvala(final DranaAndLinvala card) {
        super(card);
    }

    @Override
    public DranaAndLinvala copy() {
        return new DranaAndLinvala(this);
    }
}

class DranaAndLinvalaCantActivateEffect extends RestrictionEffect {

    DranaAndLinvalaCantActivateEffect() {
        super(Duration.WhileOnBattlefield);
        staticText = "activated abilities of creatures your opponents control can't be activated";
    }

    private DranaAndLinvalaCantActivateEffect(final DranaAndLinvalaCantActivateEffect effect) {
        super(effect);
    }

    @Override
    public boolean applies(Permanent permanent, Ability source, Game game) {
        return permanent.isCreature(game)
                && game
                .getOpponents(source.getControllerId())
                .contains(permanent.getControllerId());
    }

    @Override
    public boolean canUseActivatedAbilities(Permanent permanent, Ability source, Game game, boolean canUseChooseDialogs) {
        return false;
    }

    @Override
    public DranaAndLinvalaCantActivateEffect copy() {
        return new DranaAndLinvalaCantActivateEffect(this);
    }
}

class DranaAndLinvalaManaEffect extends AsThoughEffectImpl implements AsThoughManaEffect {

    DranaAndLinvalaManaEffect() {
        super(AsThoughEffectType.SPEND_OTHER_MANA, Duration.WhileOnBattlefield, Outcome.Benefit);
        staticText = "You may spend mana as though it were mana of any color to activate those abilities";
    }

    private DranaAndLinvalaManaEffect(final DranaAndLinvalaManaEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return true;
    }

    @Override
    public DranaAndLinvalaManaEffect copy() {
        return new DranaAndLinvalaManaEffect(this);
    }

    @Override
    public boolean applies(UUID objectId, Ability affectedAbility, Ability source, Game game, UUID playerId) {
        return CardUtil
                .getMainCardId(game, objectId)
                .equals(source.getSourceId())
                && CardUtil
                .getEffectValueFromAbility(
                        affectedAbility, "dranaLinvalaFlag", Boolean.class
                ).orElse(false)
                && source.isControlledBy(playerId);
    }

    @Override
    public boolean applies(UUID sourceId, Ability source, UUID affectedControllerId, Game game) {
        return false;
    }

    @Override
    public ManaType getAsThoughManaType(ManaType manaType, ManaPoolItem mana, UUID affectedControllerId, Ability source, Game game) {
        return mana.getFirstAvailable();
    }
}
