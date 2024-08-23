package com.miczon.cartoonme.Model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

public class FilterProcessResponse implements Serializable {

    @SerializedName("request_id")
    public String requestId;

    @SerializedName("status")
    public String status;

    @SerializedName("description")
    public String description;

    @SerializedName("err_code")
    public String errorCode;
}
