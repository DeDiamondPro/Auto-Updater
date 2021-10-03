package io.github.dediamondpro.autoupdater.data;

public class ModData {
    public String id;
    public String tag;
    public String url;
    public String name;
    public boolean update;
    public boolean usePre;

    public ModData(String id, String tag, String url, String name) {
        this.id = id;
        this.tag = tag;
        this.url = url;
        this.name = name;
    }
}
