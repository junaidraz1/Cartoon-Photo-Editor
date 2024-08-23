package com.miczon.cartoonme.Model.Response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

public class UploadImageResponse implements Serializable {

    @SerializedName("status")
    public String status;

    @SerializedName("result")
    public String imageUrl;

    @SerializedName("messages")
    public String errMessage;

    @SerializedName("info")
    public String errInfo;


}
