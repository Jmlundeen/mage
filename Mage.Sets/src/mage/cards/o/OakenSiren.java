package mage.cards.o;

import mage.MageInt;
import mage.Mana;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.VigilanceAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.SpendOrActivateManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.StaticTypedFilters;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class OakenSiren extends CardImpl {

    public OakenSiren(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{1}{U}");

        this.subtype.add(SubType.SIREN);
        this.subtype.add(SubType.PIRATE);
        this.power = new MageInt(1);
        this.toughness = new MageInt(2);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Vigilance
        this.addAbility(VigilanceAbility.getInstance());

        // {T}: Add {U}. Spend this mana only to cast an artifact spell or activate an ability of an artifact source.
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addStatic(Mana.BlueMana(1))
                .condition(new SpendOrActivateManaCondition(StaticTypedFilters.AN_ARTIFACT))
                .ruleText("Add {U}. Spend this mana only to cast an artifact spell or activate an ability of an artifact source.")
                .build()
        );
    }

    private OakenSiren(final OakenSiren card) {
        super(card);
    }

    @Override
    public OakenSiren copy() {
        return new OakenSiren(this);
    }
}
