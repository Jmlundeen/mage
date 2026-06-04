package org.mage.test.utils;

import mage.MageObject;
import mage.abilities.Ability;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.constants.AbilityType;
import mage.constants.CardType;
import mage.constants.TargetController;
import mage.filter.FilterTyped;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.typed.LogicalPredicate;
import mage.filter.predicate.typed.ability.IAbilityPredicate;
import mage.filter.predicate.typed.card.ICardPredicate;
import mage.game.Controllable;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.permanent.PermanentImpl;
import mage.players.Player;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.inject.Named;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for new composable filter API.
 */
public class FilterTypedTest {

    static Card mockCreatureCard = mock(CardImpl.class);
    static Card mockArtifactCard = mock(CardImpl.class);
    static Card mockLandCard = mock(CardImpl.class);
    static Card mockInstantCard = mock(CardImpl.class);
    static Permanent mockCreaturePermanent = mock(PermanentImpl.class);
    static Permanent mockArtifactPermanent = mock(PermanentImpl.class);
    static Permanent mockLandPermanent = mock(PermanentImpl.class);
    static Ability mockActivatedAbility = mock(Ability.class);
    static Ability mockTriggeredAbility = mock(Ability.class);
    static Game mockGame = mock(Game.class);
    static final ICardPredicate creatureCardPredicate = (osp, game) -> osp.getObject().getCardType(game).contains(CardType.CREATURE);
    static final IAbilityPredicate activatedAbilityPredicate = (osp, game) -> {
        boolean isActivate = osp.getObject().getAbilityType() == AbilityType.ACTIVATED_NONMANA;
        MageObject object = game.getObject(osp.getObject().getSourceId());
        return isActivate && object != null && object.getCardType(game).contains(CardType.CREATURE);
    };

    @BeforeClass
    public static void setupMocks() {
        when(mockCreatureCard.getCardType(mockGame)).thenReturn(List.of(CardType.CREATURE));
        when(mockArtifactCard.getCardType(mockGame)).thenReturn(List.of(CardType.ARTIFACT));
        when(mockLandCard.getCardType(mockGame)).thenReturn(List.of(CardType.LAND));
        when(mockInstantCard.getCardType(mockGame)).thenReturn(List.of(CardType.INSTANT));
        when(mockCreaturePermanent.getCardType(mockGame)).thenReturn(List.of(CardType.CREATURE));
        when(mockArtifactPermanent.getCardType(mockGame)).thenReturn(List.of(CardType.ARTIFACT));
        when(mockLandPermanent.getCardType(mockGame)).thenReturn(List.of(CardType.LAND));
        when(mockActivatedAbility.getAbilityType()).thenReturn(AbilityType.ACTIVATED_NONMANA);
        when(mockTriggeredAbility.getAbilityType()).thenReturn(AbilityType.TRIGGERED_NONMANA);
    }

    @Test()
    @Named("Filter uses ObjectSourcePlayer predicate against object type")
    public void test_FilterUsesObjectSourcePlayerPredicateAgainstObjectType() {
        FilterTyped filter = new FilterTyped("creature")
                .add(creatureCardPredicate);

        assertTrue(filter.match(mockCreatureCard, null, null, mockGame));
        assertFalse(filter.match(mockArtifactCard, null, null, mockGame));
        assertFalse(filter.match(mockActivatedAbility, null, null, mockGame));
    }

    @Test
    public void test_FilterHoldsSeparateTypes() {
        UUID sourceId = UUID.randomUUID();
        UUID artifactSourceId = UUID.randomUUID();
        when(mockActivatedAbility.getSourceId()).thenReturn(sourceId);
        when(mockCreaturePermanent.getCardType(mockGame)).thenReturn(List.of(CardType.CREATURE));
        when(mockGame.getObject(sourceId)).thenReturn(mockCreaturePermanent);
        FilterTyped filter = new FilterTyped("creature card or activated ability of a creature")
                .add(creatureCardPredicate)
                .add(activatedAbilityPredicate);

        assertTrue(filter.match(mockActivatedAbility, UUID.randomUUID(), null, mockGame));
        assertFalse(filter.match(mockTriggeredAbility, UUID.randomUUID(), null, mockGame));
        when(mockActivatedAbility.getSourceId()).thenReturn(artifactSourceId);
        when(mockGame.getObject(artifactSourceId)).thenReturn(mockArtifactPermanent);
        assertFalse(filter.match(mockActivatedAbility, UUID.randomUUID(), null, mockGame));
    }

    @Test
    public void test_FilterRequiresAtLeastOneCompatiblePredicate() {
        FilterTyped filter = new FilterTyped("creature")
                .add(creatureCardPredicate);

        assertTrue(filter.match(new ObjectSourcePlayer<>(mockCreaturePermanent, UUID.randomUUID(), null), mockGame));
    }

    @Test
    public void test_LogicalPredicateAndRequiresAllMixedTypesToApply() {
        UUID playerId = UUID.randomUUID();
        when(mockCreaturePermanent.isControlledBy(playerId)).thenReturn(true);

        FilterTyped filter = new FilterTyped("creature you control")
                .addAll(CardType.CREATURE.getPredicate(), TargetController.YOU.getControllerPredicate());

        assertFalse(filter.match(mockCreatureCard, playerId, null, null));
        assertTrue(filter.match(new ObjectSourcePlayer<>(mockCreaturePermanent, playerId, null), mockGame));
    }

    @Test
    public void test_LogicalPredicateOrMixedTypesUsesCompatibleBranch() {
        UUID sourceId = UUID.randomUUID();
        when(mockActivatedAbility.getSourceId()).thenReturn(sourceId);
        when(mockGame.getObject(sourceId)).thenReturn(mockCreaturePermanent);

        FilterTyped filter = new FilterTyped("creature card or activated ability of creature")
                .addAny(creatureCardPredicate, activatedAbilityPredicate);

        assertTrue(filter.match(mockCreatureCard, UUID.randomUUID(), null, mockGame));
        assertTrue(filter.match(mockActivatedAbility, UUID.randomUUID(), null, mockGame));
        assertFalse(filter.match(new ObjectSourcePlayer<>(mockArtifactPermanent, UUID.randomUUID(), null), mockGame));
    }

    @Test
    public void test_LogicalPredicateNotNeedsCompatiblePredicate() {
        FilterTyped filter = new FilterTyped("not creature card")
                .add(LogicalPredicate.not(creatureCardPredicate));

        assertFalse(filter.match(mockActivatedAbility, UUID.randomUUID(), null, mockGame));
        assertFalse(filter.match(mockCreatureCard, UUID.randomUUID(), null, mockGame));
        assertTrue(filter.match(mockArtifactCard, UUID.randomUUID(), null, mockGame));
    }

    @Test
    public void test_TargetControllerTypedOwnerPredicate() {
        UUID playerId = UUID.randomUUID();
        Card ownedCard = mock(Card.class);
        Card otherCard = mock(Card.class);
        when(ownedCard.isOwnedBy(playerId)).thenReturn(true);
        when(otherCard.isOwnedBy(playerId)).thenReturn(false);

        FilterTyped filter = new FilterTyped("owned by you")
                .add(TargetController.YOU.getOwnerPredicate());

        assertTrue(filter.match(ownedCard, playerId, null, mockGame));
        assertFalse(filter.match(otherCard, playerId, null, mockGame));
        assertTrue(TargetController.YOU.getOwnerPredicate().tryApply(new ObjectSourcePlayer<>(mockActivatedAbility, playerId, null), mockGame));
    }

    @Test
    public void test_TargetControllerTypedPlayerPredicate() {
        UUID playerId = UUID.randomUUID();
        Player you = mock(Player.class);
        Player otherPlayer = mock(Player.class);
        when(you.getId()).thenReturn(playerId);
        when(otherPlayer.getId()).thenReturn(UUID.randomUUID());

        FilterTyped filter = new FilterTyped("you")
                .add(TargetController.YOU.getPlayerPredicate());

        assertTrue(filter.match(new ObjectSourcePlayer<>(you, playerId, null), mockGame));
        assertFalse(filter.match(new ObjectSourcePlayer<>(otherPlayer, playerId, null), mockGame));
        assertTrue(TargetController.YOU.getPlayerPredicate().tryApply(new ObjectSourcePlayer<>(mockCreatureCard, playerId, null), mockGame));
    }

    @Test
    public void test_TargetControllerTypedControllerPredicate() {
        UUID playerId = UUID.randomUUID();
        Controllable controlled = mock(Controllable.class);
        Controllable notControlled = mock(Controllable.class);
        when(controlled.isControlledBy(playerId)).thenReturn(true);
        when(notControlled.isControlledBy(playerId)).thenReturn(false);

        FilterTyped filter = new FilterTyped("you control")
                .add(TargetController.YOU.getControllerPredicate());

        assertTrue(filter.match(new ObjectSourcePlayer<>(controlled, playerId, null), mockGame));
        assertFalse(filter.match(new ObjectSourcePlayer<>(notControlled, playerId, null), mockGame));
        assertTrue(TargetController.YOU.getControllerPredicate().tryApply(new ObjectSourcePlayer<>(mockCreatureCard, playerId, null), mockGame));
    }
}
