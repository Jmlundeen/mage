package mage.cards.o;

import mage.MageItem;
import mage.MageObjectReference;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.common.SpellCastOpponentTriggeredAbility;
import mage.abilities.condition.common.SourceIsEnchantmentCondition;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.ProtectionAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.stack.Spell;

import java.util.List;
import java.util.UUID;

/**
 * @author jeffwadsworth
 */
public final class OpalTitan extends CardImpl {

    public OpalTitan(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{2}{W}{W}");

        // When an opponent casts a creature spell, if Opal Titan is an enchantment, Opal Titan becomes a 4/4 Giant creature with protection from each of that spell's colors.
        this.addAbility(new SpellCastOpponentTriggeredAbility(
                Zone.BATTLEFIELD, new OpalTitanBecomesCreatureEffect(),
                StaticFilters.FILTER_SPELL_A_CREATURE, false, SetTargetPointer.SPELL
        ).withInterveningIf(SourceIsEnchantmentCondition.instance)
                .setTriggerPhrase("When an opponent casts a creature spell, "));
    }

    private OpalTitan(final OpalTitan card) {
        super(card);
    }

    @Override
    public OpalTitan copy() {
        return new OpalTitan(this);
    }
}

class OpalTitanBecomesCreatureEffect extends ContinuousEffectImpl {

    OpalTitanBecomesCreatureEffect() {
        super(Duration.WhileOnBattlefield, Outcome.BecomeCreature);
        staticText = "it becomes a 4/4 Giant creature with protection from each of that spell's colors.";
        this.addDependencyType(DependencyType.BecomeCreature);
    }

    private OpalTitanBecomesCreatureEffect(final OpalTitanBecomesCreatureEffect effect) {
        super(effect);
    }

    @Override
    public OpalTitanBecomesCreatureEffect copy() {
        return new OpalTitanBecomesCreatureEffect(this);
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        affectedObjectList.add(new MageObjectReference(source.getSourceId(), game));
        Spell creatureSpellCast = game.getSpell(getTargetPointer().getFirst(game, source));
        if (creatureSpellCast != null
                && creatureSpellCast.getColor(game).hasColor()) {
            game.getState().setValue("opalTitanColor" + source.getSourceId(), creatureSpellCast.getColor(game));
        }
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            switch (layer) {
                case TypeChangingEffects_4:
                    permanent.removeAllCardTypes(game);
                    permanent.addCardType(game, CardType.CREATURE);
                    permanent.removeAllSubTypes(game);
                    permanent.addSubType(game, SubType.GIANT);
                    break;
                case AbilityAddingRemovingEffects_6:
                    if (game.getState().getValue("opalTitanColor" + source.getSourceId()) != null) {
                        for (ObjectColor color : ((ObjectColor) game.getState().getValue("opalTitanColor" + source.getSourceId())).getColors()) {
                            if (!permanent.getAbilities().contains(ProtectionAbility.from(color))) {
                                permanent.addAbility(ProtectionAbility.from(color), source.getSourceId(), game);
                            }
                        }
                    }
                    break;
                case PTChangingEffects_7:
                    if (sublayer == SubLayer.SetPT_7b) {
                        permanent.getPower().setModifiedBaseValue(4);
                        permanent.getToughness().setModifiedBaseValue(4);
                    }
                    break;
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageObjectReference mor : affectedObjectList) {
            Permanent permanent = mor.getPermanent(game);
            if (permanent != null) {
                affectedObjects.add(permanent);
            }
        }
        if (affectedObjects.isEmpty()) {
            discard();
        }
        return !affectedObjects.isEmpty();
    }

    @Override
    public boolean hasLayer(Layer layer) {
        return layer == Layer.PTChangingEffects_7
                || layer == Layer.AbilityAddingRemovingEffects_6
                || layer == Layer.TypeChangingEffects_4;
    }

    @Override
    public boolean hasSubLayer(SubLayer sublayer) {
        return sublayer == SubLayer.NA || sublayer == SubLayer.SetPT_7b;
    }
}
