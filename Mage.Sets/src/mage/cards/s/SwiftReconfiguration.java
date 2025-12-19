package mage.cards.s;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.keyword.CrewAbility;
import mage.abilities.keyword.EnchantAbility;
import mage.abilities.keyword.FlashAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.TargetPermanent;

import java.util.List;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class SwiftReconfiguration extends CardImpl {

    public SwiftReconfiguration(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{W}");

        this.subtype.add(SubType.AURA);

        // Flash
        this.addAbility(FlashAbility.getInstance());

        // Enchant creature or Vehicle
        TargetPermanent auraTarget = new TargetPermanent(StaticFilters.FILTER_PERMANENT_CREATURE_OR_VEHICLE);
        this.getSpellAbility().addTarget(auraTarget);
        this.getSpellAbility().addEffect(new AttachEffect(Outcome.BoostCreature));
        this.addAbility(new EnchantAbility(auraTarget));

        // Enchanted permanent is a Vehicle artifact with crew 5 and it loses all other card types.
        this.addAbility(new SimpleStaticAbility(new SwiftReconfigurationEffect()));
    }

    private SwiftReconfiguration(final SwiftReconfiguration card) {
        super(card);
    }

    @Override
    public SwiftReconfiguration copy() {
        return new SwiftReconfiguration(this);
    }
}

class SwiftReconfigurationEffect extends ContinuousEffectImpl {

    SwiftReconfigurationEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Detriment);
        staticText = "enchanted permanent is a Vehicle artifact with crew 5 and it loses all other card types";
    }

    private SwiftReconfigurationEffect(final SwiftReconfigurationEffect effect) {
        super(effect);
    }

    @Override
    public SwiftReconfigurationEffect copy() {
        return new SwiftReconfigurationEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            switch (layer) {
                case TypeChangingEffects_4:
                    permanent.removeAllCardTypes(game);
                    permanent.addCardType(game, CardType.ARTIFACT);
                    permanent.addSubType(game, SubType.VEHICLE);
                    break;
                case AbilityAddingRemovingEffects_6:
                    permanent.addAbility(new CrewAbility(5), source.getSourceId(), game);
                    break;
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent aura = source.getSourcePermanentIfItStillExists(game);
        if (aura == null) {
            return false;
        }
        Permanent permanent = game.getPermanent(aura.getAttachedTo());
        if (permanent != null) {
            affectedObjects.add(permanent);
            return true;
        }
        return false;
    }

    @Override
    public boolean hasLayer(Layer layer) {
        return layer == Layer.TypeChangingEffects_4
                || layer == Layer.AbilityAddingRemovingEffects_6;
    }
}
