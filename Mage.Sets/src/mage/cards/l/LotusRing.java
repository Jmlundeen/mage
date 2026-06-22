package mage.cards.l;

import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.SacrificeSourceCost;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.common.continuous.generic.GenericContinuousEffect;
import mage.abilities.keyword.EquipAbility;
import mage.abilities.keyword.IndestructibleAbility;
import mage.abilities.keyword.VigilanceAbility;
import mage.abilities.mana.AnyColorManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class LotusRing extends CardImpl {

    public LotusRing(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{3}");

        this.subtype.add(SubType.EQUIPMENT);

        // Indestructible
        this.addAbility(IndestructibleAbility.getInstance());

        // Equipped creature gets +3/+3 and has vigilance and "{T}, Sacrifice this creature: Add three mana of any one color."
        Ability manaAbility = new AnyColorManaAbility(3);
        manaAbility.addCost(new SacrificeSourceCost().setText("sacrifice this creature"));
        ContinuousEffect effect = new GenericContinuousEffect(Duration.WhileOnBattlefield, Outcome.BoostCreature)
                .setAffected(ContinuousAffected.ATTACHED_TO)
                .withAddPower(3)
                .withAddToughness(3)
                .withGainedAbilities(VigilanceAbility.getInstance(), manaAbility)
                .setText("Equipped creature gets +3/+3 and has vigilance and \"{T}, Sacrifice this creature: Add three mana of any one color.\"");
        this.addAbility(new SimpleStaticAbility(effect));

        // Equip {3}
        this.addAbility(new EquipAbility(3, false));
    }

    private LotusRing(final LotusRing card) {
        super(card);
    }

    @Override
    public LotusRing copy() {
        return new LotusRing(this);
    }
}
