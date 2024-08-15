package pt.up.fe.specs.cmender.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class CliArgsParserTest {
    @Test
    public void testParseWithUnknownOption() {
        var exception = assertThrows(CliArgsParserException.class,
                () -> CliArgsParser.parseArgs(new String[] { "--help", "--unknown-option"}),
                "Should throw an exception if an unknown option is encountered");

        assertThat(exception.getMessage(), containsString("unknown option"));
        assertThat(exception.getMessage(), containsString("'--unknown-option'"));

        exception = assertThrows(CliArgsParserException.class,
                () -> CliArgsParser.parseArgs(new String[] { "--unknown-option"}),
                "Should throw an exception if an unknown option is encountered");

        assertThat(exception.getMessage(), containsString("unknown option"));
        assertThat(exception.getMessage(), containsString("'--unknown-option'"));
    }

    @Test
    public void testParseWithRequiredOption() {
        var exception = assertThrows(CliArgsParserException.class,
                () -> CliArgsParser.parseArgs(new String[] { }),
                "Should throw an exception if a required option is not provided");

        assertThat(exception.getMessage(), containsString("missing option"));
        assertThat(exception.getMessage(), containsString("'dex'"));
    }

    @Test
    public void testParseWithOptionMissingItsArgument() {
        var exception = assertThrows(CliArgsParserException.class,
                () -> CliArgsParser.parseArgs(new String[] { "-dex" }),
                "Should throw an exception if an option's required argument is not provided");

        assertThat(exception.getMessage(), containsString("missing argument"));
        assertThat(exception.getMessage(), containsString("'dex'"));
    }
}
