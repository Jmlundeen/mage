package mage.cards.t;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.effects.common.DontUntapInControllersUntapStepEnchantedEffect;
import mage.abilities.keyword.EnchantAbility;
import mage.abilities.keyword.FlashAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterPermanent;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.TargetPermanent;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author weirddan455
 */
public final class TamiyosCompleation extends CardImpl {

    private static final FilterPermanent filter = new FilterPermanent("artifact, creature, or planeswalker");

    static {
        filter.add(Predicates.or(CardType.ARTIFACT.getPredicate(), CardType.CREATURE.getPredicate(), CardType.PLANESWALKER.getPredicate()));
    }

    public TamiyosCompleation(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{U}");

        this.subtype.add(SubType.AURA);

        // Flash
        this.addAbility(FlashAbility.getInstance());

        // Enchant artifact, creature, or planeswalker
        TargetPermanent auraTarget = new TargetPermanent(filter);
        this.getSpellAbility().addTarget(auraTarget);
        this.getSpellAbility().addEffect(new AttachEffect(Outcome.LoseAbility));
        this.addAbility(new EnchantAbility(auraTarget));

        // When Tamiyo's Compleation enters the battlefield, tap enchanted permanent. If it's an Equipment, unattach it.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new TamiyosCompleationTapEffect()));

        // Enchanted permanent loses all abilities and doesn't untap during its controller's untap step.
        Ability ability = new SimpleStaticAbility(new TamiyosCompleationLoseAbilitiesEffect());
        ability.addEffect(new DontUntapInControllersUntapStepEnchantedEffect().setText("and doesn't untap during its controller's untap step"));
        this.addAbility(ability);
    }

    private TamiyosCompleation(final TamiyosCompleation card) {
        super(card);
    }

    @Override
    public TamiyosCompleation copy() {
        return new TamiyosCompleation(this);
    }
}

class TamiyosCompleationTapEffect extends OneShotEffect {

    TamiyosCompleationTapEffect() {
        super(Outcome.Tap);
        this.staticText = "tap enchanted permanent. If it's an Equipment, unattach it";
    }

    private TamiyosCompleationTapEffect(final TamiyosCompleationTapEffect effect) {
        super(effect);
    }

    @Override
    public TamiyosCompleationTapEffect copy() {
        return new TamiyosCompleationTapEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent enchantment = source.getSourcePermanentOrLKI(game);
        if (enchantment != null) {
            Permanent enchanted = game.getPermanent(enchantment.getAttachedTo());
            if (enchanted != null) {
                enchanted.tap(source, game);
                if (enchanted.hasSubtype(SubType.EQUIPMENT, game)) {
                    Permanent creature = game.getPermanent(enchanted.getAttachedTo());
                    if (creature != null) {
                        creature.removeAttachment(enchanted.getId(), source, game);
                    }
                }
                return true;
            }
        }
        return false;
    }
}

class TamiyosCompleationLoseAbilitiesEffect extends ContinuousEffectImpl {

    TamiyosCompleationLoseAbilitiesEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.LoseAbility);
        this.staticText = "Enchanted permanent loses all abilities";
    }

    private TamiyosCompleationLoseAbilitiesEffect(final TamiyosCompleationLoseAbilitiesEffect effect) {
        super(effect);
    }

    @Override
    public TamiyosCompleationLoseAbilitiesEffect copy() {
        return new TamiyosCompleationLoseAbilitiesEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Permanent) object).removeAllAbilities(source.getSourceId(), game);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent enchantment = game.getPermanent(source.getSourceId());
        if (enchantment == null || enchantment.getAttachedTo() == null) {
            return false;
        }
        Permanent enchanted = game.getPermanent(enchantment.getAttachedTo());
        if (enchanted == null) {
            return false;
        } else {
            affectedObjects.add(enchanted);
            return true;
        }
    }
}
