package mage.cards;

import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.constants.*;
import mage.game.Game;

import java.util.Arrays;
import java.util.UUID;

public class FlipCardHalfImpl extends CardPart<FlipCard> implements FlipCardHalf {

    public FlipCardHalfImpl(
            UUID ownerId, CardSetInfo setInfo,
            SuperType[] cardSuperTypes, CardType[] cardTypes, SubType[] cardSubTypes,
            String costs, FlipCard parentCard, SpellAbilityType spellAbilityType) {
        super(ownerId, setInfo, cardTypes, costs, parentCard, spellAbilityType);
        this.supertype.addAll(Arrays.asList(cardSuperTypes));
        this.subtype.addAll(Arrays.asList(cardSubTypes));
    }

    protected FlipCardHalfImpl(final FlipCardHalfImpl card) {
        super(card);
    }

    @Override
    public FlipCardHalfImpl copy() {
        return new FlipCardHalfImpl(this);
    }

    @Override
    public void setZone(Zone zone, Game game) {
        game.setZone(getParentCard().getId(), zone);
        game.setZone(this.getId(), zone);

        Card otherSide = getOtherSide();

        switch (zone) {
            case STACK:
            case BATTLEFIELD:
                game.setZone(otherSide.getId(), Zone.OUTSIDE);
                break;
            default:
                game.setZone(otherSide.getId(), zone);
                break;
        }
        getParentCard().checkGoodZones(game);
    }

    @Override
    public boolean cast(Game game, Zone fromZone, SpellAbility ability, UUID controllerId) {
        if (getId().equals(getParentCard().getRightHalfCard().getId())) {
            throw new IllegalStateException("Wrong code usage: You can't cast the flip side of a flip card");
        }
        return super.cast(game, fromZone, ability, controllerId);
    }

    @Override
    public UUID getIdForBattlefield(Game game, Ability source) {
        if (getId().equals(getParentCard().getRightHalfCard().getId())) {
            return null;
        }
        return getId();
    }
}
