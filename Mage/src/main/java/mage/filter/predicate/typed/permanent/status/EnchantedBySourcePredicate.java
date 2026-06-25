package mage.filter.predicate.typed.permanent.status;

import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.typed.permanent.IPermanentPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;

public enum EnchantedBySourcePredicate implements IPermanentPredicate {
    instance;

    @Override
    public boolean apply(ObjectSourcePlayer<Permanent> input, Game game) {
        Permanent enchantment = game.getPermanent(input.getSourceId());
        if (enchantment == null || !enchantment.isEnchantment(game)) {
            return false;
        }
        return enchantment.getAttachedTo() != null && enchantment.getAttachedTo().equals(input.getObject().getId());
    }
}
