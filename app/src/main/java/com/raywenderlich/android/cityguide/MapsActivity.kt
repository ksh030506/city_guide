package com.raywenderlich.android.cityguide

import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.IOException


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener {

      private lateinit var map: GoogleMap
      private lateinit var fusedLocationClient: FusedLocationProviderClient
      private lateinit var lastLocation: Location

      private lateinit var locationCallback: LocationCallback
      private lateinit var locationRequest: LocationRequest
      private var locationUpdateState = false

      companion object {
      private const val LOCATION_PERMISSION_REQUEST_CODE = 1
      private const val REQUEST_CHECK_SETTINGS = 2
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_maps)


    btn1.setOnClickListener {
      val intent = Intent(this, SubActivity::class.java)
      startActivity(intent)
    }


    val mapFragment = supportFragmentManager
        .findFragmentById(R.id.map) as SupportMapFragment
    mapFragment.getMapAsync(this)

    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    locationCallback = object : LocationCallback() {
      override fun onLocationResult(p0: LocationResult) {
        super.onLocationResult(p0)

        lastLocation = p0.lastLocation
        placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
      }
    }
    createLocationRequest()
  }


    override fun onPause() {
    super.onPause()
    fusedLocationClient.removeLocationUpdates(locationCallback)
  }

  public override fun onResume() {
    super.onResume()
    if (!locationUpdateState) {
      startLocationUpdates()
    }
  }

  override fun onMapReady(googleMap: GoogleMap) {
    map = googleMap
    map.uiSettings.isZoomControlsEnabled = true
    map.setOnMarkerClickListener(this)
    setUpMap()
  }

  override fun onMarkerClick(p0: Marker?) = false
  private fun setUpMap() {
    if (ActivityCompat.checkSelfPermission(this,
        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this,
          arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
      return
    }

    map.isMyLocationEnabled = true
    map.mapType = GoogleMap.MAP_TYPE_NORMAL

    fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
      if (location != null) {
        lastLocation = location
        val currentLatLng = LatLng(location.latitude, location.longitude)
        placeMarkerOnMap(currentLatLng)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
      }
    }
  }

  private fun placeMarkerOnMap(location: LatLng) {
    val markerOptions = MarkerOptions().position(location)

    val titleStr = getAddress(location)  // add these two lines
    markerOptions.title(titleStr)
    map.addMarker(markerOptions)
  }

  private fun getAddress(latLng: LatLng): String {
    // 1
    val geocoder = Geocoder(this)
    val addresses: List<Address>?
    val address: Address?
    var addressText = ""

    try {
      // 2
      addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
      // 3
      if (null != addresses && !addresses.isEmpty()) {
        address = addresses[0]
        for (i in 0 until address.maxAddressLineIndex) {
          addressText += if (i == 0) address.getAddressLine(i) else "\n" + address.getAddressLine(i)
        }
      }
    } catch (e: IOException) {
      Log.e("MapsActivity", e.localizedMessage)
    }

    return addressText
  }

  private fun startLocationUpdates() {
    if (ActivityCompat.checkSelfPermission(this,
        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this,
          arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
          LOCATION_PERMISSION_REQUEST_CODE)
      return
    }
    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
  }

  private fun createLocationRequest() {
    locationRequest = LocationRequest()
    locationRequest.interval = 10000
    locationRequest.fastestInterval = 5000
    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

    val builder = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)
    val client = LocationServices.getSettingsClient(this)
    val task = client.checkLocationSettings(builder.build())

    task.addOnSuccessListener {
      locationUpdateState = true
      startLocationUpdates()
    }
    task.addOnFailureListener { e ->
      if (e is ResolvableApiException) {
        try {
          e.startResolutionForResult(this@MapsActivity,
              REQUEST_CHECK_SETTINGS)
        } catch (sendEx: IntentSender.SendIntentException) {
        }
      }
    }
  }
}
