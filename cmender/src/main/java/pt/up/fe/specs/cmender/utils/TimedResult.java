package pt.up.fe.specs.cmender.utils;

public record TimedResult<T>(T result, long elapsedTime) { }
