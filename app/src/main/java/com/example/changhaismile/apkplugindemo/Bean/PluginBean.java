package com.example.changhaismile.apkplugindemo.Bean;

/**
 * Created by changhaismile on 2017/5/19.
 */

public class PluginBean {
    /**
     * 插件名称
     */
    private String label;
    /**
     * 插件包名
     */
    private String packageName;

    public PluginBean(String label, String packageName) {
        this.label = label;
        this.packageName = packageName;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
