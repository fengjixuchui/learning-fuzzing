package net.praseodym.activelearner;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Forkserver Mealy Oracle
 * <p>
 * This is an optimisation to the normal SULOracle: instead of replaying the prefix and suffix as single steps,
 * we execute the entire prefix/suffix in one step.
 */
@ParametersAreNonnullByDefault
public class AFLMealyOracle implements MembershipOracle.MealyMembershipOracle<String, String> {

    private final Logger log = LoggerFactory.getLogger(AFLMealyOracle.class);

    @Autowired
    private AFLSUL aflSUL;

    @Override
    public void processQueries(Collection<? extends Query<String, Word<String>>> queries) {
        for (Query<String, Word<String>> q : queries) {
            Word<String> output = answerQuery(q.getPrefix(), q.getSuffix());
            q.answer(output);
        }
    }

    @Override
    public Word<String> answerQuery(Word<String> prefix, Word<String> suffix) {
        aflSUL.pre();
        byte[] prefixInput, prefixOutput, suffixInput, suffixOutput;
        if (prefix.isEmpty()) {
            prefixInput = null;
            prefixOutput = null;
        } else {
            prefixInput = concatenateWord(prefix).getBytes();
            prefixOutput = aflSUL.run(null, null, prefixInput);
        }
        suffixInput = concatenateWord(suffix).getBytes();
        suffixOutput = aflSUL.run(prefixInput, prefixOutput, suffixInput);
        aflSUL.post();

        String output = new String(suffixOutput);
        log.debug("Answered query with prefix [{}] and suffix [{}]: [{}]", prefix, suffix, output);
        return buildWord(output, suffix.length());
    }

    private String concatenateWord(Word<String> in) {
        return in.stream().collect(Collectors.joining(AFLSUL.SEPARATOR));
    }

    /**
     * Build a Word from a String. We need the output Word to be same length as the suffix, otherwise counterexample
     * finding will end up in an infinite loop because the hypothesis will never match the actual answer we give.
     */
    private Word<String> buildWord(@Nullable String in, int length) {
        WordBuilder<String> wb = new WordBuilder<>();
        if (in != null) {
            Collections.addAll(wb, in.split(AFLSUL.SEPARATOR));
        }
        while (wb.size() != length) {
            wb.add("");
        }
        return wb.toWord();
    }

}