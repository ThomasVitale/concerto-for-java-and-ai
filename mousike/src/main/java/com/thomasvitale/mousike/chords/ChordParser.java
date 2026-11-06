package com.thomasvitale.mousike.chords;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses chord symbols ("Dm", "Bbmaj7", "F#dim", "Gsus4", "C/E")
 * and roman numerals relative to a key ("i", "bVI", "III", "VII", "iv7", "V7", "ii°", "bII").
 * Unknown qualities fail loudly: the AI's chord has to be real.
 */
public final class ChordParser {

    private static final Pattern ROMAN = Pattern.compile("^([#♯b♭]?)([ivIV]+)(°|o|dim|\\+|aug)?(maj7|M7|Δ7|m7b5|ø7?|dim7|7sus4|7|9|6|sus2|sus4|add9)?$");
    private static final Pattern ABSOLUTE = Pattern.compile("^([A-Ga-g][#♯b♭]*)(.*?)(?:/([A-Ga-g][#♯b♭]*))?$");

    private static final Map<String, List<Integer>> QUALITIES = Map.ofEntries(
            Map.entry("", List.of(0, 4, 7)),
            Map.entry("maj", List.of(0, 4, 7)),
            Map.entry("major", List.of(0, 4, 7)),
            Map.entry("m", List.of(0, 3, 7)),
            Map.entry("min", List.of(0, 3, 7)),
            Map.entry("minor", List.of(0, 3, 7)),
            Map.entry("-", List.of(0, 3, 7)),
            Map.entry("dim", List.of(0, 3, 6)),
            Map.entry("°", List.of(0, 3, 6)),
            Map.entry("o", List.of(0, 3, 6)),
            Map.entry("aug", List.of(0, 4, 8)),
            Map.entry("+", List.of(0, 4, 8)),
            Map.entry("7", List.of(0, 4, 7, 10)),
            Map.entry("dom7", List.of(0, 4, 7, 10)),
            Map.entry("maj7", List.of(0, 4, 7, 11)),
            Map.entry("M7", List.of(0, 4, 7, 11)),
            Map.entry("Δ7", List.of(0, 4, 7, 11)),
            Map.entry("m7", List.of(0, 3, 7, 10)),
            Map.entry("min7", List.of(0, 3, 7, 10)),
            Map.entry("-7", List.of(0, 3, 7, 10)),
            Map.entry("m7b5", List.of(0, 3, 6, 10)),
            Map.entry("ø", List.of(0, 3, 6, 10)),
            Map.entry("ø7", List.of(0, 3, 6, 10)),
            Map.entry("dim7", List.of(0, 3, 6, 9)),
            Map.entry("°7", List.of(0, 3, 6, 9)),
            Map.entry("sus2", List.of(0, 2, 7)),
            Map.entry("sus4", List.of(0, 5, 7)),
            Map.entry("7sus4", List.of(0, 5, 7, 10)),
            Map.entry("add9", List.of(0, 4, 7, 14)),
            Map.entry("madd9", List.of(0, 3, 7, 14)),
            Map.entry("6", List.of(0, 4, 7, 9)),
            Map.entry("m6", List.of(0, 3, 7, 9)),
            Map.entry("9", List.of(0, 4, 7, 10, 14)),
            Map.entry("m9", List.of(0, 3, 7, 10, 14)),
            Map.entry("maj9", List.of(0, 4, 7, 11, 14)),
            Map.entry("5", List.of(0, 7)));

    private static final List<String> LEAD_SHEET_QUALITIES = List.of("", "m", "dim", "aug", "7", "maj7", "m7", "m7b5", "dim7",
            "sus2", "sus4", "7sus4", "add9", "madd9", "6", "m6", "9", "m9", "maj9", "5");

    private ChordParser() {}

    public static List<Chord> parseAll(List<String> symbols, Key key) {
        return symbols.stream().map(symbol -> parse(symbol, key)).toList();
    }

    public static Chord parse(String symbol, Key key) {
        String text = symbol.strip().replace("–", "-");
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Empty chord symbol");
        }
        Matcher roman = ROMAN.matcher(text);
        if (roman.matches()) {
            return fromRoman(text, roman, key);
        }
        Matcher absolute = ABSOLUTE.matcher(text);
        if (absolute.matches()) {
            int root = PitchClass.parse(absolute.group(1));
            String quality = absolute.group(2).strip();
            Integer bass = absolute.group(3) == null ? null : PitchClass.parse(absolute.group(3));
            return new Chord(text, root, quality(quality, text), bass);
        }
        throw new IllegalArgumentException("Cannot read chord symbol: " + symbol);
    }

    private static Chord fromRoman(String text, Matcher matcher, Key key) {
        String accidental = matcher.group(1);
        String numeral = matcher.group(2);
        String triadMark = matcher.group(3);
        String extension = matcher.group(4);
        int degree = switch (numeral.toLowerCase(Locale.ROOT)) {
            case "i" -> 0; case "ii" -> 1; case "iii" -> 2; case "iv" -> 3; case "v" -> 4; case "vi" -> 5; case "vii" -> 6;
            default -> throw new IllegalArgumentException("Not a roman numeral: " + numeral);
        };
        // A bare numeral names a degree of the key: ii-V-I in C minor is Ddim G Cm, whatever the case it was written in
        if (accidental.isEmpty() && triadMark == null && extension == null) {
            return key.diatonic(text, degree);
        }
        int root = key.degreePitchClass(degree);
        if (accidental.equals("#") || accidental.equals("♯")) {
            root = (root + 1) % 12;
        }
        else if (accidental.equals("b") || accidental.equals("♭")) {
            root = Math.floorMod(root - 1, 12);
        }
        boolean minor = numeral.equals(numeral.toLowerCase(Locale.ROOT));
        String quality;
        if (triadMark != null && (triadMark.equals("°") || triadMark.equals("o") || triadMark.equals("dim"))) {
            quality = extension != null && extension.equals("7") ? "dim7" : "dim";
            if (extension != null && (extension.equals("ø") || extension.equals("ø7") || extension.equals("m7b5"))) {
                quality = "m7b5";
            }
        }
        else if (triadMark != null) {
            quality = "aug";
        }
        else if (extension == null) {
            quality = minor ? "m" : "";
        }
        else {
            quality = switch (extension) {
                case "7" -> minor ? "m7" : "7";
                case "9" -> minor ? "m9" : "9";
                case "6" -> minor ? "m6" : "6";
                case "add9" -> minor ? "madd9" : "add9";
                case "maj7", "M7", "Δ7" -> "maj7";
                case "m7b5", "ø", "ø7" -> "m7b5";
                case "dim7" -> "dim7";
                default -> extension;
            };
        }
        return new Chord(text, root, quality(quality, text), null);
    }

    /** How a lead sheet writes these intervals, or the plain triad they start from when no symbol covers them. */
    static String quality(List<Integer> intervals) {
        return LEAD_SHEET_QUALITIES.stream()
                .filter(quality -> QUALITIES.get(quality).equals(intervals))
                .findFirst()
                .orElseGet(() -> intervals.size() > 3 ? quality(intervals.subList(0, 3)) : "");
    }

    private static List<Integer> quality(String quality, String symbol) {
        List<Integer> intervals = QUALITIES.get(quality);
        if (intervals == null) {
            intervals = QUALITIES.get(quality.toLowerCase(Locale.ROOT));
        }
        if (intervals == null) {
            intervals = altered(quality, symbol);
        }
        if (intervals == null) {
            throw new IllegalArgumentException("Unknown chord quality '" + quality + "' in " + symbol);
        }
        return intervals;
    }

    private static final Pattern ALTERATION = Pattern.compile("^(.*?)([b♭#♯]?)(5|9|11|13)$");

    /**
     * Tensions on top of a known quality: "7b9", "m7b9", "7#11", "maj7#11", "7b13", "13". A flat or sharp fifth
     * replaces the fifth; the others add a note above the chord. Unaltered 9, 11 and 13 on a bare root imply the
     * dominant seventh, as on a lead sheet.
     */
    private static @Nullable List<Integer> altered(String quality, String symbol) {
        Matcher matcher = ALTERATION.matcher(quality);
        if (!matcher.matches()) {
            return null;
        }
        String base = matcher.group(1);
        String accidental = matcher.group(2);
        int degree = Integer.parseInt(matcher.group(3));
        if (base.isEmpty() && accidental.isEmpty() && degree != 5) {
            base = "7";
        }
        if (base.isEmpty() && degree == 5) {
            return null;
        }
        List<Integer> below;
        try {
            below = quality(base, symbol);
        }
        catch (IllegalArgumentException ex) {
            return null;
        }
        int shift = accidental.isEmpty() ? 0 : accidental.equals("b") || accidental.equals("♭") ? -1 : 1;
        int interval = switch (degree) {
            case 5 -> 7 + shift;
            case 9 -> 14 + shift;
            case 11 -> 17 + shift;
            default -> 21 + shift;
        };
        List<Integer> tones = new ArrayList<>(below);
        if (degree == 5) {
            tones.remove(Integer.valueOf(7));
        }
        if (!tones.contains(interval)) {
            tones.add(interval);
        }
        return List.copyOf(tones);
    }

}
