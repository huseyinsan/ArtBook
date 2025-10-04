package com.huseyinsan.artbook;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MetApiService {
    // For search item

    @GET("public/collection/v1/search")
    Call<SearchResponse> searchObjects(@Query("q") String query);

    // To get the ID of the details of the item

    @GET("public/collection/v1/objects/{objectID}")
    Call<ArtObject> getObjectDetails(@Path("objectID") int objectID);
}
