package com.miczon.cartoonme.Model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

public class FinalResultDetails implements Serializable {

    @SerializedName("request_id")
    public String requestId;

    @SerializedName("status")
    public String status;

    @SerializedName("result_url")
    public String resultUrl;

    @SerializedName("result_url_alt")
    public String resultUrlAlt1;

    @SerializedName("nowm_image_url")
    public String resultUrlAlt2;

    @SerializedName("duration")
    public String duration;

    @SerializedName("total_duration")
    public String totalDuration;

    @SerializedName("err_code")
    public String errorCode;

    @SerializedName("description")
    public String description;

}
