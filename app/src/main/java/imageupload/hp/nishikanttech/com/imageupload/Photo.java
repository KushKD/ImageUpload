package imageupload.hp.nishikanttech.com.imageupload;

import java.io.Serializable;

/**
 * Created by kuush on 8/7/2016.
 */
public class Photo implements Serializable {

    public String photoname;

    public String getPhotoname() {
        return photoname;
    }

    public void setPhotoname(String photoname) {
        this.photoname = photoname;
    }

    public String getPhotobase64encode() {
        return photobase64encode;
    }

    public void setPhotobase64encode(String photobase64encode) {
        this.photobase64encode = photobase64encode;
    }

    public String photobase64encode;


}
