package com.htdevs.samaritan;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Husain T on 4/9/2016.
 */
public class FetchMovieTask extends AsyncTask<String, Void, String> {

    //String for Logs
    private final static String TAG = FetchMovieTask.class.getSimpleName();
    //Constructor for initializing MessageAdapter
    MessageAdapter messageAdapter;

    public FetchMovieTask(MessageAdapter messageAdapter) {
        this.messageAdapter = messageAdapter;
    }

    private String getMovieDataFromJson(String movieJsonStr) throws JSONException {
        //These are the JSON Objects which are to be extracted
        final String MDB_RESULTS = "results";
        final String MDB_POSTER_PATH = "poster_path";
        final String MDB_OVERVIEW = "overview";
        final String MDB_TITLE = "title";
        final String MDB_RELEASE_DATE = "release_date";
        final String MDB_GENRE_IDS = "genre_ids";
        final String MDB_VOTE_AVERAGE = "vote_average";
        final String MDB_TOTAL_RESULTS = "total_results";

        JSONObject movieJson = new JSONObject(movieJsonStr);
        int totalResults = movieJson.getInt(MDB_TOTAL_RESULTS);

        //Return zero if search query not found
        if (totalResults==0) {
            return "zero";
        } else {
            JSONArray results = movieJson.getJSONArray(MDB_RESULTS);

            //Get the first results from the array
            JSONObject movieInfo = results.getJSONObject(0);
            JSONArray genreIdsArray = movieInfo.getJSONArray(MDB_GENRE_IDS);

            String title = movieInfo.getString(MDB_TITLE);
            String overview = movieInfo.getString(MDB_OVERVIEW);
            String posterPath = movieInfo.getString(MDB_POSTER_PATH);
            String releaseDate = movieInfo.getString(MDB_RELEASE_DATE);
            String genreStr = getGenreIds(genreIdsArray);
            double voteAverage = movieInfo.getDouble(MDB_VOTE_AVERAGE);

            String finalMovieStr = "Movie you requested...<br><br >";
            finalMovieStr += "<b>Title : </b>" + title + "<br>";
            finalMovieStr += "<b>Overview : </b>" + overview + "<br>";
            finalMovieStr += "<b>Poster : </b>" + posterPath + "<br>";
            finalMovieStr += "<b>Release Date : </b>" + releaseDate + "<br>";
            finalMovieStr += "<b>Genres : </b>" + genreStr + "<br>";
            finalMovieStr += "<b>Ratings : </b>" + voteAverage + "<br>";

            return finalMovieStr;
        }
    }

    //Gets name of genres from their ids
    private String getGenreIds(JSONArray genreIdsArray) throws JSONException {
        //Default parsing for given string list
        String genreIdsStr = "{\"genres\":[{\"id\":28,\"name\":\"Action\"},{\"id\":12,\"name\":\"Adventure\"},{\"id\":16,\"name\":\"Animation\"},{\"id\":35,\"name\":\"Comedy\"},{\"id\":80,\"name\":\"Crime\"},{\"id\":99,\"name\":\"Documentary\"},{\"id\":18,\"name\":\"Drama\"},{\"id\":10751,\"name\":\"Family\"},{\"id\":14,\"name\":\"Fantasy\"},{\"id\":10769,\"name\":\"Foreign\"},{\"id\":36,\"name\":\"History\"},{\"id\":27,\"name\":\"Horror\"},{\"id\":10402,\"name\":\"Music\"},{\"id\":9648,\"name\":\"Mystery\"},{\"id\":10749,\"name\":\"Romance\"},{\"id\":878,\"name\":\"Science Fiction\"},{\"id\":10770,\"name\":\"TV Movie\"},{\"id\":53,\"name\":\"Thriller\"},{\"id\":10752,\"name\":\"War\"},{\"id\":37,\"name\":\"Western\"}]}";
        JSONObject genreObject = new JSONObject(genreIdsStr);
        JSONArray genresArray = genreObject.getJSONArray("genres");
        Map<Integer, String> genreMap = new HashMap<Integer, String>();
        for (int i = 0; i < genresArray.length(); i++) {
            //Add every array field to map
            JSONObject tempObject = genresArray.getJSONObject(i);
            int id = tempObject.getInt("id");
            String name = tempObject.getString("name");
            genreMap.put(id, name);
        }

        //Parsing of given genreIds
        String genreList = "";
        for (int i = 0; i < genreIdsArray.length(); i++) {
            int tempId = genreIdsArray.getInt(i);
            genreList = genreList + genreMap.get(tempId) + ",";
        }
        if(genreList.length()>0) {
            genreList = genreList.substring(0, genreList.length() - 1);
        }

        return genreList;
    }

    @Override
    protected String doInBackground(String... params) {

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String movieQuery = params[0];
        String appId = BuildConfig.MOVIEDB_API_KEY;

        // Will contain the raw JSON response as a string.
        String movieJsonStr = null;

        try {
            // Construct the URL for the OpenWeatherMap query
            //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=Coimbatore&units=metric&cnt=7&appid=a092392796c52740b65540b6b23e26e3");
            //To build a URI string
            final String MOVIE_SEARCH_BASE_URL = "https://api.themoviedb.org/3/search/movie?";
            final String QUERY_PARAM = "query";
            final String UNITS_PARAM = "units";
            final String COUNT_PARAM = "cnt";
            final String APP_ID_PARAM = "api_key";

            Uri buildUri = Uri.parse(MOVIE_SEARCH_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, movieQuery)
                    .appendQueryParameter(APP_ID_PARAM, appId).build();
            URL url = new URL(buildUri.toString());
            Log.v(TAG, "Built URL : " + url.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                movieJsonStr = null;
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                movieJsonStr = null;
            }
            movieJsonStr = buffer.toString();


            Log.v(TAG, movieJsonStr);
        } catch (IOException e) {
            Log.e(TAG, "Error ", e);
            // If the code didn't successfully get the movie data, there's no point in attempting
            // to parse it.
            movieJsonStr = null;
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }

        try {
            return getMovieDataFromJson(movieJsonStr);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String finalString) {
        Log.d(TAG, "finalString :- " + finalString);
        if (finalString == null) {
            messageAdapter.addMessage("You're not connected to the internet! Try again later.", MessageAdapter.DIRECTION_OUTGOING);
        } else if (finalString.equals("zero")) {
            messageAdapter.addMessage("Uh oh! You just got 404'D. Movie not found", MessageAdapter.DIRECTION_OUTGOING);
        } else {
            messageAdapter.addMessage(finalString, MessageAdapter.DIRECTION_OUTGOING);
        }
    }
}


