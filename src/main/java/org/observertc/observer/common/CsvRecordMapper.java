package org.observertc.observer.common;

import java.util.function.Function;

/**
 * This class is based on the CSVWriter class in open-csv project. The project is available at http://opencsv.sourceforge.net/licenses.html
 */
public class CsvRecordMapper implements Function<Iterable<?>, String> {

    public static final int INITIAL_STRING_SIZE = 1024;
    /**
     * The character used for escaping quotes.
     */
    public static final char DEFAULT_ESCAPE_CHARACTER = '"';
    /**
     * The default separator to use if none is supplied to the constructor.
     */
    public static final char DEFAULT_SEPARATOR = ',';
    /**
     * The default quote character to use if none is supplied to the
     * constructor.
     */
    public static final char DEFAULT_QUOTE_CHARACTER = '"';
    /**
     * The quote constant to use when you wish to suppress all quoting.
     */
    public static final char NO_QUOTE_CHARACTER = '\u0000';
    /**
     * The escape constant to use when you wish to suppress all escaping.
     */
    public static final char NO_ESCAPE_CHARACTER = '\u0000';
    /**
     * Default line terminator.
     */
    public static final String DEFAULT_LINE_END = "\n";
    /**
     * RFC 4180 compliant line terminator.
     */
    public static final String RFC4180_LINE_END = "\r\n";

    /**
     * Checks to see if the line contains special characters.
     * @param line Element of data to check for special characters.
     * @return True if the line contains the quote, escape, separator, newline, or return.
     */

    private char separator = DEFAULT_SEPARATOR;
    private char quotechar = DEFAULT_QUOTE_CHARACTER;
    private char escapechar = DEFAULT_ESCAPE_CHARACTER;

    public static Builder builder() {
        return new Builder();
    }

    private CsvRecordMapper() {

    }

    public String apply(Iterable<?> record) {
        var line = new StringBuilder();
        var it = record.iterator();
        for (var firstColumn = true; it.hasNext(); firstColumn = false) {
            if (!firstColumn) {
                line.append(separator);
            }
            var column = it.next();

            if (column == null) {
                continue;
            }
            var value = column.toString();

            if (!this.hasSpecialCharacter(value)) {
                line.append(value);
                continue;
            }

            if (quotechar != NO_QUOTE_CHARACTER) {
                line.append(quotechar);
            }

            for (int j = 0; j < value.length(); j++) {
                char nextChar = value.charAt(j);
                if (escapechar != NO_ESCAPE_CHARACTER && isEscapeCharacter(nextChar)) {
                    line.append(escapechar);
                }
                line.append(nextChar);
            }

            if (quotechar != NO_QUOTE_CHARACTER) {
                line.append(quotechar);
            }

        }
        return line.toString();
    }

    protected boolean isEscapeCharacter(char nextChar) {
        return quotechar == NO_QUOTE_CHARACTER
                ? (nextChar == quotechar || nextChar == escapechar || nextChar == separator || nextChar == '\n')
                : (nextChar == quotechar || nextChar == escapechar);
    }

    private boolean hasSpecialCharacter(String value) {
        return value.indexOf(quotechar) != -1
                || value.indexOf(escapechar) != -1
                || value.indexOf(separator) != -1
                || value.contains(DEFAULT_LINE_END)
                || value.contains("\r");
    }

    public static class Builder {
        private CsvRecordMapper result = new CsvRecordMapper();
        Builder() {

        }

        public Builder setSeparator(char value) {
            this.result.separator = value;
            return this;
        }

        public Builder setQuoteCharacter(char value) {
            this.result.quotechar = value;
            return this;
        }

        public Builder setEscapeCharacter(char value) {
            this.result.escapechar = value;
            return this;
        }

        public CsvRecordMapper build() {
            return this.result;
        }
    }
}
