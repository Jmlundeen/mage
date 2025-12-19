package mage.cards.t;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldControlledTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.AddContinuousEffectToGame;
import mage.abilities.effects.common.continuous.BoostEquippedEffect;
import mage.abilities.keyword.EquipAbility;
import mage.abilities.mana.ColorlessManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;
import java.util.UUID;

/**
 * @author Susucr
 */
public final class TheIrencrag extends CardImpl {

    private static final FilterPermanent filter = new FilterControlledCreaturePermanent("a legendary creature");

    static {
        filter.add(SuperType.LEGENDARY.getPredicate());
        filter.add(TargetController.YOU.getControllerPredicate());
    }

    public TheIrencrag(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{2}");

        this.supertype.add(SuperType.LEGENDARY);

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // Whenever a legendary creature you control enters, you may have The Irencrag become a legendary Equipment artifact named Everflame, Heroes' Legacy. If you do, it gains equip {3} and "Equipped creature gets +3/+3" and loses all other abilities.
        this.addAbility(new EntersBattlefieldControlledTriggeredAbility(
                Zone.BATTLEFIELD, new AddContinuousEffectToGame(new TheIrencragBecomesContinuousEffect()),
                filter, true, SetTargetPointer.NONE
        ));
    }

    private TheIrencrag(final TheIrencrag card) {
        super(card);
    }

    @Override
    public TheIrencrag copy() {
        return new TheIrencrag(this);
    }
}

class TheIrencragBecomesContinuousEffect extends ContinuousEffectImpl {

    TheIrencragBecomesContinuousEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Benefit);
        staticText = "{this} become a legendary Equipment artifact named Everflame, Heroes' Legacy. "
                + "If you do, it gains equip {3} and \"Equipped creature gets +3/+3\" and loses all other abilities";
        dependencyTypes.add(DependencyType.ArtifactAddingRemoving);
    }

    protected TheIrencragBecomesContinuousEffect(final TheIrencragBecomesContinuousEffect effect) {
        super(effect);
    }

    @Override
    public TheIrencragBecomesContinuousEffect copy() {
        return new TheIrencragBecomesContinuousEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            switch (layer) {
                case TextChangingEffects_3:
                    permanent.setName("Everflame, Heroes' Legacy");
                    break;
                case TypeChangingEffects_4:
                    permanent.removeAllCardTypes(game);
                    permanent.addSuperType(game, SuperType.LEGENDARY);
                    permanent.addCardType(game, CardType.ARTIFACT);
                    permanent.retainAllArtifactSubTypes(game);
                    permanent.addSubType(game, SubType.EQUIPMENT);
                    break;
                case AbilityAddingRemovingEffects_6:
                    permanent.removeAllAbilities(source.getSourceId(), game);
                    permanent.addAbility(
                            new EquipAbility(3, false),
                            source.getSourceId(), game
                    );
                    permanent.addAbility(
                            new SimpleStaticAbility(new BoostEquippedEffect(3, 3)),
                            source.getSourceId(), game
                    );
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = game.getPermanent(source.getSourceId());
        if (permanent == null) {
            this.discard();
            return false;
        }
        affectedObjects.add(permanent);
        return true;
    }

    @Override
    public boolean hasLayer(Layer layer) {
        return layer == Layer.TextChangingEffects_3
                || layer == Layer.TypeChangingEffects_4
                || layer == Layer.AbilityAddingRemovingEffects_6;
    }
}
