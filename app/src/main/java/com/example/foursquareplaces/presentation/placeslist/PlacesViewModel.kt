package com.example.foursquareplaces.presentation.placeslist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foursquareplaces.data.FetchPlacesRepository
import com.example.foursquareplaces.model.NetworkResult
import com.example.foursquareplaces.model.PlacesResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlacesViewModel @Inject constructor(private val placesRepository: FetchPlacesRepository) :
    ViewModel() {

    private val _userLatLong:MutableLiveData<String?> = MutableLiveData()
    val userLatLong:LiveData<String?> = _userLatLong

    private val _selectedCategory:MutableLiveData<String> = MutableLiveData("")
    val selectedCategory:LiveData<String> = _selectedCategory

    private val _placesList: MutableLiveData<NetworkResult<PlacesResponse>> = MutableLiveData()
    val placesList: LiveData<NetworkResult<PlacesResponse>> = _placesList

    private val _searchQuery: MutableStateFlow<String> = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    init {
        searchPlaces()
    }

    fun getPlacesList(latlong:String,limit: Int=50,categories:String="") = viewModelScope.launch {
        placesRepository.searchPlaces(latlong, limit, categories = categories).catch {
            _placesList.postValue(NetworkResult.Error(it.message))
        }.collect{
            _placesList.postValue(it)
        }
    }

    fun setSearchQuery(query:String)=viewModelScope.launch{
        _searchQuery.emit(query)
    }

    fun setSelectedCategory(category:String)=viewModelScope.launch{
        _selectedCategory.postValue(category)
    }

    fun setUserCurrentLocation(latlong:String)=viewModelScope.launch {
        _userLatLong.postValue(latlong)
    }

    @OptIn(FlowPreview::class)
    fun searchPlaces() = viewModelScope.launch {
        searchQuery.debounce(750)
            .filter {
                return@filter !userLatLong.value.isNullOrEmpty()
            }
            .distinctUntilChanged()
            .flatMapLatest { query ->
                placesRepository.searchPlaces(userLatLong.value.toString(), 50, query, categories = selectedCategory.value.toString()).catch {
                    emit(NetworkResult.Error(it.message))
                }
            }
            .flowOn(Dispatchers.Default)
            .collect { result ->
                _placesList.postValue(result)
            }
    }
}