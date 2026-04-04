package utils.io.text_formatting;

import utils.helpers.Functions;

import java.util.*;

public class FormattedText {

    // ─── Properties ─── //

    private final List<Segment> segments;

    // ─── Constructors ─── //

    public FormattedText(List<Segment> segments) {
        this.segments = new ArrayList<>(segments);
    }

    public FormattedText(String rawText, TextFormattingOptions formatting) {
        this.segments = new ArrayList<>();
        this.segments.add(new Segment(rawText, formatting));
    }

    // ─── Getters ─── //

    public List<Segment> getSegments() {
        return Collections.unmodifiableList(this.segments);
    }

    // ─── Utility methods ─── //

    public int visibleLength() {
        int length = 0;

        for (Segment segment : this.segments) {
            length += Functions.visibleLength(segment.rawText());
        }

        return length;
    }

    public FormattedText withMergedFormatting(TextFormattingOptions outerFormatting) {
        List<Segment> mergedSegments = new ArrayList<>();

        for (Segment segment : this.segments) {
            TextFormattingOptions mergedFormatting = new TextFormattingOptions(segment.formatting());
            mergedFormatting.mergeWith(outerFormatting);
            mergedSegments.add(new Segment(segment.rawText(), mergedFormatting));
        }

        return new FormattedText(mergedSegments);
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        for (Segment segment : this.segments) {
            String escapeSequence = segment.formatting().buildEscapeSequence();
            String resetSequence = segment.formatting().buildResetSequence();

            if (!escapeSequence.isEmpty()) {
                stringBuilder.append(escapeSequence);
                stringBuilder.append(segment.rawText());
                stringBuilder.append(resetSequence);
            } else {
                stringBuilder.append(segment.rawText());
            }
        }

        return stringBuilder.toString();
    }

    // ─── Sub classes ─── //

    public record Segment(String rawText, TextFormattingOptions formatting) {}
}
