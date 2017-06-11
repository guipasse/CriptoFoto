package tfm.uoc.edu.criptofoto.model;

public class IntrusionRegisterItem {

    public IntrusionRegisterItem(Integer id, String data, String hora, String fotoPath, String clau, String symetricKey) {
        this.id = id;
        this.data = data;
        this.hora = hora;
        this.fotoPath = fotoPath;
        this.clau = clau;
        this.symetricKey = symetricKey;
    }

    private Integer id;

    private String data;

    private String hora;

    private String fotoPath;

    private String clau;

    private String symetricKey;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getFotoPath() {
        return fotoPath;
    }

    public void setFotoPath(String fotPath) {
        this.fotoPath = fotPath;
    }

    public String getClau() {
        return clau;
    }

    public void setClau(String clau) {
        this.clau = clau;
    }

    public String getSymetricKey() {
        return symetricKey;
    }

    public void setSymetricKey(String symetricKey) {
        this.symetricKey = symetricKey;
    }
}
