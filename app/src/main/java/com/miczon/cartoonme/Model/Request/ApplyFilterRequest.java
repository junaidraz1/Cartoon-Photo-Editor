package com.miczon.cartoonme.Model.Request;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

public class ApplyFilterRequest implements Serializable {

    @SerializedName("image_url")
    public String imageUrl;

    @SerializedName("id")
    public String filterId;


}
