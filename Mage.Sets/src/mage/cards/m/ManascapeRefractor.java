package mage.cards.m;

import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTappedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.AsThoughEffectImpl;
import mage.abilities.effects.AsThoughManaEffect;
import mage.abilities.effects.common.continuous.layers.L6_Abilities.GainAbilitiesOfEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.ability.type.ActivatedAbilityPredicate;
import mage.game.Game;
import mage.players.ManaPoolItem;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author AsterAether
 */
public final class ManascapeRefractor extends CardImpl {

    static final FilterTyped filter = new FilterTyped("activated ability of a land on the battlefield")
            .add(CardType.LAND.getPredicate())
            .add(ActivatedAbilityPredicate.instance);

    public ManascapeRefractor(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{3}");

        // Manascape Refractor enters the battlefield tapped.
        this.addAbility(new EntersBattlefieldTappedAbility());

        // Manascape Refractor has all activated abilities of all lands on the battlefield.
        this.addAbility(new SimpleStaticAbility(new GainAbilitiesOfEffect()
                .setAbilityFilter(filter, Zone.BATTLEFIELD)
                .setText("{this} has all activated abilities of all lands on the battlefield.")
        ));

        // You may spend mana as though it were mana of any color to pay the activation costs of Manascape Refractor's abilities.
        this.addAbility(new SimpleStaticAbility(new ManascapeRefractorSpendAnyManaEffect()));
    }

    private ManascapeRefractor(final ManascapeRefractor card) {
        super(card);
    }

    @Override
    public ManascapeRefractor copy() {
        return new ManascapeRefractor(this);
    }
}

class ManascapeRefractorSpendAnyManaEffect extends AsThoughEffectImpl implements AsThoughManaEffect {

    ManascapeRefractorSpendAnyManaEffect() {
        super(AsThoughEffectType.SPEND_OTHER_MANA, Duration.WhileOnBattlefield, Outcome.Benefit);
        staticText = "You may spend mana as though it were mana of any color to pay the activation costs of {this}'s abilities.";
    }

    private ManascapeRefractorSpendAnyManaEffect(final ManascapeRefractorSpendAnyManaEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return true;
    }

    @Override
    public ManascapeRefractorSpendAnyManaEffect copy() {
        return new ManascapeRefractorSpendAnyManaEffect(this);
    }

    @Override
    public boolean applies(UUID objectId, Ability source, UUID affectedControllerId, Game game) {
        objectId = CardUtil.getMainCardId(game, objectId); // for split cards
        return objectId.equals(source.getSourceId()) && source.isControlledBy(affectedControllerId);
    }

    @Override
    public ManaType getAsThoughManaType(ManaType manaType, ManaPoolItem mana, UUID affectedControllerId, Ability source, Game game) {
        return mana.getFirstAvailable();
    }
}
