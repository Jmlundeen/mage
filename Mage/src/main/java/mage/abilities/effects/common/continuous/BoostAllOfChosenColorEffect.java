package mage.abilities.effects.common.continuous;

import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.common.ChooseColorEffect;
import mage.constants.Duration;
import mage.filter.common.FilterCreaturePermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;

/**
 * @author LevelX2
 */
public class BoostAllOfChosenColorEffect extends BoostAllEffect {

    private ObjectColor color = null;

    public BoostAllOfChosenColorEffect(int power, int toughness, Duration duration, boolean excludeSource) {
        super(power, toughness, duration, new FilterCreaturePermanent("creatures of the chosen color"), excludeSource);
    }

    public BoostAllOfChosenColorEffect(int power, int toughness, Duration duration, FilterCreaturePermanent filter, boolean excludeSource) {
        super(power, toughness, duration, filter, excludeSource);
    }

    public BoostAllOfChosenColorEffect(DynamicValue power, DynamicValue toughness, Duration duration, FilterCreaturePermanent filter, boolean excludeSource) {
        super(power, toughness, duration, filter, excludeSource);
    }

    protected BoostAllOfChosenColorEffect(final BoostAllOfChosenColorEffect effect) {
        super(effect);
        this.color = effect.color;
    }

    @Override
    public BoostAllOfChosenColorEffect copy() {
        return new BoostAllOfChosenColorEffect(this);
    }

    @Override
    protected boolean selectedByRuntimeData(Permanent permanent, Ability source, Game game) {
        if (color != null) {
            return permanent.getColor(game).contains(color);
        }
        return false;
    }

    @Override
    protected void setRuntimeData(Ability source, Game game) {
        ObjectColor chosenColor = ChooseColorEffect.getChosenColor(source.getSourceId(), game);
        if (chosenColor != null) {
            color = chosenColor;
        } else {
            discard();
        }
    }
}
