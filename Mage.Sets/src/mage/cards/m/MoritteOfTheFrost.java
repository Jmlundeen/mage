package mage.cards.m;

import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.CopyPermanentEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.ChangelingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterControlledPermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.util.functions.CopyApplier;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class MoritteOfTheFrost extends CardImpl {

    private static final FilterPermanent filter = new FilterControlledPermanent("a permanent you control");

    public MoritteOfTheFrost(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}{U}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.supertype.add(SuperType.SNOW);
        this.subtype.add(SubType.SHAPESHIFTER);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // Changeling
        this.addAbility(new ChangelingAbility());

        // You may have Moritte of the Frost enter the battlefield as a copy of a permanent you control, except it's legendary and snow in addition to its other types and, if it's a creature, it enters with two additional +1/+1 counters on it and has changeling.
        this.addAbility(new EntersBattlefieldAbility(new CopyPermanentEffect(filter, new MoritteOfTheFrostCopyApplier()), true));
    }

    private MoritteOfTheFrost(final MoritteOfTheFrost card) {
        super(card);
    }

    @Override
    public MoritteOfTheFrost copy() {
        return new MoritteOfTheFrost(this);
    }
}

class MoritteOfTheFrostCopyApplier extends CopyApplier {

    @Override
    public String getText() {
        return ", except it's legendary and snow in addition to its other types and, if it's a creature, "
                + "it enters with two additional +1/+1 counters on it and has changeling";
    }

    @Override
    public boolean apply(Game game, MageObject blueprint, Ability source, UUID copyToObjectId) {
        blueprint.addSuperType(SuperType.LEGENDARY);
        blueprint.addSuperType(SuperType.SNOW);
        return true;
    }

    @Override
    public void applyExceptions(Game game, Permanent targetPermanent, Ability source) {
        if (targetPermanent.isCreature(game)) {
            targetPermanent.addAbility(new ChangelingAbility(), source.getSourceId(), game);
            targetPermanent.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.P1P1.createInstance(2))), source.getSourceId(), game);
        }
    }
}
