package mage.cards.j;

import mage.MageInt;
import mage.MageItem;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.DoubleStrikeAbility;
import mage.abilities.keyword.TrampleAbility;
import mage.abilities.keyword.VigilanceAbility;
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
public final class JetmirNexusOfRevels extends CardImpl {

    public JetmirNexusOfRevels(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{R}{G}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.CAT);
        this.subtype.add(SubType.DEMON);
        this.power = new MageInt(5);
        this.toughness = new MageInt(4);

        // Creatures you control get +1/+0 and have vigilance as long as you control three or more creatures.
        // Creatures you control also get +1/+0 and have trample as long as you control six or more creatures.
        // Creatures you control also get +1/+0 and have double strike as long as you control nine or more creatures.
        this.addAbility(new SimpleStaticAbility(new JetmirNexusOfRevelsEffect()));
    }

    private JetmirNexusOfRevels(final JetmirNexusOfRevels card) {
        super(card);
    }

    @Override
    public JetmirNexusOfRevels copy() {
        return new JetmirNexusOfRevels(this);
    }
}

class JetmirNexusOfRevelsEffect extends ContinuousEffectImpl {

    JetmirNexusOfRevelsEffect() {
        super(Duration.WhileOnBattlefield, Outcome.BoostCreature);
        staticText = "Creatures you control get +1/+0 and have vigilance as long as you control three or more creatures.<br>" +
                "Creatures you control also get +1/+0 and have trample as long as you control six or more creatures.<br>" +
                "Creatures you control also get +1/+0 and have double strike as long as you control nine or more creatures.";
    }

    private JetmirNexusOfRevelsEffect(final JetmirNexusOfRevelsEffect effect) {
        super(effect);
    }

    @Override
    public JetmirNexusOfRevelsEffect copy() {
        return new JetmirNexusOfRevelsEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            int level = Math.min(affectedObjects.size() / 3, 3);
            switch (layer) {
                case AbilityAddingRemovingEffects_6:
                    permanent.addAbility(VigilanceAbility.getInstance(), source.getSourceId(), game);
                    if (level > 1) {
                        permanent.addAbility(TrampleAbility.getInstance(), source.getSourceId(), game);
                    }
                    if (level > 2) {
                        permanent.addAbility(DoubleStrikeAbility.getInstance(), source.getSourceId(), game);
                    }
                    break;
                case PTChangingEffects_7:
                    if (sublayer == SubLayer.ModifyPT_7c) {
                        permanent.addPower(level);
                    }
                    break;
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        if (layer == Layer.AbilityAddingRemovingEffects_6) {
            List<Permanent> permanents = game
                    .getBattlefield()
                    .getActivePermanents(
                            StaticFilters.FILTER_CONTROLLED_CREATURE,
                            source.getControllerId(), source, game
                    );
            int level = Math.min(permanents.size() / 3, 3);
            if (level < 1) {
                return false;
            }
            affectedObjects.addAll(permanents);
            permanents.forEach(perm -> affectedObjectList.add(new MageObjectReference(perm, game)));
        } else {
            for (MageObjectReference mor : affectedObjectList) {
                Permanent permanent = mor.getPermanent(game);
                if (permanent != null) {
                    affectedObjects.add(permanent);
                }
            }
        }
        return !affectedObjects.isEmpty();
    }

    @Override
    public boolean hasLayer(Layer layer) {
        return layer == Layer.PTChangingEffects_7
                || layer == Layer.AbilityAddingRemovingEffects_6;
    }

    @Override
    public boolean hasSubLayer(SubLayer sublayer) {
        return sublayer == SubLayer.ModifyPT_7c
                || sublayer == SubLayer.NA;
    }
}
