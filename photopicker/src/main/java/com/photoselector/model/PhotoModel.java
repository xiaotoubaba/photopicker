
package com.photoselector.model;

import java.io.Serializable;

/**
 * 图片模型
 */

public class PhotoModel implements Serializable{

    private static final long serialVersionUID = 1L;

    private String originalPath;
    private boolean isChecked;
    private boolean isNetImg;

    public PhotoModel(String originalPath, boolean isChecked) {
        super();
        this.originalPath = originalPath;
        this.isChecked = isChecked;
    }

    public PhotoModel(String originalPath, boolean isChecked, boolean isNetImg) {
        this.originalPath = originalPath;
        this.isChecked = isChecked;
        this.isNetImg = isNetImg;
    }

    public PhotoModel(String originalPath) {
        this.originalPath = originalPath;
    }

    public PhotoModel() {
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public boolean isNetImg() {
        return isNetImg;
    }

    public void setNetImg(boolean netImg) {
        isNetImg = netImg;
    }

    // @Override
    // public boolean equals(Object o) {
    // if (o.getClass() == getClass()) {
    // PhotoModel model = (PhotoModel) o;
    // if (this.getOriginalPath().equals(model.getOriginalPath())) {
    // return true;
    // }
    // }
    // return false;
    // }

    public void setChecked(boolean isChecked) {
        System.out.println("checked " + isChecked + " for " + originalPath);
        this.isChecked = isChecked;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((originalPath == null) ? 0 : originalPath.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PhotoModel)) {
            return false;
        }
        PhotoModel other = (PhotoModel) obj;
        if (originalPath == null) {
            if (other.originalPath != null) {
                return false;
            }
        } else if (!originalPath.equals(other.originalPath)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PhotoModel{" +
                "originalPath='" + originalPath + '\'' +
                ", isChecked=" + isChecked +
                '}';
    }
}
