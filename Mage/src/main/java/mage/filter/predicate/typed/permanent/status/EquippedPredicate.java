package mage.filter.predicate.typed.permanent.status;

import mage.constants.SubType;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.typed.permanent.IPermanentPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;

public enum EquippedPredicate implements IPermanentPredicate {
    instance;

    @Override
    public boolean apply(ObjectSourcePlayer<Permanent> input, Game game) {
        return input.getObject().getAttachments()
                .stream()
                .map(game::getPermanent)
                .anyMatch(permanent -> permanent != null && permanent.hasSubtype(SubType.EQUIPMENT, game));
    }
}
