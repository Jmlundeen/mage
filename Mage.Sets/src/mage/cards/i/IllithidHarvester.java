package mage.cards.i;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DontUntapInControllersNextUntapStepTargetEffect;
import mage.abilities.effects.common.TapTargetEffect;
import mage.abilities.effects.common.continuous.BecomesFaceDownCreatureEffect;
import mage.cards.AdventureCard;
import mage.cards.CardSetInfo;
import mage.cards.ModalDoubleFacedCardHalf;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.permanent.TappedPredicate;
import mage.filter.predicate.permanent.TokenPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.permanent.PermanentCard;
import mage.target.Target;
import mage.target.TargetPermanent;
import mage.target.common.TargetCreaturePermanent;
import mage.target.targetadjustment.XTargetsCountAdjuster;

import java.util.UUID;

/**
 * @author PurpleCrowbar
 */
public final class IllithidHarvester extends AdventureCard {

    private static final FilterPermanent filter = new FilterCreaturePermanent("tapped nontoken creatures");

    static {
        filter.add(TappedPredicate.TAPPED);
        filter.add(TokenPredicate.FALSE);
    }

    public IllithidHarvester(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, new CardType[]{CardType.SORCERY}, "{4}{U}", "Plant Tadpoles", "{X}{U}{U}");
        this.subtype.add(SubType.HORROR);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // Ceremorphosis — When Illithid Harvester enters the battlefield, turn any number
        // of target tapped nontoken creatures face down. They're 2/2 Horror creatures.
        Ability ability = new EntersBattlefieldTriggeredAbility(new IllithidHarvesterEffect());
        ability.addTarget(new TargetPermanent(0, Integer.MAX_VALUE, filter));
        this.addAbility(ability.withFlavorWord("Ceremorphosis"));

        // Plant Tadpoles
        // Tap X target creatures. They don't untap during their controllers' next untap steps.
        this.getSpellCard().getSpellAbility().addEffect(new TapTargetEffect("tap X target creatures"));
        this.getSpellCard().getSpellAbility().addEffect(new DontUntapInControllersNextUntapStepTargetEffect()
                .setText("They don't untap during their controllers' next untap steps"));
        this.getSpellCard().getSpellAbility().addTarget(new TargetCreaturePermanent());
        this.getSpellCard().getSpellAbility().setTargetAdjuster(new XTargetsCountAdjuster());

        this.finalizeAdventure();
    }

    private IllithidHarvester(final IllithidHarvester card) {
        super(card);
    }

    @Override
    public IllithidHarvester copy() {
        return new IllithidHarvester(this);
    }
}

class IllithidHarvesterEffect extends OneShotEffect {

    IllithidHarvesterEffect() {
        super(Outcome.Detriment);
        this.staticText = "turn any number of target tapped nontoken creatures face down. They're 2/2 Horror creatures";
    }

    private IllithidHarvesterEffect(final IllithidHarvesterEffect effect) {
        super(effect);
    }

    @Override
    public IllithidHarvesterEffect copy() {
        return new IllithidHarvesterEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        for (Target target : source.getTargets()) {
            for (UUID targetId : target.getTargets()) {
                Permanent permanent = game.getPermanent(targetId);
                if (!permanent.isFaceDown() && !permanent.isTransformable() && !(((PermanentCard) permanent).getCard() instanceof ModalDoubleFacedCardHalf)) {
                    BecomesFaceDownCreatureEffect.FaceDownType type = BecomesFaceDownCreatureEffect.findFaceDownType(game, permanent);
                    BecomesFaceDownCreatureEffect.makeFaceDownObject(game, source.getSourceId(), permanent, type, null);
                    permanent.setFaceDown(true);
                    permanent.getFaceDownValues().getSubtype().add(SubType.HORROR);
                }
            }
        }
        return true;
    }
}
