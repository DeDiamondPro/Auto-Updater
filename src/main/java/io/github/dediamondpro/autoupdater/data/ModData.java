package io.github.dediamondpro.autoupdater.data;

public class ModData {
    public String id;
    public String tag;
    public String url;
    public String name;
    public boolean update;
    public boolean usePre;
    public boolean useSkyClient = false;
    public String skyClientVersion;

    public ModData(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
