package com.shakev;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

public class NaiveBayes {
    private static final List<String> spamDataSetFilepaths = List.of("./spam-dataset.txt");

    private static final List<String> hamDataSetFilepaths = List.of("./ham-dataset.txt");

    private static final Map<String, Integer> spamWordOccurrencesFromDataset = new HashMap<>();

    private static final Map<String, Integer> hamWordOccurrencesFromDataset = new HashMap<>();

    private static final Map<String, Float> inputWordsSpamProbabilities = new HashMap<>();

    private static final Map<String, Float> inputWordsHamProbabilities = new HashMap<>();

    private static final Set<String> allDatasetsWords = new HashSet<>();

    private static final int DEFAULT_MINIMAL_WORD_OCCURRENCE = 1;

    // spam and ham datasets contain equal number of sample emails
    private static final float PRIOR_PROBABILITY = 0.5F;

    public static void main(String[] args) {
        readDataSetsAndCountOccurrencesOfEachWord();
        final String inputFile = askForInputFile();
        tokenizeAndFindInputTextProbabilities(fileToString(inputFile));
        decideIfSpam();
    }

    private static void decideIfSpam() {
        if (spamProbability() > hamProbability()) {
            System.out.println("This is spam!");
            return;
        }
        System.out.println("This is not spam.");
    }

    private static String askForInputFile() {
        final Scanner sc = new Scanner(System.in);
        System.out.print("Enter the name of the input file containing email content: ");
        return sc.nextLine();
    }

    private static void readDataSetsAndCountOccurrencesOfEachWord() {
        final StringBuilder text = new StringBuilder();
        for (String filepath : spamDataSetFilepaths) {
            text.append(fileToString(filepath));
            tokenizeAndFindDatasetWordsOccurrences(text.toString(), spamWordOccurrencesFromDataset);
            text.setLength(0);
        }

        for (String filepath : hamDataSetFilepaths) {
            text.append(fileToString(filepath));
            tokenizeAndFindDatasetWordsOccurrences(text.toString(), hamWordOccurrencesFromDataset);
            text.setLength(0);
        }
        setOneOccurrenceForEachWord();
    }

    private static String fileToString(final String filePath){
        final Scanner sc;
        final StringBuilder sb = new StringBuilder();
        try {
            sc = new Scanner(new File(filePath));
            while (sc.hasNextLine()) {
                sb.append(sc.nextLine());
                sb.append(" ");
            }
        }
        catch(Exception e) {
            System.out.println(e);
            System.exit(1);
        }
        return sb.toString().toLowerCase();
    }

    private static void tokenizeAndFindDatasetWordsOccurrences(String text, final Map<String, Integer> map) {
        text = text.replaceAll("[0-9]", " ");
        final String delimeter = "[\s\n,.:/?!\\]";
        final StringTokenizer tokenizer = new StringTokenizer(text, delimeter);
        final StringBuilder word = new StringBuilder();

        while(tokenizer.hasMoreTokens()) {
            word.setLength(0);
            word.append(tokenizer.nextToken());
            int occurrences = map.containsKey(word.toString()) ? map.get(word.toString()) + 1 : 1;
            map.put(word.toString(), occurrences);
        }
    }

    private static void setOneOccurrenceForEachWord() {
        allDatasetsWords.addAll(spamWordOccurrencesFromDataset.keySet());
        allDatasetsWords.addAll(hamWordOccurrencesFromDataset.keySet());

        int occurrences;

        for(String word : allDatasetsWords) {
            for(int i = 0; i < DEFAULT_MINIMAL_WORD_OCCURRENCE; i ++) {
                occurrences = spamWordOccurrencesFromDataset.containsKey(word) ? spamWordOccurrencesFromDataset.get(word) + 1 : 1;
                spamWordOccurrencesFromDataset.put(word, occurrences);

                occurrences = hamWordOccurrencesFromDataset.containsKey(word) ? hamWordOccurrencesFromDataset.get(word) + 1 : 1;
                hamWordOccurrencesFromDataset.put(word, occurrences);
            }
        }
    }

    private static float spamProbability() {
        float res = PRIOR_PROBABILITY;
        for(String word : inputWordsSpamProbabilities.keySet()) {
            res *= inputWordsSpamProbabilities.get(word);
        }
        return res;
    }

    private static float hamProbability() {
        float res = PRIOR_PROBABILITY;
        for(String word : inputWordsHamProbabilities.keySet()) {
            res *= inputWordsHamProbabilities.get(word);
        }
        return res;
    }

    private static void tokenizeAndFindInputTextProbabilities(final String text) {
        final String delimeter = "[\s\n,.:/?!\\]";
        final StringTokenizer tokenizer = new StringTokenizer(text, delimeter);
        final StringBuilder word = new StringBuilder();

        while(tokenizer.hasMoreTokens()) {
            if(allDatasetsWords.contains(tokenizer.nextToken())) {
                setProbabilitiesOfInputWords(tokenizer, word);
            }
        }
    }

    private static void setProbabilitiesOfInputWords(final StringTokenizer tokenizer, final StringBuilder word) {
        word.setLength(0);
        word.append(tokenizer.nextToken());

        doSetProbabilities(word, spamWordOccurrencesFromDataset, inputWordsSpamProbabilities);
        doSetProbabilities(word, hamWordOccurrencesFromDataset, inputWordsHamProbabilities);
    }

    private static void doSetProbabilities(final StringBuilder word,
                                           final Map<String, Integer> wordsCollection,
                                           final Map<String, Float> resultCollection) {
        if(!wordsCollection.containsKey(word.toString())) {
            return;
        }
        final float allWordOccurrences = wordsCollection
                .values()
                .stream()
                .reduce(0, Integer::sum);
        final float currWordOccurrences = wordsCollection.get(word.toString());
        final float probability = currWordOccurrences / allWordOccurrences;

        if (!resultCollection.containsKey(word.toString())) {
            resultCollection.put(word.toString(), probability);
            return;
        }
        final float currProbability = resultCollection.get(word.toString());
        resultCollection.put(word.toString(), currProbability * probability);
    }
}