package mage.abilities.mana;

import mage.abilities.Abilities;
import mage.abilities.Ability;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.StaticValue;
import mage.abilities.effects.mana.ManaEffect;
import mage.abilities.mana.providers.ManaTypeProvider;
import mage.constants.ManaType;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterLandPermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * @author LevelX2
 */
public class AnyColorLandsProduceManaAbility extends ActivatedManaAbilityImpl {

    public AnyColorLandsProduceManaAbility(TargetController targetController) {
        this(targetController, true);
    }

    public AnyColorLandsProduceManaAbility(TargetController targetController, boolean onlyColors) {
        this(targetController, onlyColors, null);
    }

    public AnyColorLandsProduceManaAbility(TargetController targetController, boolean onlyColors, FilterPermanent filter) {
        this(targetController, onlyColors, filter, StaticValue.get(1), null);
    }

    public AnyColorLandsProduceManaAbility(TargetController targetController, boolean onlyColors,
                                           DynamicValue amount, String ruleText) {
        this(targetController, onlyColors, null, amount, ruleText);
    }

    public AnyColorLandsProduceManaAbility(TargetController targetController, boolean onlyColors,
                                           FilterPermanent filter, DynamicValue amount, String ruleText) {
        super(Zone.BATTLEFIELD, createEffect(targetController, onlyColors, filter, amount, ruleText), new TapSourceCost());
    }

    protected AnyColorLandsProduceManaAbility(final AnyColorLandsProduceManaAbility ability) {
        super(ability);
    }

    @Override
    public AnyColorLandsProduceManaAbility copy() {
        return new AnyColorLandsProduceManaAbility(this);
    }

    @Override
    public boolean definesMana(Game game) {
        return true;
    }

    private static ManaEffect createEffect(TargetController targetController, boolean onlyColors,
                                           FilterPermanent filter, DynamicValue amount, String ruleText) {
        FilterPermanent actualFilter = filter == null ? new FilterLandPermanent() : filter.copy();
        actualFilter.add(targetController.getControllerPredicate());
        String text = targetController == TargetController.OPPONENT ? "an opponent controls" : "you control";
        return new ComposedManaAbilityBuilder()
                .addDynamicChoice(amount, new AnyColorLandsProduceManaTypeProvider(actualFilter, onlyColors))
                .ruleText(ruleText != null
                        ? ruleText
                        : "Add one mana of any " + (onlyColors ? "color" : "type") + " that a "
                        + (filter == null ? "land " : filter.getMessage() + " ") + text + " could produce")
                .buildEffect();
    }

    public static Set<ManaType> getManaTypesFromPermanent(Permanent permanent, Game game) {
        Set<ManaType> allTypes = new HashSet<>(6);
        if (permanent != null) {
            Abilities<ActivatedManaAbilityImpl> manaAbilities = permanent.getAbilities().getActivatedManaAbilities(Zone.BATTLEFIELD);
            for (ActivatedManaAbilityImpl ability : manaAbilities) {
                allTypes.addAll(ability.getProducableManaTypes(game));
            }
        }
        return allTypes;
    }
}

class AnyColorLandsProduceManaTypeProvider implements ManaTypeProvider {

    private final FilterPermanent filter;
    private final boolean onlyColors;
    private transient boolean inManaTypeCalculation;

    AnyColorLandsProduceManaTypeProvider(FilterPermanent filter, boolean onlyColors) {
        this.filter = filter.copy();
        this.onlyColors = onlyColors;
    }

    private AnyColorLandsProduceManaTypeProvider(final AnyColorLandsProduceManaTypeProvider effect) {
        this.filter = effect.filter.copy();
        this.onlyColors = effect.onlyColors;
    }

    @Override
    public Set<ManaType> getManaTypes(Game game, Ability source, mage.abilities.effects.Effect effect) {
        Set<ManaType> types = EnumSet.noneOf(ManaType.class);
        if (game == null || game.getPhase() == null) {
            return types;
        }
        if (inManaTypeCalculation) { // Stop endless loops
            return types;
        }
        inManaTypeCalculation = true;
        try {
            for (Permanent land : game.getBattlefield().getActivePermanents(filter, source.getControllerId(), source, game)) {
                if (!land.getId().equals(source.getSourceId())) {
                    types.addAll(AnyColorLandsProduceManaAbility.getManaTypesFromPermanent(land, game));
                }
            }
        } finally {
            inManaTypeCalculation = false;
        }

        if (onlyColors) {
            types.remove(ManaType.COLORLESS);
        }

        return types;
    }

    @Override
    public AnyColorLandsProduceManaTypeProvider copy() {
        return new AnyColorLandsProduceManaTypeProvider(this);
    }

}
