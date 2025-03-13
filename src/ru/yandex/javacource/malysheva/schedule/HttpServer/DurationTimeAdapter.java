package ru.yandex.javacource.malysheva.schedule.HttpServer;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import ru.yandex.javacource.malysheva.schedule.tasks.Duration;

import java.io.IOException;

public class DurationTimeAdapter extends TypeAdapter<Duration> {
    @Override
    public void write(JsonWriter out, Duration duration) throws IOException {
        if (duration == null) {
            out.nullValue();
            return;
        }

        out.value(duration.getMinutes());
    }

    @Override
    public Duration read(JsonReader in) throws IOException {
        switch (in.peek()) {
            case NULL:
                in.nextNull();
                return null;
            case NUMBER:
                int minutes = in.nextInt();
                return new Duration(minutes);
            case STRING:
                try {
                    int parsedMinutes = Integer.parseInt(in.nextString());
                    return new Duration(parsedMinutes);
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid duration format", e);
                }
            default:
                throw new IOException("Unexpected JSON type for Duration");
        }
    }

    public static class Util {
        public static Duration parseJson(String json) {
            try {
                int minutes = Integer.parseInt(json);
                return new Duration(minutes);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid duration format", e);
            }
        }

        public static String toJson(Duration duration) {
            return duration != null ? String.valueOf(duration.getMinutes()) : "null";
        }
    }
}