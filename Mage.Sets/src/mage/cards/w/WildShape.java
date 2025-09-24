package mage.cards.w;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.HexproofAbility;
import mage.abilities.keyword.ReachAbility;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetControlledCreaturePermanent;

import java.util.List;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class WildShape extends CardImpl {

    public WildShape(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{G}");

        // Choose one. Until end of turn, target creature you control has that base power and toughness, becomes that creature type, and gains that ability.
        this.getSpellAbility().getModes().setChooseText(
                "Choose one. Until end of turn, target creature you control has that base power and toughness, " +
                        "becomes that creature type, and gains that ability."
        );

        // • 1/3 Turtle with hexproof.
        this.getSpellAbility().addEffect(new WildShapeEffect(
                1, 3, SubType.TURTLE, HexproofAbility.getInstance()
        ));
        this.getSpellAbility().addTarget(new TargetControlledCreaturePermanent());

        // • 1/5 Spider with reach.
        Mode mode = new Mode(new WildShapeEffect(
                1, 5, SubType.SPIDER, ReachAbility.getInstance()
        ));
        mode.addTarget(new TargetControlledCreaturePermanent());
        this.getSpellAbility().addMode(mode);

        // • 3/3 Elephant with trample.
        mode = new Mode(new WildShapeEffect(
                3, 3, SubType.ELEPHANT, TrampleAbility.getInstance()
        ));
        mode.addTarget(new TargetControlledCreaturePermanent());
        this.getSpellAbility().addMode(mode);
    }

    private WildShape(final WildShape card) {
        super(card);
    }

    @Override
    public WildShape copy() {
        return new WildShape(this);
    }
}

class WildShapeEffect extends ContinuousEffectImpl {

    private final int power;
    private final int toughness;
    private final SubType subType;
    private final Ability ability;

    WildShapeEffect(int power, int toughness, SubType subType, Ability ability) {
        super(Duration.EndOfTurn, Outcome.Benefit);
        this.power = power;
        this.toughness = toughness;
        this.subType = subType;
        this.ability = ability;
        staticText = power + "/" + toughness + " " + subType.getDescription() + " with " + ability.getRule();
    }

    private WildShapeEffect(final WildShapeEffect effect) {
        super(effect);
        this.power = effect.power;
        this.toughness = effect.toughness;
        this.subType = effect.subType;
        this.ability = effect.ability;
    }

    @Override
    public WildShapeEffect copy() {
        return new WildShapeEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            switch (layer) {
                case TypeChangingEffects_4:
                    permanent.removeAllCreatureTypes(game);
                    permanent.addSubType(game, SubType.HUMAN);
                    break;
                case AbilityAddingRemovingEffects_6:
                    permanent.addAbility(ability, source.getSourceId(), game);
                    break;
                case PTChangingEffects_7:
                    if (sublayer == SubLayer.SetPT_7b) {
                        permanent.getPower().setModifiedBaseValue(power);
                        permanent.getToughness().setModifiedBaseValue(toughness);
                        break;
                    }
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent == null) {
            discard();
            return false;
        }
        affectedObjects.add(permanent);
        return true;
    }

    @Override
    public boolean hasLayer(Layer layer) {
        switch (layer) {
            case TypeChangingEffects_4:
            case AbilityAddingRemovingEffects_6:
            case PTChangingEffects_7:
                return true;
        }
        return false;
    }

    @Override
    public boolean hasSubLayer(SubLayer sublayer) {
        return sublayer == SubLayer.SetPT_7b || sublayer == SubLayer.NA;
    }
}
