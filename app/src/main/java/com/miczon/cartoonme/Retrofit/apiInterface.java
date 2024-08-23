package com.miczon.cartoonme.Retrofit;

import com.miczon.cartoonme.Model.Response.FilterResponse;
import com.miczon.cartoonme.Model.Response.FinalResultResponse;
import com.miczon.cartoonme.Model.Response.UploadImageResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

public interface apiInterface {

    @Multipart
    @POST("upload/")
    Call<UploadImageResponse> uploadPicture(@Header("X-RapidAPI-Key") String apiKey , @Part MultipartBody.Part filePart);

    @FormUrlEncoded
    @POST("toonme/v1/")
    Call<FilterResponse> applyFilter(@Header("X-RapidAPI-Key") String apiKey , @Field("id") String id, @Field("image_url") String url);

    @FormUrlEncoded
    @POST("toonme/v1/result/")
    Call<FinalResultResponse> getResult(@Header("X-RapidAPI-Key") String apiKey , @Field("request_id") String requestId);

}
