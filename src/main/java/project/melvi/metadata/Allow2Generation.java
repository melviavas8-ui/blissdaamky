package project.melvi.metadata;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
public @interface Allow2Generation {
    GenerationLevel level();

    enum GenerationLevel {
        FAST,
        BALANCED
    }
}