
package com.example.android.navigationdrawerexample.response;

//import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

//@Generated("org.jsonschema2pojo")
public class OpeningHours {

    @SerializedName("open_now")
    @Expose
    private boolean openNow;

    /**
     * 
     * @return
     *     The openNow
     */
    public boolean isOpenNow() {
        return openNow;
    }

    /**
     * 
     * @param openNow
     *     The open_now
     */
    public void setOpenNow(boolean openNow) {
        this.openNow = openNow;
    }

}
