
package mage.cards.e;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.combat.CanBlockAdditionalCreatureAttachedEffect;
import mage.abilities.keyword.EquipAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.target.common.TargetControlledCreaturePermanent;

import java.util.UUID;


/**
 * @author nantuko
 */
public final class EchoCirclet extends CardImpl {

    public EchoCirclet(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ARTIFACT},"{2}");
        this.subtype.add(SubType.EQUIPMENT);

        // Equipped creature can block an additional creature each combat. (static ability of equipment, no ability that will be gained to equipped creature!)
        this.addAbility(new SimpleStaticAbility(new CanBlockAdditionalCreatureAttachedEffect(AttachmentType.EQUIPMENT)));

        // Equip {1}
        this.addAbility(new EquipAbility(Outcome.BoostCreature, new GenericManaCost(1), new TargetControlledCreaturePermanent(), false));
    }

    private EchoCirclet(final EchoCirclet card) {
        super(card);
    }

    @Override
    public EchoCirclet copy() {
        return new EchoCirclet(this);
    }
}
