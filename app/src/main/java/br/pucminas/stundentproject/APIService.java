package br.pucminas.stundentproject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by luket on 03-Sep-16.
 */
public interface APIService {
    //URL to access the API
    String BASE_URL  = "https://parseapi.back4app.com/classes/";
    //Headers needed for every call to the API
    String HEADER_ID = "X-Parse-Application-Id: FWmmldOSRF8GE7jR8424Ex9Tu2ZHLTrggQHLJvjY";
    String HEADER_KEY = "X-Parse-REST-API-Key: RegHHKDEd3qf260q0mGUM7Z7GMsWry79eKsv3Jic";

    //Request the list of students
    @Headers({HEADER_ID, HEADER_KEY})
    @GET("Aluno")
    Call<ResultStudents> getStudents();

    //Delete the specified student
    @Headers({HEADER_ID, HEADER_KEY})
    @DELETE("Aluno/{objectId}")
    Call<ResponseBody> deleteStudent(@Path("objectId") String objectId);

    //Add the specified student
    @Headers({HEADER_ID, HEADER_KEY})
    @POST("Aluno")
    Call<ResponseBody> addStudent(@Body Student student);
}
