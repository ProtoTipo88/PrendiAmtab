package ferrara.org.prendiamtab.app;


import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Imp on 10/02/2015.
 */
public class AmtabFragment extends Fragment {

    private ArrayAdapter<String> mAmtabAdapter;

    public AmtabFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        // Create some dummy data for the ListView.  Here's a sample weekly forecast
        String[] data = {
                "Numero Linea - Percorso A/R - Quartiere",
                "Linea 01 - P.zza Eroi del Mare - S.Spirito",
                "Linea 02 - Piscine Comunali - Japigia"
        };

        List<String> NumLinea = new ArrayList<String>(Arrays.asList(data));

        // Now that we have some dummy forecast data, create an ArrayAdapter.
        // The ArrayAdapter will take data from a source (like our dummy forecast) and
        // use it to populate the ListView it's attached to.
        mAmtabAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_bus, // The name of the layout ID.
                        R.id.list_view_bus, // The ID of the textview to populate.

                        NumLinea);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.list_view_bus);
        listView.setAdapter(mAmtabAdapter);
        return super.onCreateView(inflater, container, savedInstanceState);


    }

    public class FetchLineaTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchLineaTask.class.getSimpleName();

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * <p/>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getLineefromJson(String JsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.

            final String OWM_DESCRIPTION = "DescizioneLinea";
            final String OWM_IDBUS = "IdLinea";


            JSONArray LineeAttiveArray = new JSONArray(JsonStr);

            ArrayList<String> resultStrs = new ArrayList<String>();
            for (int i = 0; i < LineeAttiveArray.length(); i++) {


                String description;
                String Id;

                JSONObject descAmtab = LineeAttiveArray.getJSONObject(i);

                description = descAmtab.getString(OWM_DESCRIPTION);
                Id = descAmtab.getString(OWM_IDBUS);

                resultStrs.add(Id + " - " + description);
            }

            String[] outputArray = new String[resultStrs.size()];
            return resultStrs.toArray(outputArray);

        }

        @Override
        protected String[] doInBackground(String... params) {

            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;


            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String FORECAST_BASE_URL =
                        "http://bari.opendata.planetek.it/OrariBus/v2.1/OpenDataService.svc/REST/rete/Linee";


                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
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
                    return null;
                }
                forecastJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }


            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        //   @Override
        //   protected void onPostExecute(String[] result) {
        //       if (result != null) {
        //           mForecastAdapter.clear();
        //          for(String dayForecastStr : result) {
        //              mForecastAdapter.add(dayForecastStr);
        //           }
        //          // New data is back from the server.  Hooray!
        //      }
        //   }
        // }


    }
}
