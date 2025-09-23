package mage.cards.o;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SkipUntapOptionalAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.ObjectSourcePlayerPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.TargetPermanent;

import java.util.List;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class OldManOfTheSea extends CardImpl {

    private static final FilterPermanent filter
            = new FilterCreaturePermanent("creature with less than or equal power");

    static {
        filter.add(OldManOfTheSeaPredicate.instance);
    }

    public OldManOfTheSea(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}{U}");
        this.subtype.add(SubType.DJINN);

        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // You may choose not to untap Old Man of the Sea during your untap step.
        this.addAbility(new SkipUntapOptionalAbility());

        // {tap}: Gain control of target creature with power less than or equal to Old Man of the Sea's power for as long as Old Man of the Sea remains tapped and that creature's power remains less than or equal to Old Man of the Sea's power.
        Ability ability = new SimpleActivatedAbility(new OldManOfTheSeaEffect(), new TapSourceCost());
        ability.addTarget(new TargetPermanent(filter));
        this.addAbility(ability);
    }

    private OldManOfTheSea(final OldManOfTheSea card) {
        super(card);
    }

    @Override
    public OldManOfTheSea copy() {
        return new OldManOfTheSea(this);
    }
}

enum OldManOfTheSeaPredicate implements ObjectSourcePlayerPredicate<Permanent> {
    instance;

    @Override
    public boolean apply(ObjectSourcePlayer<Permanent> input, Game game) {
        Permanent sourcePermanent = input.getSource().getSourcePermanentOrLKI(game);
        return sourcePermanent != null
                && input.getObject().getPower().getValue() <= sourcePermanent.getPower().getValue();
    }
}

class OldManOfTheSeaEffect extends ContinuousEffectImpl {

    OldManOfTheSeaEffect() {
        super(Duration.Custom, Layer.ControlChangingEffects_2, SubLayer.NA, Outcome.GainControl);
        staticText = "gain control of target creature with power less than or equal to {this}'s power for as long as {this} remains tapped and that creature's power remains less than or equal to {this}'s power";
    }

    private OldManOfTheSeaEffect(final OldManOfTheSeaEffect effect) {
        super(effect);
    }

    @Override
    public OldManOfTheSeaEffect copy() {
        return new OldManOfTheSeaEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Permanent) object).changeControllerId(source.getControllerId(), game, source);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent sourcePermanent = source.getSourcePermanentIfItStillExists(game);
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (sourcePermanent == null || permanent == null || !sourcePermanent.isTapped() || permanent.getPower().getValue() > sourcePermanent.getPower().getValue()) {
            discard();
            return false;
        }
        affectedObjects.add(permanent);
        return true;
    }
}
