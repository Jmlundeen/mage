package mage.cards.g;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.continuous.generic.GenericContinuousEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticTypedFilters;
import mage.game.permanent.token.TreasureToken;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class GalazethPrismari extends CardImpl {

    public GalazethPrismari(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ELDER);
        this.subtype.add(SubType.DRAGON);
        this.power = new MageInt(3);
        this.toughness = new MageInt(4);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // When Galazeth Prismari enters the battlefield, create a Treasure token.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new CreateTokenEffect(new TreasureToken())));

        // Artifacts you control have "{T}: Add one mana of any color. Spend this mana only to cast an instant or sorcery spell."
        Ability manaAbility = ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addAnyColor(1)
                .condition(new FilteredSpellManaCondition(StaticTypedFilters.AN_INSTANT_OR_SORCERY_SPELL))
                .ruleText("Add one mana of any color. Spend this mana only to cast an instant or sorcery spell")
                .build();
        this.addAbility(new SimpleStaticAbility(new GenericContinuousEffect(Duration.WhileOnBattlefield, Outcome.AddAbility, StaticTypedFilters.ARTIFACT_YOU_CONTROL, Zone.BATTLEFIELD)
                .withGainedAbilities(manaAbility)
                .setText("Artifacts you control have \"{T}: Add one mana of any color. Spend this mana only to cast an instant or sorcery spell.\"")
        ));
    }

    private GalazethPrismari(final GalazethPrismari card) {
        super(card);
    }

    @Override
    public GalazethPrismari copy() {
        return new GalazethPrismari(this);
    }
}
