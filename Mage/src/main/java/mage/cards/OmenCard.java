package mage.cards;

import mage.abilities.SpellAbility;
import mage.constants.CardType;
import mage.constants.SpellAbilityType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.util.CardUtil;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Jmlundeen
 */
public abstract class OmenCard extends CardWithSpellOption<OmenCardHalf, OmenCard> {

    public OmenCard(
            UUID ownerId, CardSetInfo setInfo,
            CardType[] typesLeft, SubType[] subTypesLeft, String costsLeft,
            String omenName,
            CardType[] typesRight, SubType[] subTypesRight, String costsRight
    ) {
        this(
                ownerId, setInfo,
                new SuperType[]{}, typesLeft, subTypesLeft, costsLeft,
                omenName,
                new SuperType[]{}, typesRight, subTypesRight, costsRight
        );
    }

    public OmenCard(
            UUID ownerId, CardSetInfo setInfo,
            SuperType[] superTypesLeft, CardType[] typesLeft, SubType[] subTypesLeft, String costsLeft,
            String omenName,
            SuperType[] superTypesRight,  CardType[] typesRight, SubType[] subTypesRight, String costsRight
    ) {
        super(ownerId, setInfo, typesLeft, costsLeft + costsRight, SpellAbilityType.ADVENTURE_OMEN);
        // main card name must be same as left side
        leftHalfCard = new OmenCardHalf(
                this.getOwnerId(), setInfo.copy(),
                superTypesLeft, typesLeft, subTypesLeft, costsLeft,
                this, SpellAbilityType.ADVENTURE_OMEN_LEFT
        );
        rightHalfCard = new OmenCardHalf(
                this.getOwnerId(), new CardSetInfo(omenName, setInfo),
                superTypesRight, typesRight, subTypesRight, costsRight,
                this, SpellAbilityType.ADVENTURE_OMEN_RIGHT
        );
        OmenCardSpellAbility newSpellAbility = new OmenCardSpellAbility(
                getSpellAbility(),
                rightHalfCard.getName(),
                typesLeft,
                costsLeft
        );
        this.getRightHalfCard().replaceSpellAbility(newSpellAbility);
    }

    public OmenCard(final OmenCard card) {
        super(card);
    }
}

class OmenCardSpellAbility extends SpellAbility {

    public OmenCardSpellAbility(final SpellAbility baseSpellAbility, String omenName, CardType[] cardTypes, String costs) {
        super(baseSpellAbility);
        this.setName(cardTypes, omenName, costs);
        this.setCardName(omenName);
    }

    protected OmenCardSpellAbility(final OmenCardSpellAbility ability) {
        super(ability);
    }

    public void setName(CardType[] cardTypes, String omenName, String costs) {
        this.name = "Omen "
                + Arrays.stream(cardTypes).map(CardType::toString).collect(Collectors.joining(" "))
                + " &mdash; "
                + omenName
                + " " + costs;
    }

    @Override
    public String getRule(boolean all) {
        return this.name
                + " &mdash; "
                + CardUtil.getTextWithFirstCharUpperCase(super.getRule(false)) // without cost
                + " <i>(Then shuffle this card into its owner's library.)</i>";
    }

    @Override
    public OmenCardSpellAbility copy() {
        return new OmenCardSpellAbility(this);
    }
}
