package com.example.foursquareplaces.presentation.placeslist

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.IntentSender
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foursquareplaces.R
import com.example.foursquareplaces.databinding.FragmentPlacesBinding
import com.example.foursquareplaces.model.NetworkResult
import com.example.foursquareplaces.model.Place
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlacesFragment : Fragment() {

    private lateinit var binding: FragmentPlacesBinding
    private val placesViewModel: PlacesViewModel by viewModels()

    companion object {
        const val TAG: String = "PlacesFragment"
    }

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var placesList:MutableList<Place>
    private lateinit var placesAdapter:PlacesRecyclerAdapter

    private val resolutionForResult =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK)
                 getCurrentLocation()
            else {
                binding.shimmerView.stopShimmer()
                Toast.makeText(requireContext(),"we can't determine your location",Toast.LENGTH_SHORT).show()
            }
        }

    @SuppressLint("MissingPermission")
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
                getCurrentLocation()
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
            }

            else -> {
                // No location access granted.
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        validateGPSEnabledOrNot()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_places, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        placesViewModel.placesList.observe(viewLifecycleOwner) { it ->
            when (it) {
                is NetworkResult.Error -> {
                    binding.shimmerView.stopShimmer()
                    binding.shimmerView.visibility = View.GONE
                }

                is NetworkResult.Loading -> {
                    binding.shimmerView.startShimmer()
                    binding.shimmerView.visibility = View.VISIBLE
                    binding.placesList.visibility=View.GONE
                }

                is NetworkResult.Success -> {
                    val size = placesList.size
                    it.data?.results?.let { places->
                        placesList.clear()
                        placesAdapter.notifyItemRangeRemoved(0,size)
                        placesList.addAll(places)
                        placesAdapter.notifyItemRangeInserted(0,places.size)
                    }
                    binding.shimmerView.stopShimmer()
                    binding.shimmerView.visibility = View.GONE
                    binding.placesList.visibility=View.VISIBLE
                }
            }
        }

        placesList = mutableListOf()
        placesAdapter = PlacesRecyclerAdapter(placesList)
        binding.placesList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = placesAdapter
        }

        binding.etSearchPlaces.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    placesViewModel.setSearchQuery(it.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            val userLtLong = placesViewModel.userLatLong.value.toString()
            var categoryId=""
            if(checkedIds.size>0) {
                val chip = binding.chipGroupFilter.findViewById<Chip>(checkedIds[0])
                categoryId=chip.tag.toString()
            }
            placesViewModel.setSelectedCategory(categoryId)
            placesViewModel.getPlacesList(userLtLong, categories = categoryId)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(){
        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,CancellationTokenSource().token)
            .addOnSuccessListener {
                it?.let {
                    val ltlg = "${it.latitude},${it.longitude}"
                    placesViewModel.setUserCurrentLocation(ltlg)
                    placesViewModel.getPlacesList(ltlg)
                }
            }.addOnFailureListener {
                binding.shimmerView.stopShimmer()
                it.message?.let { it1 -> Log.i(TAG, it1) }
            }
    }

    private fun validateGPSEnabledOrNot() {
        val mLocationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,2000)
            .build()
        val settingsBuilder = LocationSettingsRequest.Builder()
            .addLocationRequest(mLocationRequest)
        settingsBuilder.setAlwaysShow(true)

        val result = LocationServices.getSettingsClient(requireContext()).checkLocationSettings(settingsBuilder.build())
        result.addOnCompleteListener { task ->
            //getting the status code from exception
            try {
                task.getResult(ApiException::class.java)
            } catch (ex: ApiException) {
                when (ex.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        Toast.makeText(requireContext(),"GPS IS OFF",Toast.LENGTH_SHORT).show()
                        val resolvableApiException = ex as ResolvableApiException
                        val intentSenderRequest = IntentSenderRequest.Builder(resolvableApiException.resolution).build()
                        resolutionForResult.launch(intentSenderRequest)
                    } catch (e: IntentSender.SendIntentException) {
                        Toast.makeText(requireContext(),"PendingIntent unable to execute request.",Toast.LENGTH_SHORT).show()
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        Toast.makeText(requireContext(), "Something is wrong in your GPS", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}