package com.verona1024.tutlub.activities.feed;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.verona1024.tutlub.R;
import com.verona1024.tutlub.dialogs.MessageDialog;
import com.verona1024.tutlub.utils.UserUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static com.seatgeek.placesautocomplete.Constants.LOG_TAG;

public class RegistrationActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {
    private GoogleApiClient mGoogleApiClient;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    // Default:
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mGoogleApiClient = new GoogleApiClient.Builder(RegistrationActivity.this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build();

        final EditText editTextFirstName = (EditText) findViewById(R.id.editFirstName);
        final EditText editTextSecindName = (EditText) findViewById(R.id.editSecondName);
        final AutoCompleteTextView editTextCountry = (AutoCompleteTextView) findViewById(R.id.editCountry);
        editTextCountry.setThreshold(3);
        editTextCountry.setOnItemClickListener(mAutocompleteClickListener);
        mPlaceArrayAdapter = new PlaceArrayAdapter(this, android.R.layout.simple_list_item_1, BOUNDS_MOUNTAIN_VIEW, null);
        editTextCountry.setAdapter(mPlaceArrayAdapter);

        findViewById(R.id.buttonNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (editTextFirstName.getText().toString().isEmpty()){
                    DialogFragment newFragment = MessageDialog.newInstance(getString(R.string.forgot_fill_in_all));
                    newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
                    return;
                }

                if (editTextSecindName.getText().toString().isEmpty()){
                    DialogFragment newFragment = MessageDialog.newInstance(getString(R.string.forgot_fill_in_all));
                    newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
                    return;
                }

                if (editTextCountry.getText().toString().isEmpty()){
                    DialogFragment newFragment = MessageDialog.newInstance(getString(R.string.forgot_fill_in_all));
                    newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
                    return;
                }

                UserUtil.name = editTextFirstName.getText().toString() + " " + editTextSecindName.getText().toString();
                UserUtil.COUNTRY = editTextCountry.getText().toString();

                startActivity(new Intent(RegistrationActivity.this, RegistrationEmailActivity.class));
                finish();
            }
        });
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            CharSequence attributions = places.getAttributions();

            if (attributions != null) {
            }
        }
    };

    @Override
    public void onConnected(Bundle bundle) {
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
        Log.i(LOG_TAG, "Google Places API connected.");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlaceArrayAdapter.setGoogleApiClient(null);
        Log.e(LOG_TAG, "Google Places API connection suspended.");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(RegistrationActivity.this, StartActivity.class));
        finish();
    }

    private class PlaceArrayAdapter
            extends ArrayAdapter<PlaceArrayAdapter.PlaceAutocomplete> implements Filterable {
        private static final String TAG = "PlaceArrayAdapter";
        private GoogleApiClient mGoogleApiClient;
        private AutocompleteFilter mPlaceFilter;
        private LatLngBounds mBounds;
        private ArrayList<PlaceAutocomplete> mResultList;

        /**
         * Constructor
         *
         * @param context  Context
         * @param resource Layout resource
         * @param bounds   Used to specify the search bounds
         * @param filter   Used to specify place types
         */
        public PlaceArrayAdapter(Context context, int resource, LatLngBounds bounds,
                                 AutocompleteFilter filter) {
            super(context, resource);
            mBounds = bounds;
            mPlaceFilter = filter;
        }

        public void setGoogleApiClient(GoogleApiClient googleApiClient) {
            if (googleApiClient == null || !googleApiClient.isConnected()) {
                mGoogleApiClient = null;
            } else {
                mGoogleApiClient = googleApiClient;
            }
        }

        @Override
        public int getCount() {
            return mResultList.size();
        }

        @Override
        public PlaceAutocomplete getItem(int position) {
            return mResultList.get(position);
        }

        private ArrayList<PlaceAutocomplete> getPredictions(CharSequence constraint) {
            if (mGoogleApiClient != null) {
                Log.e(TAG, "Executing autocomplete query for: " + constraint);
                PendingResult<AutocompletePredictionBuffer> results =
                        Places.GeoDataApi
                                .getAutocompletePredictions(mGoogleApiClient, constraint.toString(),
                                        mBounds, mPlaceFilter);
                // Wait for predictions, set the timeout.
                AutocompletePredictionBuffer autocompletePredictions = results
                        .await(60, TimeUnit.SECONDS);
                final Status status = autocompletePredictions.getStatus();
                if (!status.isSuccess()) {
                    Toast.makeText(getContext(), "Error: " + status.toString(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error getting place predictions: " + status
                            .toString());
                    autocompletePredictions.release();
                    return null;
                }

                Log.e(TAG, "Query completed. Received " + autocompletePredictions.getCount()
                        + " predictions.");
                Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
                ArrayList resultList = new ArrayList<>(autocompletePredictions.getCount());
                while (iterator.hasNext()) {
                    AutocompletePrediction prediction = iterator.next();
                    resultList.add(new PlaceAutocomplete(prediction.getPlaceId(),
                            prediction.getFullText(null)));
                }
                // Buffer release
                autocompletePredictions.release();
                return resultList;
            }
            Log.e(TAG, "Google API client is not connected.");
            return null;
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    if (constraint != null) {
                        // Query the autocomplete API for the entered constraint
                        mResultList = getPredictions(constraint);
                        if (mResultList != null) {
                            // Results
                            results.values = mResultList;
                            results.count = mResultList.size();
                        }
                    }
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        // The API returned at least one result, update the data.
                        notifyDataSetChanged();
                    } else {
                        // The API did not return any results, invalidate the data set.
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }

        class PlaceAutocomplete {

            public CharSequence placeId;
            public CharSequence description;

            PlaceAutocomplete(CharSequence placeId, CharSequence description) {
                this.placeId = placeId;
                this.description = description;
            }

            @Override
            public String toString() {
                return description.toString();
            }
        }
    }
}
