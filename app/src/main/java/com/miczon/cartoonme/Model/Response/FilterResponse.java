package com.miczon.cartoonme.Model.Response;

import com.google.gson.annotations.SerializedName;
import com.miczon.cartoonme.Model.FilterProcessResponse;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

public class FilterResponse {

    @SerializedName("status")
    public String status;

    @SerializedName("image_process_response")
    public FilterProcessResponse filterProcessResponse;
}
