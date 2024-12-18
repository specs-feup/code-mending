package pt.up.fe.specs.cmender.mending;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class MendingEngineFatalException extends RuntimeException {
    private final FatalType type;
    private final String message;
    private final long iteration;

    public enum FatalType {
        DIAG_EXPORTER,
        DIAG_EXPORTER_RESULT_DESERIALIZATION,
        MENDING,
        MENDFILE_WRITER,
        MENDING_DIR_CREATION,
        SOURCE_RESULT_WRITER; // TODO maybe this shouldnt be fatal

        @JsonValue
        public String getAsString() {
            return this.name().toLowerCase();
        }
    }

    public MendingEngineFatalException(FatalType type, String message, long iteration) {
        super(message);
        this.type = type;
        this.message = message;
        this.iteration = iteration;
    }

    public MendingEngineFatalException(FatalType type, String message, long iteration, Throwable cause) {
        super(message, cause);
        this.type = type;
        this.message = message;
        this.iteration = iteration;
    }
}
