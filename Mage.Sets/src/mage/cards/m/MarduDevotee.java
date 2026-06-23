package mage.cards.m;

import mage.MageInt;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.keyword.ScryEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ManaType;
import mage.constants.SubType;

import java.util.Set;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class MarduDevotee extends CardImpl {

    public MarduDevotee(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{W}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.SCOUT);
        this.power = new MageInt(1);
        this.toughness = new MageInt(2);

        // When this creature enters, scry 2.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new ScryEffect(2)));

        // {1}: Add {R}, {W}, or {B}. Activate only once each turn.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new GenericManaCost(1))
                .addChoice(Set.of(ManaType.RED, ManaType.WHITE, ManaType.BLACK), 1)
                .maxActivations(1)
                .ruleText("Add {R}, {W}, or {B}. Activate only once each turn.")
                .build()
        );
    }

    private MarduDevotee(final MarduDevotee card) {
        super(card);
    }

    @Override
    public MarduDevotee copy() {
        return new MarduDevotee(this);
    }
}
