package com.example.android.navigationdrawerexample;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.android.navigationdrawerexample.response.ListofPharmacies;
import com.example.android.navigationdrawerexample.response.Result;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Collections;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    double latitude;
    double longitude;
    private LocationData locationData = LocationData.getLocationData();
    public static String LOG_TAG = "MyMapApplication";
    private GoogleMap mMap;
    private List answer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        locationData.getLocation();

        mapFragment.getMapAsync(this);
        //getPlaces();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        // Add a marker in Sydney, Australia, and move the camera.

        latitude = locationData.getLocation().getLatitude();
        longitude = locationData.getLocation().getLongitude();
        getPlaces();
        LatLng myPostion = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(myPostion).title("Your location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPostion, 12));
    }


    public void getPlaces() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/place/")
                .addConverterFactory(GsonConverterFactory.create())	//parse Gson string
                .client(httpClient)	//add logging
                .build();

        NicknameService service = retrofit.create(NicknameService.class);
        String lat = String.valueOf(latitude);
        String lon = String.valueOf(longitude);
        String key = " AIzaSyBOtPkyESCMR866_0xqq8jBkw3SK5nZ98I";
        String latlong = lat+","+lon;
        int radius = 50000;
        String type = "pharmacy";
        String rank = "distance";

        Call<ListofPharmacies> queryResponseCall =
                service.getPharmacies(latlong, rank, type, key);

        //Call retrofit asynchronously
        final String okay ="ok";
        queryResponseCall.enqueue(new Callback<ListofPharmacies>() {
            @Override
            public void onResponse(retrofit2.Response<ListofPharmacies> response) {
                if (response.code() == 500) return;
                if(response.body().getStatus().equals("ZERO_RESULTS")){
                    Context context = getApplicationContext();
                    CharSequence text = "No pharmacies nearby";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
                if (!response.body().getStatus().equals("OK")) return;
                if (response.body().getStatus().equals("OK")) {
                    Log.i(LOG_TAG, "Call was made " + response.body().getStatus());
                    answer = response.body().getResults();
                    //Collections.reverse(answer);
                    //isitok = response.body().result;
                    int len = (answer.size()>20 ? 20:answer.size());
                    for(int i = 0; i<len; i++){
                        Result pharmRes = (Result)answer.get(i);
                        double latIs = pharmRes.getGeometry().getLocation().getLat();
                        double lonIs = pharmRes.getGeometry().getLocation().getLng();
                        LatLng pharmPos = new LatLng(latIs, lonIs);
                        mMap.addMarker(new MarkerOptions().position(pharmPos).title(pharmRes.getName()).snippet(pharmRes.getVicinity()));
                    }


                }
            }

            @Override
            public void onFailure(Throwable t) {
                return;
                // Log error here since request failed
            }
        });


    }

    public interface NicknameService {
        @GET("nearbysearch/json")
        Call<ListofPharmacies> getPharmacies(@Query("location") String location,

                                             //@Query("radius") Integer radius,
                                             @Query("rankby") String rankby,
                                             @Query("type") String type,
                                             @Query("key") String key

        );

    }

}
