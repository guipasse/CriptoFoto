package tfm.uoc.edu.criptofoto.model;

public class RepoItem {

    public RepoItem(Integer id, String name, String keyType, String key, String iv, String cryptoKey, Integer def, String path) {
        super();
        this.id = id;
        this.name = name;
        this.keyType = keyType;
        this.key = key;
        this.iv = iv;
        this.cryptoKey = cryptoKey;
        this.def = def;
        this.path = path;
    }

    private Integer id;

    private String name;

    private String keyType;

    private String key;

    private String iv;

    private String cryptoKey;

    private Integer def;

    private String path;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String getCryptoKey() {
        return cryptoKey;
    }

    public void setCryptoKey(String cryptoKey) {
        this.cryptoKey = cryptoKey;
    }

    public Integer getDef() {
        return def;
    }

    public void setDef(Integer def) {
        this.def = def;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return name;
    }

}
