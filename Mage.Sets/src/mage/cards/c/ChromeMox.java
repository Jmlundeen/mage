package mage.cards.c;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.dynamicvalue.common.StaticValue;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.providers.ManaTypeProvider;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.game.MoveCardsParameters;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetCard;
import mage.util.CardUtil;
import mage.util.GameLog;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Plopman
 */
public final class ChromeMox extends CardImpl {

    public ChromeMox(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{0}");

        // Imprint - When Chrome Mox enters the battlefield, you may exile a nonartifact, nonland card from your hand.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new ChromeMoxEffect(), true).setAbilityWord(AbilityWord.IMPRINT));

        // {T}: Add one mana of any of the exiled card's colors.
        this.addAbility(new ComposedManaAbilityBuilder()
                .addDynamicChoice(StaticValue.get(1), ChromeMoxManaTypes.instance)
                .cost(new TapSourceCost())
                .ruleText("Add one mana of any of the exiled card's colors")
                .build()
        );
    }

    private ChromeMox(final ChromeMox card) {
        super(card);
    }

    @Override
    public ChromeMox copy() {
        return new ChromeMox(this);
    }
}

class ChromeMoxEffect extends OneShotEffect {

    private static final FilterCard filter = new FilterCard("nonartifact, nonland card");

    static {
        filter.add(Predicates.not(Predicates.or(CardType.LAND.getPredicate(), CardType.ARTIFACT.getPredicate())));
    }

    public ChromeMoxEffect() {
        super(Outcome.Benefit);
        staticText = "exile a nonartifact, nonland card from your hand";
    }

    private ChromeMoxEffect(final ChromeMoxEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        MageObject sourceObject = source.getSourceObject(game);
        if (controller != null && sourceObject != null) {
            TargetCard target = new TargetCard(Zone.HAND, filter);
            target.withNotTarget(true);
            Card cardToImprint = null;
            Permanent sourcePermanent = game.getPermanent(source.getSourceId());
            if (!controller.getHand().isEmpty() && controller.choose(Outcome.Benefit, target, source, game)) {
                cardToImprint = controller.getHand().get(target.getFirstTarget(), game);
            }
            if (sourcePermanent != null) {
                if (cardToImprint != null) {
                    MoveCardsParameters parameters = new MoveCardsParameters(cardToImprint, Zone.EXILED)
                            .setExileId(CardUtil.getExileZoneId(game, source))
                            .setExileName(CardUtil.createObjectRelatedWindowTitle(source, game, " (Imprint)"));
                    controller.moveCards(parameters, source, game);
                    sourcePermanent.imprint(cardToImprint.getId(), game);
                    sourcePermanent.addInfo("imprint", CardUtil.addToolTipMarkTags("[Imprinted card - " + GameLog.getColoredObjectIdNameForTooltip(cardToImprint) + ']'), game);
                } else {
                    sourcePermanent.addInfo("imprint", CardUtil.addToolTipMarkTags("[Imprinted card - None]"), game);
                }
            }
            return true;

        }
        return false;
    }

    @Override
    public ChromeMoxEffect copy() {
        return new ChromeMoxEffect(this);
    }

}

enum ChromeMoxManaTypes implements ManaTypeProvider {
    instance;

    @Override
    public Set<ManaType> getManaTypes(Game game, Ability source, Effect effect) {
        if (game == null) {
            return Collections.emptySet();
        }
        Permanent permanent = game.getPermanent(source.getSourceId());
        if (permanent == null || permanent.getImprinted().isEmpty()) {
            return EnumSet.noneOf(ManaType.class);
        }
        Card imprintedCard = game.getCard(permanent.getImprinted().getFirst());
        return imprintedCard == null
                ? EnumSet.noneOf(ManaType.class)
                : ManaType.getManaTypesFromObjectColor(imprintedCard.getColor(game));
    }
}
