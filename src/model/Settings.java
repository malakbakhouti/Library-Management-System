package model;

/**
 * Paramètre générique (table settings)
 */
public class Settings {
    private int id;
    private String keyName;
    private String valueText;

    public Settings() {}
    public Settings(int id, String keyName, String valueText) {
        this.id = id; this.keyName = keyName; this.valueText = valueText;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getKeyName() { return keyName; }
    public void setKeyName(String keyName) { this.keyName = keyName; }

    public String getValueText() { return valueText; }
    public void setValueText(String valueText) { this.valueText = valueText; }

    @Override
    public String toString() { return keyName + " = " + valueText; }
}
