package ru.salerman.bitrixstorm.bitrix;/*
 * Copyright 2011-2013 Salerman <www.salerman.ru>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.PsiNavigateUtil;

import java.util.Hashtable;
import java.util.List;

/**
 * @author Mikhail Medvedev aka r3c130n <mm@salerman.ru>
 * @link http://www.salerman.ru/
 * @date: 20.05.13
 */
public class BitrixComponent {
    private String namespace;
    private String name;
	private String templateName;
    private Hashtable<String, BitrixComponentTemplate> templatesList;
	private boolean isComplex = false;

	public BitrixComponent(String namespace, String component) {
		this.name = component;
		this.namespace = namespace;
		this.isComplex = isComplex();
		this.templatesList = new BitrixComponentTemplatesManager(this.namespace, this.name).getTemplates();
	}

    public String getNamespace () {
        return this.namespace;
    }

    public String getName () {
        return this.name;
    }

    public BitrixComponentTemplate getTemplate (String templateName) {
        return this.templatesList.get(templateName);
    }

    public static Hashtable<String, String> parseComponentFromString (String includeComponentString) {
	    Hashtable<String, String> componentVars = new Hashtable<String, String>();
	    componentVars.put("namespace", getComponentNamespaceFromString(includeComponentString));
	    componentVars.put("component", getComponentNameFromString(includeComponentString));
	    componentVars.put("template", getComponentTemplateFromString(includeComponentString));
	    componentVars.put("hash", componentVars.get("namespace") + ":" + componentVars.get("component"));
        return componentVars;
    }

    /**
     * Find "component.php" or "class.php"
     *
     * @return
     */
    public PsiElement findComponentSrc(Project project) {
        PsiFile cmp;
        String[] order = getComponentSrcPath();

        for (String path : order) {
            cmp = BitrixUtils.getPsiFileByPath(path);
            if (cmp != null) {
                return cmp;
            }
        }

        return null;
    }

    public boolean isComplex() {
        String sep = BitrixSiteTemplate.sep;
        String[] templatesList = BitrixComponentTemplate.getComponentTemplatesPathOrder(this.namespace, this.name, ".default");
        for (String path : templatesList) {
            PsiFile templateFile = BitrixUtils.getPsiFileByPath(path);
            if (templateFile != null) {
                return false;
            }
        }
        return true;
    }

    public static String getComponentNamespaceFromString (String string) {
        String cleanString = clearIncludeComponentString(string);
        if (cleanString == null) return null;

        String[] pathElements = cleanString.split(":");
        if (pathElements == null) return null;

        return pathElements[0];
    }

    public static String getComponentNameFromString (String string) {
        String cleanString = clearIncludeComponentString(string);
        if (cleanString == null) return null;

        String[] pathElements = cleanString.split(":");
        if (pathElements == null) return null;

        String[] cmptpl = pathElements[1].split(",");
        if (cmptpl == null) return null;
        return cmptpl[0];
    }

    public static String getComponentTemplateFromString (String string) {
        String cleanString = clearIncludeComponentString(string);
        if (cleanString == null) return null;
        if (cleanString.endsWith(",")) return ""; // .default

        String[] pathElements = cleanString.split(":");
        if (pathElements == null) return null;

        String[] cmptpl = pathElements[1].split(",");
        if (cmptpl == null) return null;

        if (cmptpl.length == 2) {
            return cmptpl[1];
        } else {
            return "";
        }
    }

    private static String clearIncludeComponentString (String string) {
        String[] allStrings = string.toLowerCase().split("array");
        String cleanString = allStrings[0].replace("\"", "").replace("'", "").replace("\n","").replace(" ","").replace("\t","");
        return cleanString.substring(0, cleanString.length() - 1);
    }

    private String[] getComponentSrcPath() {
	    Project project = BitrixUtils.getProject();
        String[] order = new String[2];
        String sep = BitrixUtils.getEscapedSeparator();
        order[0]    = project.getBasePath()
                + sep + "bitrix"
                + sep + "components"
                + sep + this.namespace
                + sep + this.name
                + sep + "component.php";
        order[1]    = project.getBasePath()
                + sep + "bitrix"
                + sep + "components"
                + sep + this.namespace
                + sep + this.name
                + sep + "class.php";
        return order;
    }
}