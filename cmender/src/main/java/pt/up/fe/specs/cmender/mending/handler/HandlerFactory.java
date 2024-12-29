package pt.up.fe.specs.cmender.mending.handler;

import java.util.List;

public class HandlerFactory {
    public static final List<String> HANDLER_TYPES = List.of(
            BasicSequentialMendingHandler.class.getSimpleName()
    );

    public static boolean isValidHandlerType(String handlerType) {
        return HANDLER_TYPES.contains(handlerType);
    }

    public static MendingHandler createHandler(String handlerType) {
        if (handlerType.equals(BasicSequentialMendingHandler.class.getSimpleName())) {
            return new BasicSequentialMendingHandler();
        } else {
            return null;
        }
    }
}
