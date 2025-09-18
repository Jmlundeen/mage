
package mage.abilities.effects.common.continuous;

import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.keyword.ProtectionAbility;
import mage.choices.ChoiceColor;
import mage.constants.Duration;
import mage.filter.FilterCard;
import mage.filter.predicate.mageobject.ColorPredicate;
import mage.game.Game;
import mage.players.Player;

/**
 * @author LevelX2
 */
public class GainProtectionFromColorSourceEffect extends GainAbilitySourceEffect {

    FilterCard protectionFilter;

    public GainProtectionFromColorSourceEffect(Duration duration) {
        super(new ProtectionAbility(new FilterCard()), duration);
        protectionFilter = (FilterCard) ((ProtectionAbility) ability).getFilter();
    }

    protected GainProtectionFromColorSourceEffect(final GainProtectionFromColorSourceEffect effect) {
        super(effect);
        this.protectionFilter = effect.protectionFilter.copy();
    }

    @Override
    public GainProtectionFromColorSourceEffect copy() {
        return new GainProtectionFromColorSourceEffect(this);
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        Player controller = game.getPlayer(source.getControllerId());
        if (controller != null) {
            ChoiceColor colorChoice = new ChoiceColor(true);
            colorChoice.setMessage("Choose color for protection ability");
            if (controller.choose(outcome, colorChoice, game)) {
                game.informPlayers("Chosen color: " + colorChoice.getColor());
                protectionFilter.add(new ColorPredicate(colorChoice.getColor()));
                protectionFilter.setMessage(colorChoice.getChoice());
                ((ProtectionAbility) ability).setFilter(protectionFilter);
                return;
            }
        }
        discard();
    }

    @Override
    public String getText(Mode mode) {
        return "{this} gains protection from the color of your choice " + duration.toString();
    }
}
