package mage.cards;

import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.constants.*;
import mage.game.Game;

import java.util.UUID;

public class TransformingDoubleFacedCardHalf extends DoubleFacedCardHalf<TransformingDoubleFacedCard> {

    public TransformingDoubleFacedCardHalf(
            UUID ownerId, CardSetInfo setInfo,
            SuperType[] cardSuperTypes, CardType[] cardTypes, SubType[] cardSubTypes,
            String costs, TransformingDoubleFacedCard parentCard, SpellAbilityType spellAbilityType
    ) {
        super(ownerId, setInfo, cardSuperTypes, cardTypes, cardSubTypes, costs, parentCard, spellAbilityType);
    }

    protected TransformingDoubleFacedCardHalf(final TransformingDoubleFacedCardHalf card) {
        super(card);
    }

    public TransformingDoubleFacedCardHalf(
            UUID ownerId, CardSetInfo setInfo,
            SuperType[] superTypesRight, CardType[] typesRight, SubType[] subTypesRight, String colorRight, TransformingDoubleFacedCard parentCard) {
        super(ownerId, setInfo, superTypesRight, typesRight, subTypesRight, "", parentCard, SpellAbilityType.TRANSFORMED_RIGHT);
        this.getColor().setColor(new ObjectColor(colorRight));
    }

    @Override
    public boolean cast(Game game, Zone fromZone, SpellAbility ability, UUID controllerId) {
        if (ability.getSpellAbilityCastMode() == SpellAbilityCastMode.DISTURB && !isBackSide()) {
            return getOtherSide().cast(game, fromZone, ability, controllerId);
        }
        return super.cast(game, fromZone, ability, controllerId);
    }

    @Override
    public TransformingDoubleFacedCardHalf copy() {
        return new TransformingDoubleFacedCardHalf(this);
    }

    @Override
    public UUID getIdForBattlefield(Game game, Ability source) {
        Boolean enterTransformed = (Boolean) game.getState().getValue(TransformingDoubleFacedCard.VALUE_KEY_ENTER_TRANSFORMED + getId());
        if (enterTransformed == null) {
            enterTransformed = false;
        }
        if (enterTransformed && !isBackSide()) {
            return getOtherSide().getId();
        }
        if (!enterTransformed && isBackSide()) {
            return null;
        }
        return getId();
    }
}
