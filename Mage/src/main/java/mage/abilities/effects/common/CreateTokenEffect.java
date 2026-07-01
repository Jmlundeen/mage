package mage.abilities.effects.common;

import mage.abilities.Ability;
import mage.abilities.DelayedTriggeredAbility;
import mage.abilities.common.delayed.AtTheBeginOfNextEndStepDelayedTriggeredAbility;
import mage.abilities.common.delayed.AtTheEndOfCombatDelayedTriggeredAbility;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.StaticValue;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.constants.Outcome;
import mage.constants.PhaseStep;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.Token;
import mage.target.targetpointer.FixedTargets;
import mage.util.CardUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class CreateTokenEffect extends OneShotEffect {

    @FunctionalInterface
    public interface MakeTokenFunction {
        List<Token> apply(Game game, Ability source, Effect effect);
    }

    private final List<Token> tokens = new ArrayList<>();
    private final DynamicValue amount;
    private final boolean tapped;
    private final boolean attacking;
    private String additionalRules;
    private boolean oldPhrasing = false; // true for "token. It has " instead of "token with "
    private List<UUID> lastAddedTokenIds = new ArrayList<>();
    private CounterType counterType;
    private DynamicValue numberOfCounters;
    private MakeTokenFunction makeTokenFunction;

    public CreateTokenEffect(MakeTokenFunction makeTokenFunction) {
        this(null, StaticValue.get(1), false, false, makeTokenFunction);
    }

    public CreateTokenEffect(Token token) {
        this(token, StaticValue.get(1));
    }

    public CreateTokenEffect(Token token, int amount) {
        this(token, StaticValue.get(amount));
    }

    public CreateTokenEffect(Token token, DynamicValue amount) {
        this(token, amount, false, false, null);
    }

    public CreateTokenEffect(Token token, int amount, boolean tapped) {
        this(token, amount, tapped, false);
    }

    public CreateTokenEffect(Token token, int amount, boolean tapped, boolean attacking) {
        this(token, StaticValue.get(amount), tapped, attacking, null);
    }

    public CreateTokenEffect(Token token, DynamicValue amount, boolean tapped, boolean attacking) {
        this(token, amount, tapped, attacking, null);
    }

    public CreateTokenEffect(Token token, DynamicValue amount, boolean tapped, boolean attacking, MakeTokenFunction makeTokenFunction) {
        super(Outcome.PutCreatureInPlay);
        if (token == null && makeTokenFunction == null) {
            throw new IllegalArgumentException("Wrong code usage. Token provided to CreateTokenEffect must not be null.");
        }
        if (token != null) {
            this.tokens.add(token);
        }
        this.makeTokenFunction = makeTokenFunction;
        this.amount = amount.copy();
        this.tapped = tapped;
        this.attacking = attacking;
        setText();
    }

    protected CreateTokenEffect(final CreateTokenEffect effect) {
        super(effect);
        this.amount = effect.amount.copy();
        for (Token token : effect.tokens) {
            this.tokens.add(token.copy());
        }
        this.tapped = effect.tapped;
        this.attacking = effect.attacking;
        this.lastAddedTokenIds.addAll(effect.lastAddedTokenIds);
        this.counterType = effect.counterType;
        this.numberOfCounters = effect.numberOfCounters;
        this.additionalRules = effect.additionalRules;
        this.oldPhrasing = effect.oldPhrasing;
        this.makeTokenFunction = effect.makeTokenFunction;
    }

    public CreateTokenEffect entersWithCounters(CounterType counterType, DynamicValue numberOfCounters) {
        this.counterType = counterType;
        this.numberOfCounters = numberOfCounters;
        return this;
    }

    public CreateTokenEffect withAdditionalTokens(Token... tokens) {
        this.tokens.addAll(Arrays.asList(tokens));
        setText();
        return this;
    }

    public CreateTokenEffect withMakeTokenFunction(MakeTokenFunction makeTokenFunction) {
        this.makeTokenFunction = makeTokenFunction;
        return this;
    }

    @Override
    public CreateTokenEffect copy() {
        return new CreateTokenEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        int value = amount.calculate(game, source, this);
        List<Token> tokens = new ArrayList<>(this.tokens);
        if (makeTokenFunction != null) {
            tokens.addAll(makeTokenFunction.apply(game, source, this));
        }
        tokens.getFirst().putOntoBattlefield(value, game, source, source.getControllerId(), tapped, attacking, null, null, true, tokens);
        this.lastAddedTokenIds = tokens.getFirst().getLastAddedTokenIds();
        // TODO: Workaround to add counters to all created tokens, necessary for correct interactions with cards like Chatterfang, Squirrel General and Ochre Jelly / Printlifter Ooze. See #10786
        if (counterType != null) {
            for (UUID tokenId : lastAddedTokenIds) {
                Permanent tokenPermanent = game.getPermanent(tokenId);
                if (tokenPermanent != null) {
                    game.addEnterWithCounters(tokenPermanent.getId(), counterType.createInstance(numberOfCounters.calculate(game, source, this)));
                }
            }
        }

        return true;
    }

    public List<UUID> getLastAddedTokenIds() {
        return lastAddedTokenIds;
    }

    public void exileTokensCreatedAtNextEndStep(Game game, Ability source) {
        removeTokensCreatedAt(game, source, true, PhaseStep.END_TURN, TargetController.ANY);
    }

    public void removeTokensCreatedAt(Game game, Ability source, boolean exile, PhaseStep phaseStep, TargetController targetController) {
        List<Permanent> permanents = this.getLastAddedTokenIds()
                .stream()
                .map(game::getPermanent)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Effect effect = exile
                ? new ExileTargetEffect(null, "", Zone.BATTLEFIELD).setText("exile those tokens")
                : new SacrificeTargetEffect("sacrifice those tokens", source.getControllerId());
        effect.setTargetPointer(new FixedTargets(permanents, game));

        DelayedTriggeredAbility exileAbility;
        switch (phaseStep) {
            case END_TURN:
                exileAbility = new AtTheBeginOfNextEndStepDelayedTriggeredAbility(effect, targetController);
                break;
            case END_COMBAT:
                exileAbility = new AtTheEndOfCombatDelayedTriggeredAbility(effect);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported PhaseStep in CreateTokenEffect::removeTokensCreatedAt");
        }

        game.addDelayedTriggeredAbility(exileAbility, source);
    }

    /**
     * For adding reminder text to the effect
     */
    public CreateTokenEffect withAdditionalRules(String additionalRules) {
        this.additionalRules = additionalRules;
        setText();
        return this;
    }

    /**
     * For older cards that use the phrasing "token. It has " rather than "token with ".
     */
    public CreateTokenEffect withTextOptions(boolean oldPhrasing) {
        this.oldPhrasing = oldPhrasing;
        setText();
        return this;
    }

    private void setText() {
        boolean singular = amount.toString().equals("1");
        StringBuilder sb = new StringBuilder("create ");
        for (int i = 0; i < tokens.size(); i++) {
            if (i > 0) {
                if (tokens.size() > 2) {
                    sb.append(", ");
                } else {
                    sb.append(" ");
                }
                if (i+1 == tokens.size()) {
                    sb.append("and ");
                }
            }
            String tokenDescription = tokens.get(i).getDescription();
            if (tokenDescription.contains(", a legendary")) {
                sb.append(tokenDescription);
                continue;
            }
            if (oldPhrasing) {
                tokenDescription = tokenDescription.replace("token with \"",
                        singular ? "token. It has \"" : "tokens. They have \"");
            }
            if (singular) {
                if (tapped && !attacking) {
                    sb.append("a tapped ");
                    sb.append(tokenDescription);
                } else {
                    sb.append(CardUtil.addArticle(tokenDescription));
                }
            } else {
                sb.append(CardUtil.numberToText(amount.toString())).append(' ');
                if (tapped && !attacking) {
                    sb.append("tapped ");
                }
                sb.append(tokenDescription);
                if (tokenDescription.endsWith("token")) {
                    sb.append("s");
                }
                int tokenLocation = sb.indexOf("token ");
                if (tokenLocation != -1) {
                    sb.replace(tokenLocation, tokenLocation + 6, "tokens ");
                }
            }
        }

        if (attacking) {
            if (singular && tokens.size() == 1) {
                sb.append(" that's");
            } else {
                sb.append(" that are");
            }
            if (tapped) {
                sb.append(" tapped and");
            }
            sb.append(" attacking");
        }

        String message = amount.getMessage();
        if (!message.isEmpty()) {
            if (amount.toString().equals("X")) {
                if (sb.toString().endsWith(".\"")) {
                    sb.replace(sb.length() - 2, sb.length(), ",\" where X is ");
                } else {
                    sb.append(", where X is ");
                }
            } else {
                sb.append(" for each ");
            }
        }
        sb.append(message);

        if (this.additionalRules != null) {
            sb.append(this.additionalRules);
        }

        staticText = sb.toString();
    }
}
