package lab.s2jh.module.sys.vo;

import java.io.Serializable;

import lab.s2jh.core.annotation.MetaData;

public class NavMenuVO implements Serializable {

    private static final long serialVersionUID = 9047695739997529718L;

    @MetaData(value = "Id")
    private Long id;

    @MetaData(value = "Parent ID")
    private Long parentId;

    @MetaData(value = "Menu Name")
    private String name;

    @MetaData(value = "Menu Path")
    private String path;

    @MetaData(value = "Menu URL")
    private String url;

    @MetaData(value = "Icon style")
    private String style;

    @MetaData(value = "Expand logo", tooltips = "Expand the menu if the default group")
    private Boolean initOpen = Boolean.FALSE;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public Boolean getInitOpen() {
        return initOpen;
    }

    public void setInitOpen(Boolean initOpen) {
        this.initOpen = initOpen;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

}
