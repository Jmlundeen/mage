package mage.cards.i;

import mage.MageItem;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.keyword.EnchantAbility;
import mage.abilities.mana.ColorlessManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterPermanent;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.TargetPermanent;

import java.util.List;
import java.util.UUID;

/**
 * @author fireshoes
 */
public final class ImprisonedInTheMoon extends CardImpl {

    private static final FilterPermanent filter = new FilterPermanent("creature, land, or planeswalker");

    static {
        filter.add(Predicates.or(
                CardType.CREATURE.getPredicate(),
                CardType.LAND.getPredicate(),
                CardType.PLANESWALKER.getPredicate()
        ));
    }

    public ImprisonedInTheMoon(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{2}{U}");
        this.subtype.add(SubType.AURA);

        // Enchant creature, land, or planeswalker
        TargetPermanent auraTarget = new TargetPermanent(filter);
        this.getSpellAbility().addTarget(auraTarget);
        this.getSpellAbility().addEffect(new AttachEffect(Outcome.Detriment));
        Ability ability = new EnchantAbility(auraTarget);
        this.addAbility(ability);

        // Enchanted permanent is a colorless land with "{T}: Add {C}" and loses all other card types and abilities.
        this.addAbility(new SimpleStaticAbility(new ImprisonedInTheMoonEffect()));
    }

    private ImprisonedInTheMoon(final ImprisonedInTheMoon card) {
        super(card);
    }

    @Override
    public ImprisonedInTheMoon copy() {
        return new ImprisonedInTheMoon(this);
    }
}

class ImprisonedInTheMoonEffect extends ContinuousEffectImpl {

    ImprisonedInTheMoonEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Detriment);
        this.staticText = "Enchanted permanent is a colorless land " +
                "with \"{T}: Add {C}\" and loses all other card types and abilities";
    }

    private ImprisonedInTheMoonEffect(final ImprisonedInTheMoonEffect effect) {
        super(effect);
    }

    @Override
    public ImprisonedInTheMoonEffect copy() {
        return new ImprisonedInTheMoonEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            switch (layer) {
                case TypeChangingEffects_4:
                    permanent.removeAllCardTypes(game);
                    permanent.addCardType(game, CardType.LAND);
                    permanent.retainAllLandSubTypes(game);
                    break;
                case ColorChangingEffects_5:
                    permanent.getColor(game).setWhite(false);
                    permanent.getColor(game).setBlue(false);
                    permanent.getColor(game).setBlack(false);
                    permanent.getColor(game).setRed(false);
                    permanent.getColor(game).setGreen(false);
                    break;
                case AbilityAddingRemovingEffects_6:
                    permanent.removeAllAbilities(source.getSourceId(), game);
                    permanent.addAbility(new ColorlessManaAbility(), source.getSourceId(), game);
                    break;
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent enchantment = source.getSourcePermanentIfItStillExists(game);
        if (enchantment == null
                || enchantment.getAttachedTo() == null) {
            return false;
        }
        if (layer == Layer.TypeChangingEffects_4) {
            affectedObjectList.clear();
            Permanent permanent = game.getPermanent(enchantment.getAttachedTo());
            if (permanent == null) {
                return false;
            }
            affectedObjectList.add(new MageObjectReference(permanent, game));
            affectedObjects.add(permanent);
        }
        else {
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
        return layer == Layer.AbilityAddingRemovingEffects_6
                || layer == Layer.ColorChangingEffects_5
                || layer == Layer.TypeChangingEffects_4;
    }
}
