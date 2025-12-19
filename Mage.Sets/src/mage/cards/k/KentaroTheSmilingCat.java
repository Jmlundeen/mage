
package mage.cards.k;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.SourceIsSpellCondition;
import mage.abilities.costs.AlternativeCostSourceAbility;
import mage.abilities.costs.Cost;
import mage.abilities.costs.DynamicCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.BushidoAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.game.Game;
import mage.players.Player;

import java.util.List;
import java.util.UUID;

/**
 * @author LevelX2
 */
public final class KentaroTheSmilingCat extends CardImpl {

    public KentaroTheSmilingCat(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{1}{W}");
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.SAMURAI);

        this.power = new MageInt(2);
        this.toughness = new MageInt(1);

        // Bushido 1 (When this blocks or becomes blocked, it gets +1/+1 until end of turn.)
        this.addAbility(new BushidoAbility(1));
        
        // You may pay {X} rather than pay the mana cost for Samurai spells you cast, where X is that spell's converted mana cost.
        this.addAbility(new SimpleStaticAbility(new KentaroTheSmilingCatCastingEffect()));
        

    }

    private KentaroTheSmilingCat(final KentaroTheSmilingCat card) {
        super(card);
    }

    @Override
    public KentaroTheSmilingCat copy() {
        return new KentaroTheSmilingCat(this);
    }
}  

class KentaroTheSmilingCatCastingEffect extends ContinuousEffectImpl {
	
	private static final FilterCard filterSamurai = new FilterCard();
	
	static {
		filterSamurai.add(SubType.SAMURAI.getPredicate());
	}
	
    private final AlternativeCostSourceAbility alternativeCastingCostAbility = new AlternativeCostSourceAbility(
    		SourceIsSpellCondition.instance, null, filterSamurai, true, new ColorlessManaValue());
	
    public KentaroTheSmilingCatCastingEffect() {
        super(Duration.WhileOnBattlefield, Layer.RulesEffects, SubLayer.NA, Outcome.Benefit);
        staticText = "You may pay {X} rather than pay the mana cost for Samurai spells you cast, where X is that spell's mana value";
    }
    
    private KentaroTheSmilingCatCastingEffect(final KentaroTheSmilingCatCastingEffect effect) {
        super(effect);
    }

    @Override
    public KentaroTheSmilingCatCastingEffect copy() {
        return new KentaroTheSmilingCatCastingEffect(this);
    }

    @Override
    public void init(Ability source, Game game, UUID activePlayerId) {
        super.init(source, game, activePlayerId);
        alternativeCastingCostAbility.setSourceId(source.getSourceId());
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Player) object).getAlternativeSourceCosts().add(alternativeCastingCostAbility);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        affectedObjects.add(controller);
        return true;
    }
}

class ColorlessManaValue implements DynamicCost {

	@Override
	public Cost getCost(Ability ability, Game game) {
		return new GenericManaCost(ability.getManaCosts().manaValue());
	}

	@Override
	public String getText(Ability ability, Game game) {
		return "Pay " + getCost(ability, game).getText() + " rather than " + ability.getManaCosts().getText() + " for Samurai card?";
	}
}
