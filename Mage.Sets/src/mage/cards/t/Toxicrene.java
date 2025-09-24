package mage.cards.t;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.DeathtouchAbility;
import mage.abilities.keyword.ReachAbility;
import mage.abilities.mana.AnyColorManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class Toxicrene extends CardImpl {

    public Toxicrene(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{G}");

        this.subtype.add(SubType.TYRANID);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // Reach
        this.addAbility(ReachAbility.getInstance());

        // Deathtouch
        this.addAbility(DeathtouchAbility.getInstance());

        // Hypertoxic Miasma -- All lands have "{T}: Add one mana of any color" and lose all other abilities.
        this.addAbility(new SimpleStaticAbility(new ToxicreneEffect()).withFlavorWord("Hypertoxic Miasma"));
    }

    private Toxicrene(final Toxicrene card) {
        super(card);
    }

    @Override
    public Toxicrene copy() {
        return new Toxicrene(this);
    }
}

class ToxicreneEffect extends ContinuousEffectImpl {

    ToxicreneEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.LoseAbility);
        staticText = "all lands have \"{T}: Add one mana of any color\" and lose all other abilities";
    }

    private ToxicreneEffect(final ToxicreneEffect effect) {
        super(effect);
    }

    @Override
    public ToxicreneEffect copy() {
        return new ToxicreneEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            permanent.removeAllAbilities(source.getSourceId(), game);
            permanent.addAbility(new AnyColorManaAbility(), source.getSourceId(), game);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        affectedObjects.addAll(game.getBattlefield().getActivePermanents(
                StaticFilters.FILTER_LAND, source.getControllerId(), source, game
        ));
        return !affectedObjects.isEmpty();
    }
}
