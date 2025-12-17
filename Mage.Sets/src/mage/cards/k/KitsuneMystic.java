package mage.cards.k;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.condition.Condition;
import mage.abilities.condition.common.EnchantedSourceCondition;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.FlipSourceEffect;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.cards.CardSetInfo;
import mage.cards.FlipCard;
import mage.constants.*;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.common.FilterEnchantmentPermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.permanent.AttachmentAttachedToCardTypePredicate;
import mage.filter.predicate.permanent.PermanentIdPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetImpl;
import mage.target.TargetPermanent;

import java.util.Optional;
import java.util.UUID;

/**
 * @author LevelX2
 */
public final class KitsuneMystic extends FlipCard {

    private static final Condition condition = new EnchantedSourceCondition(2);

    private static final FilterEnchantmentPermanent filter = new FilterEnchantmentPermanent("Aura attached to a creature");

    static {
        filter.add(new AttachmentAttachedToCardTypePredicate(CardType.CREATURE));
        filter.add(SubType.AURA.getPredicate());
    }

    public KitsuneMystic(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo,
                new CardType[]{CardType.CREATURE}, new SubType[]{SubType.FOX, SubType.WIZARD}, "{3}{W}",
                "Autumn-Tail, Kitsune Sage",
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.CREATURE}, new SubType[]{SubType.FOX, SubType.WIZARD});

        // Kitsune Mystic
        this.getLeftHalfCard().setPT(2, 3);

        // At the beginning of the end step, if Kitsune Mystic is enchanted by two or more Auras, flip it.
        this.getLeftHalfCard().addAbility(new BeginningOfEndStepTriggeredAbility(
                TargetController.NEXT, new FlipSourceEffect().setText("flip it"), false, condition
        ));

        // Autumn-Tail, Kitsune Sage
        this.getRightHalfCard().setPT(4, 5);

        // {1}: Attach target Aura attached to a creature to another creature.
        Ability ability = new SimpleActivatedAbility(new AutumnTailEffect(), new GenericManaCost(1));
        ability.addTarget(new TargetPermanent(filter));
        this.getRightHalfCard().addAbility(ability);
    }

    private KitsuneMystic(final KitsuneMystic card) {
        super(card);
    }

    @Override
    public KitsuneMystic copy() {
        return new KitsuneMystic(this);
    }
}

class AutumnTailEffect extends OneShotEffect {

    AutumnTailEffect() {
        super(Outcome.BoostCreature);
        this.staticText = "attach target Aura attached to a creature to another creature";
    }

    private AutumnTailEffect(final AutumnTailEffect effect) {
        super(effect);
    }

    @Override
    public AutumnTailEffect copy() {
        return new AutumnTailEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        Permanent aura = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (player == null || aura == null) {
            return false;
        }
        FilterPermanent filter = new FilterCreaturePermanent();
        filter.add(Predicates.not(new PermanentIdPredicate(aura.getAttachedTo())));
        if (!game.getBattlefield().contains(filter, source.getControllerId(), source, game, 1)) {
            return false;
        }
        TargetPermanent target = new TargetPermanent(filter);
        target.withNotTarget(true);
        player.choose(outcome, target, source, game);
        return Optional
                .ofNullable(target)
                .map(TargetImpl::getFirstTarget)
                .map(game::getPermanent)
                .filter(permanent -> permanent.addAttachment(aura.getId(), source, game))
                .isPresent();
    }
}
