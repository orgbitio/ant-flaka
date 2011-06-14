package it.haefelinger.flaka.util;

import java.io.File;

public interface Groovenizer {
    public Class parse(File file);
    public Class parse(String text);
}
