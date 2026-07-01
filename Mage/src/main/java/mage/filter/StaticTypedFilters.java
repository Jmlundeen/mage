package mage.filter;

import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.TargetController;
import mage.filter.predicate.typed.LogicalPredicate;
import mage.filter.predicate.typed.Spell.SpellPredicate;
import mage.filter.predicate.typed.ability.type.ActivatedAbilityPredicate;
import mage.filter.predicate.typed.card.CardPredicate;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;
import mage.filter.predicate.typed.mageObject.color.MultiColoredPredicate;

public class StaticTypedFilters {

    private StaticTypedFilters() {
        // prevent instantiation
    }

    public static final FilterTyped CARD = new FilterTyped("card")
            .add(CardPredicate.instance)
            .setLocked(true);

    public static final FilterTyped A_LAND = new FilterTyped("a land")
            .add(IMageObjectPredicate.getOSPPredicate(CardType.LAND.getPredicate()))
            .setLocked(true);

    public static final FilterTyped A_LAND_CARD = new FilterTyped("a land card")
            .addAll(CardPredicate.instance, IMageObjectPredicate.getOSPPredicate(CardType.LAND.getPredicate()))
            .setLocked(true);

    public static final FilterTyped A_BASIC_LAND = new FilterTyped("a basic land")
            .addAll(IMageObjectPredicate.getOSPPredicate(CardType.LAND.getPredicate()), IMageObjectPredicate.getOSPPredicate(SuperType.BASIC.getPredicate()))
            .setLocked(true);

    public static final FilterTyped A_NONBASIC_LAND = new FilterTyped("a nonbasic land")
            .addAll(IMageObjectPredicate.getOSPPredicate(CardType.LAND.getPredicate()), LogicalPredicate.not(IMageObjectPredicate.getOSPPredicate(SuperType.BASIC.getPredicate())))
            .setLocked(true);

    public static final FilterTyped LAND_YOU_CONTROL = new FilterTyped("a land you control")
            .addAll(IMageObjectPredicate.getOSPPredicate(CardType.LAND.getPredicate()), TargetController.YOU.getControllerPredicate())
            .setLocked(true);

    public static final FilterTyped BASIC_LAND_YOU_CONTROL = new FilterTyped("a basic land you control")
            .addAll(IMageObjectPredicate.getOSPPredicate(CardType.LAND.getPredicate()), IMageObjectPredicate.getOSPPredicate(SuperType.BASIC.getPredicate()), TargetController.YOU.getControllerPredicate())
            .setLocked(true);

    public static final FilterTyped LAND_AN_OPPONENT_CONTROLS = new FilterTyped("a land an opponent controls")
            .addAll(IMageObjectPredicate.getOSPPredicate(CardType.LAND.getPredicate()), TargetController.OPPONENT.getControllerPredicate())
            .setLocked(true);

    public static final FilterTyped A_SWAMP = new FilterTyped("a Swamp")
            .addAll(IMageObjectPredicate.getOSPPredicate(CardType.LAND.getPredicate()), IMageObjectPredicate.getOSPPredicate(SubType.SWAMP.getPredicate()))
            .setLocked(true);

    public static final FilterTyped SWAMP_YOU_CONTROL = new FilterTyped("a Swamp you control")
            .addAll(IMageObjectPredicate.getOSPPredicate(CardType.LAND.getPredicate()), IMageObjectPredicate.getOSPPredicate(SubType.SWAMP.getPredicate()), TargetController.YOU.getControllerPredicate())
            .setLocked(true);

    public static final FilterTyped A_FOREST = new FilterTyped("a Forest")
            .addAll(IMageObjectPredicate.getOSPPredicate(CardType.LAND.getPredicate()), IMageObjectPredicate.getOSPPredicate(SubType.FOREST.getPredicate()))
            .setLocked(true);

    public static final FilterTyped FOREST_YOU_CONTROL = new FilterTyped("a Forest you control")
            .addAll(IMageObjectPredicate.getOSPPredicate(CardType.LAND.getPredicate()), IMageObjectPredicate.getOSPPredicate(SubType.FOREST.getPredicate()), TargetController.YOU.getControllerPredicate())
            .setLocked(true);

    public static final FilterTyped AN_ISLAND = new FilterTyped("an Island")
            .addAll(IMageObjectPredicate.getOSPPredicate(CardType.LAND.getPredicate()), IMageObjectPredicate.getOSPPredicate(SubType.ISLAND.getPredicate()))
            .setLocked(true);

    public static final FilterTyped A_MOUNTAIN = new FilterTyped("a Mountain")
            .addAll(IMageObjectPredicate.getOSPPredicate(CardType.LAND.getPredicate()), IMageObjectPredicate.getOSPPredicate(SubType.MOUNTAIN.getPredicate()))
            .setLocked(true);

    public static final FilterTyped AN_INSTANT_OR_SORCERY_SPELL = new FilterTyped("an instant or sorcery spell")
            .addAll(
                    SpellPredicate.instance,
                    LogicalPredicate.or(
                            IMageObjectPredicate.getOSPPredicate(CardType.INSTANT.getPredicate()),
                            IMageObjectPredicate.getOSPPredicate(CardType.SORCERY.getPredicate())
                    )
            )
            .setLocked(true);

    public static final FilterTyped A_CREATURE_CARD = new FilterTyped("a creature card")
            .addAll(
                    CardPredicate.instance,
                    IMageObjectPredicate.getOSPPredicate(CardType.CREATURE.getPredicate()))
            .setLocked(true);

    public static final FilterTyped CREATURE_YOU_CONTROL = new FilterTyped("a creature you control")
            .addAll(
                    IMageObjectPredicate.getOSPPredicate(CardType.CREATURE.getPredicate()),
                    TargetController.YOU.getControllerPredicate())
            .setLocked(true);

    public static final FilterTyped A_CREATURE_SPELL = new FilterTyped("a creature spell")
            .addAll(
                    SpellPredicate.instance,
                    IMageObjectPredicate.getOSPPredicate(CardType.CREATURE.getPredicate())
            )
            .setLocked(true);

    public static final FilterTyped A_NON_CREATURE_SPELL = new FilterTyped("a noncreature spell")
            .addAll(
                    SpellPredicate.instance,
                    LogicalPredicate.not(IMageObjectPredicate.getOSPPredicate(CardType.CREATURE.getPredicate()))
            )
            .setLocked(true);

    public static final FilterTyped AN_ARTIFACT = new FilterTyped("an artifact")
            .add(IMageObjectPredicate.getOSPPredicate(CardType.ARTIFACT.getPredicate()))
            .setLocked(true);

    public static final FilterTyped ARTIFACT_YOU_CONTROL = new FilterTyped("an artifact you control")
            .addAll(
                    IMageObjectPredicate.getOSPPredicate(CardType.ARTIFACT.getPredicate()),
                    TargetController.YOU.getControllerPredicate())
            .setLocked(true);

    public static final FilterTyped AN_ARTIFACT_SPELL = new FilterTyped("an artifact spell")
            .addAll(
                    SpellPredicate.instance,
                    IMageObjectPredicate.getOSPPredicate(CardType.ARTIFACT.getPredicate())
            )
            .setLocked(true);

    public static final FilterTyped A_NON_ARTIFACT_SPELL = new FilterTyped("a nonartifact spell")
            .addAll(
                    SpellPredicate.instance,
                    LogicalPredicate.not(IMageObjectPredicate.getOSPPredicate(CardType.ARTIFACT.getPredicate())))
            .setLocked(true);

    public static final FilterTyped SPELL = new FilterTyped("spell")
            .add(SpellPredicate.instance)
            .setLocked(true);

    public static final FilterTyped A_SPELL = new FilterTyped("a spell")
            .add(SpellPredicate.instance)
            .setLocked(true);

    public static final FilterTyped A_MULTICOLORED_SPELL = new FilterTyped("a multicolored spell")
            .addAll(
                    SpellPredicate.instance,
                    MultiColoredPredicate.instance
            )
            .setLocked(true);

    public static final FilterTyped ACTIVATED_ABILITY = new FilterTyped("an activated ability")
            .add(ActivatedAbilityPredicate.instance)
            .setLocked(true);

    public static final FilterTyped PLAYER = new FilterTyped("player")
            .add(TargetController.ANY.getPlayerPredicate())
            .setLocked(true);

    public static final FilterTyped YOU = new FilterTyped("you")
            .add(TargetController.YOU.getPlayerPredicate())
            .setLocked(true);

    public static final FilterTyped OPPONENT = new FilterTyped("opponent")
            .add(TargetController.OPPONENT.getPlayerPredicate())
            .setLocked(true);
}
