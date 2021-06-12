package com.matt.shopline.fragments.register;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.matt.shopline.R;

import java.util.Collections;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class Step3 extends Fragment {
    private static final int AUTOCOMPLETE_REQUEST_CODE = 7;
    OnDataPass dataPass;
    private EditText etPhone, etLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_step3, container, false);

        etPhone = rootView.findViewById(R.id.etPhone);
        etLocation = rootView.findViewById(R.id.etLocation);

        // capture empty values just in case
        getData();

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                getData();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };

        // add listener to text fields
        etPhone.addTextChangedListener(watcher);
        etLocation.addTextChangedListener(watcher);

        initializePlaces();
        etLocation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    onSearchCalled();
                }
            }
        });

        return rootView;
    }

    private void getData() {
        String phone = etPhone.getText().toString();
        String location = etLocation.getText().toString().trim();
        String[] data = {phone, location};
        onDataPass(data);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                etLocation.setText(place.getName());
                etLocation.clearFocus();

//                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId() + ", " + place.getAddress());
              /*  Toast.makeText(getActivity(), "ID: " + place.getId()
                                + "address:" + place.getAddress()
                                + "Name:" + place.getName()
                                + " latlong: " + place.getLatLng(),
                        Toast.LENGTH_LONG).show();
                String address = place.getAddress();*/
                // do query with address

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(getActivity(), "Error: " + status.getStatusMessage(), Toast.LENGTH_LONG).show();
//                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                etLocation.clearFocus();
                // The user canceled the operation.
            }
        }
    }

    public void onSearchCalled() {
        // Set the fields to specify which types of place data to return.
//        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

        List<Place.Field> fields = Collections.singletonList(Place.Field.NAME);
        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields).setCountry("UG") //Uganda
                .build(getActivity());
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

   /* private void loadAutoComplete() {
        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.
                Toast.makeText(getActivity(), "Place: " + place.getName() + ", " + place.getId(), Toast.LENGTH_SHORT).show();
                //  Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(getActivity(), "An error occurred: " + status, Toast.LENGTH_SHORT).show();
                Log.i("Response", status.getStatusMessage());
            }
        });
    }*/

    private void initializePlaces() {
        String apiKey = getString(R.string.google_api_key);

        /**
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        if (!Places.isInitialized()) {
            Places.initialize(getActivity(), apiKey);
        }

        // Create a new Places client instance.
//        PlacesClient placesClient = Places.createClient(getActivity());

    }

  /*  @Override
    public void onPause() {
        super.onPause();

       *//* if (!TextUtils.isEmpty(phone) || !TextUtils.isEmpty(location)) {
            onDataPass(data);
        }*//*
    }*/

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        dataPass = (OnDataPass) context;
    }

    public void onDataPass(String[] data) {
        dataPass.DataStep3(data);
    }

}