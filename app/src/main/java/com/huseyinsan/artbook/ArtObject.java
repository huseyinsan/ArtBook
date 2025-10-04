package com.huseyinsan.artbook;

import com.google.gson.annotations.SerializedName;

public class ArtObject {
    @SerializedName("title")
    public String title;

    @SerializedName("artistDisplayName")
    public String artistDisplayName;

    @SerializedName("objectDate")
    public String objectDate;

    @SerializedName("primaryImageSmall")
    public String primaryImageSmall;
}
