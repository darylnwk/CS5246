package processor;

public class Processor {
    /**
     * This function uses Bernoulli model
     * @param tokens
     */
    public void naiveBayes(String[] tokens) {

    }

    /**
     * This function negates all tokens following a "not"
     * @param tokens
     */
    public void negationHandling(String[] tokens) {
        boolean negate = false;
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equalsIgnoreCase("not")) {
                negate = !negate;
                continue;
            }

            if (negate) {
                tokens[i] = negate(tokens[i]);
            }
        }
    }

    private String negate(String token) {
        return "not_" + token;
    }
}
