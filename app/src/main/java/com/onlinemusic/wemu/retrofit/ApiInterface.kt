package com.onlinemusic.wemu.retrofit

import com.onlinemusic.wemu.responseModel.FavouritesResponseModel
import com.onlinemusic.wemu.responseModel.add_to_playlist.AddToPlaylistRequestModel
import com.onlinemusic.wemu.responseModel.album.response.AlbumResponseModel
import com.onlinemusic.wemu.responseModel.albumdetails.response.AlbumDetailsResponseModel
import com.onlinemusic.wemu.responseModel.albumslist.response.AlbumListResponseMainModel
import com.onlinemusic.wemu.responseModel.artist.response.ArtistMainResponseModel
import com.onlinemusic.wemu.responseModel.artist.songsdetails.response.ArtistAlbumResponseModel
import com.onlinemusic.wemu.responseModel.artistsongs.response.ArtistSongsResponseMainModel
import com.onlinemusic.wemu.responseModel.commonsection.response.CommonDataResponseMainModel
import com.onlinemusic.wemu.responseModel.dashboard.response.DashboardResponseModel
import com.onlinemusic.wemu.responseModel.favourites.response.FavouritesAddResponseModel
import com.onlinemusic.wemu.responseModel.genredetails.response.GenreDetailsResponseModel
import com.onlinemusic.wemu.responseModel.genres.response.GenresDataResponseModel
import com.onlinemusic.wemu.responseModel.notification.response.NotificationResponseModel
import com.onlinemusic.wemu.responseModel.playlist.responce.PlaylistResponseMainModel
import com.onlinemusic.wemu.responseModel.playlist_details_response.PlayListDetailsResponse
import com.onlinemusic.wemu.responseModel.playlistitems.response.PlaylistCreatedResponseModel
import com.onlinemusic.wemu.responseModel.recent_play.RecentPlayDataResponseMainModel
import com.onlinemusic.wemu.responseModel.remove_from_playlist.RemoveFromPlayListRequestModel
import com.onlinemusic.wemu.responseModel.search.response.SearchResponseModel
import com.onlinemusic.wemu.responseModel.searchalbum.response.SearchAlbumsMainResponseModel
import com.onlinemusic.wemu.responseModel.searchartist.response.SearchSingerResponseMainModel
import com.onlinemusic.wemu.responseModel.searchcategory.response.SearchCategoryResponseModel
import com.onlinemusic.wemu.responseModel.songsmoresearch.response.SearchSongsMainResponseModel
import com.onlinemusic.wemu.responseModel.version_check.VersionRequestModel
import com.onlinemusic.wemu.responseModel.version_check.version_response.VersionCheckResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {
    @Multipart
    @POST("register")
    fun signup(
        @Part("name") name: RequestBody?,
        @Part("email") email: RequestBody?,
        @Part("password") password: RequestBody?,
        @Part("dob") dob: RequestBody?,
        @Part("gender") gender: RequestBody?,
        @Part("username") username: RequestBody?,
        @Part("password_confirmation") password_confirmation: RequestBody?,
        @Part("subscription") subscription: RequestBody?
    ): Call<ResponseBody?>?


    @Multipart
    @POST("login")
    fun login(
        @Part("email") name: RequestBody?,
        @Part("password") email: RequestBody?,
        @Part("udid") udid: RequestBody?,
        @Part("device_type") device_type: RequestBody?
    ): Call<ResponseBody?>?


    @Multipart
    @POST("verify-otp")
    fun verifyotp(
        @Part("email") email: RequestBody?,
        @Part("otp") otp: RequestBody?
    ): Call<ResponseBody?>?


    @Multipart
    @POST("forgot-password")
    fun forgotpassword(
        @Part("email") email: RequestBody?,
    ): Call<ResponseBody?>?


    @Multipart
    @POST("reset-password")
    fun resetpassword(
        @Part("email") email: RequestBody?,
        @Part("password") password: RequestBody?,
        @Part("password_confirmation") password_confirmation: RequestBody?,
    ): Call<ResponseBody?>?


    @Multipart
    @POST("update-profile")
    fun changepassword(
        @Header("Authorization") Authorization: String?,
        @Part("password") password: RequestBody?,
        @Part("password_confirmation") password_confirmation: RequestBody?,
        @Part("old_password") old_password: RequestBody?,
    ): Call<ResponseBody?>?


    @Multipart
    @POST("resend-otp")
    fun resendotp(
        @Part("email") email: RequestBody?,
    ): Call<ResponseBody?>?

    @Multipart
    @POST("upload-image")
    fun uploadImage(
        @Header("Authorization") Authorization: String?,
        @Part("type") type: RequestBody?,
        @Part image: MultipartBody.Part
    ): Call<ResponseBody?>?

    @Multipart
    @POST("create-playlist")
    fun createPlaylist(
        @Header("Authorization") Authorization: String?,
        @Part("title") title: RequestBody?,
        @Part image: MultipartBody.Part
    ): Call<FavouritesResponseModel?>?


    @GET("my-profile")
    fun myprofile(@Header("Authorization") Authorization: String): Call<ResponseBody?>?


    @GET("delete-account")
    fun deleteProfile(@Header("Authorization") Authorization: String): Call<ResponseBody?>?

    @GET("dashboard")
    fun getDashboardData(@Header("Authorization") Authorization: String): Call<DashboardResponseModel?>?

   /* @GET("dashboard")
    fun getAlbumData(@Header("Authorization") Authorization: String): Call<AlbumResponseModel?>?*/


    @GET("country-list")
    fun getCountryData(@Header("Authorization") Authorization: String): Call<ResponseBody?>?

    @Multipart
    @POST("update-profile")
    fun updateGeneralSettings(
        @Header("Authorization") Authorization: String,
        @Part("country_id") country_id: RequestBody?,
        @Part("gender") gender: RequestBody?,
        @Part("age") age: RequestBody?
    ): Call<ResponseBody?>?


    @Multipart
    @POST("update-profile")
    fun updateProfileSettings(
        @Header("Authorization") Authorization: String,
        @Part("name") name: RequestBody?,
        @Part("about") about: RequestBody?,
        @Part("facebook") facebook: RequestBody?,
        @Part("website") website: RequestBody?
    ): Call<ResponseBody?>?


    @GET("song-list/{key}")
    fun getData(
        @Header("Authorization") Authorization: String,
        @Path("key") key: String
    ): Call<ResponseBody?>?

    @GET("song-list/{key}")
    fun getNewData(
        @Header("Authorization") Authorization: String,
        @Path("key") key: String, @Query("page") page: String
    ): Call<CommonDataResponseMainModel?>?


    @GET("recent-play")
    fun getRecentPlayData(@Header("Authorization") Authorization: String): Call<RecentPlayDataResponseMainModel?>?


    @GET("album-songs/{id}")
    fun getAlbumsData(
        @Header("Authorization") Authorization: String,
        @Path("id") id: String,
        @Query("page") page: String
    ): Call<AlbumDetailsResponseModel?>?

    @GET("artists-songs/{artist_user_id}")
    fun getArtistSongData(
        @Header("Authorization") Authorization: String,
        @Path("artist_user_id") artist_user_id: String,
        @Query("page") page: String
    ): Call<ArtistSongsResponseMainModel?>?


    @GET("genre_list")
    fun getGenresData(
        @Header("Authorization") Authorization: String,
        @Query("page") page: String
    ): Call<GenresDataResponseModel?>?

    @GET("my-playlist")
    fun getPlaylistData(
        @Header("Authorization") Authorization: String,
        @Query("page") page: String
    ): Call<PlaylistResponseMainModel?>?


    @GET("artist-list")
    fun getArtistData(
        @Header("Authorization") Authorization: String,
        @Query("page") page: String
    ): Call<ArtistMainResponseModel?>?


    @GET("genre-details/{id}")
    fun getGenreDetails(
        @Header("Authorization") Authorization: String,
        @Path("id") id: String
    ): Call<GenreDetailsResponseModel?>?


    @GET("search/{keyword}")
    fun getSearchData(
        @Header("Authorization") Authorization: String,
        @Path("keyword") keyword: String
    ): Call<SearchResponseModel?>?

    @GET("add-remove-favouirite/{id}")
    fun addtoFavourites(
        @Header("Authorization") Authorization: String,
        @Path("id") id: String,
    ): Call<FavouritesResponseModel?>?


    @GET("fav-songs")
    fun getfavouritesData(
        @Header("Authorization") Authorization: String,
        @Query("page") page: String
    ): Call<FavouritesAddResponseModel?>?


    @GET("list-albums/{keyword}")
    fun getAlbumListData(
        @Header("Authorization") Authorization: String,
        @Path("keyword") keyword: String,
        @Query("page") page: String
    ): Call<AlbumListResponseMainModel?>?

    @GET("category-album/{cat_id}")
    fun getYouMayLikeListData(
        @Header("Authorization") Authorization: String,
        @Path("cat_id") catId: String,
        @Query("page") page: String
    ): Call<AlbumListResponseMainModel?>?


    @GET("notification-get")
    fun getNotificationData(
        @Header("Authorization") Authorization: String,
        @Query("page") page: String
    ): Call<NotificationResponseModel?>?


    @Multipart
    @POST("update-profile")
    fun updateAllProfileSettings(
        @Header("Authorization") Authorization: String,
        @Part("country_id") country_id: RequestBody?,
        @Part("gender") gender: RequestBody?,
        @Part("age") age: RequestBody?,
        @Part("name") name: RequestBody?,
        @Part("about") about: RequestBody?,
        @Part("facebook") facebook: RequestBody?,
        @Part("website") website: RequestBody?

    ): Call<ResponseBody?>?


    @POST("add-remove-playlist")
    fun addtoPlaylist(
        @Header("Authorization") Authorization: String,
        @Body addToPlaylistRequestModel: AddToPlaylistRequestModel,
    ): Call<FavouritesResponseModel?>?

    @POST("song-remove-playlist")
    fun removeFromPlaylist(
        @Header("Authorization") Authorization: String,
        @Body removeFromPlayListRequestModel: RemoveFromPlayListRequestModel,
    ): Call<FavouritesResponseModel?>?

    @GET("myplaylist-songs")
    fun getAlbumsData(
        @Header("Authorization") Authorization: String,
        @Query("page") page: String
    ): Call<FavouritesAddResponseModel?>?

    @Multipart
    @POST("update-pro")
    fun updateTransaction(
        @Header("Authorization") Authorization: String?,
        @Part("amount") amount: RequestBody?,
        @Part("type") type: RequestBody?,
        @Part("via") via: RequestBody?,
        @Part("stripe_customer_id") stripe_customer_id: RequestBody?,
        @Part("customer_response") customer_response: RequestBody?,
        @Part("stripe_subscription_id") stripe_subscription_id: RequestBody?,
    ): Call<FavouritesResponseModel?>?


    @GET("search/{keyword}")
    fun getUpdatedSearchData(
        @Header("Authorization") Authorization: String,
        @Path("keyword") keyword: String
    ): Call<SearchCategoryResponseModel?>?

    @GET("search-album-lists/{keyword}")
    fun getSearchedAlbum(
        @Header("Authorization") Authorization: String,
        @Path("keyword") keyword: String,
        @Query("page") page: String
    ): Call<SearchAlbumsMainResponseModel?>?

    @GET("search-artist-lists/{keyword}")
    fun getSearchedArtist(
        @Header("Authorization") Authorization: String,
        @Path("keyword") keyword: String,
        @Query("page") page: String
    ): Call<SearchSingerResponseMainModel?>?

    @GET("search-song-lists/{keyword}")
    fun getSearchedSongsList(
        @Header("Authorization") Authorization: String,
        @Path("keyword") keyword: String,
        @Query("page") page: String
    ): Call<SearchSongsMainResponseModel?>?

    @GET("artist-album-lists/{keyword}")
    fun getArtistAlbumsList(
        @Header("Authorization") Authorization: String,
        @Path("keyword") keyword: String,
        @Query("page") page: String
    ): Call<ArtistAlbumResponseModel?>?


    @GET("song-like/{id}")
    fun addtoLikeSongs(
        @Header("Authorization") Authorization: String,
        @Path("id") id: String,
    ): Call<FavouritesResponseModel?>?


    @GET("song-view/{id}")
    fun songPlayedView(
        @Header("Authorization") Authorization: String,
        @Path("id") id: String,
    ): Call<FavouritesResponseModel?>?

    @GET("play-songs/{id}")
    fun songViewCount(
        @Header("Authorization") Authorization: String,
        @Path("id") id: String,
    ): Call<FavouritesResponseModel?>?


    @GET("my-playlist")
    fun getCreatedPlaylistData(
        @Header("Authorization") Authorization: String,
        @Query("page") page: String
    ): Call<PlaylistCreatedResponseModel?>?


    @GET("playlist-details/{playlist-id}")
    fun getPlayListDetails(
        @Header("Authorization") Authorization: String,
        @Path("playlist-id") playlistId: String,
    ): Call<PlayListDetailsResponse?>?


    @POST("app-version")
    fun postVersionCheck(
        @Body versionRequestModel: VersionRequestModel
    ): Call<VersionCheckResponse?>?


}