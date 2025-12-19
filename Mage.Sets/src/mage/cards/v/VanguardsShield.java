
package mage.cards.v;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.combat.CanBlockAdditionalCreatureAttachedEffect;
import mage.abilities.effects.common.continuous.BoostEquippedEffect;
import mage.abilities.keyword.EquipAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;

import java.util.UUID;


/**
 * @author noxx
 */
public final class VanguardsShield extends CardImpl {

    public VanguardsShield(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ARTIFACT},"{2}");
        this.subtype.add(SubType.EQUIPMENT);

        // Equipped creature gets +0/+3 and can block an additional creature each combat.
        this.addAbility(new SimpleStaticAbility(new BoostEquippedEffect(0, 3)));

        // Equipped creature can block an additional creature each combat.
        this.addAbility(new SimpleStaticAbility(new CanBlockAdditionalCreatureAttachedEffect(AttachmentType.EQUIPMENT)));

        // Equip {3}
        this.addAbility(new EquipAbility(Outcome.AddAbility, new GenericManaCost(3)));
    }

    private VanguardsShield(final VanguardsShield card) {
        super(card);
    }

    @Override
    public VanguardsShield copy() {
        return new VanguardsShield(this);
    }
}
