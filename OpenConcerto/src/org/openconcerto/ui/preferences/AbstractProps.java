/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2011 OpenConcerto, by ILM Informatique. All rights reserved.
 * 
 * The contents of this file are subject to the terms of the GNU General Public License Version 3
 * only ("GPL"). You may not use this file except in compliance with the License. You can obtain a
 * copy of the License at http://www.gnu.org/licenses/gpl-3.0.html See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each file.
 */
 
 package org.openconcerto.ui.preferences;

import org.openconcerto.utils.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JOptionPane;

public abstract class AbstractProps {

    protected final Properties props = new Properties();

    protected AbstractProps() {
        load();
    }

    public Properties getProps() {
        return this.props;
    }

    protected abstract String getPropsFileName();

    public final boolean contains(String name) {
        return this.props.containsKey(name + getPropertySuffix());
    }

    public String getPropertySuffix() {
        return "";
    }

    /**
     * Return the property for the passed key.
     * 
     * @param name the key.
     * @return the matching string, never <code>null</code>.
     * @see #getDefaultStringValue()
     */
    public String getStringProperty(String name) {
        final String property = getProperty(name);
        if (property == null)
            return getDefaultStringValue();
        else
            return property;
    }

    /**
     * Return the property for the passed key.
     * 
     * @param name the key.
     * @return the matching property value or <code>null</code>.
     */
    public final String getProperty(String name) {
        final String key = name + getPropertySuffix();
        final String property = this.props.getProperty(key);
        return property;
    }

    /**
     * Return Boolean.TRUE only if the property is defined and equals to "true". If the property is
     * not defined or not equals to "true", return Boolean.FALSE
     * */
    public final Boolean getBooleanValue(String name) {
        final String property = this.getProperty(name);
        if (property == null) {
            return Boolean.FALSE;
        }
        return Boolean.valueOf(property);
    }

    public final boolean getBooleanValue(String name, boolean defaultValue) {
        final String property = this.getProperty(name);
        if (property == null) {
            return defaultValue;
        }
        return Boolean.valueOf(property);
    }

    public String getDefaultStringValue() {
        return "";
    }

    public final int getIntProperty(String name) {
        return getIntProperty(name, getDefautIntValue());
    }

    public final int getIntProperty(String name, int defaultVal) {
        final String property = this.getProperty(name);
        return property == null ? defaultVal : Integer.valueOf(property).intValue();
    }

    protected int getDefautIntValue() {
        return -1;
    }

    /**
     * Set or remove a property.
     * 
     * @param key the key.
     * @param value the value, <code>null</code> meaning remove.
     */
    public void setProperty(String key, String value) {
        final String fullKey = key + getPropertySuffix();
        if (value == null)
            this.props.remove(fullKey);
        else
            this.props.setProperty(fullKey, value);
    }

    public void load() {
        final File file = new File(getPropsFileName());
        System.out.println("Loading properties from " + file.getAbsolutePath());
        if (!file.exists()) {
            System.out.println("Warning: " + file.getAbsolutePath() + " does not exist");
            return;
        }
        BufferedInputStream bufferedInputStream = null;
        try {
            final FileInputStream fileInputStream = new FileInputStream(file);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            this.props.load(bufferedInputStream);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Impossible de lire le fichier de configuration:\n" + file.getAbsolutePath());
            e.printStackTrace();
        } finally {
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void store() {
        BufferedOutputStream bufferedOutputStream = null;
        final File file = new File(getPropsFileName());
        try {
            FileUtils.mkdir_p(file.getParentFile());
            final FileOutputStream fileOutputStream = new FileOutputStream(file);
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            this.props.store(bufferedOutputStream, "");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Impossible d'enregistrer le fichier de configuration:\n" + file.getAbsolutePath());
            e.printStackTrace();
        } finally {
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
